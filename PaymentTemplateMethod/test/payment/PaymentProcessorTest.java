package payment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Kiểm thử mẫu Template Method trong {@link PaymentProcessor} và ba lớp con.
 *
 * <p>Nhóm 1 kiểm tra riêng KHUNG quy trình (thứ tự các bước, hook, dừng khi lỗi)
 * bằng một lớp con ghi lại các lời gọi. Nhóm 2 kiểm tra hành vi riêng của từng
 * phương thức thanh toán.
 */
public class PaymentProcessorTest {

    // =================== Lớp con ghi lại thứ tự gọi các bước ===================

    /** Lớp con dùng để quan sát thứ tự các bước mà template method gọi. */
    private static class RecordingProcessor extends PaymentProcessor {
        private final List<String> aCalls = new ArrayList<>();
        private final boolean aNeedsAuthentication;
        private final boolean aFailAuthentication;

        RecordingProcessor(boolean pNeedsAuthentication, boolean pFailAuthentication) {
            aNeedsAuthentication = pNeedsAuthentication;
            aFailAuthentication = pFailAuthentication;
        }

        @Override
        protected void validateOrder(Order pOrder) throws PaymentException {
            aCalls.add("validateOrder");
            super.validateOrder(pOrder);
        }

        @Override
        protected long calculateFee(Order pOrder) {
            aCalls.add("calculateFee");
            return 100;
        }

        @Override
        protected void authenticate(Order pOrder) throws PaymentException {
            aCalls.add("authenticate");
            if (aFailAuthentication) {
                throw new PaymentException("xac thuc loi");
            }
        }

        @Override
        protected void charge(Order pOrder, long pFee) {
            aCalls.add("charge");
        }

        @Override
        protected void issueReceipt(Order pOrder, long pFee) {
            aCalls.add("issueReceipt");
            super.issueReceipt(pOrder, pFee);
        }

        @Override
        protected void notifyCustomer(Order pOrder) {
            aCalls.add("notifyCustomer");
        }

        @Override
        protected boolean requiresAuthentication() {
            return aNeedsAuthentication;
        }
    }

    private static Order order(long pAmount) {
        return new Order("T01", "Khach Test", pAmount);
    }

    // =================== Nhóm 1: kiểm tra khung Template Method ===================

    @Test
    void testTemplateMethod_IsFinal() throws NoSuchMethodException {
        Method method = PaymentProcessor.class.getMethod("process", Order.class);
        assertTrue(Modifier.isFinal(method.getModifiers()));
    }

    @Test
    void testTemplate_StepsRunInFixedOrder() {
        RecordingProcessor processor = new RecordingProcessor(true, false);
        PaymentResult result = processor.process(order(1000));
        assertTrue(result.isSuccess());
        assertEquals(List.of("validateOrder", "calculateFee", "authenticate", "charge",
                "issueReceipt", "notifyCustomer"), processor.aCalls);
    }

    @Test
    void testTemplate_HookSkipsAuthentication() {
        RecordingProcessor processor = new RecordingProcessor(false, false);
        PaymentResult result = processor.process(order(1000));
        assertTrue(result.isSuccess());
        assertEquals(List.of("validateOrder", "calculateFee", "charge",
                "issueReceipt", "notifyCustomer"), processor.aCalls);
    }

    @Test
    void testTemplate_StopsWhenAuthenticationFails() {
        RecordingProcessor processor = new RecordingProcessor(true, true);
        Order order = order(1000);
        PaymentResult result = processor.process(order);
        assertFalse(result.isSuccess());
        assertEquals(List.of("validateOrder", "calculateFee", "authenticate"), processor.aCalls);
        assertFalse(order.isPaid());
    }

    @Test
    void testTemplate_InvalidOrderStopsAtFirstStep() {
        RecordingProcessor processor = new RecordingProcessor(true, false);
        PaymentResult result = processor.process(order(0));
        assertFalse(result.isSuccess());
        assertEquals(List.of("validateOrder"), processor.aCalls);
    }

    @Test
    void testTemplate_AlreadyPaidOrderIsRejected() {
        Order order = order(1000);
        order.markPaid();
        PaymentResult result = new RecordingProcessor(true, false).process(order);
        assertFalse(result.isSuccess());
    }

    // =================== Nhóm 2: kiểm tra từng lớp cụ thể ===================

    @Test
    void testCreditCard_Success() {
        Order order = order(1_000_000);
        PaymentResult result = new CreditCardPayment("4111", 5_000_000, "123456").process(order);
        assertTrue(result.isSuccess());
        assertEquals(20_000, result.getFee());
        assertTrue(order.isPaid());
    }

    @Test
    void testCreditCard_WrongOtp() {
        Order order = order(1_000_000);
        PaymentProcessor processor = new CreditCardPayment("4111", 5_000_000, "000000");
        PaymentResult result = processor.process(order);
        assertFalse(result.isSuccess());
        assertFalse(order.isPaid());
        assertFalse(processor.getLog().stream().anyMatch(line -> line.contains("Da tru")));
    }

    @Test
    void testCreditCard_OverLimit() {
        Order order = order(1_000_000);
        PaymentResult result = new CreditCardPayment("4111", 500_000, "123456").process(order);
        assertFalse(result.isSuccess());
        assertFalse(order.isPaid());
    }

    @Test
    void testEWallet_Success() {
        Order order = order(200_000);
        PaymentResult result = new EWalletPayment("0901", 500_000, "1234").process(order);
        assertTrue(result.isSuccess());
        assertEquals(2_000, result.getFee());
        assertTrue(order.isPaid());
    }

    @Test
    void testEWallet_MinimumFee() {
        PaymentResult result = new EWalletPayment("0901", 500_000, "1234").process(order(50_000));
        assertTrue(result.isSuccess());
        assertEquals(1_000, result.getFee());
    }

    @Test
    void testEWallet_WrongPin() {
        Order order = order(200_000);
        PaymentResult result = new EWalletPayment("0901", 500_000, "9999").process(order);
        assertFalse(result.isSuccess());
        assertFalse(order.isPaid());
    }

    @Test
    void testEWallet_InsufficientBalance() {
        Order order = order(200_000);
        PaymentResult result = new EWalletPayment("0901", 100_000, "1234").process(order);
        assertFalse(result.isSuccess());
        assertFalse(order.isPaid());
    }

    @Test
    void testEWallet_UsesOverriddenNotificationHook() {
        PaymentProcessor processor = new EWalletPayment("0901", 500_000, "1234");
        processor.process(order(200_000));
        assertTrue(processor.getLog().stream().anyMatch(line -> line.contains("tin nhan trong ung dung")));
    }

    @Test
    void testBankTransfer_SuccessWithoutAuthentication() {
        Order order = order(3_000_000);
        PaymentProcessor processor = new BankTransferPayment("0123456789");
        PaymentResult result = processor.process(order);
        assertTrue(result.isSuccess());
        assertEquals(5_000, result.getFee());
        assertTrue(order.isPaid());
        assertTrue(processor.getLog().stream().anyMatch(line -> line.contains("Bo qua xac thuc")));
    }

    @Test
    void testCreditCard_UsesDefaultNotificationHook() {
        PaymentProcessor processor = new CreditCardPayment("4111", 5_000_000, "123456");
        processor.process(order(1_000_000));
        assertTrue(processor.getLog().stream().anyMatch(line -> line.contains("Gui email xac nhan")));
    }

    @Test
    void testAllMethods_RejectInvalidOrder() {
        assertFalse(new CreditCardPayment("4111", 5_000_000, "123456").process(order(0)).isSuccess());
        assertFalse(new EWalletPayment("0901", 500_000, "1234").process(order(-5)).isSuccess());
        assertFalse(new BankTransferPayment("0123").process(order(0)).isSuccess());
    }
}
