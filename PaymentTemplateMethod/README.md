# Xử lý thanh toán – mẫu thiết kế Template Method (Java)

Bài tập môn Thiết kế phần mềm: áp dụng mẫu thiết kế **Template Method** cho quy trình xử lý thanh toán đa bước.

## Cấu trúc thư mục
```
PaymentTemplateMethod/
├── .project                                  # Cấu hình dự án Eclipse
├── .classpath                                # Cấu hình Build Path Eclipse (src, test, JRE-17, JUnit 5)
├── .settings/                                # Cấu hình UTF-8 và Compiler Compliance 17
├── src/payment/                              # Mã nguồn chính (Source Folder)
│   ├── PaymentProcessor.java                 # Lớp trừu tượng, chứa template method process() (final)
│   ├── CreditCardPayment.java                # Lớp cụ thể 1: Thẻ tín dụng
│   ├── EWalletPayment.java                   # Lớp cụ thể 2: Ví điện tử
│   ├── BankTransferPayment.java              # Lớp cụ thể 3: Chuyển khoản ngân hàng
│   ├── Order.java                            # Đơn hàng
│   ├── PaymentResult.java                    # Kết quả thanh toán (bất biến)
│   ├── PaymentException.java                 # Lỗi ở một bước nghiệp vụ
│   └── Main.java                             # Chương trình minh họa (5 trường hợp)
├── test/payment/                             # Mã nguồn kiểm thử (Test Source Folder)
│   └── PaymentProcessorTest.java             # 17 test JUnit 5
├── lib/                                      # Thư viện hỗ trợ chạy test và coverage độc lập
│   ├── junit-platform-console-standalone-1.10.2.jar
│   ├── org.jacoco.agent-0.8.12-runtime.jar
│   └── org.jacoco.cli-0.8.12-nodeps.jar
├── docs/                                     # Tài liệu minh chứng và báo cáo
│   ├── uml/                                  # Sơ đồ UML lớp (PlantUML & Mermaid)
│   │   ├── PaymentTemplateMethod_UML.puml
│   │   └── README.md
│   ├── screenshots/                          # Ảnh chụp kết quả chạy thực tế
│   │   ├── 01_main_success_cases.png
│   │   ├── 02_main_failure_cases.png
│   │   ├── 03_junit5_test_results.png
│   │   └── 04_jacoco_coverage_report.png
│   └── report/                               # Báo cáo chi tiết 2-3 trang và báo cáo JaCoCo
│       ├── BAO_CAO_TEMPLATE_METHOD.md
│       └── jacoco.csv
├── .gitignore
└── README.md
```

## Quy trình xử lý (Template Method)
Template method `process(Order)` được khai báo `final` trong lớp trừu tượng `PaymentProcessor`, cố định 6 bước:
1. **Kiểm tra đơn hàng** (`validateOrder` - phương thức chung)
2. **Tính phí giao dịch** (`calculateFee` - trừu tượng)
3. **Xác thực người trả tiền** (`authenticate` - trừu tượng, có hook `requiresAuthentication()` để bỏ qua)
4. **Thực hiện trừ tiền** (`charge` - trừu tượng)
5. **Phát hành biên lai** (`issueReceipt` - phương thức chung)
6. **Thông báo cho khách hàng** (`notifyCustomer` - hook có cài đặt mặc định gửi email)

| Phương thức | Thẻ tín dụng | Ví điện tử | Chuyển khoản ngân hàng |
|---|---|---|---|
| **Phí** | 2% giá trị đơn hàng | 1% (tối thiểu 1.000 VND) | Cố định 5.000 VND |
| **Xác thực** | OTP ngân hàng (đúng: `123456`) | PIN bảo mật (đúng: `1234`) | Bỏ qua bước xác thực (hook) |
| **Trừ tiền** | Kiểm tra hạn mức khả dụng | Kiểm tra số dư ví | Tạo mã giao dịch chuyển khoản |
| **Thông báo** | Email mặc định | Tin nhắn trong ứng dụng ví | Email kèm số tài khoản nhận tiền |



## Kết quả kiểm chứng
1. **Biên dịch:** Không lỗi, không cảnh báo.
2. **Chạy `Main`:** Thể hiện đầy đủ 5 trường hợp:
   - TH 1: Thẻ tín dụng thành công (phí 2%, OTP đúng, trừ hạn mức).
   - TH 2: Ví điện tử thành công (phí 1%, PIN đúng, thông báo qua app).
   - TH 3: Chuyển khoản thành công (phí 5.000đ, có dòng `"Bo qua xac thuc (hook requiresAuthentication = false)"`).
   - TH 4: Ví điện tử nhập sai PIN (thất bại, dừng ngay tại bước xác thực, không trừ tiền, `Don da thanh toan: false`).
   - TH 5: Thẻ tín dụng vượt hạn mức (thất bại, dừng ngay tại bước trừ tiền, `Don da thanh toan: false`).
3. **Chạy `PaymentProcessorTest`:** 17/17 ca kiểm thử đều đạt (**100% Passed**).
4. **Độ bao phủ JaCoCo:** Instruction coverage đạt **94.6%**, Line coverage đạt **96.1%**.
5. **Ảnh minh chứng:** Đã lưu đầy đủ vào thư mục `docs/screenshots/`.
