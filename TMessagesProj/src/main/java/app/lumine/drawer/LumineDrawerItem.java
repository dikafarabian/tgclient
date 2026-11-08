/*
 * luminegram - side drawer items.
 * Ids are stored in LumineConfig, never change an existing one.
 */
package app.lumine.drawer;

public enum LumineDrawerItem {

    DIVIDER(-1),
    PROFILE(18),
    ARCHIVE(14),
    BOTS(105),
    NEW_GROUP(2),
    CONTACTS(6),
    NEW_CHANNEL(3),
    CALLS(10),
    SAVED(11),
    SETTINGS(8),
    QR(17),
    GHOST_MODE(107),
    LUMINE_SETTINGS(110);

    private final int id;

    LumineDrawerItem(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static LumineDrawerItem getById(int id) {
        for (LumineDrawerItem item : values()) {
            if (item.id == id) {
                return item;
            }
        }
        return null;
    }
}
