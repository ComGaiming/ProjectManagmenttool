package de.comgaming.projectmanagmenttool;

import de.comgaming.projectmanagmenttool.commands.CMD_Help;
import de.comgaming.projectmanagmenttool.commands.CMD_stop;
import de.comgaming.projectmanagmenttool.commands.account.CMD_AddAccount;
import de.comgaming.projectmanagmenttool.utils.SetupManager;
import dev.comgaming.framework.Framework;
import dev.comgaming.framework.utils.DatabaseManager;

import java.util.Scanner;

public class ProjectManagmenttool {

    private static DatabaseManager databaseManager = null;
    private static SetupManager setupManager = new SetupManager();

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
                input = scanner.nextLine().trim().toLowerCase();
            } catch (Exception e) {
                Framework.getLogger().error("console", "Fehler beim Lesen der Konsoleneingabe: " + e.getMessage());
                break;
            }

            switch (input) {
                case "stop", "end" -> CMD_stop.onStop();
                case "help", "?" -> CMD_Help.onCommand();
                case "createaccount" -> CMD_AddAccount.onCommand();
                case "" -> {}
                default -> Framework.getLogger().info(
                        "console",
                        "Unbekannter Befehl. Nutze 'help' für eine Liste der Befehle."
                );
            }
        }

        scanner.close();
    }

    public static void onStop() {
        Framework.getLogger().info("console", "Backend is stopping");
        Framework.getLogger().info("console", "Backend is stopped");
        System.exit(0);
    }
}
