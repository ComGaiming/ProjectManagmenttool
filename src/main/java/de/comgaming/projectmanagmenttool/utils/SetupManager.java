package de.comgaming.projectmanagmenttool.utils;

import de.comgaming.projectmanagmenttool.ProjectManagmenttool;
import dev.comgaming.framework.Framework;
import dev.comgaming.framework.utils.InternalMethods;
import dev.comgaming.framework.utils.filemanager.DirectoryHandler;
import dev.comgaming.framework.utils.filemanager.FileManager;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Scanner;

@NoArgsConstructor
public class SetupManager {

    @Getter
    private static final FileManager serverFileManager = new FileManager("server.properties");

    @Getter
    private static final FileManager eulaFileManager = new FileManager("eula.txt");

    public void checkSetup() {
        Framework.getLogger().info("setup", "Checking server setup...");
        if (!serverFileManager.exists() || !eulaFileManager.exists())
            startSetup();

        checkEula();
        setupEmail();
        setupRestApi();
        InternalMethods.generateLinuxStartFiles("ProjectManagmenttool-1.0-SNAPSHOT");
        setupDatabase();
    }

    public void startSetup() {
        Framework.getLogger().info("setup", "Starting initial setup...");

        if (!eulaFileManager.exists()) {
            eulaFileManager.createFile();
            eulaFileManager.writeInNextFreeLine("You accept the terms on the website https://commgaming.de/pmt/terms.html");
            eulaFileManager.writeInNextFreeLine("eula=false");
            Framework.getLogger().info("setup", "Created eula.txt");
        }

        if (!serverFileManager.exists()) {
            serverFileManager.createFile();
            serverFileManager.writeInNextFreeLine("host=127.0.0.1");
            serverFileManager.writeInNextFreeLine("port=2256");
            Framework.getLogger().info("setup", "Created server.properties");
        }

        Framework.getLogger().info("setup", "Initial setup completed.");
    }

    private void checkEula() {
        String eula = eulaFileManager.getParams(2);
        if (!"true".equalsIgnoreCase(eula)) {
            Framework.getLogger().info("setup", "Bitte akzeptiere die EULA in eula.txt.");
            System.exit(3);
        }

        Framework.getLogger().info("setup", "EULA accepted.");
    }

    private void setupEmail() {
        DirectoryHandler directory = new DirectoryHandler("config");
        if (!directory.existsDirectory("config"))
            directory.generateDirectory();

        FileManager file = new FileManager("config/email.yml");
        if (file.exists())
            return;
        Scanner scanner = ProjectManagmenttool.getScanner();
        System.out.println();
        System.out.println("=== E-Mail Setup ===");

        System.out.print("SMTP Host: ");
        String host = scanner.nextLine().trim();

        System.out.print("SMTP Port: ");
        int port = Integer.parseInt(scanner.nextLine().trim());

        System.out.print("SMTP Username: ");
        String username = scanner.nextLine().trim();

        System.out.print("SMTP Password: ");
        String password = scanner.nextLine().trim();

        System.out.print("Sender E-Mail: ");
        String sender = scanner.nextLine().trim();

        if (!file.createFile())
            throw new IllegalStateException("email.yml konnte nicht erstellt werden.");

        file.writeInNextFreeLine("email:");
        file.writeInNextFreeLine("  sender: \"" + sender + "\"");
        file.writeInNextFreeLine("  smtp:");
        file.writeInNextFreeLine("    host: \"" + host + "\"");
        file.writeInNextFreeLine("    port: " + port);
        file.writeInNextFreeLine("    username: \"" + username + "\"");
        file.writeInNextFreeLine("    password: \"" + password + "\"");

        Framework.getLogger().info("setup","E-Mail-Konfiguration gespeichert.");
    }

    private void setupRestApi() {
        DirectoryHandler directory = new DirectoryHandler("config");

        if (!directory.existsDirectory("config"))
            directory.generateDirectory();

        FileManager file = new FileManager("config/restapi.yml");

        if (file.exists())
            return;

        Scanner scanner = ProjectManagmenttool.getScanner();
        System.out.println();
        System.out.println("=== REST API Setup ===");
        System.out.print("JWT Secret: ");
        String secret = scanner.nextLine().trim();
        System.out.print("REST API Port: ");
        int port;

        try {
            port = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("REST API Port muss eine Zahl sein.");
        }

        if (secret.isBlank())
            throw new IllegalArgumentException("JWT Secret darf nicht leer sein.");

        if (port < 1 || port > 65535)
            throw new IllegalArgumentException("REST API Port muss zwischen 1 und 65535 liegen.");

        if (!file.createFile())
            throw new IllegalStateException("restapi.yml konnte nicht erstellt werden.");

        file.writeInNextFreeLine("secret: \"" + secret + "\"");
        file.writeInNextFreeLine("port: " + port);

        Framework.getLogger().info("setup", "REST-API-Konfiguration gespeichert.");
    }

    private void setupDatabase() {
        try {
            Connection connection = ProjectManagmenttool.getDatabaseManager().getConnection();

            connection.prepareStatement("USE complaning").execute();

            connection.prepareStatement("""
                    CREATE TABLE IF NOT EXISTS groups (
                        groupid BIGINT AUTO_INCREMENT PRIMARY KEY,
                        groupname VARCHAR(255) NOT NULL UNIQUE
                    )
                    """).executeUpdate();

            connection.prepareStatement("""
                    CREATE TABLE IF NOT EXISTS accounts (
                        accountid BIGINT AUTO_INCREMENT PRIMARY KEY,
                        username VARCHAR(255) NOT NULL UNIQUE,
                        email VARCHAR(255) NOT NULL UNIQUE,
                        password VARCHAR(255) NOT NULL,
                        registdate TIMESTAMP NOT NULL,
                        lastlogindate TIMESTAMP NULL,
                        groupid BIGINT NOT NULL,
                        active BOOLEAN NOT NULL DEFAULT TRUE,
                        FOREIGN KEY (groupid)
                            REFERENCES groups(groupid)
                            ON UPDATE CASCADE
                            ON DELETE RESTRICT
                    )
                    """).executeUpdate();

            connection.prepareStatement("""
                    CREATE TABLE IF NOT EXISTS permissions (
                        permissionid BIGINT AUTO_INCREMENT PRIMARY KEY,
                        permissionname VARCHAR(100) NOT NULL UNIQUE
                    )
                    """).executeUpdate();

            connection.prepareStatement("""
                    CREATE TABLE IF NOT EXISTS grouppermissions (
                        grouppermissionid BIGINT AUTO_INCREMENT PRIMARY KEY,
                        groupid BIGINT NOT NULL,
                        permissionid BIGINT NOT NULL,
                        UNIQUE (groupid, permissionid),
                        FOREIGN KEY (groupid)
                            REFERENCES groups(groupid)
                            ON UPDATE CASCADE
                            ON DELETE CASCADE,
                        FOREIGN KEY (permissionid)
                            REFERENCES permissions(permissionid)
                            ON UPDATE CASCADE
                            ON DELETE CASCADE
                    )
                    """).executeUpdate();

            connection.prepareStatement("INSERT IGNORE INTO groups (groupname) VALUES ('admin'), ('default')").executeUpdate();

            Framework.getLogger().info("setup","Database setup completed.");

        } catch (SQLException e) {
            throw new RuntimeException("Fehler beim Einrichten der Datenbank.", e);
        }
    }

    public static void resetDatabase() {
        try {
            Connection connection = ProjectManagmenttool.getDatabaseManager().getConnection();
            connection.prepareStatement("USE complaning").execute();
            connection.prepareStatement("DROP TABLE IF EXISTS grouppermissions").executeUpdate();
            connection.prepareStatement("DROP TABLE IF EXISTS accounts").executeUpdate();
            connection.prepareStatement("DROP TABLE IF EXISTS permissions").executeUpdate();
            connection.prepareStatement("DROP TABLE IF EXISTS groups").executeUpdate();
            Framework.getLogger().info("setup","Database has been reset.");
        } catch (SQLException e) {
            throw new RuntimeException("Fehler beim Zurücksetzen der Datenbank.", e);
        }
    }
}