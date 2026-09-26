package de.comgaming.projectmanagmenttool.usermanagment;

import java.util.Date;

public class Account {

    private Long id;
    private String username;
    private String email;
    private String password;
    private Date registDate;
    private Date lastLoginDate;
    private Long groupid;
    private boolean active;

    public Account() {
    }

    public Account(
            Long id,
            String username,
            String email,
            String password,
            Date registDate,
            Date lastLoginDate,
            Long groupid,
            boolean active
    ) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.registDate = registDate;
        this.lastLoginDate = lastLoginDate;
        this.groupid = groupid;
        this.active = active;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Date getRegistDate() {
        return registDate;
    }

    public void setRegistDate(Date registDate) {
        this.registDate = registDate;
    }

    public Date getLastLoginDate() {
        return lastLoginDate;
    }

    public void setLastLoginDate(Date lastLoginDate) {
        this.lastLoginDate = lastLoginDate;
    }

    public Long getGroupid() {
        return groupid;
    }

    public void setGroupid(Long groupid) {
        this.groupid = groupid;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
