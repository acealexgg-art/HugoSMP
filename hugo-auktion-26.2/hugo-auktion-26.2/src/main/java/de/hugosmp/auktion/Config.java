package de.hugosmp.auktion;

import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/** Speichert Mindestgebot und Dauer in config/hugoauktion.properties. */
public final class Config {

    public static final int[] DURATIONS = {30, 60, 120, 300, 600, 900};

    public static long minBid = 0;
    public static int durationIndex = 1;

    private Config() {
    }

    private static Path file() {
        return FabricLoader.getInstance().getConfigDir().resolve("hugoauktion.properties");
    }

    public static void load() {
        Path f = file();
        if (!Files.exists(f)) return;
        try (InputStream in = Files.newInputStream(f)) {
            Properties p = new Properties();
            p.load(in);
            minBid = Math.max(0, Long.parseLong(p.getProperty("minBid", "0")));
            int idx = Integer.parseInt(p.getProperty("durationIndex", "1"));
            durationIndex = Math.min(Math.max(idx, 0), DURATIONS.length - 1);
        } catch (IOException | NumberFormatException ignored) {
        }
    }

    public static void save() {
        Properties p = new Properties();
        p.setProperty("minBid", Long.toString(minBid));
        p.setProperty("durationIndex", Integer.toString(durationIndex));
        try (OutputStream out = Files.newOutputStream(file())) {
            p.store(out, "Hugo Auktion");
        } catch (IOException ignored) {
        }
    }
}
