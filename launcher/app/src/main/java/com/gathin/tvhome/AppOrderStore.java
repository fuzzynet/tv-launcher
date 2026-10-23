package com.gathin.tvhome;

import android.content.Context;
import android.content.SharedPreferences;

/** One preferences string. The ordering rules live in {@link AppOrder}, which has no Android types. */
final class AppOrderStore {

    private static final String PREFS = "home_order";
    private static final String KEY = "state";

    private final SharedPreferences prefs;

    AppOrderStore(Context ctx) {
        prefs = ctx.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    AppOrder.State load() {
        return AppOrder.decode(prefs.getString(KEY, ""));
    }

    void save(AppOrder.State state) {
        prefs.edit().putString(KEY, AppOrder.encode(state)).apply();
    }
}
