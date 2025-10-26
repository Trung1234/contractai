package com.example.contractai.service;

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
public class ReviewService {

    private static final Logger log = LoggerFactory.getLogger(ReviewService.class);

    private final WebClient webClient;

    @Value("${openai.api.key}")
    private String openaiApiKey;

    @Value("${openai.api.url}")
    private String openaiApiUrl;

    public ReviewService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    /**
     * Rà soát bản nháp hợp đồng và đưa ra các gợi ý, cảnh báo.
     */
    public String reviewContract(String contractDraft) {
        log.info("Bắt đầu rà soát bản nháp hợp đồng...");

        String prompt = buildReviewPrompt(contractDraft);
        String review = callOpenAiApi(prompt);

        log.info("Đã hoàn thành rà soát hợp đồng.");
        return review;
    }

    /**
     * Xây dựng prompt chuyên biệt cho việc rà soát.
     */
    private String buildReviewPrompt(String contractDraft) {
        StringBuilder promptBuilder = new StringBuilder();
        promptBuilder.append("Hãy đóng vai một luật sư có kinh nghiệm. Hãy rà soát bản nháp hợp đồng dưới đây và chỉ ra các điểm sau:\n");
        promptBuilder.append("1. Các rủi ro pháp lý tiềm ẩn.\n");
        promptBuilder.append("2. Các điều khoản quan trọng có thể bị thiếu (ví dụ: phạt vi phạm, bảo mật, giải quyết tranh chấp).\n");
        promptBuilder.append("3. Bất kỳ sự mâu thuẫn hoặc không rõ ràng nào trong văn bản.\n\n");
        promptBuilder.append("--- BẢN NHÁP HỢP ĐỒNG ---\n");
        promptBuilder.append(contractDraft);
        promptBuilder.append("\n\n--- PHÂN TÍCH ---\n");
        promptBuilder.append("Hãy đưa ra phân tích của bạn một cách rõ ràng, súc tích.");

        return promptBuilder.toString();
    }

    /**
     * Gọi API của OpenAI để thực hiện rà soát.
     * Logic tương tự như trong LlmService nhưng với prompt khác.
     */
    private String callOpenAiApi(String prompt) {
        Map<String, Object> requestBody = Map.of(
                "model", "gpt-4o-mini",
                "messages", List.of(
                        Map.of("role", "system", "content", "Bạn là một luật sư chuyên rà soát hợp đồng."),
                        Map.of("role", "user", "content", prompt)
                ),
                "temperature", 0.3
        );

        Mono<Map> responseMono = this.webClient.post()
                .uri(openaiApiUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + openaiApiKey)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Map.class);

        Map response = responseMono.block();
        if (response == null || response.get("choices") == null) {
            return "Lỗi: Không thể thực hiện rà soát AI.";
        }

        List<Map> choices = (List<Map>) response.get("choices");
        Map<String, String> message = (Map<String, String>) choices.get(0).get("message");
        return message.get("content");
    }
}