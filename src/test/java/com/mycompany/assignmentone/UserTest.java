package com.mycompany.assignmentone;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class UserTest {

    // HELPER: REGISTERS A USER WITH KNOWN VALID DATA, BYPASSING INTERACTIVE INPUT
    private User createRegisteredUser() {
        User user = new User();
        user.setUsername("kyl_1");
        user.setPassword("Ch&&sec@ke99!");
        user.setPhoneNumber("+27838968976");
        user.setFirstName("Kyle");
        user.setLastName("Test");
        user.setAccountState(true);
        return user;
    }

    // TEST USERNAME VALIDATION.
    @Test
    public void testValidUsername() {
        User user = new User();
        boolean result = user.isValidUsername("kyl_1");
        System.out.println("testValidUsername result: " + result);
        assertTrue(result);
    }

    @Test
    public void testInvalidUsername() {
        User user = new User();
        boolean result = user.isValidUsername("kyle!!!!!!!");
        System.out.println("testInvalidUsername result: " + result);
        assertFalse(result);
    }

    // TEST PASSWORD VALIDATION
    @Test
    public void testValidPassword() {
        User user = new User();
        boolean result = user.isValidPassword("Ch&&sec@ke99!");
        System.out.println("testValidPassword result: " + result);
        assertTrue(result);
    }

    @Test
    public void testInvalidPassword() {
        User user = new User();
        boolean result = user.isValidPassword("oscar123");
        System.out.println("testInvalidPassword result: " + result);
        assertFalse(result);
    }

    // TEST PHONE NUMBER VALIDATION
    @Test
    public void testValidPhoneNumber() {
        User user = new User();
        boolean result = user.isValidPhoneNumber("+27838968976");
        System.out.println("testValidPhoneNumber result: " + result);
        assertTrue(result);
    }

    @Test
    public void testInvalidPhoneNumber() {
        User user = new User();
        boolean result = user.isValidPhoneNumber("08966553");
        System.out.println("testInvalidPhoneNumber result: " + result);
        assertFalse(result); // explicitly a boolean false, not an empty string
    }

    // TEST DEFAULT USER VALUES
    @Test
    public void testGetUsername() {
        User user = new User();
        String result = user.get_username();
        System.out.println("testGetUsername result: \"" + result + "\"");
        assertEquals("", result);
    }

    @Test
    public void testGetPhone() {
        User user = new User();
        String result = user.get_phone();
        System.out.println("testGetPhone result: \"" + result + "\"");
        assertEquals("", result);
    }

    // TEST LOGIN
    @Test
    public void testLoginSuccess() {
        User user = createRegisteredUser();
        boolean result = user.login("kyl_1", "Ch&&sec@ke99!");
        System.out.println("testLoginSuccess result: " + result);
        assertTrue(result);
    }

    @Test
    public void testLoginFailure() {
        User user = createRegisteredUser();
        boolean result = user.login("kyl_1", "WrongPassword1!");
        System.out.println("testLoginFailure result: " + result);
        assertFalse(result);
    }
}
