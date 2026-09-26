package de.comgaming.projectmanagmenttool.restapi;

import dev.comgaming.framework.utils.filemanager.DirectoryHandler;
import dev.comgaming.framework.utils.filemanager.FileManager;
import org.yaml.snakeyaml.Yaml;

import java.util.Map;

public class RestAPIConfig {

    private final String secret;
    private final int port;

    public RestAPIConfig() {
        DirectoryHandler dir = new DirectoryHandler("config");

        if (!dir.existsDirectory("config"))
            throw new IllegalStateException("Config-Verzeichnis fehlt.");

        FileManager file = new FileManager("config/restapi.yml");

        if (!file.exists())
            throw new IllegalStateException("restapi.yml nicht gefunden.");

        try {
            Map<String, Object> config = new Yaml().load(
                    String.join(System.lineSeparator(), file.readAll())
            );

            this.secret = config.get("secret").toString();
            this.port = Integer.parseInt(config.get("port").toString());

            if (secret.isBlank() || port <= 0 || port > 65535)
                throw new IllegalStateException("Ungültige REST-API-Konfiguration.");

        } catch (Exception e) {
            throw new IllegalStateException("Fehler beim Laden der restapi.yml.", e);
        }
    }

    public String getSecret() {
        return secret;
    }

    public int getPort() {
        return port;
    }
}
