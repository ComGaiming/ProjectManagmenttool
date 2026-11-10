package de.comgaming.projectmanagmenttool.restapi;

import de.comgaming.projectmanagmenttool.usermanagment.Account;
import de.comgaming.projectmanagmenttool.usermanagment.AccountManager;
import de.comgaming.projectmanagmenttool.usermanagment.PermissionManager;

public class PermissionService {

    private final AccountManager accountManager;
    private final PermissionManager permissionManager;

    public PermissionService() {
        this.accountManager = new AccountManager();
        this.permissionManager = new PermissionManager();
    }

    public boolean hasPermission(Long accountId, String permission) {
        if (accountId == null || permission == null || permission.isBlank()) {
            return false;
        }

        Account account = accountManager.findById(accountId).orElse(null);

        if (account == null || !account.isActive()) {
            return false;
        }

        return permissionManager.hasPermission(account.getGroupid(), permission);
    }

}