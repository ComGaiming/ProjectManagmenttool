package de.comgaming.projectmanagmenttool.usermanagment;

public class Permission {

    private Long id;
    private String permissionname;

    public Permission() {}

    public Permission(Long id, String permissionname) {
        this.id = id;
        this.permissionname = permissionname;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPermissionname() {
        return permissionname;
    }

    public void setPermissionname(String permissionname) {
        this.permissionname = permissionname;
    }
}
