package com.example.contractai.controller;

import com.example.contractai.dto.ContractRequest;
import com.example.contractai.dto.GeneratedContract;
import com.example.contractai.service.ContractOrchestrator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controller điều phối các yêu cầu liên quan đến hợp đồng.
 * Đây là điểm vào chính của người dùng trên giao diện web.
 */
@Controller
@RequestMapping("/contracts") // Định nghĩa đường dẫn cơ bản cho tất cả các method trong controller này
public class ContractController {

    @Autowired
    private ContractOrchestrator orchestrator;

    /**
     * HIỂN THỊ TRANG NHẬP LIỆU
     * Phương thức này sẽ được gọi khi người dùng truy cập URL: /contracts/new
     *
     * @param model Đối tượng dùng để truyền dữ liệu từ Controller sang View (Thymeleaf)
     * @return Tên của file template HTML cần hiển thị (contract-form.html)
     */
    @GetMapping("/new")
    public String showContractForm(Model model) {
        // Tạo một đối tượng ContractRequest rỗng
        // Thymeleaf sẽ sử dụng đối tượng này để gán các giá trị từ form vào
        model.addAttribute("contractRequest", new ContractRequest());
        return "contract-form"; // Trả về file contract-form.html trong thư mục templates
    }

    /**
     * XỬ LÝ DỮ LIỆU KHI NGƯỜI DÙNG SUBMIT FORM
     * Phương thức này sẽ được gọi khi người dùng nhấn nút "Tạo Hợp Đồng" trên form.
     * Form sẽ gửi một POST request đến URL: /contracts/create
     *
     * @param contractRequest Đối tượng chứa tất cả dữ liệu người dùng đã nhập.
     *                        Spring sẽ tự động điền dữ liệu từ form vào đối tượng này (@ModelAttribute).
     * @param model Đối tượng để truyền dữ liệu sang trang kết quả.
     * @return Tên của file template HTML hiển thị kết quả (result.html)
     */
    @PostMapping("/create")
    public String createContract(@ModelAttribute ContractRequest contractRequest, Model model) {
        // Gọi "nhạc trưởng" ContractOrchestrator để thực thi toàn bộ quy trình AI
        GeneratedContract result = orchestrator.generateContract(contractRequest);

        // Đưa đối tượng kết quả (chứa bản nháp và bản rà soát) vào model
        // để trang result.html có thể lấy ra và hiển thị
        model.addAttribute("generatedContract", result);

        // Trả về file result.html để hiển thị kết quả cho người dùng
        return "result";
    }
}