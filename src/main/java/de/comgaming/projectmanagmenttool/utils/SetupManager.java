package de.comgaming.projectmanagmenttool.utils;

import de.comgaming.projectmanagmenttool.ProjectManagmenttool;
import dev.comgaming.framework.Framework;
import dev.comgaming.framework.utils.InternalMethods;
import dev.comgaming.framework.utils.filemanager.FileManager;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.sql.SQLException;

@NoArgsConstructor
public class SetupManager {

    @Getter
    private static final FileManager serverFileManager = new FileManager("server.properties");

    @Getter
    private static final FileManager eulaFileManager = new FileManager("eula.txt");

    public void checkSetup() {
        Framework.getLogger().info("setup", "Checking server setup...");
        if (!serverFileManager.exists() || !eulaFileManager.exists()) {
            startSetup();
        }

        InternalMethods.generateLinuxStartFiles("ProjectManagmenttool-1.0-SNAPSHOT");
        checkEula();
        onSetupDatabase();
    }

    public void startSetup() {
        Framework.getLogger().info("setup", "Starting initial setup...");

        if (!eulaFileManager.exists()) {
            if (eulaFileManager.createFile()) {
                eulaFileManager.writeInNextFreeLine("You accept the terms on the website https://commgaming.de/pmt/terms.html");
                eulaFileManager.writeInNextFreeLine("eula=false");
                Framework.getLogger().info("setup","Created eula.txt");
            }
        }

        if (!serverFileManager.exists()) {
            if (serverFileManager.createFile()) {
                serverFileManager.writeInNextFreeLine("host=127.0.0.1");
                serverFileManager.writeInNextFreeLine("port=2256");
                Framework.getLogger().info("setup","Created server.properties");
            }
        }
        Framework.getLogger().info("setup","Initial setup completed.");


    }

    private void checkEula() {
        String eula = eulaFileManager.getParams(2);

        if (eula == null) {
            Framework.getLogger().info("setup", "EULA setting not found.");
            Framework.getLogger().info("setup", "Bitte akzeptiere die EULA in eula.txt.");
            System.exit(3);
        }

        if (!eula.equalsIgnoreCase("true")) {
            Framework.getLogger().info("setup", "EULA not accepted.");
            Framework.getLogger().info("setup", "Bitte akzeptiere die EULA in eula.txt.");
            System.exit(3);
        }

        Framework.getLogger().info("setup", "EULA accepted.");
    }

    public static void onSetupDatabase() {
        try {
            var connection = ProjectManagmenttool.getDatabaseManager().getConnection();
            connection.prepareStatement("USE complaning").execute();
            connection.prepareStatement("CREATE TABLE IF NOT EXISTS groups (groupid BIGINT AUTO_INCREMENT PRIMARY KEY, groupname VARCHAR(255) NOT NULL UNIQUE)").executeUpdate();
            connection.prepareStatement("INSERT IGNORE INTO groups (groupname) VALUES ('admin')").executeUpdate();
            connection.prepareStatement("CREATE TABLE IF NOT EXISTS accounts (accountid BIGINT AUTO_INCREMENT PRIMARY KEY, username VARCHAR(255) NOT NULL UNIQUE, email VARCHAR(255) NOT NULL UNIQUE, groupid BIGINT NOT NULL, FOREIGN KEY (groupid) REFERENCES groups(groupid));\n").executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void resetDatabase(){
        try {
            var connection = ProjectManagmenttool.getDatabaseManager().getConnection();

            connection.prepareStatement("USE complaning").execute();
            connection.prepareStatement("DROP TABLE IF EXISTS groups").executeUpdate();
            connection.prepareStatement("DROP TABLE IF EXISTS accounts").executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}