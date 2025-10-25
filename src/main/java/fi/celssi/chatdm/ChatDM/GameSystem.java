package fi.celssi.chatdm.ChatDM;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a tabletop RPG game system with its associated resources.
 */
public class GameSystem {
    private final String id;
    private final String name;
    private final String description;
    private final Map<String, GameResourceOracle.ResourceInfo> resources;

    public GameSystem(String id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.resources = new HashMap<>();
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Map<String, GameResourceOracle.ResourceInfo> getResources() {
        return resources;
    }

    public void addResource(String resourceId, GameResourceOracle.ResourceInfo resource) {
        resources.put(resourceId, resource);
    }

    public GameResourceOracle.ResourceInfo getResource(String resourceId) {
        return resources.get(resourceId);
    }

    public boolean hasResource(String resourceId) {
        return resources.containsKey(resourceId);
    }
}
