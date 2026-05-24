package de.comgaming.projectmanagmenttool;

import de.comgaming.projectmanagmenttool.commands.CMD_stop;
import dev.comgaming.framework.Framework;
import lombok.Getter;

import java.util.Scanner;

public class ProjectManagmenttool {

    @Getter
    private static Scanner scanner;

    static String eingabe = "";

    public static void main(String[] args) {
        while (true){
            System.out.println("> ");

            eingabe = scanner.nextLine();
            switch (eingabe){
                case "stop", "end"-> CMD_stop.onStop();
                case "help", "?" -> CMD_stop.onStop();
                default -> Framework.getLogger().info("console","Please use 'stop' for stopping the Server.\n");
            }

        }
    }

    public static void onStop(){
        Framework.getLogger().info("console", "Backend is stopping");

        Framework.getLogger().info("console", "Backend is stopped");
        System.exit(0);
    }
}