package payment;

/**
 * Chương trình minh họa mẫu Template Method: cùng một khung quy trình
 * {@link PaymentProcessor#process(Order)} chạy với các phương thức thanh toán khác nhau.
 */
public class Main {

    public static void main(String[] args) {
        run("TRUONG HOP 1: Thanh toan the tin dung (thanh cong)",
                new CreditCardPayment("4111-1111-1111-1111", 5_000_000, "123456"),
                new Order("DH001", "Nguyen Van A", 1_000_000));

        run("TRUONG HOP 2: Thanh toan vi dien tu (thanh cong)",
                new EWalletPayment("0901234567", 500_000, "1234"),
                new Order("DH002", "Tran Thi B", 200_000));

        run("TRUONG HOP 3: Chuyen khoan ngan hang (thanh cong, khong can xac thuc)",
                new BankTransferPayment("0123456789"),
                new Order("DH003", "Le Van C", 3_000_000));

        run("TRUONG HOP 4: Vi dien tu nhap sai PIN (that bai, khong tru tien)",
                new EWalletPayment("0901234567", 500_000, "9999"),
                new Order("DH004", "Tran Thi B", 200_000));

        run("TRUONG HOP 5: The tin dung vuot han muc (that bai)",
                new CreditCardPayment("4111-1111-1111-1111", 500_000, "123456"),
                new Order("DH005", "Nguyen Van A", 1_000_000));
    }

    private static void run(String pTitle, PaymentProcessor pProcessor, Order pOrder) {
        System.out.println();
        System.out.println("##### " + pTitle);
        PaymentResult result = pProcessor.process(pOrder);
        System.out.println(">>> Ket qua: " + result + " | Phi: " + result.getFee()
                + " | Don da thanh toan: " + pOrder.isPaid());
    }
}
