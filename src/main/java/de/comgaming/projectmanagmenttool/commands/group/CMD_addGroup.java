package de.comgaming.projectmanagmenttool.commands.group;

import de.comgaming.projectmanagmenttool.usermanagment.Group;
import de.comgaming.projectmanagmenttool.usermanagment.GroupManager;
import dev.comgaming.framework.Framework;

import java.util.Scanner;

public class CMD_addGroup {

    private static final GroupManager groupManager = new GroupManager();

    public static void onCommand() {
        Scanner scanner = new Scanner(System.in);

        Framework.getLogger().info("group", "Creating new group...");
        Framework.getLogger().info("group", "Gruppenname:");

        String groupname = scanner.nextLine().trim();

        if (groupname.isBlank()) {
            Framework.getLogger().info("group", "Gruppenname darf nicht leer sein.");
            return;
        }

        if (groupManager.findByGroupname(groupname).isPresent()) {
            Framework.getLogger().info("group", "Eine Gruppe mit diesem Namen existiert bereits.");
            return;
        }

        try {
            Group group = groupManager.createGroup(groupname);

            Framework.getLogger().info("group", "Gruppe erfolgreich erstellt.");
            Framework.getLogger().info("group", "ID: " + group.getId());
            Framework.getLogger().info("group", "Name: " + group.getGroupname());

        } catch (IllegalArgumentException e) {
            Framework.getLogger().info("group", e.getMessage());
        } catch (Exception e) {
            Framework.getLogger().error("group", "Fehler beim Erstellen der Gruppe: " + e.getMessage());
        }
    }


}