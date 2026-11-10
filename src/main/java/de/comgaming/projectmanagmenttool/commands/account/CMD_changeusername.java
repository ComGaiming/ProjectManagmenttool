package de.comgaming.projectmanagmenttool.commands.account;

import de.comgaming.projectmanagmenttool.usermanagment.Account;
import de.comgaming.projectmanagmenttool.usermanagment.AccountManager;
import dev.comgaming.framework.Framework;

import java.util.Locale;
import java.util.Optional;

public class CMD_changeusername {

    private static final AccountManager accountManager = new AccountManager();

    public static void onCommand(String[] args) {
        if (args == null || args.length == 0) {
            sendHelp();
            return;
        }

        Long accountId = null;
        String username = null;
        boolean verbose = false;

        for (int i = 0; i < args.length; i++) {
            String argument = args[i].toLowerCase(Locale.ROOT);

            switch (argument) {
                case "--id", "-id" -> {
                    if (i + 1 >= args.length) {
                        error("--id benötigt eine AccountID.");
                        return;
                    }

                    try {
                        accountId = Long.parseLong(args[++i]);
                    } catch (NumberFormatException e) {
                        error("Die AccountID muss eine Zahl sein.");
                        return;
                    }
                }

                case "--username", "-u" -> {
                    if (i + 1 >= args.length) {
                        error("--username benötigt einen Username.");
                        return;
                    }

                    username = args[++i];
                }

                case "--verbose", "-v" -> verbose = true;

                case "--help", "-h" -> {
                    sendHelp();
                    return;
                }

                default -> {
                    error("Unbekanntes Argument: " + args[i]);
                    sendHelp();
                    return;
                }
            }
        }

        if (accountId == null) {
            error("Bitte --id <AccountID> angeben.");
            return;
        }

        if (username == null || username.isBlank()) {
            error("Bitte --username <Username> angeben.");
            return;
        }

        if (verbose) {
            info("Ändere Username...");
            info("AccountID: " + accountId);
            info("Neuer Username: " + username);
        }

        Optional<Account> account = accountManager.findById(accountId);

        if (account.isEmpty()) {
            error("Account mit der ID " + accountId + " wurde nicht gefunden.");
            return;
        }

        Optional<Account> existingAccount = accountManager.findByUsername(username);

        if (existingAccount.isPresent() && !existingAccount.get().getId().equals(accountId)) {
            error("Username '" + username + "' ist bereits vergeben.");
            return;
        }

        String oldUsername = account.get().getUsername();

        if (oldUsername.equals(username)) {
            error("Der Account besitzt bereits den Username '" + username + "'.");
            return;
        }

        boolean changed;

        try {
            changed = accountManager.changeUsername(accountId, username);
        } catch (IllegalArgumentException e) {
            error(e.getMessage());
            return;
        }

        if (!changed) {
            error("Username konnte nicht geändert werden.");
            return;
        }

        info("Username erfolgreich geändert.");
        info("AccountID: " + accountId);
        info("Alter Username: " + oldUsername);
        info("Neuer Username: " + username);

        if (verbose) {
            info("Änderung erfolgreich in der Datenbank gespeichert.");
        }
    }

    private static void sendHelp() {
        info("""
                
                Verwendung:
                
                  changeusername --id <AccountID> --username <Username>
                
                Optionen:
                
                  --id <ID>             Account über ID auswählen
                  --username <Name>     Neuer Username
                  --verbose             Ausführliche Ausgabe
                  --help                Hilfe anzeigen
                
                Kurzformen:
                
                  -id <ID>
                  -u <Username>
                  -v
                  -h
                
                Beispiele:
                
                  changeusername --id 15 --username Max
                  changeusername --id 15 --username Max --verbose
                """);
    }

    private static void info(String message) {
        Framework.getLogger().info("console", message);
    }

    private static void error(String message) {
        Framework.getLogger().error("console", message);
    }
}
