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

        this.databaseManager =
                ProjectManagmenttool.getDatabaseManager();

        this.groupManager =
                new GroupManager();

        this.emailHandler =
                new EmailHandler();
    }

    public Optional<Account> findById(Long id) {

        if (id == null) {
            return Optional.empty();
        }

        String sql =
                "SELECT * FROM accounts WHERE accountid = ?";

        try (PreparedStatement statement =
                     databaseManager
                             .getConnection()
                             .prepareStatement(sql)) {

            statement.setLong(1, id);

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                if (resultSet.next()) {
                    return Optional.of(
                            mapAccount(resultSet)
                    );
                }
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Fehler beim Suchen des Accounts nach ID.",
                    e
            );
        }

        return Optional.empty();
    }

    public Optional<Account> findByUsername(
            String username
    ) {

        if (username == null ||
                username.isBlank()) {

            return Optional.empty();
        }

        String sql =
                "SELECT * FROM accounts WHERE username = ?";

        try (PreparedStatement statement =
                     databaseManager
                             .getConnection()
                             .prepareStatement(sql)) {

            statement.setString(1, username);

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                if (resultSet.next()) {
                    return Optional.of(
                            mapAccount(resultSet)
                    );
                }
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Fehler beim Suchen des Accounts nach Username.",
                    e
            );
        }

        return Optional.empty();
    }

    public Optional<Account> findByEmail(
            String email
    ) {

        if (email == null ||
                email.isBlank()) {

            return Optional.empty();
        }

        String sql =
                "SELECT * FROM accounts WHERE email = ?";

        try (PreparedStatement statement =
                     databaseManager
                             .getConnection()
                             .prepareStatement(sql)) {

            statement.setString(1, email);

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                if (resultSet.next()) {
                    return Optional.of(
                            mapAccount(resultSet)
                    );
                }
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Fehler beim Suchen des Accounts nach E-Mail.",
                    e
            );
        }

        return Optional.empty();
    }

    public List<Account> findAllByGroupId(
            Long groupid
    ) {

        if (groupid == null) {
            return new ArrayList<>();
        }

        String sql =
                "SELECT * FROM accounts WHERE groupid = ?";

        List<Account> accounts =
                new ArrayList<>();

        try (PreparedStatement statement =
                     databaseManager
                             .getConnection()
                             .prepareStatement(sql)) {

            statement.setLong(1, groupid);

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                while (resultSet.next()) {

                    accounts.add(
                            mapAccount(resultSet)
                    );
                }
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Fehler beim Suchen der Accounts nach Gruppe.",
                    e
            );
        }

        return accounts;
    }

    public List<Account> findAll() {

        String sql =
                "SELECT * FROM accounts";

        List<Account> accounts =
                new ArrayList<>();

        try (PreparedStatement statement =
                     databaseManager
                             .getConnection()
                             .prepareStatement(sql);

             ResultSet resultSet =
                     statement.executeQuery()) {

            while (resultSet.next()) {

                accounts.add(
                        mapAccount(resultSet)
                );
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Fehler beim Abrufen aller Accounts.",
                    e
            );
        }

        return accounts;
    }

    /**
     * Erstellt einen Account mit einem automatisch
     * generierten Passwort.
     *
     * Das Klartext-Passwort wird ausschließlich für
     * den E-Mail-Versand verwendet.
     *
     * In der Datenbank wird ausschließlich der
     * BCrypt-Hash gespeichert.
     */
    public Account createAccount(
            String username,
            String email,
            Long groupid
    ) {

        if (username == null ||
                username.isBlank()) {

            throw new IllegalArgumentException(
                    "Username darf nicht leer sein."
            );
        }

        if (email == null ||
                email.isBlank()) {

            throw new IllegalArgumentException(
                    "E-Mail-Adresse darf nicht leer sein."
            );
        }

        if (groupid == null) {

            throw new IllegalArgumentException(
                    "GroupID darf nicht null sein."
            );
        }

        if (findByUsername(username).isPresent()) {

            throw new IllegalArgumentException(
                    "Username ist bereits vergeben."
            );
        }

        if (findByEmail(email).isPresent()) {

            throw new IllegalArgumentException(
                    "E-Mail-Adresse ist bereits vergeben."
            );
        }

        if (groupManager.findById(groupid).isEmpty()) {

            throw new IllegalArgumentException(
                    "Die Gruppe mit der ID "
                            + groupid
                            + " existiert nicht."
            );
        }

        /*
         * Zufälliges Passwort generieren.
         */
        String password =
                PasswordManager.generatePassword(16);

        /*
         * Nur der Hash kommt in die Datenbank.
         */
        String passwordHash =
                PasswordManager.hashPassword(password);

        String sql = """
                INSERT INTO accounts
                (
                    username,
                    email,
                    password,
                    registdate,
                    lastlogindate,
                    groupid
                )
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        Timestamp now =
                new Timestamp(
                        System.currentTimeMillis()
                );

        Account account;

        try (PreparedStatement statement =
                     databaseManager
                             .getConnection()
                             .prepareStatement(
                                     sql,
                                     Statement.RETURN_GENERATED_KEYS
                             )) {

            statement.setString(
                    1,
                    username
            );

            statement.setString(
                    2,
                    email
            );

            statement.setString(
                    3,
                    passwordHash
            );

            statement.setTimestamp(
                    4,
                    now
            );

            statement.setTimestamp(
                    5,
                    now
            );

            statement.setLong(
                    6,
                    groupid
            );

            int affectedRows =
                    statement.executeUpdate();

            if (affectedRows == 0) {

                throw new RuntimeException(
                        "Account konnte nicht erstellt werden."
                );
            }

            try (ResultSet keys =
                         statement.getGeneratedKeys()) {

                if (!keys.next()) {

                    throw new RuntimeException(
                            "Keine AccountID erhalten."
                    );
                }

                Long accountId =
                        keys.getLong(1);

                account =
                        new Account(
                                accountId,
                                username,
                                email,
                                passwordHash,
                                now,
                                now,
                                groupid
                        );
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Fehler beim Erstellen des Accounts.",
                    e
            );
        }

        /*
         * Account wurde erfolgreich erstellt.
         *
         * Das Klartext-Passwort wird nur hier
         * für den E-Mail-Versand verwendet.
         */
        try {

            sendAccountCredentials(
                    username,
                    email,
                    password
            );

        } catch (MessagingException e) {

            /*
             * Der Account wurde bereits erfolgreich
             * gespeichert.
             *
             * Deshalb löschen wir ihn NICHT automatisch.
             */
            throw new RuntimeException(
                    "Account wurde erstellt, "
                            + "aber die Zugangsdaten konnten "
                            + "nicht per E-Mail versendet werden.",
                    e
            );
        }

        return account;
    }

    private void sendAccountCredentials(
            String username,
            String email,
            String password
    ) throws MessagingException {

        String subject =
                "Dein Account wurde erstellt";

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
                """.formatted(
                username,
                username,
                password
        );

        emailHandler.sendEmail(
                email,
                subject,
                content
        );
    }

    public boolean changeUsername(
            Long accountId,
            String username
    ) {

        if (accountId == null) {

            throw new IllegalArgumentException(
                    "AccountID darf nicht null sein."
            );
        }

        if (username == null ||
                username.isBlank()) {

            throw new IllegalArgumentException(
                    "Username darf nicht leer sein."
            );
        }

        if (findById(accountId).isEmpty()) {

            throw new IllegalArgumentException(
                    "Account mit der ID "
                            + accountId
                            + " existiert nicht."
            );
        }

        Optional<Account> existingAccount =
                findByUsername(username);

        if (existingAccount.isPresent()
                && !existingAccount
                .get()
                .getId()
                .equals(accountId)) {

            throw new IllegalArgumentException(
                    "Username ist bereits vergeben."
            );
        }

        String sql = """
                UPDATE accounts
                SET username = ?
                WHERE accountid = ?
                """;

        return executeChange(
                sql,
                username,
                accountId
        );
    }

    public boolean changeEmail(
            Long accountId,
            String email
    ) {

        if (accountId == null) {

            throw new IllegalArgumentException(
                    "AccountID darf nicht null sein."
            );
        }

        if (email == null ||
                email.isBlank()) {

            throw new IllegalArgumentException(
                    "E-Mail-Adresse darf nicht leer sein."
            );
        }

        if (findById(accountId).isEmpty()) {

            throw new IllegalArgumentException(
                    "Account mit der ID "
                            + accountId
                            + " existiert nicht."
            );
        }

        Optional<Account> existingAccount =
                findByEmail(email);

        if (existingAccount.isPresent()
                && !existingAccount
                .get()
                .getId()
                .equals(accountId)) {

            throw new IllegalArgumentException(
                    "E-Mail-Adresse ist bereits vergeben."
            );
        }

        String sql = """
                UPDATE accounts
                SET email = ?
                WHERE accountid = ?
                """;

        return executeChange(
                sql,
                email,
                accountId
        );
    }

    /**
     * Ändert das Passwort.
     *
     * Auch hier wird nur der BCrypt-Hash
     * in der Datenbank gespeichert.
     */
    public boolean changePassword(
            Long accountId,
            String password
    ) {

        if (accountId == null) {

            throw new IllegalArgumentException(
                    "AccountID darf nicht null sein."
            );
        }

        if (password == null ||
                password.isBlank()) {

            throw new IllegalArgumentException(
                    "Passwort darf nicht leer sein."
            );
        }

        if (findById(accountId).isEmpty()) {

            throw new IllegalArgumentException(
                    "Account mit der ID "
                            + accountId
                            + " existiert nicht."
            );
        }

        String passwordHash =
                PasswordManager.hashPassword(
                        password
                );

        String sql = """
                UPDATE accounts
                SET password = ?
                WHERE accountid = ?
                """;

        return executeChange(
                sql,
                passwordHash,
                accountId
        );
    }

    public boolean changeGroupId(
            Long accountId,
            Long groupid
    ) {

        if (accountId == null) {

            throw new IllegalArgumentException(
                    "AccountID darf nicht null sein."
            );
        }

        if (groupid == null) {

            throw new IllegalArgumentException(
                    "GroupID darf nicht null sein."
            );
        }

        if (findById(accountId).isEmpty()) {

            throw new IllegalArgumentException(
                    "Account mit der ID "
                            + accountId
                            + " existiert nicht."
            );
        }

        if (groupManager.findById(groupid).isEmpty()) {

            throw new IllegalArgumentException(
                    "Die Gruppe mit der ID "
                            + groupid
                            + " existiert nicht."
            );
        }

        String sql = """
                UPDATE accounts
                SET groupid = ?
                WHERE accountid = ?
                """;

        return executeChange(
                sql,
                groupid,
                accountId
        );
    }

    public boolean changeLastLoginDate(
            Long accountId,
            Timestamp lastLoginDate
    ) {

        if (accountId == null) {

            throw new IllegalArgumentException(
                    "AccountID darf nicht null sein."
            );
        }

        if (lastLoginDate == null) {

            throw new IllegalArgumentException(
                    "LastLoginDate darf nicht null sein."
            );
        }

        if (findById(accountId).isEmpty()) {

            throw new IllegalArgumentException(
                    "Account mit der ID "
                            + accountId
                            + " existiert nicht."
            );
        }

        String sql = """
                UPDATE accounts
                SET lastlogindate = ?
                WHERE accountid = ?
                """;

        return executeChange(
                sql,
                lastLoginDate,
                accountId
        );
    }

    public boolean deleteById(
            Long accountId
    ) {

        if (accountId == null) {

            throw new IllegalArgumentException(
                    "AccountID darf nicht null sein."
            );
        }

        String sql =
                "DELETE FROM accounts WHERE accountid = ?";

        try (PreparedStatement statement =
                     databaseManager
                             .getConnection()
                             .prepareStatement(sql)) {

            statement.setLong(
                    1,
                    accountId
            );

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Fehler beim Löschen des Accounts.",
                    e
            );
        }
    }

    /**
     * Überprüft ein Login.
     */
    public Optional<Account> login(
            String username,
            String password
    ) {

        if (username == null ||
                username.isBlank()) {

            return Optional.empty();
        }

        if (password == null ||
                password.isBlank()) {

            return Optional.empty();
        }

        Optional<Account> account =
                findByUsername(username);

        if (account.isEmpty()) {
            return Optional.empty();
        }

        Account foundAccount =
                account.get();

        if (!PasswordManager.verifyPassword(
                password,
                foundAccount.getPassword()
        )) {

            return Optional.empty();
        }

        Timestamp now =
                new Timestamp(
                        System.currentTimeMillis()
                );

        changeLastLoginDate(
                foundAccount.getId(),
                now
        );

        foundAccount.setLastLoginDate(now);

        return Optional.of(foundAccount);
    }

    private boolean executeChange(
            String sql,
            Object value,
            Long accountId
    ) {

        try (PreparedStatement statement =
                     databaseManager
                             .getConnection()
                             .prepareStatement(sql)) {

            statement.setObject(
                    1,
                    value
            );

            statement.setLong(
                    2,
                    accountId
            );

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Fehler beim Ändern des Accounts.",
                    e
            );
        }
    }

    private Account mapAccount(
            ResultSet resultSet
    ) throws SQLException {

        Account account =
                new Account();

        account.setId(
                resultSet.getLong("accountid")
        );

        account.setUsername(
                resultSet.getString("username")
        );

        account.setEmail(
                resultSet.getString("email")
        );

        account.setPassword(
                resultSet.getString("password")
        );

        account.setRegistDate(
                resultSet.getTimestamp("registdate")
        );

        account.setLastLoginDate(
                resultSet.getTimestamp("lastlogindate")
        );

        account.setGroupid(
                resultSet.getLong("groupid")
        );

        return account;
    }
}