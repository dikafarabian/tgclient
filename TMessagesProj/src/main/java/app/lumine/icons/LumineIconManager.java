/*
 * luminegram - icon pack engine.
 * Decides which drawable replaces a resource and keeps the decoded bitmaps.
 * Based on exteraless/exteraless (app.exteraless.icons.IconPackManager), rewritten.
 */
package app.lumine.icons;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.LruCache;

import androidx.annotation.Nullable;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.Utilities;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class LumineIconManager {

    private static final LumineIconManager instance = new LumineIconManager();

    public static LumineIconManager get() {
        return instance;
    }

    private static final class Source {
        final LumineIconPack pack;
        final String relative;

        Source(LumineIconPack pack, String relative) {
            this.pack = pack;
            this.relative = relative;
        }
    }

    private static final Map<String, String> ALIASES = new HashMap<>();

    static {
        ALIASES.put("ic_ab_search", "outline_header_search");
        ALIASES.put("message", "filled_profile_message_24");
        ALIASES.put("mute", "filled_profile_mute_24");
        ALIASES.put("unmute", "filled_profile_unmute_24");
        ALIASES.put("video", "filled_profile_video_24");
        ALIASES.put("block", "filled_profile_stop_24");
        ALIASES.put("join", "filled_profile_member_24");
        ALIASES.put("story", "filled_profile_story");
        ALIASES.put("msg_bots", "msg_bot");
    }

    private static final Set<String> BLACKLIST = new HashSet<>(Arrays.asList(
            "bar_selector", "blockpanel", "circle", "circle_big", "dice", "dino_pic",
            "camera_btn", "cancel_big", "etg_splash", "ev_minus", "ev_plus",
            "chats_archive_arrow", "chats_archive_box", "chats_archive_muted", "chats_archive_pin",
            "chats_widget_preview", "contacts_widget_preview", "fast_scroll_empty", "field_carret_empty"
    ));

    private final List<LumineIconPack> activePacks = new ArrayList<>();
    private final ConcurrentHashMap<Integer, String> names = new ConcurrentHashMap<>();
    private final Set<Integer> missing = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private final LruCache<Long, Bitmap> bitmaps;

    private volatile Map<String, Source> sources = Collections.emptyMap();
    private volatile boolean initialized;

    private LumineIconManager() {
        int maxKb = (int) (Runtime.getRuntime().maxMemory() / 1024);
        bitmaps = new LruCache<Long, Bitmap>(Math.max(2048, maxKb / 16)) {
            @Override
            protected int sizeOf(Long key, Bitmap value) {
                return value.getByteCount() / 1024;
            }
        };
    }

    public static boolean isBlacklisted(String name) {
        if (name == null) {
            return true;
        }
        return BLACKLIST.contains(name)
                || name.contains("avd")
                || name.startsWith("abc_")
                || name.endsWith("_solar")
                || name.endsWith("_remix")
                || name.contains("$")
                || name.contains("animationpin")
                || name.contains("googlepay")
                || name.contains("shadow")
                || name.startsWith("ic_monochrome")
                || name.startsWith("nocover")
                || name.startsWith("gradient_")
                || name.startsWith("stickers_back_")
                || name.startsWith("media_doc_")
                || name.startsWith("loading_animation")
                || name.startsWith("intro_")
                || name.startsWith("minibubble_")
                || name.startsWith("book_")
                || name.startsWith("call_")
                || name.startsWith("groupsintro")
                || name.startsWith("profile_level")
                || name.startsWith("widget_")
                || name.startsWith("zoom_slide")
                || name.startsWith("zoom_round")
                || name.startsWith("popup_fixed_alert")
                || name.startsWith("search_dark");
    }

    public void ensureInitialized() {
        if (initialized) {
            return;
        }
        synchronized (this) {
            if (initialized) {
                return;
            }
            initialized = true;
        }
        reloadNow();
    }

    public void reload() {
        initialized = true;
        Utilities.globalQueue.postRunnable(this::reloadNow);
    }

    public void reloadNow() {
        try {
            LumineIconStorage.invalidate();
            List<LumineIconPack> packs = new ArrayList<>();
            if (LumineIconConfig.enabled()) {
                for (String id : LumineIconConfig.activePackIds()) {
                    LumineIconPack pack = LumineIconStorage.find(id);
                    if (pack != null) {
                        packs.add(pack);
                    }
                }
            }
            Map<String, Source> built = new HashMap<>();
            for (int i = packs.size() - 1; i >= 0; i--) {
                LumineIconPack pack = packs.get(i);
                for (Map.Entry<String, String> entry : pack.getIcons().entrySet()) {
                    String key = entry.getKey();
                    if (isBlacklisted(key)) {
                        continue;
                    }
                    built.put(key, new Source(pack, entry.getValue()));
                    String alias = ALIASES.get(key);
                    if (alias != null && !isBlacklisted(alias) && !pack.getIcons().containsKey(alias)) {
                        built.put(alias, new Source(pack, entry.getValue()));
                    }
                }
            }
            synchronized (this) {
                activePacks.clear();
                activePacks.addAll(packs);
                sources = built;
                bitmaps.evictAll();
                missing.clear();
            }
        } catch (Throwable t) {
            FileLog.e("lumine icons: cannot load packs", t);
        }
    }

    public List<LumineIconPack> getActivePacks() {
        synchronized (this) {
            return new ArrayList<>(activePacks);
        }
    }

    public boolean replaces(int resId) {
        if (resId == 0) {
            return false;
        }
        if (LumineIconConfig.enabled()) {
            ensureInitialized();
            String name = resourceName(resId);
            if (name != null && sources.containsKey(name)) {
                return true;
            }
        }
        return LumineIconBase.mapped(resId) != 0;
    }

    public boolean hasReplacements() {
        return !sources.isEmpty();
    }

    public interface InstallCallback {
        void onResult(@Nullable LumineIconError error);
    }

    public void installPack(File archive, InstallCallback callback) {
        Utilities.globalQueue.postRunnable(() -> {
            LumineIconError error = LumineIconStorage.install(archive);
            if (error == null) {
                reloadNow();
            }
            AndroidUtilities.runOnUIThread(() -> callback.onResult(error));
        });
    }

    public void deletePack(String packId, Runnable done) {
        LumineIconConfig.forgetPack(packId);
        Utilities.globalQueue.postRunnable(() -> {
            LumineIconStorage.delete(packId);
            reloadNow();
            AndroidUtilities.runOnUIThread(done);
        });
    }

    @Nullable
    public Drawable drawable(int resId, int density, @Nullable Resources.Theme theme) {
        if (resId == 0 || !LumineIconConfig.enabled()) {
            return null;
        }
        ensureInitialized();
        Map<String, Source> map = sources;
        if (map.isEmpty() || missing.contains(resId)) {
            return null;
        }
        try {
            Resources app = ApplicationLoader.applicationContext.getResources();
            if (density == 0) {
                density = app.getDisplayMetrics().densityDpi;
            }
            long key = ((long) resId << 32) | (density & 0xffffffffL);
            Bitmap bitmap = bitmaps.get(key);
            if (bitmap == null || bitmap.isRecycled()) {
                String name = resourceName(resId);
                Source source = name == null ? null : map.get(name);
                File file = source == null ? null : LumineIconStorage.resolve(source.pack, source.relative);
                if (file == null || isSvg(file.getName())) {
                    missing.add(resId);
                    return null;
                }
                bitmap = decode(app, file.getAbsolutePath(), resId, density, theme);
                if (bitmap == null) {
                    missing.add(resId);
                    return null;
                }
                bitmaps.put(key, bitmap);
            }
            return new BitmapDrawable(app, bitmap);
        } catch (Throwable t) {
            FileLog.e("lumine icons: lookup failed", t);
            return null;
        }
    }

    public int resourceId(String name) {
        if (name == null) {
            return 0;
        }
        int id = lookupId(name);
        if (id != 0) {
            return id;
        }
        String alias = ALIASES.get(name);
        return alias == null ? 0 : lookupId(alias);
    }

    private static int lookupId(String name) {
        try {
            Resources app = ApplicationLoader.applicationContext.getResources();
            return app.getIdentifier(name, "drawable", ApplicationLoader.applicationContext.getPackageName());
        } catch (Throwable t) {
            return 0;
        }
    }

    public static String newPackId(String name) {
        StringBuilder slug = new StringBuilder();
        for (int i = 0; i < name.length() && slug.length() < 24; i++) {
            char c = Character.toLowerCase(name.charAt(i));
            if ((c >= 'a' && c <= 'z') || (c >= '0' && c <= '9')) {
                slug.append(c);
            } else if (slug.length() > 0 && slug.charAt(slug.length() - 1) != '_') {
                slug.append('_');
            }
        }
        String base = slug.length() == 0 ? "pack" : slug.toString();
        String id = base + "_" + Long.toString(System.currentTimeMillis(), 36);
        while (LumineIconStorage.find(id) != null) {
            id = base + "_" + Long.toString(System.nanoTime(), 36);
        }
        return id;
    }

    @Nullable
    public LumineIconPack createPack(String name, String author, String version) {
        String id = newPackId(name);
        File dir = new File(LumineIconStorage.directory(), id);
        LumineIconPack pack = new LumineIconPack(id, name, author, version, Collections.emptyMap(), dir);
        if (!LumineIconStorage.saveMetadata(pack)) {
            return null;
        }
        return LumineIconStorage.find(id);
    }

    public boolean saveIcon(String packId, int resId, File source) {
        LumineIconPack pack = LumineIconStorage.find(packId);
        String name = resourceName(resId);
        if (pack == null || name == null || source == null || !source.isFile() || isBlacklisted(name)) {
            return false;
        }
        try {
            BitmapFactory.Options bounds = new BitmapFactory.Options();
            bounds.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(source.getAbsolutePath(), bounds);
            if (bounds.outWidth <= 0 || bounds.outHeight <= 0 || (long) bounds.outWidth * bounds.outHeight > 100_000_000L) {
                return false;
            }
            String extension = "image/webp".equals(bounds.outMimeType) ? "webp" : "image/jpeg".equals(bounds.outMimeType) ? "jpg" : "png";
            File dir = new File(LumineIconStorage.directory(), pack.getId());
            dir.mkdirs();
            File destination = new File(dir, name + "." + extension);
            try (java.io.InputStream in = new java.io.FileInputStream(source); java.io.OutputStream out = new java.io.FileOutputStream(destination)) {
                byte[] buffer = new byte[8192];
                int read;
                while ((read = in.read(buffer)) > 0) {
                    out.write(buffer, 0, read);
                }
            }
            String previous = pack.getIcons().get(name);
            Map<String, String> icons = new java.util.LinkedHashMap<>(pack.getIcons());
            icons.put(name, destination.getName());
            LumineIconPack updated = new LumineIconPack(pack.getId(), pack.getName(), pack.getAuthor(), pack.getVersion(), icons, pack.getLocation());
            if (!LumineIconStorage.saveMetadata(updated)) {
                return false;
            }
            if (previous != null && !previous.equals(destination.getName())) {
                File old = LumineIconStorage.resolve(pack, previous);
                if (old != null) {
                    old.delete();
                }
            }
            reloadNow();
            return true;
        } catch (Throwable t) {
            FileLog.e("lumine icons: cannot save icon", t);
            return false;
        }
    }

    public boolean resetIcon(String packId, int resId) {
        LumineIconPack pack = LumineIconStorage.find(packId);
        String name = resourceName(resId);
        if (pack == null || name == null) {
            return false;
        }
        String relative = pack.getIcons().get(name);
        if (relative == null) {
            return false;
        }
        File file = LumineIconStorage.resolve(pack, relative);
        Map<String, String> icons = new java.util.LinkedHashMap<>(pack.getIcons());
        icons.remove(name);
        LumineIconPack updated = new LumineIconPack(pack.getId(), pack.getName(), pack.getAuthor(), pack.getVersion(), icons, pack.getLocation());
        if (!LumineIconStorage.saveMetadata(updated)) {
            return false;
        }
        if (file != null) {
            file.delete();
        }
        reloadNow();
        return true;
    }

    @Nullable
    public String resourceName(int resId) {
        String cached = names.get(resId);
        if (cached != null) {
            return cached;
        }
        try {
            String name = ApplicationLoader.applicationContext.getResources().getResourceEntryName(resId);
            if (name != null) {
                names.put(resId, name);
            }
            return name;
        } catch (Exception e) {
            return null;
        }
    }

    public static boolean isSvg(@Nullable String path) {
        return path != null && path.toLowerCase(java.util.Locale.ROOT).endsWith(".svg");
    }

    @Nullable
    private static Bitmap decode(Resources app, String path, int resId, int density, @Nullable Resources.Theme theme) {
        try {
            int width = AndroidUtilities.dp(24);
            int height = width;
            try {
                Drawable original = app.getDrawableForDensity(resId, density, theme);
                if (original != null && original.getIntrinsicWidth() > 0 && original.getIntrinsicHeight() > 0) {
                    width = original.getIntrinsicWidth();
                    height = original.getIntrinsicHeight();
                }
            } catch (Throwable ignore) {
            }
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(path, options);
            int outWidth = options.outWidth;
            int outHeight = options.outHeight;
            if (outWidth <= 0 || outHeight <= 0 || (long) outWidth * outHeight > 100_000_000L) {
                return null;
            }
            options.inSampleSize = 1;
            while (outHeight / 2 / options.inSampleSize >= height && outWidth / 2 / options.inSampleSize >= width) {
                options.inSampleSize *= 2;
            }
            long maxPixels = Math.max((long) width * height, 1024L * 1024L);
            while (true) {
                long w = (outWidth + options.inSampleSize - 1L) / options.inSampleSize;
                long h = (outHeight + options.inSampleSize - 1L) / options.inSampleSize;
                if (w * h <= maxPixels) {
                    break;
                }
                options.inSampleSize *= 2;
            }
            options.inJustDecodeBounds = false;
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            Bitmap decoded = BitmapFactory.decodeFile(path, options);
            if (decoded == null) {
                return null;
            }
            Bitmap result = decoded;
            if (decoded.getWidth() != width || decoded.getHeight() != height) {
                result = Bitmap.createScaledBitmap(decoded, width, height, true);
                if (result != decoded) {
                    decoded.recycle();
                }
            }
            result.setDensity(density);
            return result;
        } catch (OutOfMemoryError e) {
            FileLog.e("lumine icons: out of memory for " + path);
            return null;
        } catch (Throwable t) {
            FileLog.e("lumine icons: cannot decode " + path, t);
            return null;
        }
    }
}
