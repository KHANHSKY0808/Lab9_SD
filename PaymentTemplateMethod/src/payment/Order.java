package payment;

/**
 * Đơn hàng cần thanh toán. Số tiền tính bằng VND (số nguyên) để tránh sai số
 * của số thực.
 */
public class Order {

    private final String aOrderId;
    private final String aCustomerName;
    private final long aAmount;
    private boolean aPaid = false;

    /**
     * @param pOrderId      Mã đơn hàng.
     * @param pCustomerName Tên khách hàng.
     * @param pAmount       Số tiền cần thanh toán (VND).
     */
    public Order(String pOrderId, String pCustomerName, long pAmount) {
        aOrderId = pOrderId;
        aCustomerName = pCustomerName;
        aAmount = pAmount;
    }

    public String getOrderId() {
        return aOrderId;
    }

    public String getCustomerName() {
        return aCustomerName;
    }

    public long getAmount() {
        return aAmount;
    }

    public boolean isPaid() {
        return aPaid;
    }

    /** Đánh dấu đơn hàng đã thanh toán. */
    public void markPaid() {
        aPaid = true;
    }
}
