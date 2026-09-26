package de.comgaming.projectmanagmenttool.usermanagment;

public class Group {

    private Long id;
    private String groupname;

    public Group() {
    }

    public Group(Long id, String groupname) {
        this.id = id;
        this.groupname = groupname;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getGroupname() {
        return groupname;
    }

    public void setGroupname(String groupname) {
        this.groupname = groupname;
    }
}