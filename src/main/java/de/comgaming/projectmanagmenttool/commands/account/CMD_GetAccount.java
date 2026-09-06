package de.comgaming.projectmanagmenttool.commands.account;

import de.comgaming.projectmanagmenttool.usermanagment.Account;
import de.comgaming.projectmanagmenttool.usermanagment.AccountManager;
import dev.comgaming.framework.Framework;

import java.util.List;
import java.util.Optional;

public class CMD_GetAccount {

    private static final AccountManager accountManager = new AccountManager();

    public static void onCommand(String[] args) {
        if (args.length == 0) {
            sendUsage();
            return;
        }

        String type = args[0].toLowerCase();

        switch (type) {
            case "all" -> getAllAccounts();

            case "userid", "id" -> {
                if (args.length < 2) {
                    Framework.getLogger().info("console", "Benutzung: getaccount userid <userid>");
                    return;
                }
                getAccountById(args[1]);
            }

            case "username", "user" -> {
                if (args.length < 2) {
                    Framework.getLogger().info("console", "Benutzung: getaccount username <username>");
                    return;
                }
                getAccountByUsername(args[1]);
            }

            case "email", "mail" -> {
                if (args.length < 2) {
                    Framework.getLogger().info("console", "Benutzung: getaccount email <email>");
                    return;
                }
                getAccountByEmail(args[1]);
            }

            default -> sendUsage();
        }
    }

    private static void getAllAccounts() {
        List<Account> accounts = accountManager.findAll();

        if (accounts.isEmpty()) {
            Framework.getLogger().info("console", "Es wurden keine Accounts gefunden.");
            return;
        }

        Framework.getLogger().info("console", "===== Alle Accounts (" + accounts.size() + ") =====");

        for (Account account : accounts) {
            printAccount(account);
        }

        Framework.getLogger().info("console", "=================================");
    }

    private static void getAccountById(String id) {
        Long userId;

        try {
            userId = Long.parseLong(id);
        } catch (NumberFormatException e) {
            Framework.getLogger().info("console", "Die UserID muss eine Zahl sein.");
            return;
        }

        Optional<Account> account = accountManager.findById(userId);

        if (account.isEmpty()) {
            Framework.getLogger().info("console", "Kein Account mit der UserID " + userId + " gefunden.");
            return;
        }

        printAccount(account.get());
    }

    private static void getAccountByUsername(String username) {
        Optional<Account> account = accountManager.findByUsername(username);

        if (account.isEmpty()) {
            Framework.getLogger().info("console", "Kein Account mit dem Username '" + username + "' gefunden.");
            return;
        }

        printAccount(account.get());
    }

    private static void getAccountByEmail(String email) {
        Optional<Account> account = accountManager.findByEmail(email);

        if (account.isEmpty()) {
            Framework.getLogger().info("console", "Kein Account mit der E-Mail '" + email + "' gefunden.");
            return;
        }

        printAccount(account.get());
    }

    private static void printAccount(Account account) {
        Framework.getLogger().info("console", "---------------------------------");
        Framework.getLogger().info("console", "UserID: " + account.getId());
        Framework.getLogger().info("console", "Username: " + account.getUsername());
        Framework.getLogger().info("console", "E-Mail: " + account.getEmail());
        Framework.getLogger().info("console", "Registriert: " + account.getRegistDate());
        Framework.getLogger().info("console", "Letzter Login: " + account.getLastLoginDate());
        Framework.getLogger().info("console", "GroupID: " + account.getGroupid());
        Framework.getLogger().info("console", "---------------------------------");
    }

    private static void sendUsage() {
        Framework.getLogger().info("console", "Benutzung:");
        Framework.getLogger().info("console", "getaccount all");
        Framework.getLogger().info("console", "getaccount userid <userid>");
        Framework.getLogger().info("console", "getaccount username <username>");
        Framework.getLogger().info("console", "getaccount email <email>");
    }
}
