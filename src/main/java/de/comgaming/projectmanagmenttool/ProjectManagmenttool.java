package de.comgaming.projectmanagmenttool;

import de.comgaming.projectmanagmenttool.commands.CMD_Help;
import de.comgaming.projectmanagmenttool.commands.CMD_stop;
import de.comgaming.projectmanagmenttool.commands.account.CMD_AddAccount;
import de.comgaming.projectmanagmenttool.commands.account.CMD_GetAccount;
import de.comgaming.projectmanagmenttool.commands.account.CMD_deleteaccount;
import de.comgaming.projectmanagmenttool.commands.group.CMD_Updategroupname;
import de.comgaming.projectmanagmenttool.commands.group.CMD_addGroup;
import de.comgaming.projectmanagmenttool.restapi.RestAPIServer;
import de.comgaming.projectmanagmenttool.utils.SetupManager;
import dev.comgaming.framework.Framework;
import dev.comgaming.framework.utils.DatabaseManager;

import java.util.Scanner;

public class ProjectManagmenttool {

    private static DatabaseManager databaseManager;
    private static final SetupManager setupManager = new SetupManager();
    private static final Scanner scanner = new Scanner(System.in);
    private static RestAPIServer restApiServer;

    public static DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public static Scanner getScanner() {
        return scanner;
    }

    public static void main(String[] args) {
        new Framework().init();
        Framework.getLogger().info("backend", "Backend is starting..");

        try {
            databaseManager = new DatabaseManager("database", true);
            setupManager.checkSetup();

            restApiServer = new RestAPIServer();
            restApiServer.start();
        } catch (Exception e) {
            Framework.getLogger().error("backend", "Fehler beim Starten: " + e.getMessage());
            onStop();
            return;
        }

        Framework.getLogger().info("backend", "Backend started");
        consoleLoop();
    }

    private static void consoleLoop() {
        while (true) {
            System.out.print("> ");

            if (!scanner.hasNextLine()) break;

            String input = scanner.nextLine().trim();
            if (input.isEmpty()) continue;

            String[] parts = input.split("\\s+");
            String command = parts[0].toLowerCase();
            String[] args = new String[parts.length - 1];

            System.arraycopy(parts, 1, args, 0, args.length);

            try {
                switch (command) {
                    case "stop", "end", "exit" -> CMD_stop.onStop();
                    case "help", "?" -> CMD_Help.onCommand();
                    case "createaccount", "addaccount" -> CMD_AddAccount.onCommand(args);
                    case "getaccount" -> CMD_GetAccount.onCommand(args);
                    case "deleteaccount" -> CMD_deleteaccount.onCommand(args);
                    case "creategroup", "addgroup" -> CMD_addGroup.onCommand();
                    case "updategroupname" -> CMD_Updategroupname.onCommand(args);
                    default -> Framework.getLogger().info("console", "Unbekannter Befehl: " + command);
                }
            } catch (Exception e) {
                Framework.getLogger().error("console", "Fehler: " + e.getMessage());
            }
        }

        onStop();
    }

    public static void onStop() {
        Framework.getLogger().info("console", "Backend is stopping");
        if (restApiServer != null) {
            try {
                restApiServer.stop();
            } catch (Exception e) {
                Framework.getLogger().error("restapi", "Fehler beim Stoppen: " + e.getMessage());
            }
        }

        if (databaseManager != null) {
            try {
                if (databaseManager.getConnection() != null && !databaseManager.getConnection().isClosed())
                    databaseManager.getConnection().close();
            } catch (Exception e) {
                Framework.getLogger().error("database", "Fehler beim Schließen: " + e.getMessage());
            }
        }

        Framework.getLogger().info("console", "Backend is stopped");
        System.exit(0);
    }
}
