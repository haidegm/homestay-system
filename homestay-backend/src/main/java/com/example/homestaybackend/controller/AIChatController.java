package com.example.homestaybackend.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin(origins = "*")
public class AIChatController {

    @Value("${dashscope.api.key:YOUR_API_KEY_HERE}")
    private String apiKey;

    private final com.example.homestaybackend.repository.HouseRepository houseRepository;

    public AIChatController(com.example.homestaybackend.repository.HouseRepository houseRepository) {
        this.houseRepository = houseRepository;
    }

    private static final String API_URL = "https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation";
    private static final String MODEL = "qwen-turbo"; // 使用快速模型

    // 获取平台可用城市列表
    private String getAvailableCities() {
        try {
            List<com.example.homestaybackend.entity.House> houses = houseRepository.findAll();
            Set<String> cities = new java.util.HashSet<>();
            for (com.example.homestaybackend.entity.House house : houses) {
                if ("ACTIVE".equals(house.getStatus()) && house.getCity() != null) {
                    cities.add(house.getCity());
                }
            }
            return String.join("、", cities);
        } catch (Exception e) {
            return "杭州、成都、北京、上海、西安、青岛、大连、苏州、南京";
        }
    }

    // 系统提示词 - 定义AI助手的角色和行为
    private String getSystemPrompt() {
        String availableCities = getAvailableCities();
        
        return "# 你的身份\n" +
            "你是「W的homestay」民宿预订平台的AI客服助手，名字叫小W。\n\n" +
            
            "# 平台信息\n" +
            "- 平台名称：W的homestay\n" +
            "- 服务范围：仅限中国大陆地区的民宿预订\n" +
            "- 当前可预订城市：" + availableCities + "\n" +
            "- 平台特色：精选优质民宿，提供个性化住宿体验\n\n" +
            
            "# 重要规则（必须严格遵守）\n" +
            "1. ⚠️ 只推荐中国大陆的城市和民宿，绝不推荐国外城市（如日本、韩国、泰国等）\n" +
            "2. ⚠️ 如果用户问到国外民宿，礼貌告知：\"抱歉，我们平台目前只提供中国大陆地区的民宿预订服务哦😊\"\n" +
            "3. 推荐城市时，只从上面列出的可预订城市中选择\n" +
            "4. ⚠️ 当系统提供了【实时房源数据】时，你必须基于这些真实数据来推荐房源，可以介绍具体房源的名字、价格、位置等信息\n" +
            "5. 当系统没有提供实时房源数据时，不要编造具体房源信息，引导用户在平台上搜索\n" +
            "6. 价格、房源数量等具体信息，建议用户在平台上查看最新数据\n\n" +
            
            "# 预订政策\n" +
            "- 取消政策：入住前24小时可免费取消，之后取消将收取一晚房费\n" +
            "- 预订流程：选择房源 → 选择日期 → 填写信息 → 确认支付 → 预订成功\n" +
            "- 支付方式：支持支付宝、微信支付\n" +
            "- 入住时间：一般为下午14:00后，退房时间为中午12:00前\n" +
            "- 联系房东：预订成功后可在订单详情中查看房东联系方式\n\n" +
            
            "# 常见问题回答模板\n" +
            "Q: 如何预订？\n" +
            "A: 很简单！在首页搜索您想去的城市，或者浏览推荐房源，点击喜欢的房源进入详情页，选择入住日期后点击预订就可以啦😊\n\n" +
            
            "Q: 如何取消订单？\n" +
            "A: 在\"我的行程\"中找到您的订单，点击取消订单就可以了。记得要在入住前24小时取消才能免费哦！\n\n" +
            
            "Q: 推荐哪些城市？\n" +
            "A: 我们平台目前有" + availableCities + "等城市的民宿。您想去哪个城市呢？我可以给您一些建议😊\n\n" +
            
            "# 推荐话术示例\n" +
            "- \"杭州的西湖边有很多特色民宿，风景超美的！\"\n" +
            "- \"成都的宽窄巷子附近住宿很方便，还能品尝地道美食\"\n" +
            "- \"春天去苏州看园林，秋天去北京赏红叶都是不错的选择\"\n" +
            "- \"我们平台有各种价格区间的房源，您的预算大概是多少呢？\"\n\n" +
            
            "# 回答风格\n" +
            "- 语气：友好、热情、专业，像朋友一样聊天\n" +
            "- 长度：简洁明了，每次回答控制在100-150字\n" +
            "- 表情：适当使用emoji（😊🏠✨🌟💕等）\n" +
            "- 变化：同样的问题用不同方式回答\n" +
            "- 主动：适时询问用户需求（预算、人数、偏好等）\n\n" +
            
            "# 禁止事项\n" +
            "❌ 推荐国外城市或民宿（这是最重要的！）\n" +
            "❌ 编造虚假的房源信息或价格\n" +
            "❌ 承诺无法兑现的服务\n" +
            "❌ 使用过于正式或机械的话术\n" +
            "❌ 重复相同的回答内容\n\n" +
            
            "记住：你是中国民宿预订平台的客服，只服务中国大陆地区！如果用户问国外的，一定要礼貌拒绝并说明原因。";
    }

    // 判断用户是否在询问房源相关信息
    private boolean isAskingAboutHouses(String message) {
        String[] keywords = {"推荐", "房源", "民宿", "住宿", "房子", "好房",
                             "便宜", "价格", "多少钱", "预算", "找民宿",
                             "预订", "预定", "有房", "找房", "找房子",
                             "有没有房", "民宿推荐", "帮我找", "民宿价格",
                             "住宿推荐", "哪里住", "住哪里", "想去"};
        String lower = message.toLowerCase();
        for (String kw : keywords) {
            if (lower.contains(kw)) return true;
        }
        return false;
    }

    // 从用户消息中提取城市名
    private String extractCityFromMessage(String message) {
        try {
            List<com.example.homestaybackend.entity.House> houses = houseRepository.findAll();
            Set<String> cities = new java.util.HashSet<>();
            for (com.example.homestaybackend.entity.House h : houses) {
                if ("ACTIVE".equals(h.getStatus()) && h.getCity() != null) {
                    cities.add(h.getCity());
                }
            }
            // 按城市名长度降序排列，优先匹配长名称（如"张家界"优先于"张家"）
            List<String> sortedCities = new ArrayList<>(cities);
            sortedCities.sort((a, b) -> Integer.compare(b.length(), a.length()));
            for (String city : sortedCities) {
                // 去掉"市"后缀进行模糊匹配
                String shortName = city.replace("市", "");
                if (message.contains(city) || message.contains(shortName)) {
                    return city;
                }
            }
        } catch (Exception ignored) {}
        return null;
    }

    // 查询房源数据并格式化为AI可用的上下文
    private String queryHousesForContext(String city, String message) {
        try {
            List<com.example.homestaybackend.entity.House> houses;
            if (city != null) {
                houses = houseRepository.findByCityAndStatus(city, "ACTIVE");
            } else {
                houses = houseRepository.findTop8ByStatusOrderByCreatedTimeDesc("ACTIVE");
            }

            if (houses == null || houses.isEmpty()) {
                if (city != null) {
                    return "当前" + city + "暂无可用房源。请如实告知用户该城市暂无房源，并推荐其他可预订城市。";
                }
                return null;
            }

            // 限制最多8套房源，避免上下文过长
            if (houses.size() > 8) {
                houses = houses.subList(0, 8);
            }

            StringBuilder sb = new StringBuilder();
            sb.append("【实时房源数据 - 来自数据库，请基于这些真实数据回答用户】\n");
            int index = 1;
            for (com.example.homestaybackend.entity.House h : houses) {
                sb.append(index++).append(". 「").append(h.getTitle()).append("」");
                sb.append(" | ").append(h.getCity());
                if (h.getAddress() != null) sb.append(h.getAddress());
                sb.append(" | ¥").append(h.getPrice()).append("/晚");
                sb.append(" | 可住").append(h.getMaxGuests()).append("人");
                if (h.getBedCount() != null) sb.append(" | ").append(h.getBedCount()).append("张床");
                if (h.getArea() != null) sb.append(" | ").append(h.getArea()).append("㎡");
                if (h.getDescription() != null && !h.getDescription().isEmpty()) {
                    String desc = h.getDescription().replace("\n", " ").replace("\r", "");
                    if (desc.length() > 60) desc = desc.substring(0, 60) + "...";
                    sb.append(" | ").append(desc);
                }
                sb.append("\n");
            }
            return sb.toString();
        } catch (Exception e) {
            return null;
        }
    }

    @PostMapping("/chat")
    public ResponseEntity<Map<String, Object>> chat(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();

        try {
            String userMessage = (String) request.get("message");
            
            if (userMessage == null || userMessage.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "消息不能为空");
                return ResponseEntity.badRequest().body(response);
            }

            // 检查API Key
            if ("YOUR_API_KEY_HERE".equals(apiKey)) {
                response.put("success", false);
                response.put("reply", "AI服务未配置，请联系管理员设置API Key。\n\n" +
                    "配置方法：\n" +
                    "1. 访问 https://dashscope.aliyun.com/ 获取API Key\n" +
                    "2. 在 application.properties 中设置 dashscope.api.key");
                return ResponseEntity.ok(response);
            }

            // 构建消息历史
            List<Map<String, String>> messages = new ArrayList<>();
            
            // 添加系统提示词（动态生成，包含实时城市列表）
            Map<String, String> systemMessage = new HashMap<>();
            systemMessage.put("role", "system");
            systemMessage.put("content", getSystemPrompt());
            messages.add(systemMessage);

            // 添加历史消息（如果有）
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> history = (List<Map<String, Object>>) request.get("history");
            if (history != null && !history.isEmpty()) {
                // 只保留最近5轮对话
                int startIndex = Math.max(0, history.size() - 10);
                for (int i = startIndex; i < history.size(); i++) {
                    Map<String, Object> msg = history.get(i);
                    Map<String, String> historyMsg = new HashMap<>();
                    historyMsg.put("role", (String) msg.get("role"));
                    historyMsg.put("content", (String) msg.get("content"));
                    messages.add(historyMsg);
                }
            }

            // 检测用户是否在询问房源，如果是则注入真实数据
            if (isAskingAboutHouses(userMessage)) {
                String city = extractCityFromMessage(userMessage);
                String houseContext = queryHousesForContext(city, userMessage);
                if (houseContext != null) {
                    Map<String, String> dataContext = new HashMap<>();
                    dataContext.put("role", "system");
                    dataContext.put("content", houseContext);
                    messages.add(dataContext);
                }
            }

            // 添加当前用户消息
            Map<String, String> currentMessage = new HashMap<>();
            currentMessage.put("role", "user");
            currentMessage.put("content", userMessage);
            messages.add(currentMessage);

            // 调用通义千问API
            String aiReply = callDashScopeAPI(messages);

            response.put("success", true);
            response.put("reply", aiReply);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("reply", "抱歉，服务暂时不可用，请稍后再试。");
            return ResponseEntity.ok(response);
        }
    }

    private String callDashScopeAPI(List<Map<String, String>> messages) throws Exception {
        URL url = new URL(API_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        
        try {
            // 设置请求方法和头部
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Bearer " + apiKey);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(30000);

            // 构建请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", MODEL);
            
            Map<String, Object> input = new HashMap<>();
            input.put("messages", messages);
            requestBody.put("input", input);

            Map<String, Object> parameters = new HashMap<>();
            parameters.put("result_format", "message");
            parameters.put("temperature", 0.85); // 增加随机性，范围0-2，默认1.0
            parameters.put("top_p", 0.8); // 核采样参数
            parameters.put("max_tokens", 800); // 最大token数
            requestBody.put("parameters", parameters);

            // 发送请求
            String jsonInputString = toJson(requestBody);
            System.out.println("发送请求: " + jsonInputString);

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input_bytes = jsonInputString.getBytes(StandardCharsets.UTF_8);
                os.write(input_bytes, 0, input_bytes.length);
            }

            // 读取响应
            int responseCode = conn.getResponseCode();
            System.out.println("响应码: " + responseCode);

            BufferedReader br;
            if (responseCode == 200) {
                br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
            } else {
                br = new BufferedReader(new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8));
            }

            StringBuilder responseStr = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                responseStr.append(line);
            }
            br.close();

            String responseBody = responseStr.toString();
            System.out.println("响应内容: " + responseBody);

            if (responseCode != 200) {
                return "抱歉，AI服务暂时不可用（错误码：" + responseCode + "）";
            }

            // 解析响应
            return parseResponse(responseBody);

        } finally {
            conn.disconnect();
        }
    }

    // 简单的JSON序列化
    private String toJson(Map<String, Object> map) {
        StringBuilder json = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (!first) json.append(",");
            first = false;
            json.append("\"").append(entry.getKey()).append("\":");
            json.append(toJsonValue(entry.getValue()));
        }
        json.append("}");
        return json.toString();
    }

    @SuppressWarnings("unchecked")
    private String toJsonValue(Object value) {
        if (value == null) {
            return "null";
        } else if (value instanceof String) {
            return "\"" + escapeJson((String) value) + "\"";
        } else if (value instanceof Number || value instanceof Boolean) {
            return value.toString();
        } else if (value instanceof Map) {
            return toJson((Map<String, Object>) value);
        } else if (value instanceof List) {
            StringBuilder json = new StringBuilder("[");
            List<?> list = (List<?>) value;
            for (int i = 0; i < list.size(); i++) {
                if (i > 0) json.append(",");
                json.append(toJsonValue(list.get(i)));
            }
            json.append("]");
            return json.toString();
        }
        return "\"" + value.toString() + "\"";
    }

    private String escapeJson(String str) {
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }

    // 简单的JSON解析
    private String parseResponse(String json) {
        try {
            // 查找 "content" 字段
            int contentIndex = json.indexOf("\"content\"");
            if (contentIndex == -1) {
                return "抱歉，无法解析AI回复";
            }

            int startQuote = json.indexOf("\"", contentIndex + 10);
            if (startQuote == -1) {
                return "抱歉，无法解析AI回复";
            }

            int endQuote = startQuote + 1;
            while (endQuote < json.length()) {
                if (json.charAt(endQuote) == '"' && json.charAt(endQuote - 1) != '\\') {
                    break;
                }
                endQuote++;
            }

            String content = json.substring(startQuote + 1, endQuote);
            // 处理转义字符
            content = content.replace("\\n", "\n")
                           .replace("\\\"", "\"")
                           .replace("\\\\", "\\");
            
            return content;
        } catch (Exception e) {
            e.printStackTrace();
            return "抱歉，解析回复时出错";
        }
    }
}
