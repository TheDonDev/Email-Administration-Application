package emailapp.database;

import emailapp.model.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

public class DatabaseManager {
    private static final String DB_URL = "jdbc:sqlite:emailapp.db";
    private static DatabaseManager instance;

    private DatabaseManager() {
        initializeDatabase();
    }

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) instance = new DatabaseManager();
        return instance;
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    private void initializeDatabase() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS organizations (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL,
                    code TEXT NOT NULL UNIQUE,
                    country TEXT,
                    email_suffix TEXT,
                    is_active INTEGER DEFAULT 1,
                    created_at TEXT DEFAULT CURRENT_TIMESTAMP
                )""");
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS departments (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    organization_id INTEGER,
                    name TEXT NOT NULL,
                    code TEXT NOT NULL,
                    email_domain TEXT NOT NULL,
                    created_at TEXT DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (organization_id) REFERENCES organizations(id)
                )""");
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS employees (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    organization_id INTEGER,
                    first_name TEXT NOT NULL,
                    last_name TEXT NOT NULL,
                    department_id INTEGER,
                    role TEXT DEFAULT 'EMPLOYEE',
                    created_at TEXT DEFAULT CURRENT_TIMESTAMP,
                    is_active INTEGER DEFAULT 1,
                    FOREIGN KEY (organization_id) REFERENCES organizations(id),
                    FOREIGN KEY (department_id) REFERENCES departments(id)
                )""");
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS email_accounts (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    employee_id INTEGER NOT NULL,
                    email_address TEXT NOT NULL UNIQUE,
                    password TEXT NOT NULL,
                    mailbox_capacity INTEGER DEFAULT 500,
                    alternate_email TEXT,
                    created_at TEXT DEFAULT CURRENT_TIMESTAMP,
                    is_active INTEGER DEFAULT 1,
                    FOREIGN KEY (employee_id) REFERENCES employees(id)
                )""");
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS email_groups (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    organization_id INTEGER,
                    name TEXT NOT NULL,
                    email_address TEXT NOT NULL UNIQUE,
                    description TEXT,
                    created_at TEXT DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (organization_id) REFERENCES organizations(id)
                )""");
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS group_members (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    group_id INTEGER NOT NULL,
                    employee_id INTEGER NOT NULL,
                    FOREIGN KEY (group_id) REFERENCES email_groups(id),
                    FOREIGN KEY (employee_id) REFERENCES employees(id)
                )""");
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS password_policies (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    organization_id INTEGER,
                    min_length INTEGER DEFAULT 8,
                    max_length INTEGER DEFAULT 128,
                    require_uppercase INTEGER DEFAULT 1,
                    require_lowercase INTEGER DEFAULT 1,
                    require_digits INTEGER DEFAULT 1,
                    require_special_chars INTEGER DEFAULT 1,
                    expiration_days INTEGER DEFAULT 90,
                    history_count INTEGER DEFAULT 5,
                    FOREIGN KEY (organization_id) REFERENCES organizations(id)
                )""");
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS audit_logs (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id INTEGER,
                    action TEXT,
                    entity_type TEXT,
                    entity_id INTEGER,
                    details TEXT,
                    created_at TEXT DEFAULT CURRENT_TIMESTAMP
                )""");

            seedDefaultData(conn);
        } catch (SQLException e) {
            System.err.println("DB init error: " + e.getMessage());
        }
    }

    private void seedDefaultData(Connection conn) throws SQLException {
        try (Statement s = conn.createStatement(); ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM organizations")) {
            if (rs.next() && rs.getInt(1) == 0) {
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO organizations (name, code, country, email_suffix) VALUES (?,?,?,?)")) {
                    ps.setString(1, "Cellulant"); ps.setString(2, "CELL"); ps.setString(3, "Kenya"); ps.setString(4, "cellulant.com"); ps.addBatch();
                    ps.setString(1, "Acme Corp"); ps.setString(2, "ACME"); ps.setString(3, "USA"); ps.setString(4, "acmecorp.com"); ps.addBatch();
                    ps.executeBatch();
                }
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO departments (organization_id, name, code, email_domain) VALUES (?,?,?,?)")) {
                    ps.setInt(1, 1); ps.setString(2, "Engineering"); ps.setString(3, "eng"); ps.setString(4, "eng.cellulant.com"); ps.addBatch();
                    ps.setInt(1, 1); ps.setString(2, "Sales"); ps.setString(3, "sales"); ps.setString(4, "sales.cellulant.com"); ps.addBatch();
                    ps.setInt(1, 1); ps.setString(2, "Finance"); ps.setString(3, "fin"); ps.setString(4, "fin.cellulant.com"); ps.addBatch();
                    ps.setInt(1, 2); ps.setString(2, "Product"); ps.setString(3, "prod"); ps.setString(4, "prod.acmecorp.com"); ps.addBatch();
                    ps.executeBatch();
                }
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO password_policies (organization_id, min_length, require_uppercase, require_lowercase, require_digits, require_special_chars, expiration_days, history_count) VALUES (?,?,?,?,?,?,?,?)")) {
                    ps.setInt(1, 1); ps.setInt(2, 12); ps.setInt(3, 1); ps.setInt(4, 1); ps.setInt(5, 1); ps.setInt(6, 1); ps.setInt(7, 90); ps.setInt(8, 5); ps.addBatch();
                    ps.setInt(1, 2); ps.setInt(2, 8); ps.setInt(3, 1); ps.setInt(4, 1); ps.setInt(5, 1); ps.setInt(6, 1); ps.setInt(7, 60); ps.setInt(8, 3); ps.addBatch();
                    ps.executeBatch();
                }
            }
        }
    }

    /* ==================== ORGANIZATIONS ==================== */
    public List<Organization> getAllOrganizations() {
        List<Organization> list = new ArrayList<>();
        try (Connection c = getConnection(); Statement s = c.createStatement(); ResultSet rs = s.executeQuery("SELECT * FROM organizations")) {
            while (rs.next()) list.add(mapOrg(rs));
        } catch (SQLException e) { System.err.println(e.getMessage()); }
        return list;
    }

    public Organization getOrganizationById(int id) {
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement("SELECT * FROM organizations WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return mapOrg(rs); }
        } catch (SQLException e) { System.err.println(e.getMessage()); }
        return null;
    }

    public int addOrganization(Organization o) {
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(
                "INSERT INTO organizations (name, code, country, email_suffix, is_active) VALUES (?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, o.getName()); ps.setString(2, o.getCode()); ps.setString(3, o.getCountry());
            ps.setString(4, o.getEmailSuffix()); ps.setInt(5, o.isActive() ? 1 : 0);
            int affected = ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) { if (keys.next()) return keys.getInt(1); }
            return affected;
        } catch (SQLException e) { System.err.println(e.getMessage()); return -1; }
    }

    /* ==================== DEPARTMENTS ==================== */
    public List<Department> getAllDepartments() {
        List<Department> list = new ArrayList<>();
        try (Connection c = getConnection(); Statement s = c.createStatement(); ResultSet rs = s.executeQuery("SELECT * FROM departments")) {
            while (rs.next()) list.add(new Department(rs.getInt("id"), rs.getString("name"), rs.getString("code"), rs.getString("email_domain")));
        } catch (SQLException e) { System.err.println(e.getMessage()); }
        return list;
    }

    public Department getDepartmentById(int id) {
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement("SELECT * FROM departments WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return new Department(rs.getInt("id"), rs.getString("name"), rs.getString("code"), rs.getString("email_domain")); }
        } catch (SQLException e) { System.err.println(e.getMessage()); }
        return null;
    }

    public Department getDepartmentByCode(String code) {
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement("SELECT * FROM departments WHERE code = ?")) {
            ps.setString(1, code);
            try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return new Department(rs.getInt("id"), rs.getString("name"), rs.getString("code"), rs.getString("email_domain")); }
        } catch (SQLException e) { System.err.println(e.getMessage()); }
        return null;
    }

    public int addDepartment(Department d) {
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(
                "INSERT INTO departments (organization_id, name, code, email_domain) VALUES (?,?,?,?)", Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, 1); ps.setString(2, d.getName()); ps.setString(3, d.getCode()); ps.setString(4, d.getEmailDomain());
            int affected = ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) { if (keys.next()) return keys.getInt(1); }
            return affected;
        } catch (SQLException e) { System.err.println(e.getMessage()); return -1; }
    }

    /* ==================== EMPLOYEES ==================== */
    public int addEmployee(Employee emp) {
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(
                "INSERT INTO employees (organization_id, first_name, last_name, department_id, role, is_active) VALUES (?,?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, emp.getOrganizationId()); ps.setString(2, emp.getFirstName()); ps.setString(3, emp.getLastName());
            ps.setInt(4, emp.getDepartmentId()); ps.setString(5, emp.getRole().name()); ps.setInt(6, emp.isActive() ? 1 : 0);
            int affected = ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) { if (keys.next()) return keys.getInt(1); }
            return affected;
        } catch (SQLException e) { System.err.println(e.getMessage()); return -1; }
    }

    public List<Employee> getAllEmployees() {
        List<Employee> list = new ArrayList<>();
        try (Connection c = getConnection(); Statement s = c.createStatement(); ResultSet rs = s.executeQuery("SELECT * FROM employees")) {
            while (rs.next()) list.add(mapEmp(rs));
        } catch (SQLException e) { System.err.println(e.getMessage()); }
        return list;
    }

    public Employee getEmployeeById(int id) {
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement("SELECT * FROM employees WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return mapEmp(rs); }
        } catch (SQLException e) { System.err.println(e.getMessage()); }
        return null;
    }

    public boolean updateEmployee(Employee emp) {
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(
                "UPDATE employees SET organization_id=?, first_name=?, last_name=?, department_id=?, role=?, is_active=? WHERE id=?")) {
            ps.setInt(1, emp.getOrganizationId()); ps.setString(2, emp.getFirstName()); ps.setString(3, emp.getLastName());
            ps.setInt(4, emp.getDepartmentId()); ps.setString(5, emp.getRole().name()); ps.setInt(6, emp.isActive() ? 1 : 0); ps.setInt(7, emp.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { System.err.println(e.getMessage()); return false; }
    }

    public boolean deleteEmployee(int id) {
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement("UPDATE employees SET is_active = 0 WHERE id = ?")) {
            ps.setInt(1, id); return ps.executeUpdate() > 0;
        } catch (SQLException e) { System.err.println(e.getMessage()); return false; }
    }

    /* ==================== EMAIL ACCOUNTS ==================== */
    public int addEmailAccount(EmailAccount acct) {
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(
                "INSERT INTO email_accounts (employee_id, email_address, password, mailbox_capacity, alternate_email, is_active) VALUES (?,?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, acct.getEmployeeId()); ps.setString(2, acct.getEmailAddress()); ps.setString(3, acct.getPassword());
            ps.setInt(4, acct.getMailboxCapacity()); ps.setString(5, acct.getAlternateEmail()); ps.setInt(6, acct.isActive() ? 1 : 0);
            int affected = ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) { if (keys.next()) return keys.getInt(1); }
            return affected;
        } catch (SQLException e) { System.err.println(e.getMessage()); return -1; }
    }

    public List<EmailAccount> getAllEmailAccounts() {
        List<EmailAccount> list = new ArrayList<>();
        try (Connection c = getConnection(); Statement s = c.createStatement(); ResultSet rs = s.executeQuery("SELECT * FROM email_accounts")) {
            while (rs.next()) list.add(mapAcct(rs));
        } catch (SQLException e) { System.err.println(e.getMessage()); }
        return list;
    }

    public EmailAccount getEmailAccountById(int id) {
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement("SELECT * FROM email_accounts WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return mapAcct(rs); }
        } catch (SQLException e) { System.err.println(e.getMessage()); }
        return null;
    }

    public EmailAccount getEmailAccountByEmail(String email) {
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement("SELECT * FROM email_accounts WHERE email_address = ?")) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return mapAcct(rs); }
        } catch (SQLException e) { System.err.println(e.getMessage()); }
        return null;
    }

    public boolean updateEmailAccount(EmailAccount acct) {
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(
                "UPDATE email_accounts SET employee_id=?, email_address=?, password=?, mailbox_capacity=?, alternate_email=?, is_active=? WHERE id=?")) {
            ps.setInt(1, acct.getEmployeeId()); ps.setString(2, acct.getEmailAddress()); ps.setString(3, acct.getPassword());
            ps.setInt(4, acct.getMailboxCapacity()); ps.setString(5, acct.getAlternateEmail()); ps.setInt(6, acct.isActive() ? 1 : 0); ps.setInt(7, acct.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { System.err.println(e.getMessage()); return false; }
    }

    public boolean deleteEmailAccount(int id) {
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement("UPDATE email_accounts SET is_active = 0 WHERE id = ?")) {
            ps.setInt(1, id); return ps.executeUpdate() > 0;
        } catch (SQLException e) { System.err.println(e.getMessage()); return false; }
    }

    public boolean changePassword(int accountId, String newPassword) {
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement("UPDATE email_accounts SET password = ? WHERE id = ?")) {
            ps.setString(1, newPassword); ps.setInt(2, accountId); return ps.executeUpdate() > 0;
        } catch (SQLException e) { System.err.println(e.getMessage()); return false; }
    }

    public boolean setMailboxCapacity(int accountId, int capacity) {
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement("UPDATE email_accounts SET mailbox_capacity = ? WHERE id = ?")) {
            ps.setInt(1, capacity); ps.setInt(2, accountId); return ps.executeUpdate() > 0;
        } catch (SQLException e) { System.err.println(e.getMessage()); return false; }
    }

    public boolean setAlternateEmail(int accountId, String alternateEmail) {
        EmailAccount a = getEmailAccountById(accountId);
        if (a != null) { a.setAlternateEmail(alternateEmail); return updateEmailAccount(a); }
        return false;
    }

    /* ==================== EMAIL GROUPS ==================== */
    public int addEmailGroup(EmailGroup g) {
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(
                "INSERT INTO email_groups (organization_id, name, email_address, description) VALUES (?,?,?,?)", Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, g.getOrganizationId()); ps.setString(2, g.getName()); ps.setString(3, g.getEmailAddress()); ps.setString(4, g.getDescription());
            int affected = ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) { if (keys.next()) return keys.getInt(1); }
            return affected;
        } catch (SQLException e) { System.err.println(e.getMessage()); return -1; }
    }

    public List<EmailGroup> getAllEmailGroups() {
        List<EmailGroup> list = new ArrayList<>();
        try (Connection c = getConnection(); Statement s = c.createStatement(); ResultSet rs = s.executeQuery("SELECT * FROM email_groups")) {
            while (rs.next()) list.add(mapGroup(rs));
        } catch (SQLException e) { System.err.println(e.getMessage()); }
        return list;
    }

    public boolean addGroupMember(int groupId, int employeeId) {
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement("INSERT INTO group_members (group_id, employee_id) VALUES (?,?)")) {
            ps.setInt(1, groupId); ps.setInt(2, employeeId); return ps.executeUpdate() > 0;
        } catch (SQLException e) { System.err.println(e.getMessage()); return false; }
    }

    public List<Integer> getGroupMemberIds(int groupId) {
        List<Integer> ids = new ArrayList<>();
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement("SELECT employee_id FROM group_members WHERE group_id = ?")) {
            ps.setInt(1, groupId);
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) ids.add(rs.getInt("employee_id")); }
        } catch (SQLException e) { System.err.println(e.getMessage()); }
        return ids;
    }

    /* ==================== PASSWORD POLICIES ==================== */
    public PasswordPolicy getPasswordPolicy(int orgId) {
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement("SELECT * FROM password_policies WHERE organization_id = ?")) {
            ps.setInt(1, orgId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapPolicy(rs);
            }
        } catch (SQLException e) { System.err.println(e.getMessage()); }
        return new PasswordPolicy(orgId);
    }

    /* ==================== AUDIT LOGS ==================== */
    public void addAuditLog(int userId, String action, String entityType, int entityId, String details) {
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(
                "INSERT INTO audit_logs (user_id, action, entity_type, entity_id, details, created_at) VALUES (?,?,?,?,?,?)")) {
            ps.setInt(1, userId); ps.setString(2, action); ps.setString(3, entityType); ps.setInt(4, entityId);
            ps.setString(5, details); ps.setString(6, LocalDateTime.now().toString()); ps.executeUpdate();
        } catch (SQLException e) { System.err.println(e.getMessage()); }
    }

    public List<AuditLog> getAllAuditLogs() {
        List<AuditLog> list = new ArrayList<>();
        try (Connection c = getConnection(); Statement s = c.createStatement(); ResultSet rs = s.executeQuery("SELECT * FROM audit_logs ORDER BY id DESC")) {
            while (rs.next()) list.add(mapAudit(rs));
        } catch (SQLException e) { System.err.println(e.getMessage()); }
        return list;
    }

    /* ==================== MAPPERS ==================== */
    private Organization mapOrg(ResultSet rs) throws SQLException {
        return new Organization(rs.getInt("id"), rs.getString("name"), rs.getString("code"), rs.getString("country"), rs.getString("email_suffix"), rs.getInt("is_active") == 1);
    }

    private Employee mapEmp(ResultSet rs) throws SQLException {
        Employee e = new Employee(rs.getInt("id"), rs.getString("first_name"), rs.getString("last_name"), rs.getInt("department_id"), rs.getInt("is_active") == 1);
        e.setOrganizationId(rs.getInt("organization_id"));
        try { e.setRole(Role.valueOf(rs.getString("role"))); } catch (Exception ex) { e.setRole(Role.EMPLOYEE); }
        return e;
    }

    private EmailAccount mapAcct(ResultSet rs) throws SQLException {
        return new EmailAccount(rs.getInt("id"), rs.getInt("employee_id"), rs.getString("email_address"), rs.getString("password"), rs.getInt("mailbox_capacity"), rs.getString("alternate_email"), rs.getInt("is_active") == 1);
    }

    private EmailGroup mapGroup(ResultSet rs) throws SQLException {
        return new EmailGroup(rs.getInt("id"), rs.getInt("organization_id"), rs.getString("name"), rs.getString("email_address"), rs.getString("description"));
    }

    private PasswordPolicy mapPolicy(ResultSet rs) throws SQLException {
        PasswordPolicy p = new PasswordPolicy(rs.getInt("organization_id"));
        p.setMinLength(rs.getInt("min_length")); p.setMaxLength(rs.getInt("max_length"));
        p.setRequireUppercase(rs.getInt("require_uppercase") == 1); p.setRequireLowercase(rs.getInt("require_lowercase") == 1);
        p.setRequireDigits(rs.getInt("require_digits") == 1); p.setRequireSpecialChars(rs.getInt("require_special_chars") == 1);
        p.setExpirationDays(rs.getInt("expiration_days")); p.setHistoryCount(rs.getInt("history_count"));
        return p;
    }

    private AuditLog mapAudit(ResultSet rs) throws SQLException {
        return new AuditLog(rs.getInt("id"), rs.getInt("user_id"), rs.getString("action"), rs.getString("entity_type"), rs.getInt("entity_id"), rs.getString("details"), LocalDateTime.parse(rs.getString("created_at")));
    }
}
