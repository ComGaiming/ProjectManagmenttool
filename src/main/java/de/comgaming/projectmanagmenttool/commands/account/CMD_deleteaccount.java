package de.comgaming.projectmanagmenttool.commands.account;

import de.comgaming.projectmanagmenttool.usermanagment.Account;
import de.comgaming.projectmanagmenttool.usermanagment.AccountManager;
import dev.comgaming.framework.Framework;

import java.util.Locale;
import java.util.Optional;

public class CMD_deleteaccount {

    private static final AccountManager accountManager = new AccountManager();

    public static void onCommand(String[] args) {
        if (args == null || args.length == 0) {
            sendHelp();
            return;
        }

        Long accountId = null;
        String username = null;
        String email = null;
        boolean verbose = false;
        boolean confirm = false;

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

                case "--email", "-e" -> {
                    if (i + 1 >= args.length) {
                        error("--email benötigt eine E-Mail-Adresse.");
                        return;
                    }

                    email = args[++i];
                }

                case "--verbose", "-v" -> verbose = true;

                case "--confirm", "-c" -> confirm = true;

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

        int searchArguments = 0;

        if (accountId != null) {
            searchArguments++;
        }

        if (username != null) {
            searchArguments++;
        }

        if (email != null) {
            searchArguments++;
        }

        if (searchArguments == 0) {
            error("Bitte --id, --username oder --email angeben.");
            return;
        }

        if (searchArguments > 1) {
            error("Es darf nur eine Suchmethode verwendet werden.");
            return;
        }

        if (!confirm) {
            error("Löschen wurde nicht bestätigt. Verwende --confirm.");
            return;
        }

        if (verbose) {
            info("Suche Account...");
        }

        Optional<Account> account;

        if (accountId != null) {
            if (verbose) {
                info("Suchmethode: ID " + accountId);
            }

            account = accountManager.findById(accountId);

        } else if (username != null) {
            if (verbose) {
                info("Suchmethode: Username " + username);
            }

            account = accountManager.findByUsername(username);

        } else {
            if (verbose) {
                info("Suchmethode: E-Mail " + email);
            }

            account = accountManager.findByEmail(email);
        }

        if (account.isEmpty()) {
            error("Account wurde nicht gefunden.");
            return;
        }

        Account foundAccount = account.get();

        if (verbose) {
            info("Account gefunden:");
            info("ID: " + foundAccount.getId());
            info("Username: " + foundAccount.getUsername());
            info("E-Mail: " + foundAccount.getEmail());
            info("Gruppe: " + foundAccount.getGroupid());
            info("Status: " + (foundAccount.isActive() ? "Aktiv" : "Deaktiviert"));
            info("Registriert: " + foundAccount.getRegistDate());
            info("Letzter Login: " + foundAccount.getLastLoginDate());
            info("Lösche Account...");
        }

        boolean deleted = accountManager.deleteById(foundAccount.getId());

        if (!deleted) {
            error("Account konnte nicht gelöscht werden.");
            return;
        }

        info("Account erfolgreich gelöscht: " + foundAccount.getUsername() + " (ID: " + foundAccount.getId() + ")");

        if (verbose) {
            info("Löschvorgang erfolgreich abgeschlossen.");
        }
    }

    private static void sendHelp() {
        info("""
                
                Verwendung:
                
                  deleteaccount --id <AccountID> --confirm
                  deleteaccount --username <Username> --confirm
                  deleteaccount --email <E-Mail> --confirm
                
                Optionen:
                
                  --id <ID>             Account über ID auswählen
                  --username <Name>     Account über Username auswählen
                  --email <E-Mail>      Account über E-Mail auswählen
                  --confirm             Löschung bestätigen
                  --verbose             Ausführliche Ausgabe
                  --help                Hilfe anzeigen
                
                Kurzformen:
                
                  -id <ID>
                  -u <Username>
                  -e <E-Mail>
                  -c
                  -v
                  -h
                
                Beispiele:
                
                  deleteaccount --id 15 --confirm
                  deleteaccount --id 15 --confirm --verbose
                  deleteaccount --username Max --confirm
                  deleteaccount --username Max --confirm --verbose
                  deleteaccount --email max@example.com --confirm -v
                """);
    }

    private static void info(String message) {
        Framework.getLogger().info("console", message);
    }

    private static void error(String message) {
        Framework.getLogger().error("console", message);
    }
}
