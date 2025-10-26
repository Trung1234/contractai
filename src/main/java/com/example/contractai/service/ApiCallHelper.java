package com.example.contractai.service;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import java.util.function.Supplier;

public class ApiCallHelper {

    private static final Logger log = LoggerFactory.getLogger(ApiCallHelper.class);

    /**
     * Thực hiện một cuộc gọi API có cơ chế thử lại khi gặp lỗi 429.
     * @param apiCall Một hành động (lambda) đại diện cho cuộc gọi API.
     * @param maxRetries Số lần thử lại tối đa.
     * @param initialDelayMillis Thời gian đợi ban đầu (mili giây).
     * @return Kết quả của cuộc gọi API.
     */
    public static <T> T callWithRetry(Supplier<T> apiCall, int maxRetries, long initialDelayMillis) {
        int attempts = 0;
        long delay = initialDelayMillis;

        while (true) {
            try {
                attempts++;
                return apiCall.get();
            } catch (WebClientResponseException e) {
                // Chỉ thử lại nếu lỗi là 429 và chưa đạt số lần thử lại tối đa
                if (e.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS && attempts <= maxRetries) {
                    log.warn("Bị lỗi 429, đang thử lại sau {} mili giây... (Lần thử {}/{})", delay, attempts, maxRetries + 1);
                    try {
                        Thread.sleep(delay);
                        delay *= 2; // Tăng thời gian đợi cho lần sau (Exponential Backoff)
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Quá trình bị gián đoạn.", ie);
                    }
                } else {
                    // Nếu là lỗi khác hoặc đã thử lại quá số lần, ném lỗi ra ngoài
                    log.error("Gọi API thất bại sau {} lần thử. Lỗi: {}", attempts, e.getMessage());
                    throw new RuntimeException("Gọi API thất bại: " + e.getResponseBodyAsString(), e);
                }
            }
        }
    }
}