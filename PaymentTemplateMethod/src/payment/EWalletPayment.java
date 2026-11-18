package payment;

/**
 * Thanh toán bằng ví điện tử.
 * <ul>
 *   <li>Phí: 1% giá trị đơn, tối thiểu 1.000 VND.</li>
 *   <li>Xác thực: mã PIN (mô phỏng, PIN đúng là {@value #VALID_PIN}).</li>
 *   <li>Trừ tiền: từ chối nếu số dư ví không đủ.</li>
 *   <li>Hook thông báo: gửi tin nhắn trong ứng dụng thay vì email.</li>
 * </ul>
 */
public class EWalletPayment extends PaymentProcessor {

    /** PIN "đúng" của ví (mô phỏng). */
    public static final String VALID_PIN = "1234";

    private static final long MIN_FEE = 1000;

    private final String aPhoneNumber;
    private final String aEnteredPin;
    private long aBalance;

    /**
     * @param pPhoneNumber Số điện thoại gắn với ví.
     * @param pBalance     Số dư hiện có (VND).
     * @param pEnteredPin  PIN khách hàng nhập.
     */
    public EWalletPayment(String pPhoneNumber, long pBalance, String pEnteredPin) {
        aPhoneNumber = pPhoneNumber;
        aBalance = pBalance;
        aEnteredPin = pEnteredPin;
    }

    @Override
    protected long calculateFee(Order pOrder) {
        long fee = Math.max(MIN_FEE, Math.round(pOrder.getAmount() * 0.01));
        log("      Phi vi dien tu (1%, toi thieu " + money(MIN_FEE) + "): " + money(fee));
        return fee;
    }

    @Override
    protected void authenticate(Order pOrder) throws PaymentException {
        if (!VALID_PIN.equals(aEnteredPin)) {
            throw new PaymentException("Xac thuc PIN that bai");
        }
        log("      PIN hop le");
    }

    @Override
    protected void charge(Order pOrder, long pFee) throws PaymentException {
        long total = pOrder.getAmount() + pFee;
        if (total > aBalance) {
            throw new PaymentException("So du vi khong du: can " + money(total)
                    + ", hien co " + money(aBalance));
        }
        aBalance -= total;
        log("      Da tru " + money(total) + " tu vi " + aPhoneNumber
                + ", so du con lai " + money(aBalance));
    }

    /** Hook ghi đè: gửi tin nhắn trong ứng dụng. */
    @Override
    protected void notifyCustomer(Order pOrder) {
        log("      Gui tin nhan trong ung dung vi den so " + aPhoneNumber);
    }
}
