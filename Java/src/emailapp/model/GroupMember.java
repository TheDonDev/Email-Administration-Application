package emailapp.model;

public class GroupMember {
    private int id;
    private int groupId;
    private int employeeId;

    public GroupMember(int id, int groupId, int employeeId) {
        this.id = id;
        this.groupId = groupId;
        this.employeeId = employeeId;
    }

    public GroupMember(int groupId, int employeeId) {
        this(-1, groupId, employeeId);
    }

    public int getId() { return id; }
    public int getGroupId() { return groupId; }
    public int getEmployeeId() { return employeeId; }

    public void setId(int id) { this.id = id; }
    public void setGroupId(int groupId) { this.groupId = groupId; }
    public void setEmployeeId(int employeeId) { this.employeeId = employeeId; }
}
