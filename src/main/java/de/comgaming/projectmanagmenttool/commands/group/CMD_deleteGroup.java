package de.comgaming.projectmanagmenttool.commands.group;

import de.comgaming.projectmanagmenttool.ProjectManagmenttool;
import de.comgaming.projectmanagmenttool.usermanagment.Group;
import de.comgaming.projectmanagmenttool.usermanagment.GroupManager;
import dev.comgaming.framework.Framework;

import java.util.Optional;
import java.util.Scanner;

public class CMD_deleteGroup {

    private static final GroupManager groupManager = new GroupManager();

    public static void onCommand() {
        Scanner scanner = ProjectManagmenttool.getScanner();

        Framework.getLogger().info("group", "Deleting group...");
        Framework.getLogger().info("group", "GroupID:");

        if (!scanner.hasNextLine()) {
            return;
        }

        String input = scanner.nextLine().trim();

        if (input.isBlank()) {
            Framework.getLogger().info("group", "GroupID darf nicht leer sein.");
            return;
        }

        Long groupId;

        try {
            groupId = Long.parseLong(input);
        } catch (NumberFormatException e) {
            Framework.getLogger().info("group", "Ungültige GroupID.");
            return;
        }

        Optional<Group> optionalGroup = groupManager.findById(groupId);

        if (optionalGroup.isEmpty()) {
            Framework.getLogger().info("group", "Gruppe mit der ID " + groupId + " wurde nicht gefunden.");
            return;
        }

        Group group = optionalGroup.get();

        Framework.getLogger().info("group", "Gefundene Gruppe:");
        Framework.getLogger().info("group", "ID: " + group.getId());
        Framework.getLogger().info("group", "Name: " + group.getGroupname());
        Framework.getLogger().info("group", "Möchtest du diese Gruppe wirklich löschen? (yes/no)");

        if (!scanner.hasNextLine()) {
            return;
        }

        String confirmation = scanner.nextLine().trim();

        if (!confirmation.equalsIgnoreCase("yes") && !confirmation.equalsIgnoreCase("y")) {
            Framework.getLogger().info("group", "Löschen abgebrochen.");
            return;
        }

        try {
            boolean deleted = groupManager.delete(groupId);

            if (!deleted) {
                Framework.getLogger().info("group", "Gruppe konnte nicht gelöscht werden.");
                return;
            }

            Framework.getLogger().info("group", "Gruppe erfolgreich gelöscht.");
            Framework.getLogger().info("group", "ID: " + group.getId());
            Framework.getLogger().info("group", "Name: " + group.getGroupname());

        } catch (Exception e) {
            Framework.getLogger().error("group", "Fehler beim Löschen der Gruppe: " + e.getMessage());
        }
    }
}
