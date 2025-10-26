package com.example.contractai.dto;

import lombok.Data;

/**
 * DTO để chứa kết quả cuối cùng của quá trình tạo hợp đồng.
 * Nó bao gồm cả bản nháp do AI tạo ra và bản phân tích/rà soát của AI.
 */
@Data
public class GeneratedContract {

    /**
     * Bản nháp hợp đồng hoàn chỉnh được tạo ra bởi LlmService.
     * Đây là nội dung chính của hợp đồng.
     */
    private String contractDraft;

    /**
     * Bản phân tích, rà soát và gợi ý từ ReviewService.
     * Có thể chứa các cảnh báo rủi ro, các điều khoản còn thiếu, v.v.
     */
    private String aiReview;


    // Constructor có tham số (Rất hữu ích để tạo object nhanh chóng)
    public GeneratedContract(String contractDraft, String aiReview) {
        this.contractDraft = contractDraft;
        this.aiReview = aiReview;
    }
}