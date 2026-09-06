package de.comgaming.projectmanagmenttool.utils;

import dev.comgaming.framework.utils.filemanager.DirectoryHandler;
import dev.comgaming.framework.utils.filemanager.FileManager;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.util.Map;

public class EmailConfig {

    private static final String CONFIG_DIRECTORY = "config";
    private static final String CONFIG_FILE = "email.yml";

    private final String smtpHost;
    private final int smtpPort;
    private final String username;
    private final String password;
    private final String sender;

    public EmailConfig() {

        DirectoryHandler directoryHandler =
                new DirectoryHandler(CONFIG_DIRECTORY);

        if (!directoryHandler.existsDirectory(CONFIG_DIRECTORY)) {

            String result =
                    directoryHandler.generateDirectory();

            if (!directoryHandler.existsDirectory(CONFIG_DIRECTORY)) {
                throw new IllegalStateException(
                        "Config-Verzeichnis konnte nicht erstellt werden: "
                                + result
                );
            }
        }

        String filePath =
                directoryHandler.getAbsolutePath()
                        + File.separator
                        + CONFIG_FILE;

        FileManager fileManager =
                new FileManager(filePath);

        if (!fileManager.exists()) {
            throw new IllegalStateException(
                    "E-Mail-Konfiguration nicht gefunden: "
                            + filePath
            );
        }

        String content = String.join(
                System.lineSeparator(),
                fileManager.readAll()
        );

        if (content.isBlank()) {
            throw new IllegalStateException(
                    "Die E-Mail-Konfiguration ist leer."
            );
        }

        try {

            Yaml yaml = new Yaml();

            Map<String, Object> config =
                    yaml.load(content);

            if (config == null) {
                throw new IllegalStateException(
                        "Die email.yml konnte nicht gelesen werden."
                );
            }

            Map<String, Object> email =
                    getMap(config, "email");

            Map<String, Object> smtp =
                    getMap(email, "smtp");

            this.smtpHost =
                    getString(smtp, "host");

            this.smtpPort =
                    getInteger(smtp, "port");

            this.username =
                    getString(smtp, "username");

            this.password =
                    getString(smtp, "password");

            this.sender =
                    getString(email, "sender");

        } catch (Exception e) {

            throw new IllegalStateException(
                    "Fehler beim Laden der email.yml.",
                    e
            );
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getMap(
            Map<String, Object> parent,
            String key
    ) {

        Object value = parent.get(key);

        if (!(value instanceof Map<?, ?>)) {
            throw new IllegalStateException(
                    "Konfiguration '" + key
                            + "' fehlt oder ist ungültig."
            );
        }

        return (Map<String, Object>) value;
    }

    private String getString(
            Map<String, Object> map,
            String key
    ) {

        Object value = map.get(key);

        if (value == null ||
                value.toString().isBlank()) {

            throw new IllegalStateException(
                    "Konfiguration '" + key
                            + "' fehlt oder ist leer."
            );
        }

        return value.toString();
    }

    private int getInteger(
            Map<String, Object> map,
            String key
    ) {

        Object value = map.get(key);

        if (!(value instanceof Number)) {
            throw new IllegalStateException(
                    "Konfiguration '" + key
                            + "' muss eine Zahl sein."
            );
        }

        return ((Number) value).intValue();
    }

    public String getSmtpHost() {
        return smtpHost;
    }

    public int getSmtpPort() {
        return smtpPort;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getSender() {
        return sender;
    }
}
