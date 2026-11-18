package payment;

/**
 * Kết quả của một lần xử lý thanh toán (bất biến).
 */
public final class PaymentResult {

    private final boolean aSuccess;
    private final long aFee;
    private final String aMessage;

    private PaymentResult(boolean pSuccess, long pFee, String pMessage) {
        aSuccess = pSuccess;
        aFee = pFee;
        aMessage = pMessage;
    }

    public static PaymentResult success(long pFee, String pMessage) {
        return new PaymentResult(true, pFee, pMessage);
    }

    public static PaymentResult failure(String pMessage) {
        return new PaymentResult(false, 0, pMessage);
    }

    public boolean isSuccess() {
        return aSuccess;
    }

    /** @return Phí giao dịch đã tính (0 nếu thất bại). */
    public long getFee() {
        return aFee;
    }

    public String getMessage() {
        return aMessage;
    }

    @Override
    public String toString() {
        return (aSuccess ? "THANH CONG" : "THAT BAI") + ": " + aMessage;
    }
}
