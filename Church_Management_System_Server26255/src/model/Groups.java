package model;

import javax.persistence.*;
// import jakarta.persistence.OneToMany;
// import java.util.Set;

@Entity
@Table(name = "groups") // Assuming table name
public class Groups {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "group_id")
    private int groupId;

    @Column(name = "group_name", nullable = false, length = 100)
    private String groupName;

    @Column(name = "description", length = 500)
    private String description;

    // If Member.groupId refers to this, then Groups would have a OneToMany to Member
    // @OneToMany(mappedBy = "groupId") // This assumes 'groupId' field in Member is mapped by a proper FK relationship
    // private Set<Member> members;

    // Constructors
    public Groups() {}

    public Groups(String groupName, String description) {
        this.groupName = groupName;
        this.description = description;
    }

    // Getters and Setters
    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    // public Set<Member> getMembers() {
    //     return members;
    // }

    // public void setMembers(Set<Member> members) {
    //     this.members = members;
    // }
}
