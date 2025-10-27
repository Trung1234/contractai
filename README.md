# 🤖 Tool AI Soạn Hợp Đồng

Một ứng dụng web dựa trên Spring Boot và AI, giúp tự động hóa quy trình soạn thảo hợp đồng mua bán/dịch vụ. Dự án này được xây dựng theo phong cách "vibe coding", tập trung vào tính thực tế và sự đơn giản.

## ✨ Tính năng chính

-   🚀 **Giao diện nhập liệu trực quan:** Nhân viên kinh doanh dễ dàng điền thông tin hợp đồng qua một form web.
-   🧠 **Tự động soạn thảo bằng AI (RAG):** Hệ thống sử dụng kiến trúc RAG (Retrieval-Augmented Generation) để tạo bản nháp hợp đồng dựa trên các mẫu có sẵn của công ty, đảm bảo tính nhất quán.
-   ⚖️ **Rà soát pháp lý bởi AI:** Sau khi tạo, một "luật sư AI" sẽ rà soát lại bản nháp, chỉ ra các rủi ro, điều khoản còn thiếu và đưa ra gợi ý.
-   📄 **Xuất kết quả dễ dàng:** Hiển thị kết quả rõ ràng trên web và sẵn sàng để tải xuống hoặc sao chép.

## 🛠️ Công nghệ sử dụng

| Lớp               | Công nghệ                | Mô tả                                                                  |
| ----------------- | ------------------------ | ----------------------------------------------------------------------- |
| **Backend**       | Java 17, Spring Boot 3.x | Nền tảng chính, mạnh mẽ và ổn định.                                    |
| **Frontend**      | Thymeleaf                | Server-side rendering, nhanh gọn cho MVP.                               |
| **AI / LLM**      | OpenAI API (GPT-4o)      | "Bộ não" của hệ thống, chịu trách nhiệm tạo và phân tích văn bản.        |
| **Vector DB**     | Pinecone                 | Lưu trữ và truy vấn các mẫu hợp đồng một cách thông minh.               |
| **Build Tool**    | Maven                    | Quản lý dependency và build project.                                    |

## 📋 Điều kiện cần có

Trước khi bắt đầu, hãy đảm bảo bạn đã cài đặt:

-   **JDK 17** hoặc mới hơn.
-   **Maven 3.6** hoặc mới hơn.
-   **API Key** từ [OpenAI Platform](https://platform.openai.com/).
-   **API Key** và **Index Host** từ [Pinecone](https://app.pinecone.io/).

## 🚀 Bắt đầu nhanh

Hãy làm theo các bước sau để chạy dự án trên máy của bạn.

### 1. Clone repository

```bash
git clone https://github.com/your-username/your-repo-name.git
cd your-repo-name
