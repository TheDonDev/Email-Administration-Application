# Email Administration Application  

An enterprise-grade Java application for managing organizational email accounts. Built with a layered architecture (UI → Service → Database) using Java Swing for the frontend and SQLite for persistent storage.

## Features
- Generate email addresses dynamically for new employees (name.department@domain)
- Assign mailboxes with customizable storage quotas
- Department-based email domains and multi-organization support
- Secure password generation with configurable password policies
- Role-Based Access Control (RBAC): SUPER_ADMIN, ORG_ADMIN, MANAGER, EMPLOYEE
- Mailbox capacity and alternate email management
- Email groups with membership management
- Full audit trail of all administrative actions
- Search across employees and email accounts
- Java Swing desktop GUI with tabbed interface

## Tech Stack
- **Language:** Java (JDK 8+)
- **Frontend:** Java Swing (desktop GUI)
- **Database:** SQLite (file-based, zero-configuration)
- **Persistence:** JDBC
- **Build:** Plain `javac` (no external build tool required)

## Project Structure
```
Email-Administration-Application/
├── README.md
├── .gitignore
├── Java/
│   ├── .vscode/settings.json        # VS Code config
│   ├── lib/sqlite-jdbc.jar          # SQLite JDBC driver
│   ├── run.sh                       # Linux/macOS runner
│   ├── run.bat                      # Windows runner
│   └── src/emailapp/
│       ├── Main.java                # Application entry point
│       ├── model/                   # Data entities
│       │   ├── Employee.java
│       │   ├── EmailAccount.java
│       │   ├── Department.java
│       │   ├── Organization.java
│       │   ├── Role.java
│       │   ├── AuditLog.java
│       │   ├── EmailGroup.java
│       │   ├── GroupMember.java
│       │   └── PasswordPolicy.java
│       ├── database/                # Data access layer (JDBC + SQLite)
│       │   └── DatabaseManager.java
│       ├── service/                 # Business logic layer
│       │   └── EmailService.java
│       └── ui/                      # Frontend layer (Swing GUI)
│           └── EmailAdminUI.java
```

## Getting Started

### Prerequisites
- Java Development Kit (JDK) 8 or higher

### Run (Linux/macOS)
```bash
cd Java
chmod +x run.sh
./run.sh
```

### Run (Windows)
```bat
cd Java
run.bat
```

### Manual compile & run
```bash
cd Java
javac -cp "lib/sqlite-jdbc.jar" -d bin $(find src/emailapp -name "*.java")
java -cp "bin:lib/sqlite-jdbc.jar" emailapp.Main
```

## Usage
1. Launch the app — a Swing window opens with tabs.
2. **Dashboard:** View live counts of organizations, departments, employees, email accounts, groups, and audit logs.
3. **Employees:** Create new employees (auto-generates email + secure password), search, deactivate.
4. **Email Accounts:** Change passwords, set mailbox capacity, set alternate emails, deactivate accounts.
5. **Departments:** Add departments with custom email domains.
6. **Organizations:** Add multi-tenant organizations (e.g., Cellulant, Acme Corp).
7. **Groups:** Create email groups and add members.
8. **Audit Logs:** Review every create/update/delete action.
9. **Settings:** Architecture overview.

## Architecture (for interviews)
- **Model layer:** Plain Java objects (Employee, EmailAccount, Department, Organization, Role, AuditLog, EmailGroup, PasswordPolicy).
- **Database layer:** `DatabaseManager` handles all SQLite JDBC operations — schema creation, seeding default data, CRUD, and audit logging.
- **Service layer:** `EmailService` contains business logic — email generation, password policy validation, RBAC, and orchestration between models and database.
- **UI layer:** `EmailAdminUI` is a Swing-based desktop frontend with tabbed navigation, search, tables, and dialogs.
- Data persists in `emailapp.db` (SQLite file) so accounts survive restarts.

## License
MIT License
