package com.mycompany.assignmentone;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.IOException;
import java.util.List;
import java.util.function.Predicate;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

/**
 * Swing window for the whole application: registration, login, QuickChat
 * and the stored messages features. Menus are buttons on the left, results
 * are shown in the text area on the right, and input is collected with
 * pop-up dialogs.
 * @author OSCAR THE SOFTWARE G
 */
public class ChatAppGUI extends JFrame {

    private static final String ACCOUNT_CARD = "account";
    private static final String CHAT_CARD = "chat";
    private static final String STORED_CARD = "stored";
    private static final int MAX_LOGIN_ATTEMPTS = 3;

    private final User user = new User();
    private final MessageManager manager = new MessageManager();

    private final JLabel titleLabel = new JLabel("Ospoco Chatting System", SwingConstants.CENTER);
    private final CardLayout cards = new CardLayout();
    private final JPanel menuPanel = new JPanel(cards);
    private final JTextArea output = new JTextArea(22, 62);

    private String sender = "";
    private boolean loginLocked;
    private int messageLimit;
    private int messagesEntered;

    public ChatAppGUI() {
        super("Ospoco Chatting System");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 18f));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        output.setEditable(false);
        output.setLineWrap(true);
        output.setWrapStyleWord(true);
        output.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        output.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        menuPanel.add(menu("Main Menu",
                button("Register", this::register),
                button("Login", this::login),
                button("Exit", this::dispose)), ACCOUNT_CARD);

        menuPanel.add(menu("QuickChat Menu",
                button("1) Send Messages", this::sendMessages),
                button("2) Show recently sent messages", () -> show("Coming Soon.")),
                button("3) Quit", this::quit),
                button("4) Stored Messages", this::openStoredMessages)), CHAT_CARD);

        menuPanel.add(menu("Stored Messages",
                button("1) Sender and recipient of all stored messages", this::showSendersAndRecipients),
                button("2) Longest stored message", this::showLongestMessage),
                button("3) Search by message ID", this::searchByMessageId),
                button("4) Search by recipient", this::searchByRecipient),
                button("5) Delete a message by hash", this::deleteByHash),
                button("6) Report of all stored messages", () -> show(manager.displayStoredReport())),
                button("7) Back to QuickChat menu", () -> cards.show(menuPanel, CHAT_CARD))), STORED_CARD);

        add(titleLabel, BorderLayout.NORTH);
        add(menuPanel, BorderLayout.WEST);
        add(new JScrollPane(output), BorderLayout.CENTER);

        pack();
        setLocationRelativeTo(null);
        show("Welcome to Ospoco Chatting System.\nPlease register, then log in to use QuickChat.");
    }

    // REGISTRATION

    private void register() {
        if (user.getAccountState()) {
            show(user.get_username() + " is already registered. Please log in.");
            return;
        }

        String username = askUntilValid("Create your username (must contain '_' and be 5 characters or fewer):",
                user::isValidUsername,
                "Invalid username. It must contain an underscore and be no more than 5 characters.", false);
        if (username == null) {
            show("Registration cancelled.");
            return;
        }

        String password = askUntilValid("Create your password (8+ chars, 1 uppercase, 1 number, 1 special char):",
                user::isValidPassword,
                "Invalid password. It must be at least 8 characters and include an uppercase letter, "
                + "a number, and a special character.", true);
        if (password == null) {
            show("Registration cancelled.");
            return;
        }

        String firstName = askUntilValid("Enter your first name:",
                name -> name != null && !name.isBlank(),
                "First name cannot be empty.", false);
        if (firstName == null) {
            show("Registration cancelled.");
            return;
        }

        String lastName = askUntilValid("Enter your last name:",
                name -> name != null && !name.isBlank(),
                "Last name cannot be empty.", false);
        if (lastName == null) {
            show("Registration cancelled.");
            return;
        }

        String phone = askUntilValid("Enter your phone number (with country code, max 12 characters, e.g. +27831234567):",
                user::isValidPhoneNumber,
                "Invalid phone number. It must start with a '+' country code and be no more than 12 characters.", false);
        if (phone == null) {
            show("Registration cancelled.");
            return;
        }

        user.setUsername(username);
        user.setPassword(password);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setPhoneNumber(phone);
        user.setAccountState(true);

        String result = "You have successfully registered with username " + username
                + ", password " + password + " and phone number " + phone + ".\nYou can now log in.";
        show(result);
        info(result);
    }

    // LOGIN

    private void login() {
        if (!user.getAccountState()) {
            error("No account found. Please register first.");
            return;
        }
        if (loginLocked) {
            error("Too many failed attempts. Account locked for this session.");
            return;
        }

        for (int attempt = 1; attempt <= MAX_LOGIN_ATTEMPTS; attempt++) {
            JTextField usernameField = new JTextField(15);
            JPasswordField passwordField = new JPasswordField(15);
            focusWhenShown(usernameField);

            JPanel form = new JPanel(new GridLayout(0, 1, 0, 4));
            form.add(new JLabel("Username:"));
            form.add(usernameField);
            form.add(new JLabel("Password:"));
            form.add(passwordField);

            int result = JOptionPane.showConfirmDialog(this, form, "Login",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (result != JOptionPane.OK_OPTION) {
                return;
            }

            if (user.login(usernameField.getText().trim(), new String(passwordField.getPassword()))) {
                startQuickChat();
                return;
            }

            int remaining = MAX_LOGIN_ATTEMPTS - attempt;
            if (remaining > 0) {
                error("Incorrect username or password.\nYou have " + remaining + " attempt(s) left.");
            }
        }

        loginLocked = true;
        error("Too many failed attempts. Account locked for this session.");
    }

    // QUICKCHAT

    private void startQuickChat() {
        sender = (user.get_first_name() + " " + user.get_last_name()).trim();
        setTitle("QuickChat");
        titleLabel.setText("Welcome to QuickChat.");
        cards.show(menuPanel, CHAT_CARD);

        StringBuilder text = new StringBuilder();
        text.append("Welcome ").append(sender).append(", it is great to see you again.\n\n");
        text.append("Welcome to QuickChat.\n");
        try {
            manager.loadStoredMessages();
        } catch (IOException | RuntimeException e) {
            text.append("\nCould not load stored messages: ").append(e.getMessage()).append("\n");
        }
        show(text.toString());

        messageLimit = askMessageLimit();
        if (messageLimit > 0) {
            text.append("\nYou can send ").append(messageLimit).append(" message(s). Choose an option on the left.");
        } else {
            text.append("\nYou will be asked how many messages to send when you choose Send Messages.");
        }
        show(text.toString());
    }

    /**
     * @return the number of messages chosen, or 0 if the user cancelled
     */
    private int askMessageLimit() {
        while (true) {
            String input = ask("How many messages would you like to send?");
            if (input == null) {
                return 0;
            }
            try {
                int limit = Integer.parseInt(input);
                if (limit > 0) {
                    return limit;
                }
            } catch (NumberFormatException e) {
                // fall through to the error message below
            }
            error("Please enter a whole number greater than 0.");
        }
    }

    private void sendMessages() {
        if (messageLimit == 0) {
            messageLimit = askMessageLimit();
            if (messageLimit == 0) {
                return;
            }
        }
        if (messagesEntered >= messageLimit) {
            show("You have already entered all " + messageLimit + " message(s).\n\n"
                    + "Total number of messages sent: " + manager.returnTotalMessages());
            return;
        }

        StringBuilder log = new StringBuilder();
        while (messagesEntered < messageLimit) {
            String heading = "Message " + (messagesEntered + 1) + " of " + messageLimit;

            String recipient = askUntilValid(heading
                    + "\nEnter the recipient's cell number (with international code, e.g. +27718693002):",
                    User::isValidCellNumber, Message.RECIPIENT_INVALID, false);
            if (recipient == null) {
                break;
            }

            String text = askMessageText(heading);
            if (text == null) {
                break;
            }

            Message message = new Message(messagesEntered, sender, recipient, text);
            String result = chooseSendOption(message, heading);
            if (result == null) {
                break;
            }

            log.append("=== ").append(heading).append(" ===\n").append(result).append("\n\n");
            messagesEntered++;
        }

        if (messagesEntered >= messageLimit) {
            String total = "Total number of messages sent: " + manager.returnTotalMessages();
            log.append(total);
            show(log.toString());
            info(total);
        } else {
            log.append("Message entry stopped. ").append(messagesEntered).append(" of ").append(messageLimit)
                    .append(" message(s) entered. Choose Send Messages to continue.");
            show(log.toString());
        }
    }

    private String askMessageText(String heading) {
        while (true) {
            String text = ask(heading + "\nEnter your message (max " + Message.MAX_MESSAGE_LENGTH + " characters):");
            if (text == null) {
                return null;
            }
            if (text.isEmpty()) {
                error("Message cannot be empty.");
                continue;
            }
            String result = Message.checkMessageLength(text);
            if (result.equals(Message.MESSAGE_READY)) {
                return text;
            }
            error(result + "\nPlease enter a message of less than " + Message.MAX_MESSAGE_LENGTH + " characters.");
        }
    }

    /**
     * @return a description of what happened to the message, or null if cancelled
     */
    private String chooseSendOption(Message message, String heading) {
        String summary = heading + "\n\n"
                + Message.RECIPIENT_CAPTURED + "\n"
                + Message.MESSAGE_READY + "\n"
                + message.getMessageIdNotice() + "\n"
                + "Message Hash: " + message.createMessageHash() + "\n\n"
                + "What would you like to do with this message?";
        String[] options = {"Send Message", "Disregard Message", "Store Message to send later"};

        while (true) {
            int choice = JOptionPane.showOptionDialog(this, summary, "QuickChat",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

            switch (choice) {
                case 0 -> {
                    String result = message.sentMessage(1) + "\n\n" + message.printMessageDetails();
                    manager.addMessage(message);
                    info(result);
                    return result;
                }
                case 1 -> {
                    // Only mark as disregarded after the user confirms with 0.
                    String input = ask(Message.MESSAGE_DISREGARD_PROMPT);
                    if ("0".equals(input)) {
                        message.sentMessage(2);
                        manager.addMessage(message);
                        info("Message deleted.");
                        return "Message deleted.";
                    }
                    info("Message was not deleted.");
                }
                case 2 -> {
                    String result = message.sentMessage(3);
                    try {
                        manager.storeMessage(message);
                    } catch (IOException | RuntimeException e) {
                        result += "\nCould not save message to file: " + e.getMessage();
                    }
                    info(result);
                    return result + "\n\n" + message.printMessageDetails();
                }
                default -> {
                    return null;
                }
            }
        }
    }

    private void quit() {
        JOptionPane.showMessageDialog(this, "Goodbye!", "QuickChat", JOptionPane.INFORMATION_MESSAGE);
        dispose();
    }

    // STORED MESSAGES

    private void openStoredMessages() {
        cards.show(menuPanel, STORED_CARD);
        show("Stored Messages\n\nChoose an option on the left.");
    }

    private void showSendersAndRecipients() {
        show(manager.displaySendersAndRecipients());
    }

    private void showLongestMessage() {
        String longest = manager.getLongestStoredMessage();
        show(longest == null ? "No stored messages found." : "Longest stored message:\n" + longest);
    }

    private void searchByMessageId() {
        String id = ask("Enter the message ID:");
        if (id == null) {
            return;
        }
        if (id.isBlank()) {
            error("Message ID cannot be empty.");
            return;
        }
        show(manager.searchByMessageId(id));
    }

    private void searchByRecipient() {
        String recipient = ask("Enter the recipient's cell number:");
        if (recipient == null) {
            return;
        }
        if (recipient.isBlank()) {
            error("Recipient cannot be empty.");
            return;
        }
        List<String> results = manager.searchByRecipient(recipient);
        if (results.isEmpty()) {
            show("No messages found for " + recipient + ".");
            return;
        }
        StringBuilder text = new StringBuilder("Messages for " + recipient + ":\n\n");
        for (String result : results) {
            text.append("- ").append(result).append("\n");
        }
        show(text.toString());
    }

    private void deleteByHash() {
        String hash = ask("Enter the message hash of the stored message to delete:");
        if (hash == null) {
            return;
        }
        if (hash.isBlank()) {
            error("Message hash cannot be empty.");
            return;
        }
        try {
            String result = manager.deleteByHash(hash);
            show(result);
            info(result);
        } catch (IOException e) {
            error("Message deleted, but the file could not be updated: " + e.getMessage());
        }
    }

    // DIALOG AND LAYOUT HELPERS

    private void show(String text) {
        output.setText(text);
        output.setCaretPosition(0);
    }

    private void info(String message) {
        JOptionPane.showMessageDialog(this, message, getTitle(), JOptionPane.INFORMATION_MESSAGE);
    }

    private void error(String message) {
        JOptionPane.showMessageDialog(this, message, getTitle(), JOptionPane.ERROR_MESSAGE);
    }

    /**
     * @return the trimmed input, or null if the user pressed Cancel
     */
    private String ask(String prompt) {
        String input = JOptionPane.showInputDialog(this, prompt, getTitle(), JOptionPane.QUESTION_MESSAGE);
        return input == null ? null : input.trim();
    }

    private String askPassword(String prompt) {
        JPasswordField field = new JPasswordField(20);
        focusWhenShown(field);
        int result = JOptionPane.showConfirmDialog(this, new Object[]{prompt, field}, getTitle(),
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
        return result == JOptionPane.OK_OPTION ? new String(field.getPassword()) : null;
    }

    /**
     * Keeps asking until the input passes the check or the user cancels.
     * @return the valid input, or null if the user pressed Cancel
     */
    private String askUntilValid(String prompt, Predicate<String> isValid, String errorMessage, boolean secret) {
        while (true) {
            String input = secret ? askPassword(prompt) : ask(prompt);
            if (input == null || isValid.test(input)) {
                return input;
            }
            error(errorMessage);
        }
    }

    /**
     * JOptionPane focuses its OK button by default; this moves focus into the
     * text field so the user can start typing straight away.
     */
    private static void focusWhenShown(JTextField field) {
        field.addAncestorListener(new AncestorListener() {
            @Override
            public void ancestorAdded(AncestorEvent event) {
                field.requestFocusInWindow();
                field.removeAncestorListener(this);
            }

            @Override
            public void ancestorRemoved(AncestorEvent event) { }

            @Override
            public void ancestorMoved(AncestorEvent event) { }
        });
    }

    private static JButton button(String text, Runnable action) {
        JButton button = new JButton(text);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.addActionListener(e -> action.run());
        return button;
    }

    private static JPanel menu(String heading, JButton... buttons) {
        JPanel grid = new JPanel(new GridLayout(0, 1, 0, 6));
        for (JButton button : buttons) {
            grid.add(button);
        }
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(0, 10, 10, 0),
                BorderFactory.createTitledBorder(heading)));
        wrapper.add(grid, BorderLayout.NORTH);
        return wrapper;
    }
}
