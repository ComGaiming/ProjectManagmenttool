package de.comgaming.projectmanagmenttool.usermanagment;

import de.comgaming.projectmanagmenttool.ProjectManagmenttool;
import dev.comgaming.framework.utils.DatabaseManager;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class GroupPermissionManager {

    private final DatabaseManager databaseManager;
    private final PermissionManager permissionManager;
    private final GroupManager groupManager;

    public GroupPermissionManager() {
        this.databaseManager = ProjectManagmenttool.getDatabaseManager();
        this.permissionManager = new PermissionManager();
        this.groupManager = new GroupManager();
    }

    public boolean hasPermission(Long groupid, String permissionname) {
        if (groupid == null || permissionname == null || permissionname.isBlank()) {
            return false;
        }

        String sql = """
                SELECT 1
                FROM grouppermissions gp
                INNER JOIN permissions p
                    ON gp.permissionid = p.permissionid
                WHERE gp.groupid = ?
                  AND p.permissionname = ?
                LIMIT 1
                """;

        try (PreparedStatement statement = databaseManager.getConnection().prepareStatement(sql)) {
            statement.setLong(1, groupid);
            statement.setString(2, permissionname.toLowerCase());

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }

        } catch (SQLException e) {
            throw new RuntimeException("Fehler beim Prüfen der Gruppenberechtigung.", e);
        }
    }

    public boolean addPermission(Long groupid, Long permissionid) {
        validateGroup(groupid);
        validatePermission(permissionid);

        String sql = """
                INSERT IGNORE INTO grouppermissions
                (groupid, permissionid)
                VALUES (?, ?)
                """;

        try (PreparedStatement statement = databaseManager.getConnection().prepareStatement(sql)) {
            statement.setLong(1, groupid);
            statement.setLong(2, permissionid);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Fehler beim Hinzufügen der Permission zur Gruppe.", e);
        }
    }

    public boolean addPermission(Long groupid, String permissionname) {
        Permission permission = permissionManager.findByName(permissionname)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Permission '" + permissionname + "' wurde nicht gefunden."
                ));

        return addPermission(groupid, permission.getId());
    }

    public boolean removePermission(Long groupid, Long permissionid) {
        validateGroup(groupid);
        validatePermission(permissionid);

        String sql = """
                DELETE FROM grouppermissions
                WHERE groupid = ?
                  AND permissionid = ?
                """;

        try (PreparedStatement statement = databaseManager.getConnection().prepareStatement(sql)) {
            statement.setLong(1, groupid);
            statement.setLong(2, permissionid);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Fehler beim Entfernen der Permission von der Gruppe.", e);
        }
    }

    public boolean removePermission(Long groupid, String permissionname) {
        Permission permission = permissionManager.findByName(permissionname)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Permission '" + permissionname + "' wurde nicht gefunden."
                ));

        return removePermission(groupid, permission.getId());
    }

    public List<Permission> findPermissionsByGroupId(Long groupid) {
        if (groupid == null) {
            return new ArrayList<>();
        }

        String sql = """
                SELECT p.*
                FROM permissions p
                INNER JOIN grouppermissions gp
                    ON p.permissionid = gp.permissionid
                WHERE gp.groupid = ?
                ORDER BY p.permissionname ASC
                """;

        List<Permission> permissions = new ArrayList<>();

        try (PreparedStatement statement = databaseManager.getConnection().prepareStatement(sql)) {
            statement.setLong(1, groupid);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    permissions.add(mapPermission(resultSet));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Fehler beim Abrufen der Gruppen-Permissions.", e);
        }

        return permissions;
    }

    public List<Permission> findPermissionsByGroup(Group group) {
        if (group == null) {
            return new ArrayList<>();
        }

        return findPermissionsByGroupId(group.getId());
    }

    public boolean hasAnyPermission(Long groupid, String... permissions) {
        if (permissions == null) {
            return false;
        }

        for (String permission : permissions) {
            if (hasPermission(groupid, permission)) {
                return true;
            }
        }

        return false;
    }

    public boolean hasAllPermissions(Long groupid, String... permissions) {
        if (permissions == null || permissions.length == 0) {
            return true;
        }

        for (String permission : permissions) {
            if (!hasPermission(groupid, permission)) {
                return false;
            }
        }

        return true;
    }

    private void validateGroup(Long groupid) {
        if (groupid == null) {
            throw new IllegalArgumentException("GroupID darf nicht null sein.");
        }

        if (groupManager.findById(groupid).isEmpty()) {
            throw new IllegalArgumentException(
                    "Gruppe mit der ID " + groupid + " existiert nicht."
            );
        }
    }

    private void validatePermission(Long permissionid) {
        if (permissionid == null) {
            throw new IllegalArgumentException("PermissionID darf nicht null sein.");
        }

        if (permissionManager.findById(permissionid).isEmpty()) {
            throw new IllegalArgumentException(
                    "Permission mit der ID " + permissionid + " existiert nicht."
            );
        }
    }

    private Permission mapPermission(ResultSet resultSet) throws SQLException {
        return new Permission(
                resultSet.getLong("permissionid"),
                resultSet.getString("permissionname")
        );
    }
}
