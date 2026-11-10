package de.comgaming.projectmanagmenttool.commands.group;

import de.comgaming.projectmanagmenttool.usermanagment.Group;
import de.comgaming.projectmanagmenttool.usermanagment.GroupManager;
import dev.comgaming.framework.Framework;

import java.util.Optional;

public class CMD_Updategroupname {

    private static final GroupManager groupManager = new GroupManager();

    public static void onCommand(String[] args) {
        if (args == null || args.length < 2) {
            sendHelp();
            return;
        }

        Long groupId = null;
        String newGroupname = null;

        for (int i = 0; i < args.length; i++) {
            switch (args[i].toLowerCase()) {
                case "--id", "-id" -> {
                    if (i + 1 >= args.length) {
                        error("--id benötigt eine GroupID.");
                        return;
                    }

                    try {
                        groupId = Long.parseLong(args[++i]);
                    } catch (NumberFormatException e) {
                        error("Die GroupID muss eine Zahl sein.");
                        return;
                    }
                }

                case "--name", "--newname", "-n" -> {
                    if (i + 1 >= args.length) {
                        error("--name benötigt einen neuen Gruppennamen.");
                        return;
                    }

                    newGroupname = args[++i];
                }

                case "--help", "-h" -> {
                    sendHelp();
                    return;
                }

                default -> {
                    error("Unbekanntes Argument: " + args[i]);
                    sendHelp();
                    return;
                }
            }
        }

        if (groupId == null) {
            error("Bitte --id angeben.");
            return;
        }

        if (newGroupname == null || newGroupname.isBlank()) {
            error("Bitte --name mit dem neuen Gruppennamen angeben.");
            return;
        }

        newGroupname = newGroupname.trim();

        Optional<Group> group = groupManager.findById(groupId);

        if (group.isEmpty()) {
            error("Gruppe mit der ID " + groupId + " wurde nicht gefunden.");
            return;
        }

        Group foundGroup = group.get();

        if (foundGroup.getGroupname().equals(newGroupname)) {
            error("Der neue Gruppenname entspricht bereits dem aktuellen Namen.");
            return;
        }

        try {
            boolean updated = groupManager.updateGroupname(
                    groupId,
                    newGroupname
            );

            if (updated) {
                Framework.getLogger().info(
                        "group",
                        "Gruppenname erfolgreich geändert."
                );
                Framework.getLogger().info(
                        "group",
                        "ID: " + groupId
                );
                Framework.getLogger().info(
                        "group",
                        "Alter Name: " + foundGroup.getGroupname()
                );
                Framework.getLogger().info(
                        "group",
                        "Neuer Name: " + newGroupname
                );
            } else {
                error("Gruppenname konnte nicht geändert werden.");
            }

        } catch (IllegalArgumentException e) {
            error(e.getMessage());
        } catch (Exception e) {
            error("Fehler beim Ändern der Gruppe: " + e.getMessage());
        }
    }

    private static void sendHelp() {
        Framework.getLogger().info("group", """
                
                Verwendung:
                
                  updategroupname --id <GroupID> --name <NeuerName>
                
                Optionen:
                
                  --id <ID>             Gruppe auswählen
                  --name <Name>         Neuer Gruppenname
                  --help                Hilfe anzeigen
                
                Kurzformen:
                
                  -id <ID>
                  -n <Name>
                  -h
                
                Beispiele:
                
                  updategroupname --id 5 --name Administrator
                  updategroupname -id 2 -n Moderatoren
                """);
    }

    private static void error(String message) {
        Framework.getLogger().error("group", message);
    }
}
