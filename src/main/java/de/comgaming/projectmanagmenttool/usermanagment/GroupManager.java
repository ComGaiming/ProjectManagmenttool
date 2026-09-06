package de.comgaming.projectmanagmenttool.usermanagment;

import de.comgaming.projectmanagmenttool.ProjectManagmenttool;
import dev.comgaming.framework.utils.DatabaseManager;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GroupManager {

    private final DatabaseManager databaseManager;

    public GroupManager() {
        this.databaseManager = ProjectManagmenttool.getDatabaseManager();
    }

    public Optional<Group> findById(Long id) {
        String sql = "SELECT * FROM groups WHERE groupid = ?";

        try (PreparedStatement statement =
                     databaseManager.getConnection().prepareStatement(sql)) {

            statement.setLong(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapGroup(resultSet));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Fehler beim Suchen der Gruppe nach ID.", e);
        }

        return Optional.empty();
    }

    public Optional<Group> findByGroupname(String groupname) {
        String sql = "SELECT * FROM groups WHERE groupname = ?";

        try (PreparedStatement statement =
                     databaseManager.getConnection().prepareStatement(sql)) {

            statement.setString(1, groupname);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapGroup(resultSet));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Fehler beim Suchen der Gruppe nach Namen.", e);
        }

        return Optional.empty();
    }

    public List<Group> findAll() {
        String sql = "SELECT * FROM groups";
        List<Group> groups = new ArrayList<>();

        try (PreparedStatement statement =
                     databaseManager.getConnection().prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                groups.add(mapGroup(resultSet));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Fehler beim Abrufen aller Gruppen.", e);
        }

        return groups;
    }

    private Group mapGroup(ResultSet resultSet) throws SQLException {
        Group group = new Group();

        group.setId(resultSet.getLong("groupid"));
        group.setGroupname(resultSet.getString("groupname"));

        return group;
    }
}
