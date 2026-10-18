package com.mycompany.assignmentone;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;

/**
 * Keeps track of every message in the current session, split into the
 * sent, disregarded and stored arrays, plus arrays of all message hashes
 * and message IDs. Stored messages are persisted to a JSON file.
 * @author OSCAR THE SOFTWARE G
 */
public class MessageManager {

    public static final Path DEFAULT_STORAGE_FILE = Path.of("storedMessages.json");
    private static final String SEPARATOR = "----------------------------------------";

    // ARRAYS
    private final ArrayList<Message> sentMessages = new ArrayList<>();
    private final ArrayList<Message> disregardedMessages = new ArrayList<>();
    private final ArrayList<Message> storedMessages = new ArrayList<>();
    private final ArrayList<String> messageHashes = new ArrayList<>();
    private final ArrayList<String> messageIds = new ArrayList<>();

    private final Path storageFile;

    public MessageManager() {
        this(DEFAULT_STORAGE_FILE);
    }

    public MessageManager(Path storageFile) {
        this.storageFile = storageFile;
    }

    // POPULATING THE ARRAYS

    /**
     * Adds a message to the array that matches its status (sent,
     * disregarded or stored) and records its hash and ID.
     * @param message a message whose status has been set by sentMessage()
     */
    public void addMessage(Message message) {
        switch (message.getStatus()) {
            case SENT -> sentMessages.add(message);
            case DISREGARDED -> disregardedMessages.add(message);
            case STORED -> storedMessages.add(message);
            default -> throw new IllegalArgumentException("Message has not been sent, stored or disregarded yet.");
        }
        messageHashes.add(message.createMessageHash());
        messageIds.add(message.getMessageId());
    }

    /**
     * Reads the JSON storage file into the stored messages array.
     * Does nothing if the file does not exist yet.
     * @throws IOException if the file cannot be read
     */
    public void loadStoredMessages() throws IOException {
        if (!Files.exists(storageFile)) {
            return;
        }
        JSONArray json = new JSONArray(Files.readString(storageFile));
        for (int i = 0; i < json.length(); i++) {
            addMessage(Message.fromJson(json.getJSONObject(i)));
        }
    }

    /**
     * Rewrites the JSON storage file so it matches the stored messages array.
     * @throws IOException if the file cannot be written
     */
    public void saveStoredMessages() throws IOException {
        JSONArray json = new JSONArray();
        for (Message message : storedMessages) {
            json.put(message.toJson());
        }
        Files.writeString(storageFile, json.toString(2));
    }

    // PART 2: SENT MESSAGES

    /**
     * @return the full details of every message sent while the program is running
     */
    public String printMessages() {
        if (sentMessages.isEmpty()) {
            return "No messages have been sent yet.";
        }
        StringBuilder output = new StringBuilder();
        for (Message message : sentMessages) {
            output.append(message.printMessageDetails()).append("\n").append(SEPARATOR).append("\n");
        }
        return output.toString();
    }

    public int returnTotalMessages() {
        return sentMessages.size();
    }

    // PART 3: STORED MESSAGE FEATURES

    /**
     * @return one line per stored message showing its sender and recipient
     */
    public String displaySendersAndRecipients() {
        if (storedMessages.isEmpty()) {
            return "No stored messages found.";
        }
        StringBuilder output = new StringBuilder();
        for (Message message : storedMessages) {
            output.append("Sender: ").append(message.getSender())
                    .append(" | Recipient: ").append(message.getRecipient()).append("\n");
        }
        return output.toString();
    }

    /**
     * @return the text of the longest stored message, or null if there are none
     */
    public String getLongestStoredMessage() {
        String longest = null;
        for (Message message : storedMessages) {
            if (longest == null || message.getMessageText().length() > longest.length()) {
                longest = message.getMessageText();
            }
        }
        return longest;
    }

    /**
     * Looks for a message ID in the sent, stored and disregarded arrays.
     * @param messageId the ID to search for
     * @return the matching message, or null if none was found
     */
    public Message findMessageById(String messageId) {
        for (Message message : allMessages()) {
            if (message.getMessageId().equals(messageId)) {
                return message;
            }
        }
        return null;
    }

    /**
     * @param messageId the ID to search for
     * @return the recipient and message for that ID, or a not-found message
     */
    public String searchByMessageId(String messageId) {
        Message message = findMessageById(messageId);
        if (message == null) {
            return "No message found with ID: " + messageId;
        }
        return "Recipient: " + message.getRecipient() + "\nMessage: " + message.getMessageText();
    }

    /**
     * @param recipient the recipient's cell number
     * @return the text of every sent or stored message for that recipient
     */
    public List<String> searchByRecipient(String recipient) {
        List<String> results = new ArrayList<>();
        for (Message message : sentMessages) {
            if (message.getRecipient().equals(recipient)) {
                results.add(message.getMessageText());
            }
        }
        for (Message message : storedMessages) {
            if (message.getRecipient().equals(recipient)) {
                results.add(message.getMessageText());
            }
        }
        return results;
    }

    /**
     * Deletes a stored message by its hash and updates the JSON file.
     * @param messageHash the hash of the message to delete
     * @return a confirmation, or a not-found message
     * @throws IOException if the storage file cannot be updated
     */
    public String deleteByHash(String messageHash) throws IOException {
        for (int i = 0; i < storedMessages.size(); i++) {
            Message message = storedMessages.get(i);
            if (message.createMessageHash().equalsIgnoreCase(messageHash)) {
                storedMessages.remove(i);
                messageHashes.remove(message.createMessageHash());
                messageIds.remove(message.getMessageId());
                saveStoredMessages();
                return "Message: \"" + message.getMessageText() + "\" successfully deleted.";
            }
        }
        return "No stored message found with hash: " + messageHash;
    }

    /**
     * @return a report of all sent messages (ID, hash, recipient and message)
     */
    public String displayReport() {
        return buildReport("Sent Messages Report", sentMessages);
    }

    /**
     * @return a report of all stored messages (ID, hash, recipient and message)
     */
    public String displayStoredReport() {
        return buildReport("Stored Messages Report", storedMessages);
    }

    private String buildReport(String title, List<Message> messages) {
        StringBuilder report = new StringBuilder();
        report.append("===== ").append(title).append(" =====\n");
        if (messages.isEmpty()) {
            report.append("No messages to display.\n");
            return report.toString();
        }
        for (Message message : messages) {
            report.append(message.printMessageDetails()).append("\n").append(SEPARATOR).append("\n");
        }
        return report.toString();
    }

    private List<Message> allMessages() {
        List<Message> all = new ArrayList<>(sentMessages);
        all.addAll(storedMessages);
        all.addAll(disregardedMessages);
        return all;
    }

    // ARRAY GETTERS (copies, so callers cannot change the arrays directly)

    public List<String> getSentMessageTexts() { return textsOf(sentMessages); }
    public List<String> getDisregardedMessageTexts() { return textsOf(disregardedMessages); }
    public List<String> getStoredMessageTexts() { return textsOf(storedMessages); }
    public List<String> getMessageHashes() { return new ArrayList<>(messageHashes); }
    public List<String> getMessageIds() { return new ArrayList<>(messageIds); }
    public Path getStorageFile() { return storageFile; }

    private static List<String> textsOf(List<Message> messages) {
        List<String> texts = new ArrayList<>();
        for (Message message : messages) {
            texts.add(message.getMessageText());
        }
        return texts;
    }
}
