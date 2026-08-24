package com.jwidori.game;

import android.os.Handler;
import android.os.Looper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class RemoteAdminClient {

    public static final String API_URL = "https://asyboezbqabmzsyumopo.supabase.co/functions/v1/cheesetail-public";

    public static class Config {
        public boolean maintenanceMode = false;
        public String maintenanceMessage = "";
        public int startingCards = 7;
        public int botTurnDelayMs = 650;
        public String latestAnnouncementTitle = "";
        public String latestAnnouncementBody = "";
    }

    public interface ConfigCallback {
        void onResult(Config config);
    }

    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public void fetchConfig(ConfigCallback callback) {
        new Thread(() -> {
            Config config = new Config();
            HttpURLConnection connection = null;
            try {
                connection = (HttpURLConnection) new URL(API_URL).openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(3500);
                connection.setReadTimeout(3500);
                connection.setRequestProperty("Accept", "application/json");
                int status = connection.getResponseCode();
                if (status >= 200 && status < 300) {
                    JSONObject root = new JSONObject(readAll(connection.getInputStream()));
                    JSONObject remote = root.optJSONObject("config");
                    if (remote != null) {
                        config.maintenanceMode = remote.optBoolean("maintenance_mode", false);
                        config.maintenanceMessage = remote.optString("maintenance_message", "");
                        config.startingCards = clamp(remote.optInt("starting_cards", 7), 3, 15);
                        config.botTurnDelayMs = clamp(remote.optInt("bot_turn_delay_ms", 650), 200, 5000);
                    }
                    JSONArray announcements = root.optJSONArray("announcements");
                    if (announcements != null && announcements.length() > 0) {
                        JSONObject latest = announcements.optJSONObject(0);
                        if (latest != null) {
                            config.latestAnnouncementTitle = latest.optString("title", "");
                            config.latestAnnouncementBody = latest.optString("body", "");
                        }
                    }
                }
            } catch (Exception ignored) {
                // Network failure intentionally falls back to safe local defaults.
            } finally {
                if (connection != null) connection.disconnect();
            }
            Config result = config;
            mainHandler.post(() -> callback.onResult(result));
        }, "cheesetail-config").start();
    }

    public void startSession(String sessionId, String installId, String appVersion, int playerCount) {
        JSONObject body = new JSONObject();
        try {
            body.put("action", "session_start");
            body.put("session_id", sessionId);
            body.put("install_id", installId);
            body.put("app_version", appVersion);
            body.put("player_count", playerCount);
        } catch (Exception ignored) {}
        postAsync(body);
    }

    public void endSession(String sessionId, int winner) {
        if (sessionId == null || sessionId.isEmpty()) return;
        JSONObject body = new JSONObject();
        try {
            body.put("action", "session_end");
            body.put("session_id", sessionId);
            body.put("winner", winner);
        } catch (Exception ignored) {}
        postAsync(body);
    }

    private void postAsync(JSONObject body) {
        new Thread(() -> {
            HttpURLConnection connection = null;
            try {
                connection = (HttpURLConnection) new URL(API_URL).openConnection();
                connection.setRequestMethod("POST");
                connection.setConnectTimeout(3000);
                connection.setReadTimeout(3000);
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
                byte[] payload = body.toString().getBytes(StandardCharsets.UTF_8);
                connection.setFixedLengthStreamingMode(payload.length);
                try (OutputStream out = connection.getOutputStream()) {
                    out.write(payload);
                }
                InputStream input = connection.getResponseCode() >= 400 ? connection.getErrorStream() : connection.getInputStream();
                if (input != null) input.close();
            } catch (Exception ignored) {
            } finally {
                if (connection != null) connection.disconnect();
            }
        }, "cheesetail-admin-event").start();
    }

    private static String readAll(InputStream input) throws Exception {
        StringBuilder builder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) builder.append(line);
        }
        return builder.toString();
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
