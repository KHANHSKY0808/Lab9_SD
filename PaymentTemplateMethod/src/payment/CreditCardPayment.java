package payment;

/**
 * Thanh toán bằng thẻ tín dụng.
 * <ul>
 *   <li>Phí: 2% giá trị đơn.</li>
 *   <li>Xác thực: mã OTP (mô phỏng, mã đúng là {@value #VALID_OTP}).</li>
 *   <li>Trừ tiền: từ chối nếu vượt hạn mức còn lại của thẻ.</li>
 * </ul>
 */
public class CreditCardPayment extends PaymentProcessor {

    /** Mã OTP "đúng" do ngân hàng gửi (mô phỏng). */
    public static final String VALID_OTP = "123456";

    private final String aCardNumber;
    private final String aEnteredOtp;
    private long aRemainingLimit;

    /**
     * @param pCardNumber   Số thẻ.
     * @param pCreditLimit  Hạn mức còn lại của thẻ (VND).
     * @param pEnteredOtp   Mã OTP khách hàng nhập.
     */
    public CreditCardPayment(String pCardNumber, long pCreditLimit, String pEnteredOtp) {
        aCardNumber = pCardNumber;
        aRemainingLimit = pCreditLimit;
        aEnteredOtp = pEnteredOtp;
    }

    @Override
    protected long calculateFee(Order pOrder) {
        long fee = Math.round(pOrder.getAmount() * 0.02);
        log("      Phi the tin dung (2%): " + money(fee));
        return fee;
    }

    @Override
    protected void authenticate(Order pOrder) throws PaymentException {
        if (!VALID_OTP.equals(aEnteredOtp)) {
            throw new PaymentException("Xac thuc OTP that bai");
        }
        log("      OTP hop le");
    }

    @Override
    protected void charge(Order pOrder, long pFee) throws PaymentException {
        long total = pOrder.getAmount() + pFee;
        if (total > aRemainingLimit) {
            throw new PaymentException("Vuot han muc the: can " + money(total)
                    + ", con lai " + money(aRemainingLimit));
        }
        aRemainingLimit -= total;
        log("      Da tru " + money(total) + " tu the " + aCardNumber
                + ", han muc con lai " + money(aRemainingLimit));
    }
}
