package com.mycompany.assignmentone;

import java.util.Scanner;

/**
 *
 * @author OSCAR THE SOFTWARE G
 */
public class AssignmentOne {

    public static void main(String[] args) {

        try (Scanner menuScanner = new Scanner(System.in)) {
            User user = new User(menuScanner);
            boolean running = true;
            
            while (running) {
                System.out.println("\n--- Ospoco Chatting System ---");
                System.out.println("1. Register");
                System.out.println("2. Login");
                System.out.println("3. Exit");
                System.out.print("Choose an option: ");
                
                String choice = menuScanner.nextLine();
                
                switch (choice) {
                    case "1" -> user.register();
                    case "2" -> user.loginInteractive();
                    case "3" -> {
                        System.out.println("Goodbye!");
                        running = false;
                    }
                    default -> System.out.println("Invalid option, please choose 1, 2, or 3.");
                }
            }
        }
    }
}
