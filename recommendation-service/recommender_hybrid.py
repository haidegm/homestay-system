"""
混合推荐算法
结合协同过滤和基于内容的推荐
"""
import pymysql
from datetime import datetime
from collections import defaultdict

class HybridRecommender:
    """混合推荐器：协同过滤 + 基于内容"""
    
    def __init__(self, host, port, database, user, password):
        """初始化数据库连接"""
        self.db_config = {
            'host': host,
            'port': port,
            'database': database,
            'user': user,
            'password': password,
            'charset': 'utf8mb4'
        }
    
    def get_connection(self):
        """获取数据库连接"""
        return pymysql.connect(**self.db_config)
    
    def recommend(self, user_id, top_n=10):
        """
        为用户推荐房源
        
        混合策略：
        1. 协同过滤（60%权重）：找相似用户喜欢的房源
        2. 基于内容（40%权重）：找与用户浏览过的房源相似的房源
        """
        conn = self.get_connection()
        cursor = conn.cursor(pymysql.cursors.DictCursor)
        
        try:
            # 1. 协同过滤推荐（原始分数）
            cf_recommendations = self._collaborative_filtering(cursor, user_id, top_n * 2)

            # 2. 基于内容推荐（原始分数）
            content_recommendations = self._content_based(cursor, user_id, top_n * 2)

            # 记录原始分数（用于调试输出）
            cf_raw_scores = {rec['houseId']: rec['score'] for rec in cf_recommendations}
            content_raw_scores = {rec['houseId']: rec['score'] for rec in content_recommendations}

            # ---------- 分别归一化（Min-Max 到 0~1） ----------
            def normalize_dict(scores_dict):
                if not scores_dict:
                    return {}
                scores = list(scores_dict.values())
                min_s = min(scores)
                max_s = max(scores)
                if max_s == min_s:
                    return {hid: 0.5 for hid in scores_dict}
                return {hid: (s - min_s) / (max_s - min_s) for hid, s in scores_dict.items()}

            cf_norm = normalize_dict(cf_raw_scores)
            content_norm = normalize_dict(content_raw_scores)
            # ------------------------------------------------

            # 3. 混合推荐结果（使用归一化后的分数，按权重加权）
            combined_scores = {}

            for house_id, norm_score in cf_norm.items():
                combined_scores[house_id] = norm_score * 0.6

            for house_id, norm_score in content_norm.items():
                if house_id in combined_scores:
                    combined_scores[house_id] += norm_score * 0.4
                else:
                    combined_scores[house_id] = norm_score * 0.4

            # 4. 如果推荐结果不足，补充热门房源
            if len(combined_scores) < top_n:
                user_interacted = self._get_user_interacted_houses(cursor, user_id)
                popular = self._get_popular_houses(cursor, top_n * 2)

                for pop in popular:
                    house_id = pop['houseId']
                    if house_id not in combined_scores and house_id not in user_interacted:
                        combined_scores[house_id] = pop['popularityScore'] * 0.01
                        if len(combined_scores) >= top_n:
                            break

            # 5. 最终整体归一化（让最终得分落在 [0,1] 区间）
            if combined_scores:
                scores = list(combined_scores.values())
                min_score = min(scores)
                max_score = max(scores)
                if max_score > min_score:
                    for house_id in combined_scores:
                        combined_scores[house_id] = (combined_scores[house_id] - min_score) / (max_score - min_score)
                else:
                    for house_id in combined_scores:
                        combined_scores[house_id] = 0.5

            # 6. 排序并取前 top_n
            sorted_recommendations = sorted(combined_scores.items(), key=lambda x: x[1], reverse=True)[:top_n]

            # ----- 输出调试信息（不改变原有格式）-----
            print("\n========== 推荐结果 Top {} ==========".format(len(sorted_recommendations)))
            print(f"{'排名':<4} {'房源ID':<8} {'协同过滤分':<12} {'内容分':<12} {'最终分(归一化)':<16}")
            for idx, (house_id, final_score) in enumerate(sorted_recommendations, 1):
                cf_score = cf_raw_scores.get(house_id, 0.0)
                content_score = content_raw_scores.get(house_id, 0.0)
                print(f"{idx:<4} {house_id:<8} {cf_score:<12.4f} {content_score:<12.4f} {final_score:<16.4f}")
            print("=" * 70)
            # ------------------------------------

            return [{'houseId': int(house_id), 'score': float(final_score)} for house_id, final_score in sorted_recommendations]

        finally:
            conn.close()

    def _collaborative_filtering(self, cursor, user_id, top_n):
        """
        协同过滤推荐

        逻辑：
        1. 找出与当前用户有相似行为的其他用户
        2. 推荐这些相似用户喜欢但当前用户没有交互过的房源

        相似度计算：
        - 订购了相同房源的用户
        - 浏览了相同房源的用户
        - 收藏了相同房源的用户
        """
        # 1. 获取当前用户交互过的房源
        user_interacted = self._get_user_interacted_houses(cursor, user_id)

        if not user_interacted:
            return []

        # 2. 找出也交互过这些房源的其他用户（相似用户）
        similar_users = self._find_similar_users(cursor, user_id, user_interacted)

        if not similar_users:
            return []

        # 3. 统计相似用户喜欢的房源
        house_scores = defaultdict(float)

        for similar_user_id, similarity in similar_users.items():
            # 获取相似用户的交互记录
            cursor.execute("""
                SELECT house_id, 
                       CASE 
                           WHEN EXISTS (SELECT 1 FROM `order` WHERE user_id = %s AND house_id = h.house_id AND status IN ('CONFIRMED', 'COMPLETED')) THEN 5.0
                           WHEN EXISTS (SELECT 1 FROM wishlist WHERE user_id = %s AND house_id = h.house_id) THEN 3.0
                           WHEN EXISTS (SELECT 1 FROM browse_history WHERE user_id = %s AND house_id = h.house_id) THEN 2.0
                           ELSE 1.0
                       END as weight
                FROM (
                    SELECT DISTINCT house_id FROM `order` WHERE user_id = %s AND status IN ('CONFIRMED', 'COMPLETED')
                    UNION
                    SELECT DISTINCT house_id FROM wishlist WHERE user_id = %s
                    UNION
                    SELECT DISTINCT house_id FROM browse_history WHERE user_id = %s
                ) h
            """, (similar_user_id, similar_user_id, similar_user_id, similar_user_id, similar_user_id, similar_user_id))

            for row in cursor.fetchall():
                house_id = row['house_id']
                # 排除用户已经交互过的房源
                if house_id not in user_interacted:
                    # 分数 = 相似度 * 行为权重（转换为float）
                    house_scores[house_id] += similarity * float(row['weight'])

        # 4. 排序并返回
        sorted_houses = sorted(house_scores.items(), key=lambda x: x[1], reverse=True)[:top_n]

        return [{'houseId': int(house_id), 'score': float(score)} for house_id, score in sorted_houses]

    def _find_similar_users(self, cursor, user_id, user_interacted_houses):
        """
        找出相似用户

        相似度 = 共同交互的房源数量 / 总交互房源数量
        """
        similar_users = {}

        # 查找也交互过这些房源的其他用户
        placeholders = ','.join(['%s'] * len(user_interacted_houses))

        cursor.execute(f"""
            SELECT user_id, COUNT(DISTINCT house_id) as common_count
            FROM (
                SELECT user_id, house_id FROM `order` WHERE house_id IN ({placeholders}) AND status IN ('CONFIRMED', 'COMPLETED')
                UNION
                SELECT user_id, house_id FROM wishlist WHERE house_id IN ({placeholders})
                UNION
                SELECT user_id, house_id FROM browse_history WHERE house_id IN ({placeholders})
            ) interactions
            WHERE user_id != %s
            GROUP BY user_id
            HAVING common_count >= 1
            ORDER BY common_count DESC
            LIMIT 20
        """, list(user_interacted_houses) * 3 + [user_id])

        for row in cursor.fetchall():
            similar_user_id = row['user_id']
            common_count = row['common_count']
            # 相似度 = 共同房源数 / 当前用户交互房源数
            similarity = common_count / len(user_interacted_houses)
            similar_users[similar_user_id] = similarity

        return similar_users

    def _content_based(self, cursor, user_id, top_n):
        """
        基于内容的推荐

        逻辑：
        1. 找出用户浏览/订购/收藏过的房源
        2. 找出与这些房源相似的其他房源（同城市、相似价格）
        3. 根据用户行为的时间和类型加权
        """
        # 1. 获取用户交互记录及权重
        user_interactions = self._get_user_interactions_with_weight(cursor, user_id)

        if not user_interactions:
            return []

        # 2. 获取所有活跃房源
        cursor.execute("""
            SELECT id, city, price, max_guests, bed_count
            FROM house
            WHERE status = 'ACTIVE'
        """)
        all_houses = cursor.fetchall()

        # 3. 计算推荐分数
        scores = {}
        interacted_house_ids = set(user_interactions.keys())

        for house in all_houses:
            house_id = house['id']

            # 排除已交互的房源
            if house_id in interacted_house_ids:
                continue

            # 计算与用户交互房源的相似度
            score = 0
            for interacted_id, weight in user_interactions.items():
                # 获取交互房源信息
                interacted_house = next((h for h in all_houses if h['id'] == interacted_id), None)
                if not interacted_house:
                    continue

                # 计算相似度
                similarity = self._calculate_similarity(house, interacted_house)

                # 加权累加
                score += similarity * weight

            if score > 0:
                scores[house_id] = score

        # 4. 排序并返回
        sorted_houses = sorted(scores.items(), key=lambda x: x[1], reverse=True)[:top_n]

        return [{'houseId': int(house_id), 'score': float(score)} for house_id, score in sorted_houses]

    def _get_user_interacted_houses(self, cursor, user_id):
        """获取用户交互过的所有房源ID"""
        cursor.execute("""
            SELECT DISTINCT house_id
            FROM (
                SELECT house_id FROM `order` WHERE user_id = %s
                UNION
                SELECT house_id FROM wishlist WHERE user_id = %s
                UNION
                SELECT house_id FROM browse_history WHERE user_id = %s
                UNION
                SELECT house_id FROM review WHERE user_id = %s
            ) interactions
        """, (user_id, user_id, user_id, user_id))

        return set(row['house_id'] for row in cursor.fetchall())

    def _get_user_interactions_with_weight(self, cursor, user_id):
        """
        获取用户交互记录及权重

        权重规则：
        - 评价：5.0 * 评分
        - 已完成订单：4.0
        - 已确认订单：3.5
        - 收藏：3.2
        - 1天内浏览：3.0
        - 3天内浏览：2.7
        - 7天内浏览：2.4
        - 30天内浏览：2.0
        - 更早浏览：1.5
        """
        interactions = {}
        now = datetime.now()

        # 1. 评价（权重最高）
        cursor.execute("""
            SELECT house_id, rating
            FROM review
            WHERE user_id = %s
        """, (user_id,))
        for row in cursor.fetchall():
            interactions[row['house_id']] = float(row['rating'])

        # 2. 订单
        cursor.execute("""
            SELECT house_id, status
            FROM `order`
            WHERE user_id = %s AND status IN ('CONFIRMED', 'COMPLETED')
        """, (user_id,))
        for row in cursor.fetchall():
            house_id = row['house_id']
            if house_id not in interactions:
                interactions[house_id] = 4.0 if row['status'] == 'COMPLETED' else 3.5

        # 3. 收藏
        cursor.execute("""
            SELECT house_id
            FROM wishlist
            WHERE user_id = %s
        """, (user_id,))
        for row in cursor.fetchall():
            house_id = row['house_id']
            if house_id not in interactions:
                interactions[house_id] = 3.2

        # 4. 浏览记录（考虑时间衰减，提高权重）
        cursor.execute("""
            SELECT house_id, MAX(browse_time) as last_browse
            FROM browse_history
            WHERE user_id = %s
            GROUP BY house_id
        """, (user_id,))
        for row in cursor.fetchall():
            house_id = row['house_id']
            if house_id not in interactions:
                last_browse = row['last_browse']
                days_ago = (now - last_browse).days

                # 提高浏览权重，让推荐更实时
                if days_ago <= 1:
                    weight = 4.5  # 提高到4.5（接近订单权重）
                elif days_ago <= 3:
                    weight = 4.0
                elif days_ago <= 7:
                    weight = 3.5
                elif days_ago <= 30:
                    weight = 3.0
                else:
                    weight = 2.5

                interactions[house_id] = weight

        return interactions

    def _calculate_similarity(self, house1, house2):
        """
        计算两个房源的相似度

        相似度因素：
        - 同城市：+1.0
        - 价格相近（±30%）：+0.5
        - 容纳人数相同：+0.3
        - 床位数相同：+0.2
        """
        similarity = 0

        # 1. 城市相同（最重要）
        if house1['city'] == house2['city']:
            similarity += 1.0

        # 2. 价格相近
        price1 = float(house1['price'])
        price2 = float(house2['price'])
        price_diff = abs(price1 - price2) / max(price1, price2)
        if price_diff <= 0.3:
            similarity += 0.5 * (1 - price_diff / 0.3)

        # 3. 容纳人数相同
        if house1['max_guests'] == house2['max_guests']:
            similarity += 0.3

        # 4. 床位数相同
        if house1['bed_count'] == house2['bed_count']:
            similarity += 0.2

        return similarity

    def get_similar_houses(self, house_id, top_n=10):
        """获取相似房源"""
        conn = self.get_connection()
        cursor = conn.cursor(pymysql.cursors.DictCursor)

        try:
            # 获取目标房源
            cursor.execute("""
                SELECT id, city, price, max_guests, bed_count
                FROM house
                WHERE id = %s AND status = 'ACTIVE'
            """, (house_id,))
            target_house = cursor.fetchone()

            if not target_house:
                return []

            # 获取所有其他房源
            cursor.execute("""
                SELECT id, city, price, max_guests, bed_count
                FROM house
                WHERE id != %s AND status = 'ACTIVE'
            """, (house_id,))
            all_houses = cursor.fetchall()

            # 计算相似度
            similarities = []
            for house in all_houses:
                similarity = self._calculate_similarity(target_house, house)
                if similarity > 0:
                    similarities.append({
                        'houseId': house['id'],
                        'similarity': similarity
                    })

            # 排序并返回
            similarities.sort(key=lambda x: x['similarity'], reverse=True)
            return similarities[:top_n]

        finally:
            conn.close()

    def _get_popular_houses(self, cursor, top_n):
        """获取热门房源"""
        cursor.execute("""
            SELECT 
                h.id as houseId,
                COUNT(DISTINCT o.id) as order_count,
                COALESCE(AVG(r.rating), 0) as avg_rating,
                (COUNT(DISTINCT o.id) * 0.7 + COALESCE(AVG(r.rating), 0) * 0.3) as popularityScore
            FROM house h
            LEFT JOIN `order` o ON h.id = o.house_id AND o.status IN ('CONFIRMED', 'COMPLETED')
            LEFT JOIN review r ON h.id = r.house_id
            WHERE h.status = 'ACTIVE'
            GROUP BY h.id
            ORDER BY popularityScore DESC
            LIMIT %s
        """, (top_n,))

        return cursor.fetchall()

    def get_popular_houses(self, top_n=10):
        """获取热门房源（公开接口）"""
        conn = self.get_connection()
        cursor = conn.cursor(pymysql.cursors.DictCursor)

        try:
            return self._get_popular_houses(cursor, top_n)
        finally:
            conn.close()