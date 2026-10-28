package emailapp.model;

public class EmailGroup {
    private int id;
    private int organizationId;
    private String name;
    private String emailAddress;
    private String description;

    public EmailGroup(int id, int organizationId, String name, String emailAddress, String description) {
        this.id = id;
        this.organizationId = organizationId;
        this.name = name;
        this.emailAddress = emailAddress;
        this.description = description;
    }

    public EmailGroup(int organizationId, String name, String emailAddress, String description) {
        this(-1, organizationId, name, emailAddress, description);
    }

    public int getId() { return id; }
    public int getOrganizationId() { return organizationId; }
    public String getName() { return name; }
    public String getEmailAddress() { return emailAddress; }
    public String getDescription() { return description; }

    public void setId(int id) { this.id = id; }
    public void setOrganizationId(int organizationId) { this.organizationId = organizationId; }
    public void setName(String name) { this.name = name; }
    public void setEmailAddress(String emailAddress) { this.emailAddress = emailAddress; }
    public void setDescription(String description) { this.description = description; }

    @Override
    public String toString() {
        return name + " <" + emailAddress + ">";
    }
}
