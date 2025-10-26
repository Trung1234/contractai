package com.example.contractai.service;

import com.example.contractai.dto.ContractRequest;
import com.example.contractai.dto.GeneratedContract;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ContractOrchestrator {
    @Autowired
    private LlmService llmService;

    @Autowired
    private ReviewService reviewService;

    // ... (các phần khác không đổi)

    public GeneratedContract generateContract(ContractRequest request) {
        // 1. Gọi LlmService để tạo bản nháp
        String draft = llmService.generateContractDraft(request);

        // *** THÊM ĐOẠN NÀY ***
        // Ngủ 5 giây để tránh vượt rate limit của gói miễn phí
        try {
            Thread.sleep(5000); // 5000 milliseconds = 5 seconds
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            // Xử lý nếu cần, nhưng thường không cần cho demo
        }
        // *** KẾT THÚC ĐOẠNG THÊM ***

        // 2. Gọi ReviewService để rà soát bản nháp đó
        String review = reviewService.reviewContract(draft);

        // 3. Gói cả hai kết quả vào một object duy nhất
        GeneratedContract result = new GeneratedContract(draft, review);

        return result;
    }
}