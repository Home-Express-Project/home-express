package com.homeexpress.home_express_api.service.intake;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.homeexpress.home_express_api.dto.intake.IntakeParseTextResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
public class IntakeAIParsingService {

    @Value("${openai.api.key:#{null}}")
    private String openaiApiKey;

    @Value("${openai.api.url:https://api.openai.com/v1/chat/completions}")
    private String openaiApiUrl;

    @Value("${openai.model:gpt-5-mini}")
    private String openaiModel;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public IntakeAIParsingService() {
        this.restTemplate = buildRestTemplate();
    }

    public List<IntakeParseTextResponse.ParsedItem> parseWithAI(String text) {
        if (!StringUtils.hasText(text)) {
            return List.of();
        }

        // Chuẩn hóa và chia dòng
        List<String> lines = preprocessText(text);
        if (lines.isEmpty()) {
            return List.of();
        }

        // Không có API key: fallback heuristic trên từng dòng
        if (!StringUtils.hasText(openaiApiKey)) {
            log.warn("OpenAI API key missing. Using heuristic fallback.");
            return fallbackFromLines(lines);
        }

        try {
            // Gọi OpenAI với flexible item count (allow splitting complex descriptions)
            // Estimate max items: each line could potentially split into 3 items (e.g., "1 bộ bàn ăn 6 ghế" → table + chairs)
            int estimatedMaxItems = lines.size() * 3;
            String content = callOpenAI(buildSystemPrompt(), buildUserPrompt(lines), lines.size(), estimatedMaxItems);
            List<ParsedItemRaw> raw = parseRaw(content);
            if (raw == null || raw.isEmpty()) {
                log.warn("AI returned empty. Fallback heuristic.");
                return fallbackFromLines(lines);
            }

            // No alignment needed - AI can return any number of items
            log.info("AI parsing: {} input lines → {} output items", lines.size(), raw.size());
            return raw.stream().map(this::toDomain).collect(Collectors.toList());
        } catch (Exception e) {
            log.error("AI parsing failed. Fallback heuristic.", e);
            return fallbackFromLines(lines);
        }
    }

    // =================== OpenAI ===================
    private String callOpenAI(String systemPrompt, String userPrompt, int minItems, int maxItems) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openaiApiKey);

        Map<String, Object> systemMsg = Map.of("role", "system", "content", systemPrompt);
        Map<String, Object> userMsg = Map.of("role", "user", "content", userPrompt);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", openaiModel);
        requestBody.put("messages", List.of(systemMsg, userMsg));
        requestBody.put("temperature", 0.1);
        requestBody.put("max_tokens", 2000); // Increased for more items

        // Flexible JSON Schema: object { items: [ ...minItems to maxItems... ] }
        Map<String, Object> jsonSchema = buildSchema(minItems, maxItems);
        Map<String, Object> responseFormat = Map.of(
                "type", "json_schema",
                "json_schema", Map.of(
                        "name", "intake_items",
                        "schema", jsonSchema,
                        "strict", true
                )
        );

        requestBody.put("response_format", responseFormat);

        ResponseEntity<String> resp;
        try {
            resp = restTemplate.exchange(openaiApiUrl, HttpMethod.POST,
                    new HttpEntity<>(requestBody, headers), String.class);
        } catch (HttpClientErrorException.BadRequest br) {
            // Fallback: bỏ JSON Schema nếu server/model không hỗ trợ
            log.warn("response_format=json_schema not accepted. Retrying with json_object.");
            requestBody.put("response_format", Map.of("type", "json_object"));
            resp = restTemplate.exchange(openaiApiUrl, HttpMethod.POST,
                    new HttpEntity<>(requestBody, headers), String.class);
        }

        String body = resp.getBody();
        if (!StringUtils.hasText(body)) {
            throw new RuntimeException("Empty response from OpenAI");
        }

        ChatCompletionResponse dto = objectMapper.readValue(body, ChatCompletionResponse.class);
        if (dto.choices == null || dto.choices.isEmpty() || dto.choices.get(0).message == null) {
            throw new RuntimeException("No choices in OpenAI response");
        }

        String content = dto.choices.get(0).message.content;
        if (!StringUtils.hasText(content)) {
            throw new RuntimeException("Empty content");
        }
        return stripCodeFence(content.trim());
    }

    private Map<String, Object> buildSchema(int minItems, int maxItems) {
        Map<String, Object> item = Map.of(
                "type", "object",
                "required", List.of("name", "brand", "model", "quantity", "category_name", "size", "is_fragile", "requires_disassembly", "confidence"),
                "properties", Map.of(
                        "name", Map.of("type", "string", "minLength", 1),
                        "brand", Map.of("type", Arrays.asList("string", "null")),
                        "model", Map.of("type", Arrays.asList("string", "null")),
                        "quantity", Map.of("type", "integer", "minimum", 1),
                        "category_name", Map.of("type", "string", "enum", List.of("Điện tử", "Nội thất", "Đồ gia dụng", "Quần áo", "Khác")),
                        "size", Map.of("type", "string", "enum", List.of("S", "M", "L")),
                        "is_fragile", Map.of("type", "boolean"),
                        "requires_disassembly", Map.of("type", "boolean"),
                        "confidence", Map.of("type", "number", "minimum", 0.8, "maximum", 0.95)
                )
        );

        // Flexible array: allow AI to split complex descriptions into multiple items
        Map<String, Object> arrayItems = Map.of(
                "type", "array",
                "minItems", minItems,
                "maxItems", maxItems,
                "items", item
        );

        // Top-level object để nhiều model tuân theo strict hơn: { items: [...] }
        return Map.of(
                "type", "object",
                "required", List.of("items"),
                "properties", Map.of("items", arrayItems)
        );
    }

    // =================== Prompt ===================
    private String buildSystemPrompt() {
        // Enhanced prompt with smart item splitting for complex Vietnamese furniture descriptions
        return """
            Bạn là chuyên gia phân tách danh sách vật phẩm vận chuyển tiếng Việt.
            Trả về MỘT JSON object { "items": [...] }.

            QUAN TRỌNG - PHÂN TÁCH MÔ TẢ PHỨC TẠP:
            1. Nếu mô tả chứa NHIỀU LOẠI vật phẩm khác nhau, TÁCH thành NHIỀU ITEMS:
               - "1 bộ bàn ăn 6 ghế" => 2 items: [Bàn ăn (qty:1), Ghế ăn (qty:6)]
               - "1 phòng học 30 bộ bàn ghế" => 2 items: [Bàn học (qty:30), Ghế học (qty:30)]
               - "30 bộ bàn ghế" => 2 items: [Bàn (qty:30), Ghế (qty:30)]
               - "1 bàn + 4 ghế gỗ" => 2 items: [Bàn gỗ (qty:1), Ghế gỗ (qty:4)]
               - "Combo giường kèm nệm" => 2 items: [Giường (qty:1), Nệm (qty:1)]
               - "Bộ PC" => 1 item: [Bộ PC (qty:1)]

            2. Nhận diện đúng các mẫu câu có số lượng lồng nhau:
               - "X bộ bàn ăn Y ghế" = X bàn ăn + (X*Y) ghế ăn
               - "Phòng học X bộ bàn ghế" = X bàn học + X ghế học (nhân thêm số phòng nếu có)
               - "X bộ bàn ghế" = X bàn + X ghế
               - Luôn tạo riêng từng vật thể (bàn, ghế, tủ, thiết bị) thay vì gộp chung.

            3. Các từ nối như "+", "&", "và", "với", "kèm", "cùng" hoặc "combo" diễn đạt nhiều món trong cùng câu → phải tách triệt để, mỗi món có quantity riêng.

            4. Đồ điện tử / máy tính phải được đánh dấu rõ:
               - "Bộ PC", "máy tính bàn", "computer", "desktop" => category_name "Điện tử", is_fragile true, requires_disassembly false, size "M".
               - Laptop/máy tính xách tay/máy tính bảng/màn hình/TV => category_name "Điện tử", is_fragile true (cần đóng gói an toàn).
               - Điện thoại/tablet/thiết bị số => category_name "Điện tử", is_fragile true.

            5. Khi không còn mẫu đặc biệt, giữ nguyên item gốc nhưng đảm bảo quantity và category_name hợp lý.

            Trường cho mỗi item:
              - name: tên cụ thể (VD: "Bàn ăn", "Ghế học", "Bộ PC")
              - brand: thương hiệu nếu có, khác thì null
              - model: mã model nếu có, khác thì null
              - quantity: số nguyên dương; trích xuất chính xác từ mô tả
              - category_name: một trong ["Điện tử","Nội thất","Đồ gia dụng","Quần áo","Khác"]
              - size: một trong ["S","M","L"] theo quy tắc dưới
              - is_fragile: true cho điện tử (PC/laptop/tivi/màn hình/điện thoại), kính/gương, gốm sứ; false cho bàn ghế, tủ, giường
              - requires_disassembly: true cho tivi ≥40", tủ lạnh/giặt, giường, tủ lớn, bàn ăn/làm việc, bộ sofa; false cho thiết bị nhỏ và PC
              - confidence: 0.8-0.95

            size:
              - S: <20kg (laptop, ghế nhỏ, đồ bếp nhỏ)
              - M: 20-50kg (PC, tivi <50", bàn/ghế/tủ nhỏ)
              - L: >50kg (tủ lạnh, máy giặt, tivi ≥50", giường, tủ lớn, bộ sofa)

            Chỉ trả về JSON, không giải thích.
            """;
    }

    private String buildUserPrompt(List<String> lines) {
        StringBuilder sb = new StringBuilder("Danh sách cần phân tích (mỗi dòng = 1 item):\n");
        for (int i = 0; i < lines.size(); i++) {
            sb.append(i + 1).append(". ").append(lines.get(i)).append("\n");
        }
        return sb.toString();
    }

    // =================== Parse JSON ===================
    private List<ParsedItemRaw> parseRaw(String content) {
        try {
            // Ưu tiên object { items: [...] }
            if (content.trim().startsWith("{")) {
                var root = objectMapper.readTree(content);
                if (root.has("items")) {
                    return objectMapper.readValue(
                            root.get("items").toString(),
                            objectMapper.getTypeFactory().constructCollectionType(List.class, ParsedItemRaw.class)
                    );
                }
            }
            // Nếu model trả về [] trực tiếp
            if (content.trim().startsWith("[")) {
                return objectMapper.readValue(
                        content,
                        objectMapper.getTypeFactory().constructCollectionType(List.class, ParsedItemRaw.class)
                );
            }
            // Cuối cùng thử parse 1 item đơn
            ParsedItemRaw single = objectMapper.readValue(content, ParsedItemRaw.class);
            return List.of(single);
        } catch (Exception e) {
            log.error("Cannot parse AI JSON. Len={}", content.length(), e);
            return List.of();
        }
    }

    // =================== Preprocess + Fallback ===================
    private List<String> preprocessText(String text) {
        String normalized = text.replace("\r\n", "\n").trim();
        if (normalized.isEmpty()) {
            return List.of();
        }
        String[] parts = normalized.split("[,，;\\n]+");
        List<String> lines = new ArrayList<>();
        for (String p : parts) {
            String t = p.trim();
            if (!t.isEmpty()) {
                lines.add(t);
            }
        }
        if (lines.isEmpty()) {
            lines.add(normalized);
        }
        return lines;
    }

    private List<IntakeParseTextResponse.ParsedItem> fallbackFromLines(List<String> lines) {
        List<IntakeParseTextResponse.ParsedItem> out = new ArrayList<>(lines.size());
        for (String s : lines) {
            ParsedHeu h = heuristic(s);
            out.add(IntakeParseTextResponse.ParsedItem.builder()
                    .name(capitalizeName(h.name))
                    .brand(h.brand)
                    .model(null)
                    .quantity(h.quantity)
                    .categoryName(h.category)
                    .size(h.size)
                    .isFragile(h.fragile)
                    .requiresDisassembly(h.disassembly)
                    .confidence(0.9)
                    .build());
        }
        return out;
    }

    private List<ParsedItemRaw> alignToCount(List<ParsedItemRaw> ai, List<String> lines) {
        List<ParsedItemRaw> copy = new ArrayList<>(ai);
        // Cắt thừa
        if (copy.size() > lines.size()) {
            copy = copy.subList(0, lines.size());
        }
        // Bù thiếu
        while (copy.size() < lines.size()) {
            ParsedHeu h = heuristic(lines.get(copy.size()));
            ParsedItemRaw r = new ParsedItemRaw();
            r.name = capitalizeName(h.name);
            r.brand = h.brand;
            r.model = null;
            r.quantity = h.quantity;
            r.categoryName = h.category;
            r.size = h.size;
            r.isFragile = h.fragile;
            r.requiresDisassembly = h.disassembly;
            r.confidence = 0.9;
            copy.add(r);
        }
        return copy;
    }

    // =================== Heuristic utils ===================
    private static final Pattern QTY_PREFIX = Pattern.compile("^\\s*(\\d{1,3})\\s+(.+)$", Pattern.CASE_INSENSITIVE);
    private static final Pattern INCH = Pattern.compile("(\\d{2,3})\\s*inch", Pattern.CASE_INSENSITIVE);
    private static final Set<String> BRANDS = Set.of("samsung", "lg", "ikea", "sony", "xiaomi", "panasonic", "tcl", "sharp", "toshiba", "electrolux", "bosch", "whirlpool");

    private ParsedHeu heuristic(String raw) {
        String s = raw.trim();
        int qty = 1;
        Matcher m = QTY_PREFIX.matcher(s);
        if (m.find()) {
            try {
                qty = Integer.parseInt(m.group(1));
            } catch (NumberFormatException ignore) {
            }
            s = m.group(2).trim();
        }
        s = stripLeadingArticles(s);
        String brand = detectBrand(s);
        String category = detectCategory(s);
        String size = detectSize(s);
        boolean fragile = isFragile(s);
        boolean dis = requiresDisassembly(s);
        return new ParsedHeu(s, brand, qty, category, size, fragile, dis);
    }

    private static String detectBrand(String name) {
        String lower = name.toLowerCase(Locale.ROOT);
        for (String b : BRANDS) {
            if (lower.contains(b)) {
                return capitalizeWord(b);
            }
        }
        return null;
    }

    private static String detectCategory(String n) {
        String s = n.toLowerCase(Locale.ROOT);
        // Electronics: PC, computers, TVs, appliances
        if (containsAny(s, "pc", "tivi", "tv", "màn hình", "tủ lạnh", "máy giặt", "máy sấy", "máy lạnh", "điều hòa", "laptop", "máy tính", "loa", "máy nước nóng", "máy tính bảng", "tablet")) {
            return "Điện tử";
        }
        // Furniture: tables, chairs, beds, cabinets
        if (containsAny(s, "bàn", "ghế", "sofa", "giường", "tủ", "kệ", "kệ sách", "bộ bàn ghế")) {
            return "Nội thất";
        }
        // Household items: dishes, bottles, etc.
        if (containsAny(s, "bình", "lọ", "chén", "bát", "đĩa", "ấm", "bộ ấm chén")) {
            return "Đồ gia dụng";
        }
        // Clothing
        if (containsAny(s, "quần", "áo", "giày", "dép", "túi")) {
            return "Quần áo";
        }
        return "Khác";
    }

    private static String detectSize(String n0) {
        String n = n0.toLowerCase(Locale.ROOT);
        Matcher m = INCH.matcher(n);
        if (n.contains("tivi") || n.contains("tv") || n.contains("màn hình")) {
            if (m.find()) {
                return Integer.parseInt(m.group(1)) >= 50 ? "L" : "M";
            }
            return "M";
        }
        if (containsAny(n, "tủ lạnh", "máy giặt", "giường", "bộ sofa", "bộ bàn ghế", "sofa lớn")) {
            return "L";
        }
        if (containsAny(n, "bình", "lọ", "chén", "bát", "đĩa", "ấm", "bộ ấm chén", "áo", "quần")) {
            return "S";
        }
        if (containsAny(n, "bàn", "ghế", "tủ", "kệ")) {
            return "M";
        }
        return "M";
    }

    private static boolean isFragile(String n0) {
        String n = n0.toLowerCase(Locale.ROOT);
        // Explicit fragile keywords
        if (containsAny(n, "dễ vỡ", "fragile", "cẩn thận")) {
            return true;
        }
        // Electronics are fragile: PC, TV, monitors, laptops, tablets
        if (containsAny(n, "pc", "tivi", "tv", "màn hình", "laptop", "máy tính", "máy tính bảng", "tablet")) {
            return true;
        }
        // Glass, ceramics, mirrors
        if (containsAny(n, "kính", "gương", "gốm", "sứ", "chén", "bát", "đĩa", "ấm", "đèn", "tranh")) {
            return true;
        }
        return false;
    }

    private static boolean requiresDisassembly(String n0) {
        String n = n0.toLowerCase(Locale.ROOT);
        if (containsAny(n, "tháo", "tháo lắp", "tháo rời", "tháo chân")) {
            return true;
        }
        if (containsAny(n, "tủ lạnh", "máy giặt", "máy sấy", "giường", "tủ", "kệ", "bàn", "sofa")) {
            return true;
        }
        if (containsAny(n, "tivi", "tv")) {
            Matcher m = INCH.matcher(n);
            if (m.find()) {
                return Integer.parseInt(m.group(1)) >= 40;
            }
            return true;
        }
        return false;
    }

    private static boolean containsAny(String s, String... kws) {
        for (String k : kws) {
            if (s.contains(k)) {
                return true;
            }
        }
        return false;
    }

    private static String stripLeadingArticles(String s) {
        return s.replaceFirst("^(bộ|cái|chiếc)\\s+", "").trim();
    }

    private static String capitalizeWord(String s) {
        if (s == null || s.isBlank()) {
            return s;
        }
        return s.substring(0, 1).toUpperCase(Locale.ROOT) + s.substring(1).toLowerCase(Locale.ROOT);
    }

    private static String capitalizeName(String s) {
        if (s == null || s.isBlank()) {
            return s;
        }
        return s.substring(0, 1).toUpperCase(Locale.ROOT) + s.substring(1);
    }

    // =================== Mapping ===================
    private IntakeParseTextResponse.ParsedItem toDomain(ParsedItemRaw r) {
        return IntakeParseTextResponse.ParsedItem.builder()
                .name(Objects.requireNonNullElse(r.name, ""))
                .brand(emptyToNull(r.brand))
                .model(emptyToNull(r.model))
                .quantity(r.quantity != null && r.quantity > 0 ? r.quantity : 1)
                .categoryName(Objects.requireNonNullElse(r.categoryName, "Khác"))
                .size(Objects.requireNonNullElse(r.size, "M"))
                .isFragile(Boolean.TRUE.equals(r.isFragile))
                .requiresDisassembly(Boolean.TRUE.equals(r.requiresDisassembly))
                .confidence(r.confidence != null ? clamp(r.confidence, 0.8, 0.95) : 0.9)
                .build();
    }

    private static double clamp(double v, double min, double max) {
        return Math.max(min, Math.min(max, v));
    }

    private static String emptyToNull(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }

    private static String stripCodeFence(String s) {
        String t = s.trim();
        if (t.startsWith("```")) {
            int idx = t.indexOf('\n');
            if (idx > -1) {
                t = t.substring(idx + 1);
            }
            int end = t.lastIndexOf("```");
            if (end > -1) {
                t = t.substring(0, end);
            }
        }
        return t.trim();
    }

    private static RestTemplate buildRestTemplate() {
        SimpleClientHttpRequestFactory f = new SimpleClientHttpRequestFactory();
        f.setConnectTimeout((int) Duration.ofSeconds(5).toMillis());
        f.setReadTimeout((int) Duration.ofSeconds(20).toMillis());
        return new RestTemplate(f);
    }

    // =================== DTOs ===================
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class ChatCompletionResponse {

        public List<Choice> choices;

        @JsonIgnoreProperties(ignoreUnknown = true)
        static class Choice {

            public Message message;
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        static class Message {

            public String role;
            public String content;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class ParsedItemRaw {

        public String name;
        public String brand;
        public String model;
        public Integer quantity;

        @JsonProperty("category_name")
        public String categoryName;

        public String size;

        @JsonProperty("is_fragile")
        public Boolean isFragile;

        @JsonProperty("requires_disassembly")
        public Boolean requiresDisassembly;

        public Double confidence;
    }

    // Heuristic holder
    private record ParsedHeu(String name, String brand, int quantity, String category, String size, boolean fragile, boolean disassembly) {

    }
}

