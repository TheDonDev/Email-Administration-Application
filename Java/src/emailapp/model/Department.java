package emailapp.model;

public class Department {
    private int id;
    private String name;
    private String code;
    private String emailDomain;

    public Department(int id, String name, String code, String emailDomain) {
        this.id = id;
        this.name = name;
        this.code = code;
        this.emailDomain = emailDomain;
    }

    public Department(String name, String code, String emailDomain) {
        this(-1, name, code, emailDomain);
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getCode() { return code; }
    public String getEmailDomain() { return emailDomain; }

    public void setId(int id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setCode(String code) { this.code = code; }
    public void setEmailDomain(String emailDomain) { this.emailDomain = emailDomain; }

    @Override
    public String toString() {
        return name + " (" + code + ")";
    }
}
