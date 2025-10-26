package com.example.contractai.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ContractRequest {
    private String customerName;
    private String customerAddress;
    private String productType; // Ví dụ: "Phần mềm", "Dịch vụ tư vấn"
    private String productName;
    private BigDecimal contractValue;
    private String paymentTerm; // Ví dụ: "Thanh toán 1 lần", "Thanh toán 2 đợt"
    private int contractDurationMonths; // Thời hạn hợp đồng (tháng)
}