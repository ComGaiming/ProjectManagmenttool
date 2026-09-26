package de.comgaming.projectmanagmenttool.commands;

import dev.comgaming.framework.Framework;

public class CMD_Help {

    public static void onCommand() {
        Framework.getLogger().info("backend", "----------[Help]----------");
        Framework.getLogger().info("backend", "help\t\tShow all commands");
        Framework.getLogger().info("backend", "stop\t\tStop the backend");
        Framework.getLogger().info("backend", "createaccount\tCreate an account");
        Framework.getLogger().info("backend", "getaccount\tGet an account");
        Framework.getLogger().info("backend", "deleteaccount\tDelete an account");
        Framework.getLogger().info("backend", "creategroup\tCreate a group");
        Framework.getLogger().info("backend", "deletegroup\tDelete a group");
        Framework.getLogger().info("backend", "updategroupname\tUpdate group name");
    }
}
