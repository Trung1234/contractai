contract-ai/
├── src/
│   ├── main/
│   │   ├── java/com/example/contractai/
│   │   │   ├── controller/      # Điều phối request từ người dùng
│   │   │   ├── dto/            # Các đối tượng dữ liệu (Data Transfer Objects)
│   │   │   ├── service/        # Chứa logic nghiệp vụ chính (AI, Rà soát, Mẫu)
│   │   │   └── ContractAiApplication.java
│   │   └── resources/
│   │       ├── static/         # CSS, JS, hình ảnh (nếu có)
│   │       ├── templates/      # Các file HTML (Thymeleaf)
│   │       └── application.properties  # File cấu hình
├── target/                     # Thư mục chứa file .jar sau khi build
├── pom.xml                     # File cấu hình Maven
└── README.md                   # File này!
