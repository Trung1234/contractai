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

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    @Value("${gemini.api.base.url}")
    private String geminiApiBaseUrl;

    @Value("${gemini.model.name}")
    private String geminiModelName;

    public ReviewService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    public String reviewContract(String contractDraft) {
        log.info("Bắt đầu rà soát bản nháp hợp đồng bằng Gemini...");

        String prompt = buildReviewPrompt(contractDraft);
        String review = callGeminiApi(prompt);

        log.info("Đã hoàn thành rà soát hợp đồng.");
        return review;
    }

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
     * Gọi API của Gemini để thực hiện rà soát.
     * Logic tương tự như trong LlmService.
     */
    private String callGeminiApi(String prompt) {
        String apiUrl = String.format("%s/%s:generateContent", geminiApiBaseUrl, geminiModelName);

        Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                        Map.of(
                                "parts", List.of(
                                        Map.of("text", prompt)
                                )
                        )
                ),
                "generationConfig", Map.of(
                        "temperature", 0.3f,
                        "maxOutputTokens", 1024
                )
        );

        Mono<Map> responseMono = this.webClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path(apiUrl)
                        .queryParam("key", geminiApiKey)
                        .build())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Map.class);

        Map response = responseMono.block();
        if (response == null || response.get("candidates") == null) {
            return "Lỗi: Không thể thực hiện rà soát AI.";
        }

        List<Map> candidates = (List<Map>) response.get("candidates");
        if (candidates.isEmpty()) {
            return "Lỗi: AI không tạo được nội dung rà soát.";
        }

        Map<String, Object> candidate = candidates.get(0);
        Map<String, Object> content = (Map<String, Object>) candidate.get("content");
        List<Map> parts = (List<Map>) content.get("parts");

        if (parts == null || parts.isEmpty()) {
            return "Lỗi: Phản hồi rà soát từ AI không có nội dung.";
        }

        return (String) parts.get(0).get("text");
    }
}