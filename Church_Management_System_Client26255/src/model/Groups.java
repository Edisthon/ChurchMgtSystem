package model;

public class Groups {
    private int groupId;
    private String groupName;
    private String description;

    public Groups() {}

    // Getters and Setters
    public int getGroupId() { return groupId; }
    public void setGroupId(int groupId) { this.groupId = groupId; }
    public String getGroupName() { return groupName; }
    public void setGroupName(String groupName) { this.groupName = groupName; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
     @Override
    public String toString() { // For JComboBox or simple display
        return groupName + " (ID: " + groupId + ")";
    }
}
