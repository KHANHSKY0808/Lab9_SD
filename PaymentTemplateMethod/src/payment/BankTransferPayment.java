package payment;

/**
 * Thanh toán bằng chuyển khoản ngân hàng.
 * <ul>
 *   <li>Phí: cố định 5.000 VND.</li>
 *   <li>Xác thực: không cần (hook {@link #requiresAuthentication()} trả về false).</li>
 *   <li>Trừ tiền: tạo mã giao dịch chuyển khoản.</li>
 *   <li>Hook thông báo: email kèm số tài khoản nhận tiền.</li>
 * </ul>
 */
public class BankTransferPayment extends PaymentProcessor {

    private static final long FLAT_FEE = 5000;

    private final String aReceivingAccount;

    /** @param pReceivingAccount Số tài khoản cửa hàng nhận tiền. */
    public BankTransferPayment(String pReceivingAccount) {
        aReceivingAccount = pReceivingAccount;
    }

    @Override
    protected long calculateFee(Order pOrder) {
        log("      Phi chuyen khoan (co dinh): " + money(FLAT_FEE));
        return FLAT_FEE;
    }

    /** Hook ghi đè: chuyển khoản không cần bước xác thực riêng. */
    @Override
    protected boolean requiresAuthentication() {
        return false;
    }

    /** Không được gọi vì hook trả về false; cài đặt rỗng chỉ để thỏa lớp trừu tượng. */
    @Override
    protected void authenticate(Order pOrder) {
    }

    @Override
    protected void charge(Order pOrder, long pFee) {
        String code = "BT-" + pOrder.getOrderId();
        log("      Tao ma giao dich " + code + ", chuyen " + money(pOrder.getAmount() + pFee)
                + " den tai khoan " + aReceivingAccount);
    }

    /** Hook ghi đè: email kèm số tài khoản nhận tiền. */
    @Override
    protected void notifyCustomer(Order pOrder) {
        log("      Gui email xac nhan kem so tai khoan nhan " + aReceivingAccount
                + " den khach hang " + pOrder.getCustomerName());
    }
}
