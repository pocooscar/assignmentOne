package com.mycompany.assignmentone;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Represents a single QuickChat message: its ID, number, recipient, text
 * and hash, plus the checks and actions that apply to one message.
 * @author OSCAR THE SOFTWARE G
 */
public class Message {

    public enum Status { PENDING, SENT, STORED, DISREGARDED }

    public static final int MAX_MESSAGE_LENGTH = 250;
    public static final int MESSAGE_ID_LENGTH = 10;

    public static final String MESSAGE_READY = "Message ready to send.";
    public static final String RECIPIENT_CAPTURED = "Cell phone number successfully captured.";
    public static final String RECIPIENT_INVALID = "Cell phone number is incorrectly formatted or does not contain an "
            + "international code. Please correct the number and try again.";
    public static final String MESSAGE_SENT = "Message successfully sent.";
    public static final String MESSAGE_DISREGARD_PROMPT = "Press 0 to delete the message.";
    public static final String MESSAGE_STORED = "Message successfully stored.";

    private static final SecureRandom RANDOM = new SecureRandom();

    // FIELDS
    private final String messageId;
    private final int messageNumber;
    private final String sender;
    private final String recipient;
    private final String messageText;
    private Status status;

    // CONSTRUCTORS

    /**
     * Creates a message with a randomly generated ten-digit message ID.
     * @param messageNumber the number of this message (starts at 0)
     * @param sender who is sending the message
     * @param recipient the recipient's cell number
     * @param messageText the message content
     */
    public Message(int messageNumber, String sender, String recipient, String messageText) {
        this(generateMessageId(), messageNumber, sender, recipient, messageText);
    }

    /**
     * Creates a message with a known message ID (used when loading stored
     * messages and in unit tests).
     * @param messageId the ten-digit message ID
     * @param messageNumber the number of this message (starts at 0)
     * @param sender who is sending the message
     * @param recipient the recipient's cell number
     * @param messageText the message content
     */
    public Message(String messageId, int messageNumber, String sender, String recipient, String messageText) {
        this.messageId = messageId;
        this.messageNumber = messageNumber;
        this.sender = sender;
        this.recipient = recipient;
        this.messageText = messageText;
        this.status = Status.PENDING;
    }

    // MESSAGE ID

    /**
     * Generates a random ten-digit number (leading zeros allowed).
     * @return the new message ID
     */
    public static String generateMessageId() {
        StringBuilder id = new StringBuilder();
        for (int i = 0; i < MESSAGE_ID_LENGTH; i++) {
            id.append(RANDOM.nextInt(10));
        }
        return id.toString();
    }

    /**
     * @return true if the message ID is no more than ten characters
     */
    public boolean checkMessageID() {
        return messageId != null && !messageId.isEmpty() && messageId.length() <= MESSAGE_ID_LENGTH;
    }

    public String getMessageIdNotice() {
        return "Message ID generated: " + messageId;
    }

    // RECIPIENT

    public String checkRecipientCell() {
        return checkRecipientCell(recipient);
    }

    /**
     * Checks a cell number using the same rule as Part 1 registration.
     * @param cellNumber the number to check
     * @return the success or failure message
     */
    public static String checkRecipientCell(String cellNumber) {
        return User.isValidCellNumber(cellNumber) ? RECIPIENT_CAPTURED : RECIPIENT_INVALID;
    }

    // MESSAGE LENGTH

    public String checkMessageLength() {
        return checkMessageLength(messageText);
    }

    /**
     * @param text the message text to check
     * @return "Message ready to send." or how many characters too long it is
     */
    public static String checkMessageLength(String text) {
        int length = text == null ? 0 : text.length();
        if (length <= MAX_MESSAGE_LENGTH) {
            return MESSAGE_READY;
        }
        return "Message exceeds " + MAX_MESSAGE_LENGTH + " characters by " + (length - MAX_MESSAGE_LENGTH)
                + "; please reduce the size.";
    }

    // MESSAGE HASH

    /**
     * Builds the hash: first two digits of the ID, the message number and
     * the first and last words of the message, e.g. 00:0:HITONIGHT.
     * @return the message hash in capital letters
     */
    public String createMessageHash() {
        List<String> words = new ArrayList<>();
        for (String word : messageText.trim().split("\\s+")) {
            String cleaned = word.replaceAll("[^A-Za-z0-9]", "");
            if (!cleaned.isEmpty()) {
                words.add(cleaned);
            }
        }
        String firstWord = words.isEmpty() ? "" : words.get(0);
        String lastWord = words.isEmpty() ? "" : words.get(words.size() - 1);

        String idPrefix = messageId.length() >= 2 ? messageId.substring(0, 2) : messageId;
        return (idPrefix + ":" + messageNumber + ":" + firstWord + lastWord).toUpperCase();
    }

    // SEND / DISREGARD / STORE

    /**
     * Applies the user's choice for this message.
     * @param option 1 = Send, 2 = Disregard, 3 = Store to send later
     * @return the confirmation message for that choice
     */
    public String sentMessage(int option) {
        switch (option) {
            case 1 -> {
                status = Status.SENT;
                return MESSAGE_SENT;
            }
            case 2 -> {
                status = Status.DISREGARDED;
                return MESSAGE_DISREGARD_PROMPT;
            }
            case 3 -> {
                status = Status.STORED;
                return MESSAGE_STORED;
            }
            default -> {
                return "Invalid option. Please choose 1, 2 or 3.";
            }
        }
    }

    /**
     * @return the full message details in the order: ID, Hash, Recipient, Message
     */
    public String printMessageDetails() {
        return "Message ID: " + messageId
                + "\nMessage Hash: " + createMessageHash()
                + "\nRecipient: " + recipient
                + "\nMessage: " + messageText;
    }

    // JSON STORAGE

    /**
     * Appends this message to a JSON array file, creating the file if needed.
     * @param file the JSON file to store the message in
     * @throws IOException if the file cannot be read or written
     */
    public void storeMessage(Path file) throws IOException {
        JSONArray messages = Files.exists(file)
                ? new JSONArray(Files.readString(file))
                : new JSONArray();
        messages.put(toJson());
        Files.writeString(file, messages.toString(2));
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("messageId", messageId);
        json.put("messageNumber", messageNumber);
        json.put("messageHash", createMessageHash());
        json.put("sender", sender);
        json.put("recipient", recipient);
        json.put("message", messageText);
        return json;
    }

    /**
     * Rebuilds a stored message from its JSON form.
     * @param json a message object previously written by toJson()
     * @return the message, marked as stored
     */
    public static Message fromJson(JSONObject json) {
        Message message = new Message(
                json.getString("messageId"),
                json.getInt("messageNumber"),
                json.optString("sender", ""),
                json.getString("recipient"),
                json.getString("message"));
        message.status = Status.STORED;
        return message;
    }

    // GETTERS
    public String getMessageId() { return messageId; }
    public int getMessageNumber() { return messageNumber; }
    public String getSender() { return sender; }
    public String getRecipient() { return recipient; }
    public String getMessageText() { return messageText; }
    public Status getStatus() { return status; }
}
