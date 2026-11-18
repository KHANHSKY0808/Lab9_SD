package payment;

/**
 * Ngoại lệ báo một bước trong quy trình thanh toán thất bại
 * (đơn không hợp lệ, xác thực sai, không đủ tiền...).
 * Khi gặp ngoại lệ này, template method dừng quy trình ngay.
 */
public class PaymentException extends Exception {

    private static final long serialVersionUID = 1L;

    public PaymentException(String pMessage) {
        super(pMessage);
    }
}
