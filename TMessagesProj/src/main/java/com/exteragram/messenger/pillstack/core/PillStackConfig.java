package com.exteragram.messenger.pillstack.core;

import java.util.ArrayList;

public final class PillStackConfig {
    private PillStackConfig() {
    }

    public static ArrayList<Integer> getActivePills() {
        app.luminegram.pillstack.PillStackConfig.loadConfig(false);
        return app.luminegram.pillstack.PillStackConfig.getActivePills();
    }

    public static ArrayList<Integer> getHiddenPills() {
        app.luminegram.pillstack.PillStackConfig.loadConfig(false);
        return app.luminegram.pillstack.PillStackConfig.getHiddenPills();
    }

    public static int getLastActivePillId() {
        app.luminegram.pillstack.PillStackConfig.loadConfig(false);
        return app.luminegram.pillstack.PillStackConfig.lastActivePillId.Int();
    }
}
