package com.rhizomind.quickapp;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Directories {

    public static Path createCacheDirectory() {
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
            throw new RuntimeException("Nieobsługiwany system operacyjny: " + os);
        }

        try {
            // Tworzy katalog, jeśli nie istnieje
            Files.createDirectories(cachePath);
            return cachePath;
        } catch (Exception e) {
            throw new RuntimeException("Nie udało się utworzyć katalogu: " + cachePath, e);
        }
    }

    public static Path createConfigDirectory() {
        String os = System.getProperty("os.name").toLowerCase();
        Path configPath;

        if (os.contains("linux") || os.contains("mac")) {
            // Linux/macOS: Używamy XDG_CONFIG_HOME lub ~/.config
            String configHome = System.getenv("XDG_CONFIG_HOME");
            String userHome = System.getProperty("user.home");
            configPath = Paths.get(configHome != null && !configHome.isEmpty() ? configHome : Paths.get(userHome, ".config").toString(), "quick-app");
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
            throw new RuntimeException("Nieobsługiwany system operacyjny: " + os);
        }

        try {
            // Tworzy katalog, jeśli nie istnieje
            Files.createDirectories(configPath);
            return configPath;
        } catch (Exception e) {
            throw new RuntimeException("Nie udało się utworzyć katalogu: " + configPath, e);
        }
    }


}
