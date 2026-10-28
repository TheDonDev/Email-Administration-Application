package emailapp.model;

public class PasswordPolicy {
    private int id;
    private int organizationId;
    private int minLength;
    private int maxLength;
    private boolean requireUppercase;
    private boolean requireLowercase;
    private boolean requireDigits;
    private boolean requireSpecialChars;
    private int expirationDays;
    private int historyCount;

    public PasswordPolicy(int id, int organizationId, int minLength, int maxLength,
                          boolean requireUppercase, boolean requireLowercase,
                          boolean requireDigits, boolean requireSpecialChars,
                          int expirationDays, int historyCount) {
        this.id = id;
        this.organizationId = organizationId;
        this.minLength = minLength;
        this.maxLength = maxLength;
        this.requireUppercase = requireUppercase;
        this.requireLowercase = requireLowercase;
        this.requireDigits = requireDigits;
        this.requireSpecialChars = requireSpecialChars;
        this.expirationDays = expirationDays;
        this.historyCount = historyCount;
    }

    public PasswordPolicy(int organizationId) {
        this(-1, organizationId, 8, 128, true, true, true, true, 90, 5);
    }

    public int getId() { return id; }
    public int getOrganizationId() { return organizationId; }
    public int getMinLength() { return minLength; }
    public int getMaxLength() { return maxLength; }
    public boolean isRequireUppercase() { return requireUppercase; }
    public boolean isRequireLowercase() { return requireLowercase; }
    public boolean isRequireDigits() { return requireDigits; }
    public boolean isRequireSpecialChars() { return requireSpecialChars; }
    public int getExpirationDays() { return expirationDays; }
    public int getHistoryCount() { return historyCount; }

    public void setId(int id) { this.id = id; }
    public void setOrganizationId(int organizationId) { this.organizationId = organizationId; }
    public void setMinLength(int minLength) { this.minLength = minLength; }
    public void setMaxLength(int maxLength) { this.maxLength = maxLength; }
    public void setRequireUppercase(boolean requireUppercase) { this.requireUppercase = requireUppercase; }
    public void setRequireLowercase(boolean requireLowercase) { this.requireLowercase = requireLowercase; }
    public void setRequireDigits(boolean requireDigits) { this.requireDigits = requireDigits; }
    public void setRequireSpecialChars(boolean requireSpecialChars) { this.requireSpecialChars = requireSpecialChars; }
    public void setExpirationDays(int expirationDays) { this.expirationDays = expirationDays; }
    public void setHistoryCount(int historyCount) { this.historyCount = historyCount; }
}
