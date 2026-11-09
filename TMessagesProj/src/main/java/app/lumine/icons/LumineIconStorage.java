/*
 * luminegram - icon pack storage: install from a .icons archive, list, find, delete.
 * Based on exteraless/exteraless (app.exteraless.icons.IconPackStorage), rewritten.
 */
package app.lumine.icons;

import org.json.JSONException;
import org.json.JSONObject;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.FileLog;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public final class LumineIconStorage {

    public static final String PACK_EXTENSION = ".icons";
    private static final String METADATA_NAME = "metadata.json";
    private static final String PACKS_DIR = "lumine_icon_packs";

    private static final int MAX_ENTRIES = 3000;
    private static final long MAX_TOTAL_SIZE = 100L * 1024 * 1024;
    private static final long MAX_FILE_SIZE = 10L * 1024 * 1024;
    private static final double MAX_RATIO = 250.0;
    private static final long MAX_METADATA_SIZE = 262144L;

    private static final Object lock = new Object();
    private static volatile Map<String, LumineIconPack> cache;

    private LumineIconStorage() {
    }

    private static final class Failure extends Exception {
        final LumineIconError error;

        Failure(LumineIconError error) {
            super(error.name());
            this.error = error;
        }
    }

    public static File directory() {
        File dir = new File(ApplicationLoader.applicationContext.getFilesDir(), PACKS_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    public static boolean isValidPackId(String id) {
        if (id == null || id.trim().isEmpty()) {
            return false;
        }
        if (id.indexOf('/') >= 0 || id.indexOf('\\') >= 0 || id.indexOf(0) >= 0) {
            return false;
        }
        return !".".equals(id) && !"..".equals(id);
    }

    public static boolean isValidResourceName(String name) {
        if (name == null || name.isEmpty()) {
            return false;
        }
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            boolean ok = (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9') || c == '_';
            if (!ok) {
                return false;
            }
        }
        return true;
    }

    public static List<LumineIconPack> installed() {
        synchronized (lock) {
            Map<String, LumineIconPack> current = cache;
            if (current == null) {
                current = new LinkedHashMap<>();
                File[] dirs = directory().listFiles();
                if (dirs != null) {
                    for (File dir : dirs) {
                        File metadata = new File(dir, METADATA_NAME);
                        if (!dir.isDirectory() || !metadata.isFile()) {
                            continue;
                        }
                        LumineIconPack pack = parseFile(metadata);
                        if (pack != null) {
                            current.put(pack.getId(), pack);
                        }
                    }
                }
                cache = current;
            }
            return new ArrayList<>(current.values());
        }
    }

    public static LumineIconPack find(String id) {
        if (!isValidPackId(id)) {
            return null;
        }
        installed();
        synchronized (lock) {
            return cache == null ? null : cache.get(id);
        }
    }

    public static void invalidate() {
        synchronized (lock) {
            cache = null;
        }
    }

    public static void delete(String id) {
        if (!isValidPackId(id)) {
            return;
        }
        deleteRecursively(new File(directory(), id));
        invalidate();
    }

    public static File resolve(LumineIconPack pack, String relative) {
        if (pack == null || relative == null || relative.isEmpty()) {
            return null;
        }
        File root = pack.getLocation() != null ? pack.getLocation() : new File(directory(), pack.getId());
        try {
            File canonicalRoot = root.getCanonicalFile();
            File target = new File(canonicalRoot, relative).getCanonicalFile();
            if (!target.getPath().startsWith(canonicalRoot.getPath() + File.separator)) {
                return null;
            }
            return target.isFile() ? target : null;
        } catch (Exception e) {
            FileLog.e("lumine icons: cannot resolve icon of " + pack.getId(), e);
            return null;
        }
    }

    public static LumineIconError install(File archive) {
        File temp = null;
        try {
            if (archive == null || !archive.isFile()) {
                return LumineIconError.INVALID_ARCHIVE;
            }
            if (archive.length() > MAX_TOTAL_SIZE) {
                return LumineIconError.ARCHIVE_TOO_LARGE;
            }
            temp = tempDirectory("lumine_icons_install");
            JSONObject metadata = extract(archive, temp);
            LumineIconPack parsed = parse(metadata, temp, temp);
            File target = new File(directory(), parsed.getId());
            deleteRecursively(target);
            if (!temp.renameTo(target)) {
                copyRecursively(temp, target);
                deleteRecursively(temp);
            }
            temp = null;
            invalidate();
            return null;
        } catch (Failure e) {
            FileLog.e("lumine icons: install failed " + e.error);
            return e.error;
        } catch (JSONException e) {
            return LumineIconError.INVALID_METADATA;
        } catch (IOException e) {
            FileLog.e("lumine icons: install failed", e);
            return LumineIconError.STORAGE_ERROR;
        } catch (Exception e) {
            FileLog.e("lumine icons: install failed", e);
            return LumineIconError.UNKNOWN;
        } finally {
            if (temp != null) {
                deleteRecursively(temp);
            }
        }
    }

    public static LumineIconPack parseArchive(File archive) {
        File temp = null;
        try {
            temp = tempDirectory("lumine_icons_preview");
            return parse(extract(archive, temp), temp, temp);
        } catch (Exception e) {
            FileLog.e("lumine icons: cannot read archive", e);
            return null;
        } finally {
            if (temp != null) {
                deleteRecursively(temp);
            }
        }
    }

    private static File tempDirectory(String prefix) throws Failure {
        File dir = new File(ApplicationLoader.applicationContext.getCacheDir(), prefix + "_" + System.currentTimeMillis());
        deleteRecursively(dir);
        if (!dir.mkdirs()) {
            throw new Failure(LumineIconError.STORAGE_ERROR);
        }
        return dir;
    }

    private static JSONObject extract(File archive, File target) throws Exception {
        long total = 0;
        int count = 0;
        String canonicalTarget = target.getCanonicalPath();
        ZipFile zip;
        try {
            zip = new ZipFile(archive);
        } catch (Exception e) {
            throw new Failure(LumineIconError.INVALID_ARCHIVE);
        }
        try {
            for (ZipEntry entry : Collections.list(zip.entries())) {
                if (entry.isDirectory()) {
                    continue;
                }
                if (++count > MAX_ENTRIES) {
                    throw new Failure(LumineIconError.TOO_MANY_FILES);
                }
                String name = entry.getName();
                if (name.contains("..") || name.startsWith("/") || name.startsWith("\\")) {
                    throw new Failure(LumineIconError.INVALID_ARCHIVE);
                }
                File out = new File(target, name);
                if (!out.getCanonicalPath().startsWith(canonicalTarget + File.separator)) {
                    throw new Failure(LumineIconError.INVALID_ARCHIVE);
                }
                long declared = entry.getSize();
                long compressed = entry.getCompressedSize();
                if (declared > MAX_FILE_SIZE) {
                    throw new Failure(LumineIconError.FILE_TOO_LARGE);
                }
                if (declared > 0 && compressed > 0 && (double) declared / compressed > MAX_RATIO) {
                    throw new Failure(LumineIconError.COMPRESSION_RATIO_TOO_HIGH);
                }
                File parent = out.getParentFile();
                if (parent != null && !parent.exists() && !parent.mkdirs()) {
                    throw new Failure(LumineIconError.STORAGE_ERROR);
                }
                long written = 0;
                byte[] buffer = new byte[16 * 1024];
                try (InputStream in = zip.getInputStream(entry); OutputStream os = new FileOutputStream(out)) {
                    int read;
                    while ((read = in.read(buffer)) > 0) {
                        written += read;
                        if (written > MAX_FILE_SIZE) {
                            throw new Failure(LumineIconError.FILE_TOO_LARGE);
                        }
                        if (total + written > MAX_TOTAL_SIZE) {
                            throw new Failure(LumineIconError.ARCHIVE_TOO_LARGE);
                        }
                        os.write(buffer, 0, read);
                    }
                }
                if (compressed > 0 && (double) written / compressed > MAX_RATIO) {
                    throw new Failure(LumineIconError.COMPRESSION_RATIO_TOO_HIGH);
                }
                total += written;
            }
        } finally {
            try {
                zip.close();
            } catch (Exception ignore) {
            }
        }
        File metadata = new File(target, METADATA_NAME);
        if (!metadata.isFile()) {
            File[] children = target.listFiles();
            if (children == null || children.length != 1 || !children[0].isDirectory()
                    || !new File(children[0], METADATA_NAME).isFile()) {
                throw new Failure(LumineIconError.MISSING_METADATA);
            }
            flatten(children[0], target);
            metadata = new File(target, METADATA_NAME);
            if (!metadata.isFile()) {
                throw new Failure(LumineIconError.MISSING_METADATA);
            }
        }
        if (metadata.length() > MAX_METADATA_SIZE) {
            throw new Failure(LumineIconError.METADATA_TOO_LARGE);
        }
        return new JSONObject(readText(metadata));
    }

    private static void flatten(File nested, File target) {
        File[] files = nested.listFiles();
        if (files == null) {
            return;
        }
        for (File file : files) {
            File dest = new File(target, file.getName());
            if (!file.renameTo(dest)) {
                try {
                    copyRecursively(file, dest);
                } catch (Exception e) {
                    FileLog.e(e);
                }
            }
        }
        deleteRecursively(nested);
    }

    private static LumineIconPack parseFile(File file) {
        try {
            if (file.length() > MAX_METADATA_SIZE) {
                return null;
            }
            return parse(new JSONObject(readText(file)), file.getParentFile(), file.getParentFile());
        } catch (Exception e) {
            FileLog.e("lumine icons: bad metadata " + file.getParentFile(), e);
            return null;
        }
    }

    private static LumineIconPack parse(JSONObject json, File root, File location) throws Exception {
        String name = json.optString("packName", "");
        String id = json.optString("packId", "");
        if (name.isEmpty() || !isValidPackId(id)) {
            throw new Failure(LumineIconError.INVALID_METADATA);
        }
        Map<String, String> icons = new LinkedHashMap<>();
        JSONObject iconsJson = json.optJSONObject("icons");
        if (iconsJson != null) {
            String canonicalRoot = root.getCanonicalPath();
            Iterator<String> keys = iconsJson.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                String value = iconsJson.optString(key, null);
                if (value == null || value.isEmpty() || !isValidResourceName(key)) {
                    continue;
                }
                File resolved = new File(root, value).getCanonicalFile();
                if (!resolved.getPath().startsWith(canonicalRoot + File.separator)) {
                    continue;
                }
                icons.put(key, resolved.getPath().substring(canonicalRoot.length() + 1).replace(File.separatorChar, '/'));
            }
        }
        return new LumineIconPack(id, name, json.optString("author", ""), json.optString("version", "1.0"), icons, location);
    }

    private static String readText(File file) throws IOException {
        try (InputStream in = new FileInputStream(file)) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int read;
            while ((read = in.read(buffer)) > 0) {
                out.write(buffer, 0, read);
            }
            return out.toString("UTF-8");
        }
    }

    private static void copyRecursively(File source, File target) throws IOException {
        if (source.isDirectory()) {
            target.mkdirs();
            File[] children = source.listFiles();
            if (children != null) {
                for (File child : children) {
                    copyRecursively(child, new File(target, child.getName()));
                }
            }
            return;
        }
        try (InputStream in = new FileInputStream(source); OutputStream out = new FileOutputStream(target)) {
            byte[] buffer = new byte[8192];
            int read;
            while ((read = in.read(buffer)) > 0) {
                out.write(buffer, 0, read);
            }
        }
    }

    private static void deleteRecursively(File file) {
        if (file == null || !file.exists()) {
            return;
        }
        File[] children = file.isDirectory() ? file.listFiles() : null;
        if (children != null) {
            for (File child : children) {
                deleteRecursively(child);
            }
        }
        file.delete();
    }
}
