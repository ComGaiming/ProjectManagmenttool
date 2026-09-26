package de.comgaming.projectmanagmenttool.restapi.dto;

import de.comgaming.projectmanagmenttool.usermanagment.Account;

import java.util.Date;

public record AccountResponse(
        Long id,
        String username,
        String email,
        Date registDate,
        Date lastLoginDate,
        Long groupId,
        boolean active
) {

    public static AccountResponse from(Account account) {

        return new AccountResponse(
                account.getId(),
                account.getUsername(),
                account.getEmail(),
                account.getRegistDate(),
                account.getLastLoginDate(),
                account.getGroupid(),
                account.isActive()
        );
    }
}
