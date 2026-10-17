import java.util.Scanner;
import utils.InputHandler;
import utils.Validator;

public class TestValidate {
    public static void main(String[] args) {
        String[] phones = {"0901234567", "09012", "1901234567", "abc", ""};
        for (String p : phones) {
            System.out.println("phone [" + p + "] -> " + Validator.isValidPhone(p));
        }
        String[] dates = {"26/10/2026", "31/02/2026", "29/02/2028", "29/02/2027", "2026-10-26", ""};
        for (String d : dates) {
            System.out.println("date [" + d + "] -> " + Validator.isValidDate(d));
        }
        String[] ints = {"3", "0", "-3", "abc", "99999999999", " 5 ", ""};
        for (String n : ints) {
            System.out.println("int [" + n + "] -> " + Validator.isPositiveInt(n));
        }

        Scanner sc = new Scanner(System.in);
        System.out.println("Table = " + InputHandler.readTableNumber(sc, "Table: "));
        System.out.println("Date = " + InputHandler.readDate(sc, "Date: "));
    }
}