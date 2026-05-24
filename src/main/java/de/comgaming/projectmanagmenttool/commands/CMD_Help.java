package de.comgaming.projectmanagmenttool.commands;

import dev.comgaming.framework.Framework;

public class CMD_Help {

    public static void onCommand(){
        Framework.getLogger().info("backend","----------[Help]----------");
        Framework.getLogger().info("backend","help\t\tShow all commands");
        Framework.getLogger().info("backend","stop\t\tStop the backend");
    }
}
