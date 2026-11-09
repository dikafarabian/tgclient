/*
 * luminegram - icon pack model.
 * Based on exteraless/exteraless (app.exteraless.icons.IconPack), rewritten.
 */
package app.lumine.icons;

import org.json.JSONObject;

import java.io.File;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class LumineIconPack {

    private final String id;
    private final String name;
    private final String author;
    private final String version;
    private final Map<String, String> icons;
    private final File location;

    public LumineIconPack(String id, String name, String author, String version, Map<String, String> icons, File location) {
        this.id = id;
        this.name = name;
        this.author = author == null ? "" : author;
        this.version = version == null ? "1.0" : version;
        this.icons = icons == null ? Collections.emptyMap() : Collections.unmodifiableMap(new LinkedHashMap<>(icons));
        this.location = location;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getAuthor() {
        return author;
    }

    public String getVersion() {
        return version;
    }

    public Map<String, String> getIcons() {
        return icons;
    }

    public File getLocation() {
        return location;
    }

    public int getIconCount() {
        return icons.size();
    }

    public JSONObject toJson() throws Exception {
        JSONObject root = new JSONObject();
        root.put("schemaVersion", 1);
        root.put("packId", id);
        root.put("packName", name);
        root.put("author", author);
        root.put("version", version);
        JSONObject iconsJson = new JSONObject();
        for (Map.Entry<String, String> entry : icons.entrySet()) {
            iconsJson.put(entry.getKey(), entry.getValue());
        }
        root.put("icons", iconsJson);
        return root;
    }
}
