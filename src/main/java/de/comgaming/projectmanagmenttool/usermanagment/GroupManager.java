package de.comgaming.projectmanagmenttool.usermanagment;

import de.comgaming.projectmanagmenttool.ProjectManagmenttool;
import dev.comgaming.framework.utils.DatabaseManager;

import java.sql.*;
import java.util.*;

public class GroupManager {

    private final DatabaseManager databaseManager;

    public GroupManager() {
        databaseManager = ProjectManagmenttool.getDatabaseManager();
    }

    public Optional<Group> findById(Long id) {
        if (id == null) return Optional.empty();

        try (PreparedStatement s = databaseManager.getConnection().prepareStatement(
                "SELECT * FROM groups WHERE groupid = ?")) {
            s.setLong(1, id);

            try (ResultSet r = s.executeQuery()) {
                return r.next() ? Optional.of(map(r)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Fehler beim Suchen der Gruppe.", e);
        }
    }

    public Optional<Group> findByGroupname(String name) {
        if (name == null || name.isBlank()) return Optional.empty();

        try (PreparedStatement s = databaseManager.getConnection().prepareStatement(
                "SELECT * FROM groups WHERE groupname = ?")) {
            s.setString(1, name.trim());

            try (ResultSet r = s.executeQuery()) {
                return r.next() ? Optional.of(map(r)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Fehler beim Suchen der Gruppe.", e);
        }
    }

    public List<Group> findAll() {
        List<Group> list = new ArrayList<>();

        try (PreparedStatement s = databaseManager.getConnection().prepareStatement(
                "SELECT * FROM groups ORDER BY groupname ASC");
             ResultSet r = s.executeQuery()) {

            while (r.next()) list.add(map(r));

        } catch (SQLException e) {
            throw new RuntimeException("Fehler beim Abrufen aller Gruppen.", e);
        }

        return list;
    }

    public Group createGroup(String name) {
        if (name == null || name.isBlank())
            throw new IllegalArgumentException("Gruppenname darf nicht leer sein.");

        name = name.trim();

        if (findByGroupname(name).isPresent())
            throw new IllegalArgumentException("Gruppe ist bereits vorhanden.");

        try (PreparedStatement s = databaseManager.getConnection().prepareStatement(
                "INSERT INTO groups (groupname) VALUES (?)",
                Statement.RETURN_GENERATED_KEYS)) {

            s.setString(1, name);

            if (s.executeUpdate() == 0)
                throw new RuntimeException("Gruppe konnte nicht erstellt werden.");

            try (ResultSet r = s.getGeneratedKeys()) {
                if (!r.next())
                    throw new RuntimeException("Keine GroupID erhalten.");

                return new Group(r.getLong(1), name);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Fehler beim Erstellen der Gruppe.", e);
        }
    }

    public boolean delete(Long id) {
        if (id == null) return false;

        try (PreparedStatement s = databaseManager.getConnection().prepareStatement(
                "DELETE FROM groups WHERE groupid = ?")) {

            s.setLong(1, id);
            return s.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("Fehler beim Löschen der Gruppe.", e);
        }
    }

    public boolean updateGroupname(Long id, String name) {
        if (id == null)
            throw new IllegalArgumentException("GroupID darf nicht null sein.");

        if (name == null || name.isBlank())
            throw new IllegalArgumentException("Gruppenname darf nicht leer sein.");

        name = name.trim();

        Optional<Group> existing = findByGroupname(name);

        if (existing.isPresent() && !existing.get().getId().equals(id))
            throw new IllegalArgumentException("Gruppenname ist bereits vergeben.");

        try (PreparedStatement s = databaseManager.getConnection().prepareStatement(
                "UPDATE groups SET groupname = ? WHERE groupid = ?")) {

            s.setString(1, name);
            s.setLong(2, id);

            return s.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("Fehler beim Ändern des Gruppennamens.", e);
        }
    }

    private Group map(ResultSet r) throws SQLException {
        return new Group(
                r.getLong("groupid"),
                r.getString("groupname")
        );
    }


}