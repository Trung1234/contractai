package com.example.contractai.service;

import com.example.contractai.dto.ContractRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Service
public class LlmService {

    private static final Logger log = LoggerFactory.getLogger(LlmService.class);

    private final WebClient webClient;

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    @Value("${gemini.api.base.url}")
    private String geminiApiBaseUrl;

    @Value("${gemini.model.name}")
    private String geminiModelName;

    public LlmService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    public String generateContractDraft(ContractRequest request) {
        log.info("Bắt đầu tạo hợp đồng cho sản phẩm: {} bằng Gemini", request.getProductName());

        List<String> relevantContext = retrieveRelevantContext(request.getProductName());
        log.info("Đã lấy được {} đoạn context liên quan.", relevantContext.size());

        String prompt = buildPrompt(request, relevantContext);
        log.info("Đã xây dựng prompt hoàn chỉnh.");

        String contractDraft = callGeminiApi(prompt);
        log.info("Đã tạo xong bản nháp hợp đồng.");

        return contractDraft;
    }

    private List<String> retrieveRelevantContext(String productName) {
        log.warn("Đang sử dụng dữ liệu context giả lập. Cần triển khai Pinecone API.");
        return List.of(
                "Điều 1: Đối tượng hợp đồng. Bên A đồng ý cung cấp cho Bên B giải pháp phần mềm tên là [Tên sản phẩm].",
                "Điều 5: Thanh toán. Bên B thanh toán 100% giá trị hợp đồng sau khi ký hợp đồng. Phương thức thanh toán: Chuyển khoản.",
                "Điều 7: Bảo hành. Bên A bảo hành hệ thống trong 12 tháng kể từ ngày nghiệm thu."
        );
    }

    private String buildPrompt(ContractRequest request, List<String> context) {
        StringBuilder promptBuilder = new StringBuilder();
        promptBuilder.append("Bạn là một chuyên gia pháp lý. Dựa vào thông tin và các mẫu hợp đồng tham khảo dưới đây, hãy soạn thảo một bản hợp đồng hoàn chỉnh, chuyên nghiệp.\n\n");
        promptBuilder.append("--- THÔNG TIN TỪ NGƯỜI DÙNG ---\n");
        promptBuilder.append("Tên khách hàng: ").append(request.getCustomerName()).append("\n");
        promptBuilder.append("Tên sản phẩm: ").append(request.getProductName()).append("\n");
        promptBuilder.append("Giá trị hợp đồng: ").append(request.getContractValue()).append(" VNĐ\n");
        promptBuilder.append("Điều khoản thanh toán: ").append(request.getPaymentTerm()).append("\n\n");

        promptBuilder.append("--- MẪU HỢP ĐỒNG THAM KHẢO ---\n");
        for (String chunk : context) {
            promptBuilder.append(chunk).append("\n");
        }
        promptBuilder.append("\n--- YÊU CẦU ---\n");
        promptBuilder.append("Hãy soạn thảo một hợp đồng dựa trên tất cả thông tin trên. Bắt đầu bằng 'HỢP ĐỒNG CUNG CẤP SẢN PHẨM/DỊCH VỤ' và kết thúc bằng phần chữ ký. Đảm bảo điền chính xác các thông tin từ người dùng.");

        return promptBuilder.toString();
    }

    /**
     * Gọi API generateContent của Google Gemini để tạo nội dung.
     */
    private String callGeminiApi(String prompt) {
        // Xây dựng URL đầy đủ, bao gồm model name và endpoint
        String apiUrl = String.format("%s/%s:generateContent", geminiApiBaseUrl, geminiModelName);

        // Xây dựng request body theo định dạng của Gemini
        Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                        Map.of(
                                "parts", List.of(
                                        Map.of("text", prompt)
                                )
                        )
                ),
                "generationConfig", Map.of(
                        "temperature", 0.2f,
                        "maxOutputTokens", 2048
                )
        );

        // Gọi API và xử lý phản hồi
        Mono<Map> responseMono = this.webClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path(apiUrl) // Sử dụng URL đã xây dựng
                        .queryParam("key", geminiApiKey) // Thêm API key vào query parameter
                        .build())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Map.class);

        Map response = responseMono.block();
        if (response == null || response.get("candidates") == null) {
            return "Lỗi: Không nhận được phản hồi từ AI.";
        }

        List<Map> candidates = (List<Map>) response.get("candidates");
        if (candidates.isEmpty()) {
            return "Lỗi: AI không tạo được nội dung.";
        }

        Map<String, Object> candidate = candidates.get(0);
        Map<String, Object> content = (Map<String, Object>) candidate.get("content");
        List<Map> parts = (List<Map>) content.get("parts");

        if (parts == null || parts.isEmpty()) {
            return "Lỗi: Phản hồi từ AI không có nội dung.";
        }

        return (String) parts.get(0).get("text");
    }
}