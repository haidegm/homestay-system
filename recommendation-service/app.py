from flask import Flask, request, jsonify
from flask_cors import CORS
from dotenv import load_dotenv
import os
from recommender_hybrid import HybridRecommender

# 加载环境变量
load_dotenv()

app = Flask(__name__)
CORS(app)

# 初始化推荐器（使用混合推荐：协同过滤 + 基于内容）
recommender = HybridRecommender(
    host=os.getenv('DB_HOST', 'localhost'),
    port=int(os.getenv('DB_PORT', 3306)),
    database=os.getenv('DB_NAME', 'homestay'),
    user=os.getenv('DB_USER', 'root'),
    password=os.getenv('DB_PASSWORD', '20020513wlhb')
)

@app.route('/health', methods=['GET'])
def health_check():
    """健康检查接口"""
    return jsonify({
        'status': 'ok',
        'service': 'recommendation-service'
    })

@app.route('/api/recommend/user/<int:user_id>', methods=['GET'])
def recommend_for_user(user_id):
    """
    为指定用户推荐房源
    参数:
        user_id: 用户ID
        limit: 推荐数量（默认10）
    """
    try:
        limit = request.args.get('limit', 10, type=int)
        
        print(f"[推荐请求] 用户ID: {user_id}, 数量: {limit}")
        
        # 获取推荐（简单推荐器不需要训练）
        recommendations = recommender.recommend(user_id, top_n=limit)
        
        print(f"[推荐结果] 返回 {len(recommendations)} 个房源")
        if recommendations:
            print(f"[推荐详情] 前3个: {[(r['houseId'], r['score']) for r in recommendations[:3]]}")
        
        return jsonify({
            'success': True,
            'userId': user_id,
            'recommendations': recommendations
        })
    except Exception as e:
        print(f"[推荐错误] {str(e)}")
        import traceback
        traceback.print_exc()
        return jsonify({
            'success': False,
            'error': str(e)
        }), 500

@app.route('/api/recommend/similar/<int:house_id>', methods=['GET'])
def recommend_similar_houses(house_id):
    """
    推荐与指定房源相似的房源
    参数:
        house_id: 房源ID
        limit: 推荐数量（默认10）
    """
    try:
        limit = request.args.get('limit', 10, type=int)
        
        similar_houses = recommender.get_similar_houses(house_id, top_n=limit)
        
        return jsonify({
            'success': True,
            'houseId': house_id,
            'similarHouses': similar_houses
        })
    except Exception as e:
        return jsonify({
            'success': False,
            'error': str(e)
        }), 500

@app.route('/api/recommend/train', methods=['POST'])
def train_model():
    """手动触发模型训练（混合推荐器实时计算，不需要训练）"""
    return jsonify({
        'success': True,
        'message': '混合推荐器实时计算，不需要训练'
    })

@app.route('/api/recommend/popular', methods=['GET'])
def get_popular_houses():
    """获取热门房源（基于订单数和评分）"""
    try:
        limit = request.args.get('limit', 10, type=int)
        popular_houses = recommender.get_popular_houses(top_n=limit)
        
        return jsonify({
            'success': True,
            'popularHouses': popular_houses
        })
    except Exception as e:
        return jsonify({
            'success': False,
            'error': str(e)
        }), 500

if __name__ == '__main__':
    port = int(os.getenv('FLASK_PORT', 5000))
    debug = os.getenv('FLASK_DEBUG', 'True').lower() == 'true'
    app.run(host='0.0.0.0', port=port, debug=debug)
