package emailapp.model;

public class Organization {
    private int id;
    private String name;
    private String code;
    private String country;
    private String emailSuffix;
    private boolean isActive;

    public Organization(int id, String name, String code, String country, String emailSuffix, boolean isActive) {
        this.id = id;
        this.name = name;
        this.code = code;
        this.country = country;
        this.emailSuffix = emailSuffix;
        this.isActive = isActive;
    }

    public Organization(String name, String code, String country, String emailSuffix) {
        this(-1, name, code, country, emailSuffix, true);
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getCode() { return code; }
    public String getCountry() { return country; }
    public String getEmailSuffix() { return emailSuffix; }
    public boolean isActive() { return isActive; }

    public void setId(int id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setCode(String code) { this.code = code; }
    public void setCountry(String country) { this.country = country; }
    public void setEmailSuffix(String emailSuffix) { this.emailSuffix = emailSuffix; }
    public void setActive(boolean active) { isActive = active; }

    @Override
    public String toString() {
        return name + " (" + country + ")";
    }
}
