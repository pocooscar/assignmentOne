package com.mycompany.assignmentone;

import java.io.IOException;
import java.util.List;
import java.util.Scanner;

/**
 * Console interface for QuickChat. Only reachable after a successful login.
 * @author OSCAR THE SOFTWARE G
 */
public class QuickChat {

    private final Scanner scanner;
    private final String sender;
    private final MessageManager manager;
    private int messageLimit;
    private int messagesEntered;

    public QuickChat(Scanner scanner, String sender, MessageManager manager) {
        this.scanner = scanner;
        this.sender = sender;
        this.manager = manager;
    }

    /**
     * Runs QuickChat until the user chooses Quit.
     */
    public void run() {
        System.out.println("\nWelcome to QuickChat.");

        try {
            manager.loadStoredMessages();
        } catch (IOException | RuntimeException e) {
            System.out.println("Could not load stored messages: " + e.getMessage());
        }

        messageLimit = askMessageLimit();

        boolean running = true;
        while (running) {
            System.out.println("\n--- QuickChat Menu ---");
            System.out.println("1) Send Messages");
            System.out.println("2) Show recently sent messages");
            System.out.println("3) Quit");
            System.out.println("4) Stored Messages");
            System.out.print("Choose an option: ");

            switch (scanner.nextLine().trim()) {
                case "1" -> sendMessages();
                case "2" -> System.out.println("Coming Soon.");
                case "3" -> {
                    System.out.println("Goodbye!");
                    running = false;
                }
                case "4" -> storedMessagesMenu();
                default -> System.out.println("Invalid option, please choose 1, 2, 3 or 4.");
            }
        }
    }

    private int askMessageLimit() {
        while (true) {
            System.out.print("How many messages would you like to send? ");
            String input = scanner.nextLine().trim();
            try {
                int limit = Integer.parseInt(input);
                if (limit > 0) {
                    return limit;
                }
            } catch (NumberFormatException e) {
                // fall through to the error message below
            }
            System.out.println("Please enter a whole number greater than 0.");
        }
    }

    // SEND MESSAGES

    private void sendMessages() {
        if (messagesEntered >= messageLimit) {
            System.out.println("You have already entered all " + messageLimit + " message(s).");
            return;
        }

        while (messagesEntered < messageLimit) {
            System.out.println("\nMessage " + (messagesEntered + 1) + " of " + messageLimit);
            String recipient = promptRecipient();
            String text = promptMessageText();

            Message message = new Message(messagesEntered, sender, recipient, text);
            System.out.println(message.getMessageIdNotice());
            System.out.println("Message Hash: " + message.createMessageHash());

            chooseSendOption(message);
            messagesEntered++;
        }

        System.out.println("\nTotal number of messages sent: " + manager.returnTotalMessages());
    }

    private String promptRecipient() {
        while (true) {
            System.out.print("Enter the recipient's cell number (with international code, e.g. +27718693002): ");
            String recipient = scanner.nextLine().trim();
            System.out.println(Message.checkRecipientCell(recipient));
            if (User.isValidCellNumber(recipient)) {
                return recipient;
            }
        }
    }

    private String promptMessageText() {
        while (true) {
            System.out.print("Enter your message (max " + Message.MAX_MESSAGE_LENGTH + " characters): ");
            String text = scanner.nextLine().trim();
            if (text.isEmpty()) {
                System.out.println("Message cannot be empty.");
                continue;
            }
            String result = Message.checkMessageLength(text);
            System.out.println(result);
            if (result.equals(Message.MESSAGE_READY)) {
                return text;
            }
            System.out.println("Please enter a message of less than " + Message.MAX_MESSAGE_LENGTH + " characters.");
        }
    }

    private void chooseSendOption(Message message) {
        while (true) {
            System.out.println("\nWhat would you like to do with this message?");
            System.out.println("1) Send Message");
            System.out.println("2) Disregard Message");
            System.out.println("3) Store Message to send later");
            System.out.print("Choose an option: ");

            switch (scanner.nextLine().trim()) {
                case "1" -> {
                    System.out.println(message.sentMessage(1));
                    manager.addMessage(message);
                    System.out.println(message.printMessageDetails());
                    return;
                }
                case "2" -> {
                    System.out.println(message.sentMessage(2));
                    if (scanner.nextLine().trim().equals("0")) {
                        manager.addMessage(message);
                        System.out.println("Message deleted.");
                        return;
                    }
                    System.out.println("Message was not deleted.");
                }
                case "3" -> {
                    System.out.println(message.sentMessage(3));
                    manager.addMessage(message);
                    try {
                        message.storeMessage(manager.getStorageFile());
                    } catch (IOException | RuntimeException e) {
                        System.out.println("Could not save message to file: " + e.getMessage());
                    }
                    return;
                }
                default -> System.out.println("Invalid option, please choose 1, 2 or 3.");
            }
        }
    }

    // STORED MESSAGES (PART 3)

    private void storedMessagesMenu() {
        boolean inMenu = true;
        while (inMenu) {
            System.out.println("\n--- Stored Messages ---");
            System.out.println("1) Display sender and recipient of all stored messages");
            System.out.println("2) Display the longest stored message");
            System.out.println("3) Search for a message by message ID");
            System.out.println("4) Search for all messages to a recipient");
            System.out.println("5) Delete a message using its message hash");
            System.out.println("6) Display a report of all stored messages");
            System.out.println("7) Back to main menu");
            System.out.print("Choose an option: ");

            switch (scanner.nextLine().trim()) {
                case "1" -> System.out.println(manager.displaySendersAndRecipients());
                case "2" -> {
                    String longest = manager.getLongestStoredMessage();
                    System.out.println(longest == null ? "No stored messages found." : "Longest stored message: " + longest);
                }
                case "3" -> {
                    System.out.print("Enter the message ID: ");
                    System.out.println(manager.searchByMessageId(scanner.nextLine().trim()));
                }
                case "4" -> {
                    System.out.print("Enter the recipient's cell number: ");
                    String recipient = scanner.nextLine().trim();
                    List<String> results = manager.searchByRecipient(recipient);
                    if (results.isEmpty()) {
                        System.out.println("No messages found for " + recipient + ".");
                    } else {
                        results.forEach(text -> System.out.println("- " + text));
                    }
                }
                case "5" -> {
                    System.out.print("Enter the message hash: ");
                    try {
                        System.out.println(manager.deleteByHash(scanner.nextLine().trim()));
                    } catch (IOException e) {
                        System.out.println("Message deleted, but the file could not be updated: " + e.getMessage());
                    }
                }
                case "6" -> System.out.println(manager.displayStoredReport());
                case "7" -> inMenu = false;
                default -> System.out.println("Invalid option, please choose 1 to 7.");
            }
        }
    }
}
