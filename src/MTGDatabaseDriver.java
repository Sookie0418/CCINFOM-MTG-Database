import java.util.Scanner;

public class MTGDatabaseDriver {
    private static MTGDatabaseController controller;
    private static Scanner scanner;

    public static void main(String[] args) {
        scanner = new Scanner(System.in);
        controller = new MTGDatabaseController();

        System.out.println("==========================================");
        System.out.println("    MTG Commander Database System");
        System.out.println("==========================================");
    }
}