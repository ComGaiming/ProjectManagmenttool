package de.comgaming.projectmanagmenttool.commands.account;

import de.comgaming.projectmanagmenttool.usermanagment.Account;
import de.comgaming.projectmanagmenttool.usermanagment.AccountManager;
import de.comgaming.projectmanagmenttool.usermanagment.Group;
import de.comgaming.projectmanagmenttool.usermanagment.GroupManager;
import dev.comgaming.framework.Framework;

import java.util.Optional;
import java.util.Scanner;

public class CMD_AddAccount {

    public static void onCommand() {
        AccountManager accountManager = new AccountManager();
        GroupManager groupManager = new GroupManager();
        Scanner scanner = new Scanner(System.in);

        Framework.getLogger().info("account", "Creating new account...");

        Framework.getLogger().info("account", "Username:");
        String username = scanner.nextLine().trim();

        if (username.isBlank()) {
            Framework.getLogger().info("account", "Username darf nicht leer sein.");
            return;
        }

        if (accountManager.findByUsername(username).isPresent()) {
            Framework.getLogger().info("account", "Username ist bereits vergeben.");
            return;
        }

        Framework.getLogger().info("account", "E-Mail:");
        String email = scanner.nextLine().trim();

        if (email.isBlank()) {
            Framework.getLogger().info("account", "E-Mail-Adresse darf nicht leer sein.");
            return;
        }

        if (accountManager.findByEmail(email).isPresent()) {
            Framework.getLogger().info("account", "E-Mail-Adresse ist bereits vergeben.");
            return;
        }

        Framework.getLogger().info("account", "Gruppe:");
        String groupname = scanner.nextLine().trim();

        if (groupname.isBlank()) {
            Framework.getLogger().info("account", "Gruppe darf nicht leer sein.");
            return;
        }

        Optional<Group> groupOptional = groupManager.findByGroupname(groupname);

        if (groupOptional.isEmpty()) {
            Framework.getLogger().info("account", "Gruppe '" + groupname + "' wurde nicht gefunden.");
            return;
        }

        Group group = groupOptional.get();

        try {
            Account account = accountManager.createAccount(username, email, group.getId());

            Framework.getLogger().info("account", "Account erfolgreich erstellt.");
            Framework.getLogger().info("account", "ID: " + account.getId());
            Framework.getLogger().info("account", "Username: " + account.getUsername());
            Framework.getLogger().info("account", "E-Mail: " + account.getEmail());
            Framework.getLogger().info("account", "Gruppe: " + group.getGroupname());
            Framework.getLogger().info("account", "Registriert: " + account.getRegistDate());
            Framework.getLogger().info("account", "Ein zufälliges Passwort wurde generiert und per E-Mail versendet.");

        } catch (IllegalArgumentException e) {
            Framework.getLogger().info("account", e.getMessage());
        } catch (Exception e) {
            Framework.getLogger().error("account", "Fehler beim Erstellen des Accounts: " + e.getMessage());
        }
    }
}