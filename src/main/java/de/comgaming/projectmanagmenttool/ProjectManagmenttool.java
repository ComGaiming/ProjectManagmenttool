package de.comgaming.projectmanagmenttool;

import de.comgaming.projectmanagmenttool.commands.CMD_Help;
import de.comgaming.projectmanagmenttool.commands.CMD_stop;
import de.comgaming.projectmanagmenttool.commands.account.*;
import de.comgaming.projectmanagmenttool.utils.SetupManager;
import dev.comgaming.framework.Framework;
import dev.comgaming.framework.utils.DatabaseManager;

import java.util.Scanner;

public class ProjectManagmenttool {

    private static DatabaseManager databaseManager = null;
    private static final SetupManager setupManager = new SetupManager();

    public static DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public static void main(String[] args) {
        Framework framework = new Framework();
        framework.init();
        Framework.getLogger().info("backend", "Backend of ComPlaning is now starting..");

        databaseManager = new DatabaseManager("database", true);

        setupManager.checkSetup();
        Framework.getLogger().info("backend", "Backend of ComPlaning is now started");
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.print("> ");
            String input;
            try {
                input = scanner.nextLine().trim();
            } catch (Exception e) {
                Framework.getLogger().error("console", "Fehler beim Lesen der Konsoleneingabe: " + e.getMessage());
                break;
            }

            if (input.isEmpty()) {
                continue;
            }

            String[] commandParts = input.split("\\s+");
            String command = commandParts[0].toLowerCase();
            String[] commandArgs = new String[Math.max(0, commandParts.length - 1)];
            if (commandParts.length > 1) {
                System.arraycopy(commandParts, 1, commandArgs, 0, commandParts.length - 1);
            }
            switch (command) {
                case "stop", "end" -> CMD_stop.onStop();
                case "help", "?" -> CMD_Help.onCommand();
                case "createaccount" -> CMD_AddAccount.onCommand();
                case "getaccount" -> CMD_GetAccount.onCommand(commandArgs);
                case "deleteaccount" -> CMD_deleteaccount.onCommand(commandArgs);
                default -> Framework.getLogger().info("console", "Unbekannter Befehl '" + command + "'. Nutze 'help' für eine Liste der Befehle.");
            }
        }
        scanner.close();
    }

    public static void onStop() {
        Framework.getLogger().info("console", "Backend is stopping");
        if (databaseManager != null) {
            try {
                databaseManager.getConnection().close();
                Framework.getLogger().info("console", "Database connection closed");
            } catch (Exception e) {
                Framework.getLogger().error("console", "Fehler beim Schließen der Datenbankverbindung: " + e.getMessage());
            }
        }
        Framework.getLogger().info("console", "Backend is stopped");
        System.exit(0);
    }
}
