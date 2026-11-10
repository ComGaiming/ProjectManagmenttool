package de.comgaming.projectmanagmenttool.commands.account;

import de.comgaming.projectmanagmenttool.usermanagment.Account;
import de.comgaming.projectmanagmenttool.usermanagment.AccountManager;
import dev.comgaming.framework.Framework;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

public class CMD_GetAccount {

    private static final AccountManager accountManager = new AccountManager();

    public static void onCommand(String[] args) {
        if (args.length == 0) {
            sendUsage();
            return;
        }

        String type = args[0].toLowerCase(Locale.ROOT);

        switch (type) {
            case "all" -> handleAll(args);
            case "count" -> handleCount();
            case "active" -> handleActive(true, hasVerbose(args));
            case "inactive", "deactivated" -> handleActive(false, hasVerbose(args));

            case "userid", "id" -> {
                if (args.length < 2) {
                    usage("getaccount userid <userid> [verbose]");
                    return;
                }
                getAccountById(args[1], hasVerbose(args));
            }

            case "username", "user" -> {
                if (args.length < 2) {
                    usage("getaccount username <username> [verbose]");
                    return;
                }
                getAccountByUsername(args[1], hasVerbose(args));
            }

            case "email", "mail" -> {
                if (args.length < 2) {
                    usage("getaccount email <email> [verbose]");
                    return;
                }
                getAccountByEmail(args[1], hasVerbose(args));
            }

            case "group" -> {
                if (args.length < 2) {
                    usage("getaccount group <groupid> [verbose]");
                    return;
                }
                getAccountsByGroup(args[1], hasVerbose(args));
            }

            case "search", "find" -> {
                if (args.length < 2) {
                    usage("getaccount search <text> [verbose]");
                    return;
                }
                handleSearch(args[1], hasVerbose(args));
            }

            case "recent", "recentlogin" -> {
                int limit = getNumericArgument(args, 1, 10);
                handleRecentLogins(limit, hasVerbose(args));
            }

            case "neverloggedin", "neverlogin" -> handleNeverLoggedIn(hasVerbose(args));

            default -> sendUsage();
        }
    }

    private static void handleAll(String[] args) {
        boolean verbose = hasVerbose(args);
        Integer limit = getLimit(args);
        String sortField = getSortField(args);
        boolean descending = isDescending(args);

        if (verbose) {
            Framework.getLogger().info("console", "[VERBOSE] Lade alle Accounts...");
        }

        List<Account> accounts = accountManager.findAll();

        if (verbose) {
            Framework.getLogger().info("console", "[VERBOSE] " + accounts.size() + " Accounts gefunden.");
        }

        accounts = sortAccounts(accounts, sortField, descending);

        if (limit != null && limit < accounts.size()) {
            accounts = accounts.stream().limit(limit).collect(Collectors.toList());

            if (verbose) {
                Framework.getLogger().info("console", "[VERBOSE] Ergebnis auf " + limit + " Accounts begrenzt.");
            }
        }

        printAccounts(accounts, "Accounts", verbose);
    }

    private static void handleCount() {
        long total = accountManager.count();
        long active = accountManager.countActive();
        long inactive = accountManager.countInactive();

        Framework.getLogger().info("console", "===== Account Statistik =====");
        Framework.getLogger().info("console", "Gesamt: " + total);
        Framework.getLogger().info("console", "Aktiv: " + active);
        Framework.getLogger().info("console", "Deaktiviert: " + inactive);
        Framework.getLogger().info("console", "============================");
    }

    private static void handleActive(boolean active, boolean verbose) {
        if (verbose) {
            Framework.getLogger().info("console", "[VERBOSE] Suche " + (active ? "aktive" : "deaktivierte") + " Accounts...");
        }

        List<Account> accounts = active ? accountManager.findAllActive() : accountManager.findAllInactive();

        if (accounts.isEmpty()) {
            Framework.getLogger().info("console", "Keine " + (active ? "aktiven" : "deaktivierten") + " Accounts gefunden.");
            return;
        }

        printAccounts(accounts, active ? "Aktive Accounts" : "Deaktivierte Accounts", verbose);
    }

    private static void getAccountById(String id, boolean verbose) {
        if (verbose) {
            Framework.getLogger().info("console", "[VERBOSE] Suche Account mit UserID: " + id);
        }

        long userId;

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

        printAccount(account.get(), verbose);
    }

    private static void getAccountByUsername(String username, boolean verbose) {
        if (verbose) {
            Framework.getLogger().info("console", "[VERBOSE] Suche Account mit Username: " + username);
        }

        Optional<Account> account = accountManager.findByUsername(username);

        if (account.isEmpty()) {
            Framework.getLogger().info("console", "Kein Account mit dem Username '" + username + "' gefunden.");
            return;
        }

        printAccount(account.get(), verbose);
    }

    private static void getAccountByEmail(String email, boolean verbose) {
        if (verbose) {
            Framework.getLogger().info("console", "[VERBOSE] Suche Account mit E-Mail: " + email);
        }

        Optional<Account> account = accountManager.findByEmail(email);

        if (account.isEmpty()) {
            Framework.getLogger().info("console", "Kein Account mit der E-Mail '" + email + "' gefunden.");
            return;
        }

        printAccount(account.get(), verbose);
    }

    private static void getAccountsByGroup(String groupIdString, boolean verbose) {
        long groupId;

        try {
            groupId = Long.parseLong(groupIdString);
        } catch (NumberFormatException e) {
            Framework.getLogger().info("console", "Die GroupID muss eine Zahl sein.");
            return;
        }

        if (verbose) {
            Framework.getLogger().info("console", "[VERBOSE] Suche Accounts mit GroupID: " + groupId);
        }

        List<Account> accounts = accountManager.findAllByGroupId(groupId);

        if (accounts.isEmpty()) {
            Framework.getLogger().info("console", "Keine Accounts mit der GroupID " + groupId + " gefunden.");
            return;
        }

        printAccounts(accounts, "Group " + groupId, verbose);
    }

    private static void handleSearch(String search, boolean verbose) {
        if (verbose) {
            Framework.getLogger().info("console", "[VERBOSE] Suche nach: " + search);
        }

        List<Account> accounts = accountManager.search(search);

        if (accounts.isEmpty()) {
            Framework.getLogger().info("console", "Keine Accounts für '" + search + "' gefunden.");
            return;
        }

        printAccounts(accounts, "Suchergebnisse", verbose);
    }

    private static void handleRecentLogins(int limit, boolean verbose) {
        if (limit <= 0) {
            Framework.getLogger().info("console", "Das Limit muss größer als 0 sein.");
            return;
        }

        if (verbose) {
            Framework.getLogger().info("console", "[VERBOSE] Lade die " + limit + " zuletzt eingeloggten Accounts...");
        }

        List<Account> accounts = accountManager.findRecentlyLoggedIn(limit);

        if (accounts.isEmpty()) {
            Framework.getLogger().info("console", "Es wurden keine Accounts mit Login gefunden.");
            return;
        }

        printAccounts(accounts, "Letzte Logins", verbose);
    }

    private static void handleNeverLoggedIn(boolean verbose) {
        if (verbose) {
            Framework.getLogger().info("console", "[VERBOSE] Suche Accounts ohne bisherigen Login...");
        }

        List<Account> accounts = accountManager.findNeverLoggedIn();

        if (accounts.isEmpty()) {
            Framework.getLogger().info("console", "Alle Accounts haben sich bereits eingeloggt.");
            return;
        }

        printAccounts(accounts, "Accounts ohne Login", verbose);
    }

    private static List<Account> sortAccounts(List<Account> accounts, String field, boolean descending) {
        if (field == null || field.isBlank()) {
            return accounts;
        }

        Comparator<Account> comparator;

        switch (field.toLowerCase(Locale.ROOT)) {
            case "id", "userid", "accountid" -> comparator = Comparator.comparing(Account::getId);
            case "username", "user" -> comparator = Comparator.comparing(Account::getUsername, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
            case "email", "mail" -> comparator = Comparator.comparing(Account::getEmail, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
            case "group", "groupid" -> comparator = Comparator.comparing(Account::getGroupid);
            case "registered", "regist", "registdate" -> comparator = Comparator.comparing(Account::getRegistDate, Comparator.nullsLast(Comparator.naturalOrder()));
            case "lastlogin", "login", "lastlogindate" -> comparator = Comparator.comparing(Account::getLastLoginDate, Comparator.nullsLast(Comparator.naturalOrder()));
            case "active", "status" -> comparator = Comparator.comparing(Account::isActive);
            default -> {
                Framework.getLogger().info("console", "Unbekanntes Sortierfeld: " + field);
                return accounts;
            }
        }

        if (descending) {
            comparator = comparator.reversed();
        }

        return accounts.stream().sorted(comparator).collect(Collectors.toList());
    }

    private static boolean hasVerbose(String[] args) {
        for (String arg : args) {
            if (arg.equalsIgnoreCase("verbose") || arg.equalsIgnoreCase("-v")) {
                return true;
            }
        }
        return false;
    }

    private static Integer getLimit(String[] args) {
        for (int i = 0; i < args.length - 1; i++) {
            if (args[i].equalsIgnoreCase("limit")) {
                try {
                    int limit = Integer.parseInt(args[i + 1]);

                    if (limit <= 0) {
                        Framework.getLogger().info("console", "Limit muss größer als 0 sein.");
                        return null;
                    }

                    return limit;
                } catch (NumberFormatException e) {
                    Framework.getLogger().info("console", "Das Limit muss eine Zahl sein.");
                    return null;
                }
            }
        }

        return null;
    }

    private static String getSortField(String[] args) {
        for (int i = 0; i < args.length - 1; i++) {
            if (args[i].equalsIgnoreCase("sort")) {
                return args[i + 1];
            }
        }

        return null;
    }

    private static boolean isDescending(String[] args) {
        for (String arg : args) {
            if (arg.equalsIgnoreCase("desc") || arg.equalsIgnoreCase("descending")) {
                return true;
            }
        }

        return false;
    }

    private static int getNumericArgument(String[] args, int index, int defaultValue) {
        if (args.length <= index) {
            return defaultValue;
        }

        try {
            return Integer.parseInt(args[index]);
        } catch (NumberFormatException e) {
            Framework.getLogger().info("console", "Der Wert muss eine Zahl sein.");
            return defaultValue;
        }
    }

    private static void printAccounts(List<Account> accounts, String title, boolean verbose) {
        Framework.getLogger().info("console", "===== " + title + " (" + accounts.size() + ") =====");

        for (Account account : accounts) {
            printAccount(account, verbose);
        }

        Framework.getLogger().info("console", "=================================");
    }

    private static void printAccount(Account account, boolean verbose) {
        Framework.getLogger().info("console", "---------------------------------");
        Framework.getLogger().info("console", "UserID: " + account.getId());
        Framework.getLogger().info("console", "Username: " + account.getUsername());
        Framework.getLogger().info("console", "E-Mail: " + account.getEmail());
        Framework.getLogger().info("console", "Registriert: " + account.getRegistDate());
        Framework.getLogger().info("console", "Letzter Login: " + account.getLastLoginDate());
        Framework.getLogger().info("console", "GroupID: " + account.getGroupid());
        Framework.getLogger().info("console", "Status: " + (account.isActive() ? "Aktiv" : "Deaktiviert"));

        if (verbose) {
            Framework.getLogger().info("console", "Passwort: [GESCHÜTZT]");
            Framework.getLogger().info("console", "[VERBOSE] Account vollständig geladen.");
        }

        Framework.getLogger().info("console", "---------------------------------");
    }

    private static void sendUsage() {
        Framework.getLogger().info("console", "===== GetAccount =====");
        Framework.getLogger().info("console", "getaccount all [limit <n>] [sort <field>] [asc|desc] [verbose]");
        Framework.getLogger().info("console", "getaccount count");
        Framework.getLogger().info("console", "getaccount active [verbose]");
        Framework.getLogger().info("console", "getaccount inactive [verbose]");
        Framework.getLogger().info("console", "getaccount userid <userid> [verbose]");
        Framework.getLogger().info("console", "getaccount username <username> [verbose]");
        Framework.getLogger().info("console", "getaccount email <email> [verbose]");
        Framework.getLogger().info("console", "getaccount group <groupid> [verbose]");
        Framework.getLogger().info("console", "getaccount search <text> [verbose]");
        Framework.getLogger().info("console", "getaccount recent [limit] [verbose]");
        Framework.getLogger().info("console", "getaccount neverloggedin [verbose]");
        Framework.getLogger().info("console", "======================");
    }

    private static void usage(String message) {
        Framework.getLogger().info("console", "Benutzung: " + message);
    }
}
