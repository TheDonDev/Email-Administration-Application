package emailapp.service;

import emailapp.database.DatabaseManager;
import emailapp.model.*;

import java.util.*;
import java.util.stream.Collectors;

public class EmailService {
    private final DatabaseManager db;
    private static final String DEFAULT_COMPANY_SUFFIX = "aeycompany.com";
    private static final int DEFAULT_MAILBOX_CAPACITY = 500;
    private static final int DEFAULT_PASSWORD_LENGTH = 12;
    private final Random random = new Random();

    public EmailService() {
        this.db = DatabaseManager.getInstance();
    }

    /* ==================== EMPLOYEE + EMAIL CREATION ==================== */
    public int createEmployeeWithEmail(String firstName, String lastName, int departmentId, int orgId, Role role) {
        Employee emp = new Employee(orgId, firstName, lastName, departmentId, true);
        emp.setRole(role);
        int empId = db.addEmployee(emp);
        if (empId < 0) return -1;
        emp.setId(empId);

        String email = generateEmail(firstName, lastName, departmentId, orgId);
        String password = generateSecurePassword(orgId);

        EmailAccount account = new EmailAccount(empId, email, password);
        account.setOrganizationId(orgId);
        int acctId = db.addEmailAccount(account);
        if (acctId < 0) return -1;

        db.addAuditLog(0, "CREATE", "EMAIL_ACCOUNT", acctId, "Created email " + email + " for " + firstName + " " + lastName);
        return acctId;
    }

    public String generateEmail(String firstName, String lastName, int departmentId, int orgId) {
        Department dept = db.getDepartmentById(departmentId);
        Organization org = db.getOrganizationById(orgId);
        String domain;
        if (dept != null) domain = dept.getEmailDomain();
        else if (org != null && org.getEmailSuffix() != null) domain = org.getEmailSuffix();
        else domain = DEFAULT_COMPANY_SUFFIX;
        return firstName.toLowerCase() + "." + lastName.toLowerCase() + "@" + domain;
    }

    public String generateSecurePassword(int orgId) {
        PasswordPolicy policy = db.getPasswordPolicy(orgId);
        int length = Math.max(policy.getMinLength(), DEFAULT_PASSWORD_LENGTH);
        StringBuilder pool = new StringBuilder();
        if (policy.isRequireLowercase()) pool.append("abcdefghijklmnopqrstuvwxyz");
        if (policy.isRequireUppercase()) pool.append("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
        if (policy.isRequireDigits()) pool.append("0123456789");
        if (policy.isRequireSpecialChars()) pool.append("!@#$%^&*");
        if (pool.length() == 0) pool.append("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789");

        StringBuilder pwd = new StringBuilder(length);
        for (int i = 0; i < length; i++) pwd.append(pool.charAt(random.nextInt(pool.length())));
        return pwd.toString();
    }

    public boolean validatePassword(String password, int orgId) {
        PasswordPolicy p = db.getPasswordPolicy(orgId);
        if (password == null || password.length() < p.getMinLength() || password.length() > p.getMaxLength()) return false;
        if (p.isRequireUppercase() && !password.matches(".*[A-Z].*")) return false;
        if (p.isRequireLowercase() && !password.matches(".*[a-z].*")) return false;
        if (p.isRequireDigits() && !password.matches(".*\\d.*")) return false;
        if (p.isRequireSpecialChars() && !password.matches(".*[!@#$%^&*].*")) return false;
        return true;
    }

    /* ==================== QUERIES ==================== */
    public List<Employee> getAllEmployees() { return db.getAllEmployees(); }
    public List<EmailAccount> getAllEmailAccounts() { return db.getAllEmailAccounts(); }
    public List<Department> getAllDepartments() { return db.getAllDepartments(); }
    public List<Organization> getAllOrganizations() { return db.getAllOrganizations(); }
    public List<EmailGroup> getAllEmailGroups() { return db.getAllEmailGroups(); }
    public List<AuditLog> getAllAuditLogs() { return db.getAllAuditLogs(); }

    public Employee getEmployeeById(int id) { return db.getEmployeeById(id); }
    public EmailAccount getEmailAccountById(int id) { return db.getEmailAccountById(id); }
    public EmailAccount getEmailAccountByEmail(String email) { return db.getEmailAccountByEmail(email); }
    public Department getDepartmentById(int id) { return db.getDepartmentById(id); }
    public Organization getOrganizationById(int id) { return db.getOrganizationById(id); }

    public List<EmailAccount> getEmailsByEmployee(int employeeId) {
        return db.getAllEmailAccounts().stream()
                .filter(a -> a.getEmployeeId() == employeeId)
                .collect(Collectors.toList());
    }

    public List<Employee> getGroupMembers(int groupId) {
        List<Integer> ids = db.getGroupMemberIds(groupId);
        List<Employee> members = new ArrayList<>();
        for (int id : ids) {
            Employee e = db.getEmployeeById(id);
            if (e != null) members.add(e);
        }
        return members;
    }

    /* ==================== MUTATIONS ==================== */
    public boolean updateEmployee(Employee emp) {
        boolean ok = db.updateEmployee(emp);
        if (ok) db.addAuditLog(0, "UPDATE", "EMPLOYEE", emp.getId(), "Updated employee " + emp.getFullName());
        return ok;
    }

    public boolean updateEmailAccount(EmailAccount account) {
        boolean ok = db.updateEmailAccount(account);
        if (ok) db.addAuditLog(0, "UPDATE", "EMAIL_ACCOUNT", account.getId(), "Updated email " + account.getEmailAddress());
        return ok;
    }

    public boolean deleteEmployee(int id) {
        Employee e = db.getEmployeeById(id);
        boolean ok = db.deleteEmployee(id);
        if (ok && e != null) db.addAuditLog(0, "DELETE", "EMPLOYEE", id, "Deactivated employee " + e.getFullName());
        return ok;
    }

    public boolean deleteEmailAccount(int id) {
        EmailAccount a = db.getEmailAccountById(id);
        boolean ok = db.deleteEmailAccount(id);
        if (ok && a != null) db.addAuditLog(0, "DELETE", "EMAIL_ACCOUNT", id, "Deactivated email " + a.getEmailAddress());
        return ok;
    }

    public boolean changePassword(int accountId, String newPassword, int orgId) {
        if (!validatePassword(newPassword, orgId)) return false;
        boolean ok = db.changePassword(accountId, newPassword);
        if (ok) db.addAuditLog(0, "PASSWORD_CHANGE", "EMAIL_ACCOUNT", accountId, "Password changed");
        return ok;
    }

    public boolean setMailboxCapacity(int accountId, int capacity) {
        boolean ok = db.setMailboxCapacity(accountId, capacity);
        if (ok) db.addAuditLog(0, "CAPACITY_CHANGE", "EMAIL_ACCOUNT", accountId, "Mailbox set to " + capacity + "mb");
        return ok;
    }

    public boolean setAlternateEmail(int accountId, String alternateEmail) {
        return db.setAlternateEmail(accountId, alternateEmail);
    }

    public int addOrganization(Organization org) {
        int id = db.addOrganization(org);
        if (id > 0) db.addAuditLog(0, "CREATE", "ORGANIZATION", id, "Created organization " + org.getName());
        return id;
    }

    public int addDepartment(Department dept) {
        int id = db.addDepartment(dept);
        if (id > 0) db.addAuditLog(0, "CREATE", "DEPARTMENT", id, "Created department " + dept.getName());
        return id;
    }

    public int addEmailGroup(EmailGroup group) {
        int id = db.addEmailGroup(group);
        if (id > 0) db.addAuditLog(0, "CREATE", "EMAIL_GROUP", id, "Created group " + group.getName());
        return id;
    }

    public boolean addGroupMember(int groupId, int employeeId) {
        boolean ok = db.addGroupMember(groupId, employeeId);
        if (ok) db.addAuditLog(0, "ADD_MEMBER", "EMAIL_GROUP", groupId, "Added employee #" + employeeId);
        return ok;
    }

    public PasswordPolicy getPasswordPolicy(int orgId) { return db.getPasswordPolicy(orgId); }

    /* ==================== SEARCH ==================== */
    public List<Employee> searchEmployees(String query) {
        String q = query.toLowerCase();
        return db.getAllEmployees().stream()
                .filter(e -> e.getFullName().toLowerCase().contains(q)
                        || String.valueOf(e.getId()).contains(q)
                        || e.getRole().name().toLowerCase().contains(q))
                .collect(Collectors.toList());
    }

    public List<EmailAccount> searchEmails(String query) {
        String q = query.toLowerCase();
        return db.getAllEmailAccounts().stream()
                .filter(a -> a.getEmailAddress().toLowerCase().contains(q)
                        || String.valueOf(a.getId()).contains(q))
                .collect(Collectors.toList());
    }
}
