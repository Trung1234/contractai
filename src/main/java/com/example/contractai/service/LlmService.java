package com.example.contractai.service;

import com.example.contractai.dto.ContractRequest;

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

    // Sử dụng WebClient để gọi các API bên ngoài
    private final WebClient webClient;

    // Lấy các giá trị cấu hình từ application.properties
    @Value("${openai.api.key}")
    private String openaiApiKey;

    @Value("${openai.api.url}")
    private String openaiApiUrl;

//    @Value("${pinecone.api.key}")
//    private String pineconeApiKey;
//
//    @Value("${pinecone.index.host}") // Ví dụ: https://index-name-xxx.svc.aped-4627.pinecone.io
//    private String pineconeIndexHost;

    public LlmService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    /**
     * Phương thức chính để tạo bản nháp hợp đồng.
     * Triển khai luồng RAG (Retrieve-Augment-Generate).
     */
    public String generateContractDraft(ContractRequest request) {
        log.info("Bắt đầu tạo hợp đồng cho sản phẩm: {}", request.getProductName());

        // --- BƯỚC 1: RETRIEVE ---
        // Trong một ứng dụng thực tế, bạn sẽ tạo embedding cho query và gọi Pinecone.
        // Để đơn giản cho MVP, chúng ta sẽ giả lập việc lấy context.
        // TODO: Triển khai logic gọi Pinecone API ở đây.
        List<String> relevantContext = retrieveRelevantContext(request.getProductName());
        log.info("Đã lấy được {} đoạn context liên quan.", relevantContext.size());

        // --- BƯỚC 2: AUGMENT ---
        String prompt = buildPrompt(request, relevantContext);
        log.info("Đã xây dựng prompt hoàn chỉnh.");

        // --- BƯỚC 3: GENERATE ---
        String contractDraft = callOpenAiApi(prompt);
        log.info("Đã tạo xong bản nháp hợp đồng.");

        return contractDraft;
    }

    /**
     * Giả lập việc truy xuất dữ liệu từ Vector DB.
     * Trong thực tế, method này sẽ gọi API của Pinecone/Weaviate.
     */
    private List<String> retrieveRelevantContext(String productName) {
        // Đây là dữ liệu giả lập (mock data)
        // Bạn sẽ thay thế bằng logic gọi API thực tế
        log.warn("Đang sử dụng dữ liệu context giả lập. Cần triển khai Pinecone API.");
        return List.of(
                "Điều 1: Đối tượng hợp đồng. Bên A đồng ý cung cấp cho Bên B giải pháp phần mềm tên là [Tên sản phẩm].",
                "Điều 5: Thanh toán. Bên B thanh toán 100% giá trị hợp đồng sau khi ký hợp đồng. Phương thức thanh toán: Chuyển khoản.",
                "Điều 7: Bảo hành. Bên A bảo hành hệ thống trong 12 tháng kể từ ngày nghiệm thu."
        );
    }

    /**
     * Xây dựng prompt hoàn chỉnh để gửi đến LLM.
     */
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
     * Gọi API Chat Completions của OpenAI để tạo nội dung.
     */
    private String callOpenAiApi(String prompt) {
        // Sử dụng helper để gọi API với retry logic
        return ApiCallHelper.callWithRetry(() -> {
            // Đây là cuộc gọi API thực tế, được bọc trong một lambda
            Map<String, Object> requestBody = Map.of(
                    "model", "gpt-4o-mini",
                    "messages", List.of(
                            Map.of("role", "system", "content", "Bạn là một chuyên gia soạn thảo hợp đồng."),
                            Map.of("role", "user", "content", prompt)
                    ),
                    "temperature", 0.2
            );

            Map response = this.webClient.post()
                    .uri(openaiApiUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + openaiApiKey)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block(); // block() để chờ kết quả

            if (response == null || response.get("choices") == null) {
                throw new RuntimeException("Lỗi: Không nhận được phản hồi hợp lệ từ AI.");
            }

            List<Map> choices = (List<Map>) response.get("choices");
            Map<String, String> message = (Map<String, String>) choices.get(0).get("message");
            return message.get("content");
        }, 3, 2000); // Thử lại tối đa 3 lần, đợi ban đầu 2 giây
    }
}
