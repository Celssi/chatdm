package fi.celssi.chatdm.model;

/**
 * Represents a game resource (rulebook, adventure, etc.) for a tabletop RPG system.
 */
public class ResourceInfo {
    public String name;
    public String path;
    public String type; // "core", "adventure", "setting", "supplement"
    public String description;
    public String gameSystemId;

    public ResourceInfo(String name, String path, String type, String description) {
        this(name, path, type, description, null);
    }

    public ResourceInfo(String name, String path, String type, String description, String gameSystemId) {
        this.name = name;
        this.path = path;
        this.description = description;
        this.type = type;
        this.gameSystemId = gameSystemId;
    }
}
