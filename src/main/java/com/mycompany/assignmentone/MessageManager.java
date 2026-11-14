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
    private boolean storedMessagesLoaded;

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
        if (message == null) {
            throw new IllegalArgumentException("Message cannot be null.");
        }
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
     * Reads the JSON storage file into the stored messages array once.
     * Does nothing if the file does not exist yet, or if it was already loaded.
     * @throws IOException if the file cannot be read
     */
    public void loadStoredMessages() throws IOException {
        if (storedMessagesLoaded) {
            return;
        }
        storedMessagesLoaded = true;
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

    /**
     * Adds a stored message to the in-memory arrays and rewrites the JSON file
     * so the file always matches the stored messages array.
     * @param message a message whose status is STORED
     * @throws IOException if the file cannot be written
     */
    public void storeMessage(Message message) throws IOException {
        if (message == null || message.getStatus() != Message.Status.STORED) {
            throw new IllegalArgumentException("Only stored messages can be saved to the JSON file.");
        }
        addMessage(message);
        saveStoredMessages();
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
            String text = message.getMessageText();
            if (text == null) {
                continue;
            }
            if (longest == null || text.length() > longest.length()) {
                longest = text;
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
        if (messageId == null || messageId.isBlank()) {
            return null;
        }
        String target = messageId.trim();
        for (Message message : allMessages()) {
            if (target.equals(message.getMessageId())) {
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
        if (recipient == null || recipient.isBlank()) {
            return results;
        }
        String target = recipient.trim();
        for (Message message : sentMessages) {
            if (target.equals(message.getRecipient())) {
                results.add(message.getMessageText());
            }
        }
        for (Message message : storedMessages) {
            if (target.equals(message.getRecipient())) {
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
        if (messageHash == null || messageHash.isBlank()) {
            return "No stored message found with hash: " + messageHash;
        }
        String target = messageHash.trim();
        for (int i = 0; i < storedMessages.size(); i++) {
            Message message = storedMessages.get(i);
            String hash = message.createMessageHash();
            if (hash.equalsIgnoreCase(target)) {
                String id = message.getMessageId();
                storedMessages.remove(i);
                removeMatchingHashAndId(hash, id);
                saveStoredMessages();
                return "Message: \"" + message.getMessageText() + "\" successfully deleted.";
            }
        }
        return "No stored message found with hash: " + messageHash;
    }

    /**
     * Removes the hash/ID pair that belongs to the deleted stored message.
     * Matching by both values avoids wiping a different message's hash when
     * two messages happen to share a hash or ID.
     */
    private void removeMatchingHashAndId(String hash, String id) {
        for (int i = 0; i < messageHashes.size(); i++) {
            if (messageHashes.get(i).equalsIgnoreCase(hash)
                    && i < messageIds.size()
                    && messageIds.get(i).equals(id)) {
                messageHashes.remove(i);
                messageIds.remove(i);
                return;
            }
        }
        // Fall back to first matching values if the parallel arrays drifted.
        messageHashes.remove(hash);
        messageIds.remove(id);
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
    public boolean isStoredMessagesLoaded() { return storedMessagesLoaded; }

    private static List<String> textsOf(List<Message> messages) {
        List<String> texts = new ArrayList<>();
        for (Message message : messages) {
            texts.add(message.getMessageText());
        }
        return texts;
    }
}
