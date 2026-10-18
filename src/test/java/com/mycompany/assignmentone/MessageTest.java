package com.mycompany.assignmentone;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Part 2 unit tests for the Message class.
 */
public class MessageTest {

    private static final String SENDER = "Kyle Test";

    // TEST DATA FOR MESSAGE 1 AND MESSAGE 2
    private static final String RECIPIENT_1 = "+27718693002";
    private static final String TEXT_1 = "Hi Mike, can you join us for dinner tonight?";
    private static final String RECIPIENT_2 = "08575975889";
    private static final String TEXT_2 = "Hi Keegan, did you receive the payment?";

    // MESSAGE LENGTH
    @Test
    public void testMessageLengthSuccess() {
        Message message = new Message(0, SENDER, RECIPIENT_1, TEXT_1);
        assertEquals("Message ready to send.", message.checkMessageLength());
    }

    @Test
    public void testMessageLengthFailure() {
        Message message = new Message(0, SENDER, RECIPIENT_1, "A".repeat(260));
        assertEquals("Message exceeds 250 characters by 10; please reduce the size.", message.checkMessageLength());
    }

    // RECIPIENT NUMBER
    @Test
    public void testRecipientNumberSuccess() {
        Message message = new Message(0, SENDER, RECIPIENT_1, TEXT_1);
        assertEquals("Cell phone number successfully captured.", message.checkRecipientCell());
    }

    @Test
    public void testRecipientNumberFailure() {
        Message message = new Message(1, SENDER, RECIPIENT_2, TEXT_2);
        assertEquals("Cell phone number is incorrectly formatted or does not contain an international code. "
                + "Please correct the number and try again.", message.checkRecipientCell());
    }

    // MESSAGE HASH
    @Test
    public void testMessageHashForTestCase1() {
        Message message = new Message("0012345678", 0, SENDER, RECIPIENT_1, TEXT_1);
        assertEquals("00:0:HITONIGHT", message.createMessageHash());
    }

    @Test
    public void testMessageHashesInLoop() {
        String[] ids = {"0012345678", "4598765432", "7811122233"};
        String[] texts = {TEXT_1, TEXT_2, "Did you get the cake?"};
        String[] expectedHashes = {"00:0:HITONIGHT", "45:1:HIPAYMENT", "78:2:DIDCAKE"};

        for (int i = 0; i < texts.length; i++) {
            Message message = new Message(ids[i], i, SENDER, RECIPIENT_1, texts[i]);
            assertEquals(expectedHashes[i], message.createMessageHash(), "Hash for message " + i);
        }
    }

    // MESSAGE ID
    @Test
    public void testMessageIdCreated() {
        Message message = new Message(0, SENDER, RECIPIENT_1, TEXT_1);
        String id = message.getMessageId();

        assertTrue(message.checkMessageID());
        assertTrue(id.matches("\\d{10}"), "Message ID should be ten digits: " + id);
        assertEquals("Message ID generated: " + id, message.getMessageIdNotice());
    }

    @Test
    public void testCheckMessageIdRejectsLongId() {
        Message message = new Message("12345678901", 0, SENDER, RECIPIENT_1, TEXT_1);
        assertFalse(message.checkMessageID());
    }

    // SEND / DISREGARD / STORE
    @Test
    public void testSentMessageSend() {
        Message message = new Message(0, SENDER, RECIPIENT_1, TEXT_1);
        assertEquals("Message successfully sent.", message.sentMessage(1));
        assertEquals(Message.Status.SENT, message.getStatus());
    }

    @Test
    public void testSentMessageDisregard() {
        Message message = new Message(1, SENDER, RECIPIENT_2, TEXT_2);
        assertEquals("Press 0 to delete the message.", message.sentMessage(2));
        assertEquals(Message.Status.DISREGARDED, message.getStatus());
    }

    @Test
    public void testSentMessageStore() {
        Message message = new Message(0, SENDER, RECIPIENT_1, TEXT_1);
        assertEquals("Message successfully stored.", message.sentMessage(3));
        assertEquals(Message.Status.STORED, message.getStatus());
    }

    // TOTAL MESSAGES (NUM MESSAGES = 2: MESSAGE 1 SENT, MESSAGE 2 DISCARDED)
    @Test
    public void testReturnTotalMessages(@TempDir Path tempDir) {
        MessageManager manager = new MessageManager(tempDir.resolve("stored.json"));

        Message message1 = new Message(0, SENDER, RECIPIENT_1, TEXT_1);
        message1.sentMessage(1);
        manager.addMessage(message1);

        Message message2 = new Message(1, SENDER, RECIPIENT_2, TEXT_2);
        message2.sentMessage(2);
        manager.addMessage(message2);

        assertEquals(1, manager.returnTotalMessages());
        assertEquals(List.of(TEXT_1), manager.getSentMessageTexts());
        assertEquals(List.of(TEXT_2), manager.getDisregardedMessageTexts());
    }

    // JSON STORAGE
    @Test
    public void testStoreMessageToJsonAndReadBack(@TempDir Path tempDir) throws IOException {
        Path file = tempDir.resolve("stored.json");
        Message message = new Message("0012345678", 0, SENDER, RECIPIENT_1, TEXT_1);
        message.sentMessage(3);
        message.storeMessage(file);

        MessageManager manager = new MessageManager(file);
        manager.loadStoredMessages();

        assertEquals(List.of(TEXT_1), manager.getStoredMessageTexts());
        assertEquals(List.of("00:0:HITONIGHT"), manager.getMessageHashes());
        assertEquals(List.of("0012345678"), manager.getMessageIds());
    }
}
