package de.comgaming.projectmanagmenttool.usermanagment;

import de.comgaming.projectmanagmenttool.ProjectManagmenttool;
import de.comgaming.projectmanagmenttool.utils.EmailHandler;
import dev.comgaming.framework.utils.DatabaseManager;
import dev.comgaming.framework.utils.PasswordManager;
import jakarta.mail.MessagingException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AccountManager {

    private final DatabaseManager databaseManager;
    private final GroupManager groupManager;
    private final EmailHandler emailHandler;

    public AccountManager() {
        this.databaseManager = ProjectManagmenttool.getDatabaseManager();
        this.groupManager = new GroupManager();
        this.emailHandler = new EmailHandler();
    }

    public Optional<Account> findById(Long accountId) {
        if (accountId == null) {
            return Optional.empty();
        }

        String sql = "SELECT * FROM accounts WHERE accountid = ?";

        try (PreparedStatement statement = databaseManager.getConnection().prepareStatement(sql)) {
            statement.setLong(1, accountId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapAccount(resultSet));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Fehler beim Suchen des Accounts nach ID.", e);
        }

        return Optional.empty();
    }

    public Optional<Account> findByUsername(String username) {
        if (username == null || username.isBlank()) {
            return Optional.empty();
        }

        String sql = "SELECT * FROM accounts WHERE username = ?";

        try (PreparedStatement statement = databaseManager.getConnection().prepareStatement(sql)) {
            statement.setString(1, username.trim());

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapAccount(resultSet));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Fehler beim Suchen des Accounts nach Username.", e);
        }

        return Optional.empty();
    }

    public Optional<Account> findByEmail(String email) {
        if (email == null || email.isBlank()) {
            return Optional.empty();
        }

        String sql = "SELECT * FROM accounts WHERE email = ?";

        try (PreparedStatement statement = databaseManager.getConnection().prepareStatement(sql)) {
            statement.setString(1, email.trim());

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapAccount(resultSet));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Fehler beim Suchen des Accounts nach E-Mail-Adresse.", e);
        }

        return Optional.empty();
    }

    public List<Account> findAll() {
        return findAll(null, null, "id", false);
    }

    public List<Account> findAll(Integer limit, Integer offset, String sort, boolean descending) {
        validatePagination(limit, offset);

        StringBuilder sql = new StringBuilder("SELECT * FROM accounts ORDER BY ");
        sql.append(getOrderColumn(sort));
        sql.append(descending ? " DESC" : " ASC");

        if (limit != null) {
            sql.append(" LIMIT ?");
        }

        if (offset != null) {
            if (limit == null) {
                sql.append(" LIMIT 18446744073709551615");
            }

            sql.append(" OFFSET ?");
        }

        List<Account> accounts = new ArrayList<>();

        try (PreparedStatement statement = databaseManager.getConnection().prepareStatement(sql.toString())) {
            int parameterIndex = 1;

            if (limit != null) {
                statement.setInt(parameterIndex++, limit);
            }

            if (offset != null) {
                statement.setInt(parameterIndex, offset);
            }

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    accounts.add(mapAccount(resultSet));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Fehler beim Abrufen der Accounts.", e);
        }

        return accounts;
    }

    public List<Account> findAllByGroupId(Long groupId) {
        return findAllByGroupId(groupId, null, null, "id", false);
    }

    public List<Account> findAllByGroupId(Long groupId, Integer limit, Integer offset, String sort, boolean descending) {
        if (groupId == null) {
            return new ArrayList<>();
        }

        validatePagination(limit, offset);

        StringBuilder sql = new StringBuilder("SELECT * FROM accounts WHERE groupid = ? ORDER BY ");
        sql.append(getOrderColumn(sort));
        sql.append(descending ? " DESC" : " ASC");

        if (limit != null) {
            sql.append(" LIMIT ?");
        }

        if (offset != null) {
            if (limit == null) {
                sql.append(" LIMIT 18446744073709551615");
            }

            sql.append(" OFFSET ?");
        }

        List<Account> accounts = new ArrayList<>();

        try (PreparedStatement statement = databaseManager.getConnection().prepareStatement(sql.toString())) {
            int parameterIndex = 1;

            statement.setLong(parameterIndex++, groupId);

            if (limit != null) {
                statement.setInt(parameterIndex++, limit);
            }

            if (offset != null) {
                statement.setInt(parameterIndex, offset);
            }

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    accounts.add(mapAccount(resultSet));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Fehler beim Suchen der Accounts nach Gruppe.", e);
        }

        return accounts;
    }

    public List<Account> findAllActive() {
        return findByActive(true);
    }

    public List<Account> findAllInactive() {
        return findByActive(false);
    }

    public List<Account> findByActive(boolean active) {
        String sql = "SELECT * FROM accounts WHERE active = ? ORDER BY username ASC";

        List<Account> accounts = new ArrayList<>();

        try (PreparedStatement statement = databaseManager.getConnection().prepareStatement(sql)) {
            statement.setBoolean(1, active);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    accounts.add(mapAccount(resultSet));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Fehler beim Abrufen der Accounts nach Aktivierungsstatus.", e);
        }

        return accounts;
    }

    public List<Account> search(String search) {
        if (search == null || search.isBlank()) {
            return new ArrayList<>();
        }

        String trimmedSearch = search.trim();
        String query = "%" + trimmedSearch.toLowerCase() + "%";

        String sql = """
                SELECT *
                FROM accounts
                WHERE LOWER(username) LIKE ?
                   OR LOWER(email) LIKE ?
                ORDER BY username ASC
                """;

        List<Account> accounts = new ArrayList<>();

        try (PreparedStatement statement = databaseManager.getConnection().prepareStatement(sql)) {
            statement.setString(1, query);
            statement.setString(2, query);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    accounts.add(mapAccount(resultSet));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Fehler bei der Account-Suche.", e);
        }

        if (isNumeric(trimmedSearch)) {
            Long accountId = Long.parseLong(trimmedSearch);

            findById(accountId).ifPresent(account -> {
                boolean exists = accounts.stream().anyMatch(existing -> existing.getId().equals(account.getId()));

                if (!exists) {
                    accounts.add(0, account);
                }
            });
        }

        return accounts;
    }

    public List<Account> findRecentlyLoggedIn(int limit) {
        if (limit <= 0) {
            throw new IllegalArgumentException("Limit muss größer als 0 sein.");
        }

        String sql = "SELECT * FROM accounts WHERE lastlogindate IS NOT NULL ORDER BY lastlogindate DESC LIMIT ?";

        List<Account> accounts = new ArrayList<>();

        try (PreparedStatement statement = databaseManager.getConnection().prepareStatement(sql)) {
            statement.setInt(1, limit);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    accounts.add(mapAccount(resultSet));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Fehler beim Abrufen der letzten Logins.", e);
        }

        return accounts;
    }

    public List<Account> findNeverLoggedIn() {
        String sql = "SELECT * FROM accounts WHERE lastlogindate IS NULL ORDER BY registdate ASC";

        List<Account> accounts = new ArrayList<>();

        try (PreparedStatement statement = databaseManager.getConnection().prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                accounts.add(mapAccount(resultSet));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Fehler beim Abrufen der Accounts ohne Login.", e);
        }

        return accounts;
    }

    public long count() {
        String sql = "SELECT COUNT(*) FROM accounts";

        try (PreparedStatement statement = databaseManager.getConnection().prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            if (resultSet.next()) {
                return resultSet.getLong(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Fehler beim Zählen der Accounts.", e);
        }

        return 0;
    }

    public long countActive() {
        return countByActive(true);
    }

    public long countInactive() {
        return countByActive(false);
    }

    public long countByActive(boolean active) {
        String sql = "SELECT COUNT(*) FROM accounts WHERE active = ?";

        try (PreparedStatement statement = databaseManager.getConnection().prepareStatement(sql)) {
            statement.setBoolean(1, active);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getLong(1);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Fehler beim Zählen der Accounts.", e);
        }

        return 0;
    }

    public long countByGroupId(Long groupId) {
        if (groupId == null) {
            return 0;
        }

        String sql = "SELECT COUNT(*) FROM accounts WHERE groupid = ?";

        try (PreparedStatement statement = databaseManager.getConnection().prepareStatement(sql)) {
            statement.setLong(1, groupId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getLong(1);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Fehler beim Zählen der Accounts der Gruppe.", e);
        }

        return 0;
    }

    public boolean existsById(Long accountId) {
        if (accountId == null) {
            return false;
        }

        String sql = "SELECT 1 FROM accounts WHERE accountid = ? LIMIT 1";

        try (PreparedStatement statement = databaseManager.getConnection().prepareStatement(sql)) {
            statement.setLong(1, accountId);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Fehler beim Prüfen der AccountID.", e);
        }
    }

    public boolean existsByUsername(String username) {
        if (username == null || username.isBlank()) {
            return false;
        }

        String sql = "SELECT 1 FROM accounts WHERE username = ? LIMIT 1";

        try (PreparedStatement statement = databaseManager.getConnection().prepareStatement(sql)) {
            statement.setString(1, username.trim());

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Fehler beim Prüfen des Usernames.", e);
        }
    }

    public boolean existsByEmail(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }

        String sql = "SELECT 1 FROM accounts WHERE email = ? LIMIT 1";

        try (PreparedStatement statement = databaseManager.getConnection().prepareStatement(sql)) {
            statement.setString(1, email.trim());

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Fehler beim Prüfen der E-Mail-Adresse.", e);
        }
    }

    public boolean isActive(Long accountId) {
        if (accountId == null) {
            return false;
        }

        String sql = "SELECT active FROM accounts WHERE accountid = ?";

        try (PreparedStatement statement = databaseManager.getConnection().prepareStatement(sql)) {
            statement.setLong(1, accountId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getBoolean("active");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Fehler beim Prüfen des Aktivierungsstatus.", e);
        }

        return false;
    }

    public boolean activate(Long accountId) {
        validateAccount(accountId);

        String sql = "UPDATE accounts SET active = TRUE WHERE accountid = ?";

        return executeSingleChange(sql, accountId);
    }

    public boolean deactivate(Long accountId) {
        validateAccount(accountId);

        String sql = "UPDATE accounts SET active = FALSE WHERE accountid = ?";

        return executeSingleChange(sql, accountId);
    }

    public Account createAccount(String username, String email, Long groupId) {
        validateUsername(username);
        validateEmail(email);

        if (groupId == null) {
            throw new IllegalArgumentException("GroupID darf nicht null sein.");
        }

        username = username.trim();
        email = email.trim();

        if (existsByUsername(username)) {
            throw new IllegalArgumentException("Username ist bereits vergeben.");
        }

        if (existsByEmail(email)) {
            throw new IllegalArgumentException("E-Mail-Adresse ist bereits vergeben.");
        }

        if (groupManager.findById(groupId).isEmpty()) {
            throw new IllegalArgumentException("Die Gruppe mit der ID " + groupId + " existiert nicht.");
        }

        String password = PasswordManager.generatePassword(16);
        String passwordHash = PasswordManager.hashPassword(password);

        String sql = """
                INSERT INTO accounts
                (
                    username,
                    email,
                    password,
                    registdate,
                    lastlogindate,
                    groupid,
                    active
                )
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

        Timestamp registrationDate = new Timestamp(System.currentTimeMillis());

        Account account;

        try (PreparedStatement statement = databaseManager.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, username);
            statement.setString(2, email);
            statement.setString(3, passwordHash);
            statement.setTimestamp(4, registrationDate);
            statement.setNull(5, Types.TIMESTAMP);
            statement.setLong(6, groupId);
            statement.setBoolean(7, true);

            int affectedRows = statement.executeUpdate();

            if (affectedRows == 0) {
                throw new RuntimeException("Account konnte nicht erstellt werden.");
            }

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new RuntimeException("Keine AccountID erhalten.");
                }

                Long accountId = keys.getLong(1);

                account = new Account(
                        accountId,
                        username,
                        email,
                        passwordHash,
                        registrationDate,
                        null,
                        groupId,
                        true
                );
            }
        } catch (SQLException e) {
            throw new RuntimeException("Fehler beim Erstellen des Accounts.", e);
        }

        try {
            sendAccountCredentials(username, email, password);
        } catch (MessagingException e) {
            throw new RuntimeException("Account wurde erstellt, aber die Zugangsdaten konnten nicht per E-Mail versendet werden.", e);
        }

        return account;
    }

    private void sendAccountCredentials(String username, String email, String password) throws MessagingException {
        String subject = "Dein Account wurde erstellt";

        String content = """
                Hallo %s,

                dein Account wurde erfolgreich erstellt.

                Deine Zugangsdaten:

                Benutzername: %s
                Passwort: %s

                Bitte ändere dein Passwort nach
                dem ersten Login.

                Viele Grüße

                Project Management Tool
                """.formatted(username, username, password);

        emailHandler.sendEmail(email, subject, content);
    }

    public boolean changeUsername(Long accountId, String username) {
        validateAccount(accountId);
        validateUsername(username);

        username = username.trim();

        Optional<Account> existingAccount = findByUsername(username);

        if (existingAccount.isPresent() && !existingAccount.get().getId().equals(accountId)) {
            throw new IllegalArgumentException("Username ist bereits vergeben.");
        }

        String sql = "UPDATE accounts SET username = ? WHERE accountid = ?";

        return executeChange(sql, username, accountId);
    }

    public boolean changeEmail(Long accountId, String email) {
        validateAccount(accountId);
        validateEmail(email);

        email = email.trim();

        Optional<Account> existingAccount = findByEmail(email);

        if (existingAccount.isPresent() && !existingAccount.get().getId().equals(accountId)) {
            throw new IllegalArgumentException("E-Mail-Adresse ist bereits vergeben.");
        }

        String sql = "UPDATE accounts SET email = ? WHERE accountid = ?";

        return executeChange(sql, email, accountId);
    }

    public boolean changePassword(Long accountId, String password) {
        validateAccount(accountId);

        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Passwort darf nicht leer sein.");
        }

        String passwordHash = PasswordManager.hashPassword(password);

        String sql = "UPDATE accounts SET password = ? WHERE accountid = ?";

        return executeChange(sql, passwordHash, accountId);
    }

    public boolean changeGroupId(Long accountId, Long groupId) {
        validateAccount(accountId);

        if (groupId == null) {
            throw new IllegalArgumentException("GroupID darf nicht null sein.");
        }

        if (groupManager.findById(groupId).isEmpty()) {
            throw new IllegalArgumentException("Die Gruppe mit der ID " + groupId + " existiert nicht.");
        }

        String sql = "UPDATE accounts SET groupid = ? WHERE accountid = ?";

        return executeChange(sql, groupId, accountId);
    }

    public boolean changeLastLoginDate(Long accountId, Timestamp lastLoginDate) {
        validateAccount(accountId);

        if (lastLoginDate == null) {
            throw new IllegalArgumentException("LastLoginDate darf nicht null sein.");
        }

        String sql = "UPDATE accounts SET lastlogindate = ? WHERE accountid = ?";

        return executeChange(sql, lastLoginDate, accountId);
    }

    public boolean deleteById(Long accountId) {
        validateAccount(accountId);

        String sql = "DELETE FROM accounts WHERE accountid = ?";

        try (PreparedStatement statement = databaseManager.getConnection().prepareStatement(sql)) {
            statement.setLong(1, accountId);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Fehler beim Löschen des Accounts.", e);
        }
    }

    public boolean deleteByUsername(String username) {
        if (username == null || username.isBlank()) {
            return false;
        }

        Optional<Account> account = findByUsername(username);

        return account.map(Account::getId).map(this::deleteById).orElse(false);
    }

    public boolean deleteByEmail(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }

        Optional<Account> account = findByEmail(email);

        return account.map(Account::getId).map(this::deleteById).orElse(false);
    }

    public Optional<Account> login(String username, String password) {
        if (username == null || username.isBlank()) {
            return Optional.empty();
        }

        if (password == null || password.isBlank()) {
            return Optional.empty();
        }

        Optional<Account> account = findByUsername(username.trim());

        if (account.isEmpty()) {
            return Optional.empty();
        }

        Account foundAccount = account.get();

        if (!foundAccount.isActive()) {
            return Optional.empty();
        }

        if (!PasswordManager.verifyPassword(password, foundAccount.getPassword())) {
            return Optional.empty();
        }

        Timestamp now = new Timestamp(System.currentTimeMillis());

        changeLastLoginDate(foundAccount.getId(), now);
        foundAccount.setLastLoginDate(now);

        return Optional.of(foundAccount);
    }

    private void validateAccount(Long accountId) {
        if (accountId == null) {
            throw new IllegalArgumentException("AccountID darf nicht null sein.");
        }

        if (!existsById(accountId)) {
            throw new IllegalArgumentException("Account mit der ID " + accountId + " existiert nicht.");
        }
    }

    private void validateUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username darf nicht leer sein.");
        }
    }

    private void validateEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("E-Mail-Adresse darf nicht leer sein.");
        }
    }

    private void validatePagination(Integer limit, Integer offset) {
        if (limit != null && limit <= 0) {
            throw new IllegalArgumentException("Limit muss größer als 0 sein.");
        }

        if (offset != null && offset < 0) {
            throw new IllegalArgumentException("Offset darf nicht negativ sein.");
        }
    }

    private boolean executeChange(String sql, Object value, Long accountId) {
        try (PreparedStatement statement = databaseManager.getConnection().prepareStatement(sql)) {
            statement.setObject(1, value);
            statement.setLong(2, accountId);

            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Fehler beim Ändern des Accounts.", e);
        }
    }

    private boolean executeSingleChange(String sql, Long accountId) {
        try (PreparedStatement statement = databaseManager.getConnection().prepareStatement(sql)) {
            statement.setLong(1, accountId);

            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Fehler beim Ändern des Accounts.", e);
        }
    }

    private String getOrderColumn(String sort) {
        if (sort == null || sort.isBlank()) {
            return "accountid";
        }

        return switch (sort.trim().toLowerCase()) {
            case "id", "userid", "accountid" -> "accountid";
            case "username", "user" -> "username";
            case "email", "mail" -> "email";
            case "group", "groupid" -> "groupid";
            case "registered", "regist", "registdate" -> "registdate";
            case "lastlogin", "login", "lastlogindate" -> "lastlogindate";
            case "active", "status" -> "active";
            default -> throw new IllegalArgumentException("Unbekanntes Sortierfeld: " + sort);
        };
    }

    private boolean isNumeric(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }

        try {
            Long.parseLong(value.trim());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private Account mapAccount(ResultSet resultSet) throws SQLException {
        Account account = new Account();

        account.setId(resultSet.getLong("accountid"));
        account.setUsername(resultSet.getString("username"));
        account.setEmail(resultSet.getString("email"));
        account.setPassword(resultSet.getString("password"));
        account.setRegistDate(resultSet.getTimestamp("registdate"));
        account.setLastLoginDate(resultSet.getTimestamp("lastlogindate"));

        long groupId = resultSet.getLong("groupid");

        if (resultSet.wasNull()) {
            account.setGroupid(null);
        } else {
            account.setGroupid(groupId);
        }

        account.setActive(resultSet.getBoolean("active"));

        return account;
    }
}
