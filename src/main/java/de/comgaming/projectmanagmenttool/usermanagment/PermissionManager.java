package de.comgaming.projectmanagmenttool.usermanagment;

import de.comgaming.projectmanagmenttool.ProjectManagmenttool;
import dev.comgaming.framework.utils.DatabaseManager;

import java.sql.*;
import java.util.*;

public class PermissionManager {

    private final DatabaseManager databaseManager;

    public PermissionManager() {
        databaseManager = ProjectManagmenttool.getDatabaseManager();
    }

    public Optional<Permission> findById(Long id) {
        if (id == null) return Optional.empty();

        try (PreparedStatement s = databaseManager.getConnection().prepareStatement("SELECT * FROM permissions WHERE permissionid = ?")) {

            s.setLong(1, id);

            try (ResultSet r = s.executeQuery()) {
                return r.next() ? Optional.of(map(r)) : Optional.empty();
            }

        } catch (SQLException e) {
            throw new RuntimeException("Fehler beim Suchen der Permission.", e);
        }
    }

    public Optional<Permission> findByName(String name) {
        if (name == null || name.isBlank()) return Optional.empty();

        try (PreparedStatement s = databaseManager.getConnection().prepareStatement(
                "SELECT * FROM permissions WHERE permissionname = ?")) {

            s.setString(1, name.trim());

            try (ResultSet r = s.executeQuery()) {
                return r.next() ? Optional.of(map(r)) : Optional.empty();
            }

        } catch (SQLException e) {
            throw new RuntimeException("Fehler beim Suchen der Permission.", e);
        }
    }

    public List<Permission> findAll() {
        List<Permission> list = new ArrayList<>();

        try (PreparedStatement s = databaseManager.getConnection().prepareStatement(
                "SELECT * FROM permissions ORDER BY permissionname ASC");
             ResultSet r = s.executeQuery()) {

            while (r.next()) list.add(map(r));

        } catch (SQLException e) {
            throw new RuntimeException("Fehler beim Abrufen der Permissions.", e);
        }

        return list;
    }

    public Permission create(String name) {
        if (name == null || name.isBlank())
            throw new IllegalArgumentException("Permission darf nicht leer sein.");

        name = name.trim();

        if (findByName(name).isPresent())
            throw new IllegalArgumentException("Permission existiert bereits.");

        try (PreparedStatement s = databaseManager.getConnection().prepareStatement(
                "INSERT INTO permissions (permissionname) VALUES (?)",
                Statement.RETURN_GENERATED_KEYS)) {

            s.setString(1, name);

            if (s.executeUpdate() == 0)
                throw new RuntimeException("Permission konnte nicht erstellt werden.");

            try (ResultSet r = s.getGeneratedKeys()) {
                if (!r.next())
                    throw new RuntimeException("Keine PermissionID erhalten.");

                return new Permission(r.getLong(1), name);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Fehler beim Erstellen der Permission.", e);
        }
    }

    public boolean delete(Long id) {
        if (id == null) return false;

        try (PreparedStatement s = databaseManager.getConnection().prepareStatement(
                "DELETE FROM permissions WHERE permissionid = ?")) {

            s.setLong(1, id);
            return s.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("Fehler beim Löschen der Permission.", e);
        }
    }

    public boolean assignToGroup(Long groupId, Long permissionId) {
        if (groupId == null || permissionId == null) return false;

        if (!groupExists(groupId))
            throw new IllegalArgumentException("Gruppe existiert nicht.");

        if (findById(permissionId).isEmpty())
            throw new IllegalArgumentException("Permission existiert nicht.");

        try (PreparedStatement s = databaseManager.getConnection().prepareStatement(
                "INSERT IGNORE INTO grouppermissions (groupid, permissionid) VALUES (?, ?)")) {

            s.setLong(1, groupId);
            s.setLong(2, permissionId);

            return s.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("Fehler beim Zuweisen der Permission.", e);
        }
    }

    public boolean removeFromGroup(Long groupId, Long permissionId) {
        if (groupId == null || permissionId == null) return false;

        try (PreparedStatement s = databaseManager.getConnection().prepareStatement(
                "DELETE FROM grouppermissions WHERE groupid = ? AND permissionid = ?")) {

            s.setLong(1, groupId);
            s.setLong(2, permissionId);

            return s.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("Fehler beim Entfernen der Permission.", e);
        }
    }

    public List<Permission> findByGroupId(Long groupId) {
        List<Permission> list = new ArrayList<>();

        if (groupId == null) return list;

        String sql = """
            SELECT p.*
            FROM permissions p
            JOIN grouppermissions gp ON gp.permissionid = p.permissionid
            WHERE gp.groupid = ?
            ORDER BY p.permissionname ASC
            """;

        try (PreparedStatement s = databaseManager.getConnection().prepareStatement(sql)) {

            s.setLong(1, groupId);

            try (ResultSet r = s.executeQuery()) {
                while (r.next())
                    list.add(map(r));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Fehler beim Abrufen der Gruppen-Permissions.", e);
        }

        return list;
    }

    public boolean hasPermission(Long groupId, String permission) {
        if (groupId == null || permission == null || permission.isBlank())
            return false;

        String sql = """
            SELECT 1
            FROM groups g
            LEFT JOIN grouppermissions gp ON gp.groupid = g.groupid
            LEFT JOIN permissions p ON p.permissionid = gp.permissionid
            WHERE g.groupid = ?
              AND (LOWER(g.groupname) = 'admin' OR p.permissionname = ?)
            LIMIT 1
            """;

        try (PreparedStatement s = databaseManager.getConnection().prepareStatement(sql)) {

            s.setLong(1, groupId);
            s.setString(2, permission.trim());

            try (ResultSet r = s.executeQuery()) {
                return r.next();
            }

        } catch (SQLException e) {
            throw new RuntimeException("Fehler beim Prüfen der Permission.", e);
        }
    }

    private boolean groupExists(Long groupId) {
        try (PreparedStatement s = databaseManager.getConnection().prepareStatement(
                "SELECT 1 FROM groups WHERE groupid = ?")) {

            s.setLong(1, groupId);

            try (ResultSet r = s.executeQuery()) {
                return r.next();
            }

        } catch (SQLException e) {
            throw new RuntimeException("Fehler beim Prüfen der Gruppe.", e);
        }
    }

    private Permission map(ResultSet r) throws SQLException {
        return new Permission(
                r.getLong("permissionid"),
                r.getString("permissionname")
        );
    }


}