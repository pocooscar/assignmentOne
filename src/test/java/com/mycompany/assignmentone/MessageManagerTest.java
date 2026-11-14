package com.mycompany.assignmentone;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Part 3 unit tests for the MessageManager arrays and stored-message features.
 */
public class MessageManagerTest {

    private static final String SENDER = "Developer";

    private static final String TEXT_1 = "Did you get the cake?";
    private static final String TEXT_2 = "Where are you? You are late! I have asked you to be on time.";
    private static final String TEXT_3 = "Yohoooo, I am at your gate.";
    private static final String TEXT_4 = "It is dinner time!";
    private static final String TEXT_5 = "Ok, I am leaving without you.";

    @TempDir
    Path tempDir;

    private MessageManager manager;

    @BeforeEach
    public void populateArrays() {
        manager = new MessageManager(tempDir.resolve("storedMessages.json"));
        add("4512345678", 0, "+27834557896", TEXT_1, 1); // Sent
        add("7812345678", 1, "+27838884567", TEXT_2, 3); // Stored
        add("3312345678", 2, "+27834484567", TEXT_3, 2); // Disregard
        add("0838884567", 3, "0838884567", TEXT_4, 1);   // Sent
        add("9912345678", 4, "+27838884567", TEXT_5, 3); // Stored
    }

    private void add(String id, int number, String recipient, String text, int option) {
        Message message = new Message(id, number, SENDER, recipient, text);
        message.sentMessage(option);
        manager.addMessage(message);
    }

    @Test
    public void testSentMessagesArrayCorrectlyPopulated() {
        assertEquals(List.of("Did you get the cake?", "It is dinner time!"), manager.getSentMessageTexts());
    }

    @Test
    public void testOtherArraysCorrectlyPopulated() {
        assertEquals(List.of(TEXT_2, TEXT_5), manager.getStoredMessageTexts());
        assertEquals(List.of(TEXT_3), manager.getDisregardedMessageTexts());
        assertEquals(List.of("45:0:DIDCAKE", "78:1:WHERETIME", "33:2:YOHOOOOGATE", "08:3:ITTIME", "99:4:OKYOU"),
                manager.getMessageHashes());
        assertEquals(List.of("4512345678", "7812345678", "3312345678", "0838884567", "9912345678"),
                manager.getMessageIds());
    }

    @Test
    public void testDisplayLongestMessage() {
        assertEquals("Where are you? You are late! I have asked you to be on time.",
                manager.getLongestStoredMessage());
    }

    @Test
    public void testSearchForMessageId() {
        assertEquals("It is dinner time!", manager.findMessageById("0838884567").getMessageText());
        assertEquals("Recipient: 0838884567\nMessage: It is dinner time!", manager.searchByMessageId("0838884567"));
    }

    @Test
    public void testSearchForUnknownMessageId() {
        assertNull(manager.findMessageById("0000000000"));
        assertEquals("No message found with ID: 0000000000", manager.searchByMessageId("0000000000"));
    }

    @Test
    public void testSearchMessagesForRecipient() {
        assertEquals(List.of("Where are you? You are late! I have asked you to be on time.",
                "Ok, I am leaving without you."), manager.searchByRecipient("+27838884567"));
    }

    @Test
    public void testDeleteMessageUsingHash() throws IOException {
        String result = manager.deleteByHash("78:1:WHERETIME");

        assertEquals("Message: \"Where are you? You are late! I have asked you to be on time.\" successfully deleted.",
                result);
        assertEquals(List.of(TEXT_5), manager.getStoredMessageTexts());
        assertFalse(manager.getMessageHashes().contains("78:1:WHERETIME"));
        assertFalse(Files.readString(manager.getStorageFile()).contains(TEXT_2));
    }

    @Test
    public void testDeleteUnknownHash() throws IOException {
        assertEquals("No stored message found with hash: 00:0:NOTHERE", manager.deleteByHash("00:0:NOTHERE"));
        assertEquals(List.of(TEXT_2, TEXT_5), manager.getStoredMessageTexts());
    }

    @Test
    public void testDisplayReport() {
        String separator = "----------------------------------------";
        String expected = "===== Sent Messages Report =====\n"
                + "Message ID: 4512345678\n"
                + "Message Hash: 45:0:DIDCAKE\n"
                + "Recipient: +27834557896\n"
                + "Message: Did you get the cake?\n"
                + separator + "\n"
                + "Message ID: 0838884567\n"
                + "Message Hash: 08:3:ITTIME\n"
                + "Recipient: 0838884567\n"
                + "Message: It is dinner time!\n"
                + separator + "\n";
        assertEquals(expected, manager.displayReport());
    }

    @Test
    public void testStoredMessagesSurviveSaveAndLoad() throws IOException {
        manager.saveStoredMessages();

        MessageManager reloaded = new MessageManager(manager.getStorageFile());
        reloaded.loadStoredMessages();

        assertEquals(List.of(TEXT_2, TEXT_5), reloaded.getStoredMessageTexts());
        assertEquals(List.of("78:1:WHERETIME", "99:4:OKYOU"), reloaded.getMessageHashes());
    }

    @Test
    public void testLoadStoredMessagesDoesNotDuplicate() throws IOException {
        manager.saveStoredMessages();

        MessageManager reloaded = new MessageManager(manager.getStorageFile());
        reloaded.loadStoredMessages();
        reloaded.loadStoredMessages();

        assertEquals(List.of(TEXT_2, TEXT_5), reloaded.getStoredMessageTexts());
        assertEquals(2, reloaded.getMessageHashes().size());
        assertTrue(reloaded.isStoredMessagesLoaded());
    }

    @Test
    public void testStoreMessageKeepsFileInSyncWithArray() throws IOException {
        MessageManager fresh = new MessageManager(tempDir.resolve("freshStored.json"));
        Message stored = new Message("5512345678", 0, SENDER, "+27830001111", "Sync check");
        stored.sentMessage(3);

        fresh.storeMessage(stored);

        assertEquals(List.of("Sync check"), fresh.getStoredMessageTexts());
        assertTrue(Files.readString(fresh.getStorageFile()).contains("Sync check"));

        MessageManager reloaded = new MessageManager(fresh.getStorageFile());
        reloaded.loadStoredMessages();
        assertEquals(List.of("Sync check"), reloaded.getStoredMessageTexts());
    }

    @Test
    public void testSearchIgnoresBlankInput() {
        assertNull(manager.findMessageById("   "));
        assertTrue(manager.searchByRecipient("").isEmpty());
        assertTrue(manager.searchByRecipient(null).isEmpty());
    }

    @Test
    public void testDeleteByHashIgnoresBlankInput() throws IOException {
        assertEquals("No stored message found with hash: null", manager.deleteByHash(null));
        assertEquals("No stored message found with hash:    ", manager.deleteByHash("   "));
        assertEquals(List.of(TEXT_2, TEXT_5), manager.getStoredMessageTexts());
    }
}
