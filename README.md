# Ospoco Chatting System — Assignment One

## Overview

This project is a Java desktop (Swing) application that implements a simple
**user registration and login system**, built using object-oriented
programming (OOP) principles. It was developed in NetBeans as a Maven
project and is tested with JUnit 5.

The core idea: a `User` class encapsulates all account data (username,
password, phone number, first/last name) and account behavior (validation,
registration, login), while `ChatAppGUI` provides the window the user
interacts with. `AssignmentOne` is the entry point that opens it.

After a successful login the user enters **QuickChat** (Parts 2 and 3),
where they can send, store or disregard messages, and search, delete and
report on stored messages. See [QuickChat](#quickchat-parts-2-and-3).

---

## Project Structure

```
assignmentOne/
├── pom.xml
├── src/
│   ├── main/
│   │   └── java/
│   │       └── com/mycompany/assignmentone/
│   │           ├── AssignmentOne.java   # Entry point, opens the GUI
│   │           ├── ChatAppGUI.java      # Swing window for the whole app
│   │           ├── User.java            # Account registration & login
│   │           ├── Message.java         # A single message
│   │           └── MessageManager.java  # Message arrays, search, reports
│   └── test/
│       └── java/
│           └── com/mycompany/assignmentone/
│               ├── UserTest.java            # Part 1 tests
│               ├── MessageTest.java         # Part 2 tests
│               └── MessageManagerTest.java  # Part 3 tests
├── storedMessages.json                  # Created at runtime (ignored by Git)
└── target/                              # Build output
```

---

## Requirements

- **Java 21** (JDK)
- **Maven** (bundled with NetBeans, or installed separately)
- **JUnit 5 (Jupiter)** — declared as a test-scoped dependency in `pom.xml`
- **org.json** — used to read and write `storedMessages.json`

---

## Building & Running

### Compile the project
```bash
mvn compile
```

### Run the tests
```bash
mvn test
```

### Run the application
From NetBeans: right-click the project → **Run**, or run the `main` method
in `AssignmentOne.java` directly.

From the command line (after building a jar, if configured):
```bash
mvn package
java -jar target/assignmentOne-1.0-SNAPSHOT.jar
```

---

## Class: `User`

Represents a single user account. All fields are `private`; access is
provided through getters/setters, following encapsulation principles.

### Fields

| Field         | Type      | Purpose                                    |
|---------------|-----------|---------------------------------------------|
| `username`    | `String`  | Stored username                             |
| `password`    | `String`  | Stored password                             |
| `phoneNumber` | `String`  | Stored phone number (with country code)     |
| `first_name`  | `String`  | User's first name                           |
| `last_name`   | `String`  | User's last name                            |
| `account`     | `boolean` | Whether the account has completed registration |
| `scanner`     | `Scanner` | Reads console input during interactive flows |

### Validation Rules

| Field         | Rule                                                                 |
|---------------|-----------------------------------------------------------------------|
| Username      | Must contain an underscore (`_`) and be 5 characters or fewer        |
| Password      | At least 8 characters, containing an uppercase letter, a digit, and a special character |
| Phone number  | Must start with `+` (country code) followed by digits, max 12 characters total |

These are implemented as reusable, `public` helper methods so they can be
called both internally (during registration) and directly from unit tests:

```java
boolean isValidUsername(String input)
boolean isValidPassword(String input)
boolean isValidPhoneNumber(String input)
```

### Registration — `register()`

Interactively prompts the user (via `Scanner`) for a username, password,
first name, last name, and phone number. Username, password, and phone
number are each validated in a `do-while` loop, re-prompting until valid
input is provided. On success, `account` is set to `true` and a
confirmation message is printed.

If `register()` is called on an account that's already registered, it
prints a message and returns immediately without re-prompting.

### Login — two methods

Login is split into two methods to separate **testable logic** from
**console interaction**:

- **`login(String inputUsername, String inputPassword)`** — pure logic.
  Compares the given credentials against the stored account. Returns
  `true` on success, `false` on failure. Takes no input from `System.in`,
  which makes it directly callable from JUnit tests.

- **`loginInteractive()`** — console-facing wrapper used by the menu in
  `AssignmentOne`. Prompts for username and password via `Scanner`,
  allows up to **3 attempts**, and delegates the actual check to
  `login(...)`. Locks the session after repeated failures. Returns `true`
  if the user logged in, which is what unlocks QuickChat.

### Getters

```java
String  get_username()
String  get_phone()
String  get_first_name()
String  get_last_name()
boolean getAccountState()
```

### Setters

Used primarily to build a "pre-registered" `User` object programmatically
(e.g. in tests), without going through interactive `Scanner` input:

```java
void setUsername(String username)
void setPassword(String password)
void setPhoneNumber(String phoneNumber)
void setFirstName(String first_name)
void setLastName(String last_name)
void setAccountState(boolean account)
```

### Cleanup

```java
void close_scanner()
```

Closes the internal `Scanner`. Should be called **once**, at the very end
of the program's lifetime — not inside individual methods — since closing
`System.in` closes it permanently for the whole JVM process.

---

## The GUI: `AssignmentOne` and `ChatAppGUI`

`AssignmentOne.main` opens a `ChatAppGUI` window using the operating
system's normal look. Everything happens in that window:

- **Menu buttons** on the left (the menu changes as you log in and move
  into QuickChat or Stored Messages).
- **An output area** on the right that shows results, messages and reports.
- **Pop-up dialogs** for typing input (username, password, cell number,
  message) and for confirmations and errors.

The first menu is **Register**, **Login** and **Exit**. Registration and
login use the same validation rules as Part 1 (`isValidUsername`,
`isValidPassword`, `isValidPhoneNumber` and `login(...)`), with up to
3 login attempts before the account is locked for the session. Pressing
**Cancel** in any input dialog stops the current task and returns to the
menu.

The console methods `register()` and `loginInteractive()` are still in
`User` from Part 1, but the GUI does not use them.

---

## QuickChat (Parts 2 and 3)

QuickChat opens only after a successful login. It shows
**"Welcome to QuickChat."**, loads any messages saved in
`storedMessages.json`, and asks how many messages the user wants to send.
The user can only enter that many messages.

QuickChat menu buttons:

1. Send Messages
2. Show recently sent messages (shows "Coming Soon.")
3. Quit
4. Stored Messages

### Sending a message

For each message the user enters:

1. **Recipient** — reuses the Part 1 phone rule (`User.isValidCellNumber`):
   a `+` international code followed by digits, 12 characters max.
2. **Message** — up to 250 characters.
3. A random ten-digit **Message ID** and a **Message Hash** are generated.
   The hash is the first two digits of the ID, the message number
   (starting at 0) and the first and last words, in capitals,
   e.g. `00:0:HITONIGHT`.
4. The user chooses **Send**, **Disregard** (then press `0` to delete) or
   **Store** (saved to `storedMessages.json`).

Sent messages are displayed in the order Message ID, Message Hash,
Recipient, Message. Once all messages are entered, the total number sent
is displayed.

### Stored Messages menu

1. Display the sender and recipient of all stored messages
2. Display the longest stored message
3. Search for a message ID (shows recipient and message)
4. Search for all messages to a recipient
5. Delete a stored message using its message hash (also updates the JSON file)
6. Display a report of all stored messages

### Class: `Message`

| Method | Purpose |
|--------|---------|
| `boolean checkMessageID()` | Message ID is no more than ten characters |
| `String checkRecipientCell()` | `"Cell phone number successfully captured."` or the incorrectly-formatted message |
| `String checkMessageLength()` | `"Message ready to send."` or `"Message exceeds 250 characters by X; please reduce the size."` |
| `String createMessageHash()` | Builds the message hash |
| `String sentMessage(int option)` | 1 = Send, 2 = Disregard, 3 = Store; returns the matching confirmation |
| `String getMessageIdNotice()` | `"Message ID generated: <Message ID>"` |
| `String printMessageDetails()` | Message ID, Hash, Recipient and Message |
| `void storeMessage(Path file)` | Appends the message to a JSON file |

### Class: `MessageManager`

Holds the Part 3 arrays: **sent**, **disregarded** and **stored** messages,
plus all **message hashes** and **message IDs**. It also provides
`printMessages()` and `returnTotalMessages()` (these work on the whole
list of messages, so they live here rather than on a single `Message`),
`loadStoredMessages()` to read the JSON file into the stored array once
(reloading is ignored so messages are not duplicated), `storeMessage(...)`
to add a stored message and rewrite the JSON file so it stays in sync with
the array, and the search, delete and report features used by the Stored
Messages menu.

---

## Testing

Unit tests use **JUnit 5** (`org.junit.jupiter`):

- `MessageTest.java` — Part 2: message length, recipient format, message
  hash (including a loop over several messages), message ID creation,
  send/disregard/store responses, total sent, and JSON storage.
- `MessageManagerTest.java` — Part 3: populates the arrays with the
  five test messages and checks the sent array, longest message, message
  ID search, recipient search, delete by hash and the report.
- `UserTest.java` — Part 1, described below.

Part 1 tests live in `UserTest.java`.
Each test prints the actual boolean result to the console (via
`System.out.println`) before asserting it, so pass/fail values are visible
in the Maven build log in addition to Surefire's summary report.

### Covered cases

| Test                     | Verifies                                             | Expected |
|---------------------------|-------------------------------------------------------|----------|
| `testValidUsername`       | `"kyl_1"` passes username rules                       | `true`   |
| `testInvalidUsername`     | `"kyle!!!!!!!"` fails username rules                  | `false`  |
| `testValidPassword`       | `"Ch&&sec@ke99!"` passes password rules               | `true`   |
| `testInvalidPassword`     | `"oscar123"` fails password rules                     | `false`  |
| `testValidPhoneNumber`    | `"+27838968976"` passes phone rules                   | `true`   |
| `testInvalidPhoneNumber`  | `"08966553"` fails phone rules (no `+`)               | `false`  |
| `testGetUsername`         | New `User` has empty-string username by default        | `""`     |
| `testGetPhone`            | New `User` has empty-string phone number by default     | `""`     |
| `testLoginSuccess`        | Correct credentials log in                            | `true`   |
| `testLoginFailure`        | Incorrect credentials are rejected                     | `false`  |

A private helper, `createRegisteredUser()`, builds a `User` with known
valid data via setters (bypassing `Scanner` input entirely), so login
tests can run without any console interaction.

### Sample output

```
testValidPassword result: true
testInvalidUsername result: false
testValidPhoneNumber result: true
testInvalidPhoneNumber result: false
testValidUsername result: true
testInvalidPassword result: false
testLoginSuccess result: true
testLoginFailure result: false
testGetUsername result: ""
testGetPhone result: ""

Tests run: 10, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

---

## Design Notes & Rationale

- **Encapsulation**: all fields are `private`; external code interacts only
  through getters/setters and public behavior methods (`register`, `login`,
  validators).
- **Separation of concerns**: validation logic, registration flow, and
  login flow are each their own methods, rather than one large block —
  making the class easier to read, test, and extend.
- **Testability over convenience**: `login()` was deliberately split into a
  pure logic method and a console-interactive wrapper. Methods that read
  directly from `System.in` are difficult to unit test, so isolating the
  decision logic from I/O was a key design choice.
- **String comparison**: credential checks use `.equals()`, never `==`,
  since `==` compares object references rather than string content in Java.
- **Known limitation**: passwords are stored and compared as plain text.
  This is acceptable for a class assignment, but a production system should
  hash passwords (e.g. with BCrypt) rather than storing them as readable
  strings.
- **Known limitation**: the current design supports a single `User` per
  object/session. A multi-user system would need a collection (e.g.
  `ArrayList<User>`) or a dedicated manager class to register and look up
  many accounts .