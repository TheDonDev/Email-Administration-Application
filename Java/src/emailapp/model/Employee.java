package emailapp.model;

public class Employee {
    private int id;
    private int organizationId;
    private String firstName;
    private String lastName;
    private int departmentId;
    private Role role;
    private boolean isActive;

    public Employee(int id, String firstName, String lastName, int departmentId, boolean isActive) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.departmentId = departmentId;
        this.isActive = isActive;
        this.role = Role.EMPLOYEE;
    }

    public Employee(int id, int organizationId, String firstName, String lastName, int departmentId, boolean isActive) {
        this(id, firstName, lastName, departmentId, isActive);
        this.organizationId = organizationId;
    }

    public Employee(String firstName, String lastName, int departmentId) {
        this(-1, firstName, lastName, departmentId, true);
    }

    public int getId() { return id; }
    public int getOrganizationId() { return organizationId; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public int getDepartmentId() { return departmentId; }
    public Role getRole() { return role; }
    public boolean isActive() { return isActive; }

    public void setId(int id) { this.id = id; }
    public void setOrganizationId(int organizationId) { this.organizationId = organizationId; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public void setDepartmentId(int departmentId) { this.departmentId = departmentId; }
    public void setRole(Role role) { this.role = role; }
    public void setActive(boolean active) { isActive = active; }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    @Override
    public String toString() {
        return getFullName();
    }
}
