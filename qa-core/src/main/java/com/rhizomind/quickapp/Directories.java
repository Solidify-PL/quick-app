package com.rhizomind.quickapp;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Directories {

    public static File ensureCacheDirectoryExists(File cacheDir) {
        try {
            // Tworzy katalog, jeśli nie istnieje
            Files.createDirectories(cacheDir.toPath());
        } catch (Exception e) {
            throw new RuntimeException("Could not create directory: " + cacheDir, e);
        }
        return cacheDir;
    }

    public static File getDefaultCacheDir() {
        String os = System.getProperty("os.name").toLowerCase();
        Path cachePath;

        if (os.contains("linux") || os.contains("mac")) {
            // Linux/macOS: Używamy ~/.cache/quick-app
            String userHome = System.getProperty("user.home");
            cachePath = Paths.get(userHome, ".cache", "quick-app");
        } else if (os.contains("win")) {
            // Windows: Próbujemy użyć %LocalAppData%\quick-app
            String localAppData = System.getenv("LOCALAPPDATA");
            if (localAppData != null && !localAppData.isEmpty()) {
                cachePath = Paths.get(localAppData, "quick-app");
            } else {
                // Fallback: Używamy katalogu domowego i tworzymy .cache/quick-app
                String userHome = System.getProperty("user.home");
                cachePath = Paths.get(userHome, ".cache", "quick-app");
            }
        } else {
            throw new RuntimeException("Unsupported operating system: " + os);
        }

        return cachePath.toFile();
    }

    public static Path ensureConfigDirectoryExists() {
        String os = System.getProperty("os.name").toLowerCase();
        Path configPath;

        if (os.contains("linux") || os.contains("mac")) {
            // Linux/macOS: Używamy XDG_CONFIG_HOME lub ~/.config
            String configHome = System.getenv("XDG_CONFIG_HOME");
            String userHome = System.getProperty("user.home");
            configPath = Paths.get(configHome != null && !configHome.isEmpty() ? configHome
                    : Paths.get(userHome, ".config").toString(), "quick-app");
        } else if (os.contains("win")) {
            // Windows: Próbujemy użyć %APPDATA%\quick-app
            String appData = System.getenv("APPDATA");
            if (appData != null && !appData.isEmpty()) {
                configPath = Paths.get(appData, "quick-app");
            } else {
                // Fallback: Używamy katalogu domowego i tworzymy .config/quick-app
                String userHome = System.getProperty("user.home");
                configPath = Paths.get(userHome, ".config", "quick-app");
            }
        } else {
            throw new RuntimeException("Unsupported operating system: " + os);
        }

        try {
            // Tworzy katalog, jeśli nie istnieje
            Files.createDirectories(configPath);
            return configPath;
        } catch (Exception e) {
            throw new RuntimeException("Could not create directory: " + configPath, e);
        }
    }


    public static File ensureConfigFileExists(File file) throws IOException {
        file.getParentFile().mkdirs();
        file.createNewFile();
        return file;
    }

    public static File defaultConfigFile() {
        return new File(
                ensureConfigDirectoryExists().toFile(),
                "repositories.yaml"
        );
    }
}
