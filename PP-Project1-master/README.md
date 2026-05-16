# Comic Book Management System

This project is a JavaFX desktop app for managing a small comic book store. It uses a SQLite database, JDBC, and DAO classes to keep the database work separate from the screens.

The app lets users manage customers, employees, comics, and orders. It also supports English, French, and Spanish through Java resource bundles.

## How to Run the Program

You need Java 21 and Maven installed.

From the project folder, run:

```bash
mvn javafx:run
```

If your IDE supports Maven, you can also create a Maven run configuration with this goal:

```text
javafx:run
```

The main class is:

```text
main.TestFX
```

The app uses this SQLite database file:

```text
test.db
```

If the login table is empty, the program creates two test accounts automatically:

```text
Admin account:    admin / admin123
Employee account: employee / employee123
```

The admin account can use every feature, including deleting records. The employee account can add, update, search, and view data, but cannot delete records.

## Login and Permissions

When the app starts, it opens the login screen first.

There are two roles:

- `ADMIN`: full access, including delete
- `EMPLOYEE`: full access except delete

Employees do not see the delete buttons. The controllers also check the role before deleting, so the rule is enforced in code too.

There is also a logout button in the main sidebar. Logging out clears the current session and returns to the login screen.

## Language Support

The app supports:

- English
- French
- Spanish

Language can be changed from the login screen or from the main app sidebar. The translations are stored in Java resource bundle files:

```text
src/main/resources/i18n/messages.properties
src/main/resources/i18n/messages_fr.properties
src/main/resources/i18n/messages_es.properties
```

The `I18nManager` class loads the selected bundle and remembers the preferred language.

## Main Features

Customer management:

- Add customers
- Update customer information
- Delete customers as admin
- Search by ID, name, email, or phone number
- View customer records in a table

Employee management:

- Add employees
- Update employee names
- Delete employees as admin
- Search by ID, first name, or last name
- View employee records in a table

Comic management:

- Add comics
- Update title, issue, publisher, price, and stock
- Delete comics as admin
- Search by ID, title, issue, or publisher
- View comic inventory in a table

Order management:

- Create orders
- Update orders
- Delete orders as admin
- Search by order ID, date, customer ID, comic ID, or status
- View orders in a table

## Project Structure

The project is organized by responsibility.

```text
src/main/java/
```

Main Java source folder.

```text
src/main/java/main/
```

JavaFX application and screen controllers. This includes the login screen, main screen, and each management panel.

Important classes:

- `TestFX.java`: starts the JavaFX app
- `LoginController.java`: handles login
- `MainController.java`: controls the main app layout, language selector, logout, and module switching
- `CustomerController.java`: customer screen logic
- `EmployeeController.java`: employee screen logic
- `ComicController.java`: comic screen logic
- `OrderController.java`: order screen logic

```text
src/main/java/auth/
```

Login and permission code.

Important classes:

- `Role.java`: defines `ADMIN` and `EMPLOYEE`
- `UserSession.java`: stores the currently logged-in user role
- `UserAccountDAO.java`: login DAO interface
- `UserAccountDAOImpl.java`: checks usernames and passwords against SQLite
- `RoleGuard.java`: hides delete buttons and blocks delete actions for employees

```text
src/main/java/Customer/
src/main/java/Employee/
src/main/java/Comic/
src/main/java/Order/
```

Model and DAO classes for each part of the store. These classes handle the data objects and database queries.

```text
src/main/java/I18n/
```

Internationalization support. `I18nManager.java` loads the correct language file.

```text
src/main/java/util/
```

Utility classes. `DBConnectionUtility.java` creates the SQLite connection.

```text
src/main/resources/
```

FXML screens, CSS, and translation files.

Important files:

- `login-view.fxml`: login screen
- `main-view.fxml`: main app layout
- `customer-view.fxml`: customer panel
- `employee-view.fxml`: employee panel
- `comic-view.fxml`: comic panel
- `order-view.fxml`: order panel
- `stage-manager.css`: app styling
- `i18n/messages*.properties`: translated UI text

## Technologies Used

- Java 21
- JavaFX
- FXML
- Maven
- SQLite
- JDBC
- DAO pattern
- ResourceBundle-based I18n

## Design Pattern Notes

The app mainly follows an MVC-style structure:

- Models are the data classes, such as `Customer`, `Employee`, `Comic`, and `Order`.
- Views are the FXML files in `src/main/resources`.
- Controllers are the JavaFX controller classes in `src/main/java/main`.

The DAO pattern is used for database access. This keeps SQL code out of the JavaFX controllers as much as possible.

The language manager uses a singleton-style approach so the app has one shared place for the current language and resource bundle.
