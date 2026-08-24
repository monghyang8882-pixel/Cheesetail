package com.jwidori.game;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.UUID;

public final class AdminRuntime {

    private static final Object LOCK = new Object();
    private static RemoteAdminClient client;
    private static String installId = "";
    private static RemoteAdminClient.Config config = new RemoteAdminClient.Config();
    private static String activeSessionId = "";

    private AdminRuntime() {}

    public static void init(Context context) {
        synchronized (LOCK) {
            if (client != null) return;
            client = new RemoteAdminClient();
            SharedPreferences prefs = context.getSharedPreferences("cheesetail_admin_runtime", Context.MODE_PRIVATE);
            installId = prefs.getString("install_id", "");
            if (installId == null || installId.isEmpty()) {
                installId = UUID.randomUUID().toString();
                prefs.edit().putString("install_id", installId).apply();
            }
            client.fetchConfig(remote -> {
                synchronized (LOCK) {
                    config = remote;
                }
            });
        }
    }

    public static void refresh(RemoteAdminClient.ConfigCallback callback) {
        RemoteAdminClient local;
        synchronized (LOCK) { local = client; }
        if (local == null) {
            callback.onResult(getConfig());
            return;
        }
        local.fetchConfig(remote -> {
            synchronized (LOCK) { config = remote; }
            callback.onResult(remote);
        });
    }

    public static RemoteAdminClient.Config getConfig() {
        synchronized (LOCK) { return config; }
    }

    public static int getStartingCards(int fallback) {
        synchronized (LOCK) {
            int value = config == null ? fallback : config.startingCards;
            return Math.max(3, Math.min(15, value));
        }
    }

    public static void gameStarted(int playerCount) {
        synchronized (LOCK) {
            if (client == null) return;
            activeSessionId = UUID.randomUUID().toString();
            client.startSession(activeSessionId, installId, BuildConfig.VERSION_NAME, playerCount);
        }
    }

    public static void gameFinished(int winner) {
        synchronized (LOCK) {
            if (client == null || activeSessionId == null || activeSessionId.isEmpty()) return;
            client.endSession(activeSessionId, winner);
            activeSessionId = "";
        }
    }
}
