/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.assignmentone;

import java.util.Scanner;

/**
 * Represents a user account with registration and login behavior.
 * @author OSCAR THE SOFTWARE G
 */
public class User {

    // FIELDS
    private String username;
    private String password;
    private String phoneNumber;
    private String first_name;
    private String last_name;
    private final Scanner scanner;
    private boolean account;

    // CONSTRUCTORS
    public User() {
        this(new Scanner(System.in));
    }

    /**
     * Only one Scanner should ever wrap System.in, because each Scanner
     * buffers input ahead and would steal lines meant for the other.
     * @param scanner the shared console Scanner
     */
    public User(Scanner scanner) {
        this.username = "";
        this.password = "";
        this.phoneNumber = "";
        this.first_name = "";
        this.last_name = "";
        this.account = false;
        this.scanner = scanner;
    }

    // VALIDATION HELPERS

    public boolean isValidUsername(String input) {
        return input != null && input.length() <= 5 && input.contains("_");
    }

    public boolean isValidPassword(String input) {
        String pattern = "^(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]).{8,}$";
        return input != null && input.matches(pattern);
    }

    public boolean isValidPhoneNumber(String input) {
        return isValidCellNumber(input);
    }

    /**
     * Shared cell number rule: a '+' country code followed by digits,
     * no more than 12 characters in total. Static so that other classes
     * (e.g. Message) can reuse it without creating a User.
     * @param input the cell number to check
     * @return true if the number is correctly formatted
     */
    public static boolean isValidCellNumber(String input) {
        String pattern = "^\\+\\d{1,11}$";
        return input != null && input.length() <= 12 && input.matches(pattern);
    }

    // REGISTRATION 

    public void register() {

        if (account) {
            System.out.println(username + " is already registered.");
            return;
        }

        System.out.println("Welcome to Ospoco Chatting System.");
        System.out.println("We have detected that you don't have an account yet, please register to continue.");

        // USERNAME
        String inputUsername;
        do {
            System.out.print("Please create your username (must contain '_' and be 5 characters or fewer): ");
            inputUsername = scanner.nextLine();
            if (!isValidUsername(inputUsername)) {
                System.out.println("Invalid username. It must contain an underscore and be no more than 5 characters.");
            }
        } while (!isValidUsername(inputUsername));
        this.username = inputUsername;
        System.out.println("Username successfully captured.");

        // PASSWORD
        String inputPassword;
        do {
            System.out.print("Please create your password (8+ chars, 1 uppercase, 1 number, 1 special char): ");
            inputPassword = scanner.nextLine();
            if (!isValidPassword(inputPassword)) {
                System.out.println("Invalid password. It must be at least 8 characters and include an uppercase letter, a number, and a special character.");
            }
        } while (!isValidPassword(inputPassword));
        this.password = inputPassword;
        System.out.println("Password successfully captured.");

        // FULL NAMES
        String first_name_value;
        do {
            System.out.print("Please enter your first name: ");
            first_name_value = scanner.nextLine().trim();
            if (first_name_value.isEmpty()) {
                System.out.println("First name cannot be empty.");
            }
        } while (first_name_value.isEmpty());
        this.first_name = first_name_value;

        String last_name_value;
        do {
            System.out.print("Please enter your last name: ");
            last_name_value = scanner.nextLine().trim();
            if (last_name_value.isEmpty()) {
                System.out.println("Last name cannot be empty.");
            }
        } while (last_name_value.isEmpty());
        this.last_name = last_name_value;

        // PHONE NUMBER
        String inputPhone;
        do {
            System.out.print("Please enter your phone number (with country code, max 12 characters, e.g. +27831234567): ");
            inputPhone = scanner.nextLine();
            if (!isValidPhoneNumber(inputPhone)) {
                System.out.println("Invalid phone number. It must start with a '+' country code and be no more than 12 characters.");
            }
        } while (!isValidPhoneNumber(inputPhone));
        this.phoneNumber = inputPhone;
        System.out.println("Phone number successfully captured.");

        // FINALIZE
        this.account = true;
        System.out.println("You have successfully registered with username " + username
                + ", password " + password + " and phone number " + phoneNumber);
    }

    // LOGIN 

    /**
     * Attempts to log in with the given credentials.
     * Returns true on success, false on failure — no console I/O involved,
     * so this can be called directly from unit tests.
     * @param inputUsername
     * @param inputPassword
     * @return 
     */
    public boolean login(String inputUsername, String inputPassword) {

        if (!account) {
            System.out.println("No account found. Please register first.");
            return false;
        }

        if (this.username.equals(inputUsername) && this.password.equals(inputPassword)) {
            System.out.println("Welcome " + first_name + " " + last_name + ", it is great to see you again.");
            return true;
        } else {
            System.out.println("Incorrect username or password.");
            return false;
        }
    }

    //  LOGIN
    /**
     * Console login with up to 3 attempts.
     * @return true if the user logged in successfully
     */
    public boolean loginInteractive() {

        if (!account) {
            System.out.println("No account found. Please register first.");
            return false;
        }

        System.out.println("Welcome back to Ospoco Chatting System.");

        int attempts = 0;
        final int MAX_ATTEMPTS = 3;
        boolean loggedIn = false;

        while (attempts < MAX_ATTEMPTS && !loggedIn) {

            System.out.print("Enter your username: ");
            String inputUsername = scanner.nextLine();

            System.out.print("Enter your password: ");
            String inputPassword = scanner.nextLine();

            loggedIn = login(inputUsername, inputPassword);

            if (!loggedIn) {
                attempts++;
                int remaining = MAX_ATTEMPTS - attempts;
                if (remaining > 0) {
                    System.out.println("You have " + remaining + " attempt(s) left.");
                } else {
                    System.out.println("Too many failed attempts. Account locked for this session.");
                }
            }
        }
        return loggedIn;
    }

    // GETTERS 
    public String get_username() { return username; }
    public String get_phone() { return phoneNumber; }
    public String get_first_name() { return first_name; }
    public String get_last_name() { return last_name; }
    public boolean getAccountState() { return account; }

    // SETTERS 
    public void setUsername(String username) { this.username = username; }
    public void setPassword(String password) { this.password = password; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public void setFirstName(String first_name) { this.first_name = first_name; }
    public void setLastName(String last_name) { this.last_name = last_name; }
    public void setAccountState(boolean account) { this.account = account; }

    public void close_scanner() { scanner.close(); }
}