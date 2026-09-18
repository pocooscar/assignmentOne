# Ospoco Chatting System — Assignment One

## Overview

This project is a Java console application that implements a simple **user
registration and login system**, built using object-oriented programming
(OOP) principles. It was developed in NetBeans as a Maven project and is
tested with JUnit 5.

The core idea: a `User` class encapsulates all account data (username,
password, phone number, first/last name) and account behavior (validation,
registration, login), while `AssignmentOne` provides a menu-driven console
interface for interacting with a `User` object.

---

## Project Structure

```
assignmentOne/
├── pom.xml
├── src/
│   ├── main/
│   │   └── java/
│   │       └── com/mycompany/assignmentone/
│   │           ├── AssignmentOne.java   # Entry point / console menu
│   │           └── User.java            # Core OOP class
│   └── test/
│       └── java/
│           └── com/mycompany/assignmentone/
│               └── UserTest.java        # JUnit 5 unit tests
└── target/                              # Build output (ignored by Git)
```

---

## Requirements

- **Java 21** (JDK)
- **Maven** (bundled with NetBeans, or installed separately)
- **JUnit 5 (Jupiter)** — declared as a test-scoped dependency in `pom.xml`

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
  `login(...)`. Locks the session after repeated failures.

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

## Class: `AssignmentOne`

The application's entry point. Presents a simple text menu in a loop:

```
--- Ospoco Chatting System ---
1. Register
2. Login
3. Exit
Choose an option:
```

| Option | Action                        |
|--------|--------------------------------|
| 1      | Calls `user.register()`        |
| 2      | Calls `user.loginInteractive()`|
| 3      | Exits the loop and closes scanners |

Two `Scanner` instances are used in the current design — one owned by
`User` (for registration/login prompts) and one in `main` (for the menu
itself). Both wrap `System.in` and are closed once, at program exit.

---

## Testing

Unit tests live in `UserTest.java` and use **JUnit 5** (`org.junit.jupiter`).
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
  many accounts.
