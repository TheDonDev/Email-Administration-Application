package emailapp.model;

public class EmailAccount {
    private int id;
    private int employeeId;
    private int organizationId;
    private String emailAddress;
    private String password;
    private int mailboxCapacity;
    private String alternateEmail;
    private boolean isActive;

    public EmailAccount(int id, int employeeId, String emailAddress, String password,
                        int mailboxCapacity, String alternateEmail, boolean isActive) {
        this.id = id;
        this.employeeId = employeeId;
        this.emailAddress = emailAddress;
        this.password = password;
        this.mailboxCapacity = mailboxCapacity;
        this.alternateEmail = alternateEmail;
        this.isActive = isActive;
    }

    public EmailAccount(int employeeId, String emailAddress, String password) {
        this(-1, employeeId, emailAddress, password, 500, null, true);
    }

    public int getId() { return id; }
    public int getEmployeeId() { return employeeId; }
    public int getOrganizationId() { return organizationId; }
    public String getEmailAddress() { return emailAddress; }
    public String getPassword() { return password; }
    public int getMailboxCapacity() { return mailboxCapacity; }
    public String getAlternateEmail() { return alternateEmail; }
    public boolean isActive() { return isActive; }

    public void setId(int id) { this.id = id; }
    public void setEmployeeId(int employeeId) { this.employeeId = employeeId; }
    public void setOrganizationId(int organizationId) { this.organizationId = organizationId; }
    public void setEmailAddress(String emailAddress) { this.emailAddress = emailAddress; }
    public void setPassword(String password) { this.password = password; }
    public void setMailboxCapacity(int mailboxCapacity) { this.mailboxCapacity = mailboxCapacity; }
    public void setAlternateEmail(String alternateEmail) { this.alternateEmail = alternateEmail; }
    public void setActive(boolean active) { isActive = active; }

    @Override
    public String toString() {
        return emailAddress;
    }
}
