package de.comgaming.projectmanagmenttool.commands.account;

import de.comgaming.projectmanagmenttool.usermanagment.Account;
import de.comgaming.projectmanagmenttool.usermanagment.AccountManager;
import de.comgaming.projectmanagmenttool.usermanagment.Group;
import de.comgaming.projectmanagmenttool.usermanagment.GroupManager;
import dev.comgaming.framework.Framework;

import java.util.Locale;
import java.util.Optional;

public class CMD_AddAccount {

    private static final AccountManager accountManager = new AccountManager();
    private static final GroupManager groupManager = new GroupManager();

    public static void onCommand(String[] args) {
        if (args == null || args.length == 0) {
            sendHelp();
            return;
        }

        String username = null;
        String email = null;
        String group = null;
        boolean active = true;
        boolean verbose = false;

        for (int i = 0; i < args.length; i++) {
            switch (args[i].toLowerCase(Locale.ROOT)) {
                case "--username", "-u" -> {
                    if (i + 1 >= args.length) {
                        error("--username benötigt einen Username.");
                        return;
                    }
                    username = args[++i];
                }

                case "--email", "-e" -> {
                    if (i + 1 >= args.length) {
                        error("--email benötigt eine E-Mail-Adresse.");
                        return;
                    }
                    email = args[++i];
                }

                case "--group", "-g" -> {
                    if (i + 1 >= args.length) {
                        error("--group benötigt einen Gruppennamen.");
                        return;
                    }
                    group = args[++i];
                }

                case "--inactive", "-i" -> active = false;

                case "--active", "-a" -> active = true;

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

        if (username == null || username.isBlank()) {
            error("Bitte --username <Username> angeben.");
            return;
        }

        if (email == null || email.isBlank()) {
            error("Bitte --email <E-Mail> angeben.");
            return;
        }

        if (group == null || group.isBlank()) {
            error("Bitte --group <Gruppenname> angeben.");
            return;
        }

        if (accountManager.findByUsername(username).isPresent()) {
            error("Username ist bereits vergeben.");
            return;
        }

        if (accountManager.findByEmail(email).isPresent()) {
            error("E-Mail-Adresse ist bereits vergeben.");
            return;
        }

        Optional<Group> groupOptional = groupManager.findByGroupname(group);

        if (groupOptional.isEmpty()) {
            error("Gruppe '" + group + "' wurde nicht gefunden.");
            return;
        }

        Group selectedGroup = groupOptional.get();

        if (verbose) {
            info("Erstelle neuen Account...");
            info("Username: " + username);
            info("E-Mail: " + email);
            info("Gruppe: " + selectedGroup.getGroupname());
            info("Status: " + (active ? "Aktiv" : "Inaktiv"));
        }

        try {
            Account account = accountManager.createAccount(username, email, selectedGroup.getId());

            if (!active) {
                accountManager.deactivate(account.getId());
                account.setActive(false);
            }

            info("Account erfolgreich erstellt.");
            info("ID: " + account.getId());
            info("Username: " + account.getUsername());
            info("E-Mail: " + account.getEmail());
            info("Gruppe: " + selectedGroup.getGroupname());
            info("Status: " + (account.isActive() ? "Aktiv" : "Inaktiv"));
            info("Registriert: " + account.getRegistDate());
            info("Ein zufälliges Passwort wurde generiert und per E-Mail versendet.");

            if (verbose) {
                info("Account-Erstellung erfolgreich abgeschlossen.");
            }

        } catch (IllegalArgumentException e) {
            error(e.getMessage());
        } catch (Exception e) {
            error("Fehler beim Erstellen des Accounts: " + e.getMessage());
        }
    }

    private static void sendHelp() {
        info("""
            
            Verwendung:
            
              addaccount --username <Username> --email <E-Mail> --group <Gruppe>
            
            Optionen:
            
              --username <Name>     Username des Accounts
              --email <E-Mail>      E-Mail-Adresse des Accounts
              --group <Gruppe>      Gruppe des Accounts
              --active              Account aktiv erstellen
              --inactive            Account deaktiviert erstellen
              --verbose             Ausführliche Ausgabe
              --help                Hilfe anzeigen
            
            Kurzformen:
            
              -u <Username>
              -e <E-Mail>
              -g <Gruppe>
              -a
              -i
              -v
              -h
            
            Beispiele:
            
              addaccount --username Max --email max@example.com --group Admin
              addaccount -u Max -e max@example.com -g User
              addaccount -u Max -e max@example.com -g User --inactive
              addaccount -u Max -e max@example.com -g Admin --verbose
            """);
    }

    private static void info(String message) {
        Framework.getLogger().info("account", message);
    }

    private static void error(String message) {
        Framework.getLogger().error("account", message);
    }


}