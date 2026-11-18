package payment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Lớp trừu tượng định nghĩa KHUNG của quy trình thanh toán (mẫu Template Method).
 *
 * <p>Quy trình gồm 6 bước theo thứ tự cố định, được viết trong phương thức
 * {@link #process(Order)} (template method, khai báo {@code final} để lớp con
 * không thể thay đổi thứ tự):
 * <ol>
 *   <li>Kiểm tra đơn hàng ({@link #validateOrder}) - bước CHUNG</li>
 *   <li>Tính phí ({@link #calculateFee}) - bước THAY ĐỔI (abstract)</li>
 *   <li>Xác thực ({@link #authenticate}) - bước THAY ĐỔI (abstract), có thể bỏ qua
 *       bằng hook {@link #requiresAuthentication()}</li>
 *   <li>Trừ tiền ({@link #charge}) - bước THAY ĐỔI (abstract)</li>
 *   <li>Phát hành biên lai ({@link #issueReceipt}) - bước CHUNG</li>
 *   <li>Thông báo cho khách ({@link #notifyCustomer}) - HOOK có cài đặt mặc định</li>
 * </ol>
 *
 * <p>Lớp cha gọi lớp con chứ không phải ngược lại (nguyên lý Hollywood).
 */
public abstract class PaymentProcessor {

    private final List<String> aLog = new ArrayList<>();

    // ===================== TEMPLATE METHOD =====================

    /**
     * Xử lý thanh toán cho một đơn hàng theo đúng thứ tự các bước.
     * Dừng ngay khi một bước ném {@link PaymentException}.
     *
     * @param pOrder Đơn hàng cần thanh toán.
     * @return Kết quả thanh toán.
     * @pre pOrder != null
     */
    public final PaymentResult process(Order pOrder) {
        assert pOrder != null;
        log("=== " + getClass().getSimpleName() + " xu ly don " + pOrder.getOrderId() + " ===");
        try {
            log("[1/6] Kiem tra don hang");
            validateOrder(pOrder);

            log("[2/6] Tinh phi giao dich");
            long fee = calculateFee(pOrder);

            if (requiresAuthentication()) {
                log("[3/6] Xac thuc nguoi tra tien");
                authenticate(pOrder);
            }
            else {
                log("[3/6] Bo qua xac thuc (hook requiresAuthentication = false)");
            }

            log("[4/6] Thuc hien tru tien");
            charge(pOrder, fee);
            pOrder.markPaid();

            log("[5/6] Phat hanh bien lai");
            issueReceipt(pOrder, fee);

            log("[6/6] Thong bao cho khach hang");
            notifyCustomer(pOrder);

            return PaymentResult.success(fee, "Thanh toan don " + pOrder.getOrderId() + " thanh cong");
        }
        catch (PaymentException e) {
            log("THAT BAI: " + e.getMessage());
            return PaymentResult.failure(e.getMessage());
        }
    }

    // ===================== BƯỚC CHUNG (đã cài đặt) =====================

    /**
     * Bước chung: kiểm tra đơn hàng hợp lệ.
     *
     * @throws PaymentException nếu đơn không hợp lệ hoặc đã thanh toán.
     */
    protected void validateOrder(Order pOrder) throws PaymentException {
        if (pOrder.getAmount() <= 0) {
            throw new PaymentException("Don hang khong hop le: so tien phai lon hon 0");
        }
        if (pOrder.getCustomerName() == null || pOrder.getCustomerName().isBlank()) {
            throw new PaymentException("Don hang khong hop le: thieu ten khach hang");
        }
        if (pOrder.isPaid()) {
            throw new PaymentException("Don hang da duoc thanh toan truoc do");
        }
    }

    /** Bước chung: phát hành biên lai với cùng một định dạng cho mọi phương thức. */
    protected void issueReceipt(Order pOrder, long pFee) {
        log("      Bien lai | don: " + pOrder.getOrderId()
                + " | khach: " + pOrder.getCustomerName()
                + " | tien hang: " + money(pOrder.getAmount())
                + " | phi: " + money(pFee)
                + " | tong: " + money(pOrder.getAmount() + pFee));
    }

    // ===================== BƯỚC THAY ĐỔI (abstract) =====================

    /**
     * Bước thay đổi: tính phí giao dịch theo phương thức thanh toán.
     *
     * @return Phí (VND).
     */
    protected abstract long calculateFee(Order pOrder);

    /**
     * Bước thay đổi: xác thực người trả tiền (OTP, PIN...).
     *
     * @throws PaymentException nếu xác thực không thành công.
     */
    protected abstract void authenticate(Order pOrder) throws PaymentException;

    /**
     * Bước thay đổi: trừ tiền theo phương thức thanh toán.
     *
     * @param pFee Phí đã tính ở bước 2.
     * @throws PaymentException nếu không trừ được tiền (không đủ hạn mức, số dư...).
     */
    protected abstract void charge(Order pOrder, long pFee) throws PaymentException;

    // ===================== HOOK (có cài đặt mặc định) =====================

    /**
     * Hook: có cần bước xác thực hay không. Mặc định là có.
     * Lớp con có thể ghi đè trả về false để bỏ qua bước 3.
     */
    protected boolean requiresAuthentication() {
        return true;
    }

    /**
     * Hook: thông báo cho khách hàng. Mặc định gửi email.
     * Lớp con có thể ghi đè để đổi kênh hoặc nội dung.
     */
    protected void notifyCustomer(Order pOrder) {
        log("      Gui email xac nhan den khach hang " + pOrder.getCustomerName());
    }

    // ===================== TIỆN ÍCH =====================

    /** Ghi một dòng nhật ký và in ra màn hình. */
    protected final void log(String pMessage) {
        aLog.add(pMessage);
        System.out.println(pMessage);
    }

    /** @return Bản sao chỉ đọc của nhật ký các bước đã chạy. */
    public final List<String> getLog() {
        return Collections.unmodifiableList(aLog);
    }

    /** Định dạng số tiền, ví dụ 1,000,000 VND. */
    protected static String money(long pAmount) {
        return String.format(Locale.US, "%,d VND", pAmount);
    }
}
