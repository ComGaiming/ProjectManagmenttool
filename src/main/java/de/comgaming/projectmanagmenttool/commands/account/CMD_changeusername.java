package de.comgaming.projectmanagmenttool.usermanagment.commands;

import de.comgaming.projectmanagmenttool.usermanagment.Account;
import de.comgaming.projectmanagmenttool.usermanagment.AccountManager;

import java.util.Optional;

public class CMD_changeusername {

    private final AccountManager accountManager;

    public CMD_changeusername() {
        this.accountManager = new AccountManager();
    }

    public boolean execute(Long accountId, String username) {

        if (accountId == null) {
            throw new IllegalArgumentException("AccountID darf nicht null sein.");
        }

        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username darf nicht leer sein.");
        }

        Optional<Account> existingAccount =
                accountManager.findByUsername(username);

        if (existingAccount.isPresent()
                && !existingAccount.get().getId().equals(accountId)) {
            throw new IllegalArgumentException("Username ist bereits vergeben.");
        }

        return accountManager.changeUsername(accountId, username);
    }
}
