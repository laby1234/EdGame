package org.example.leaderboard;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class LeaderboardStore {

    private static final Path LEADERBOARD_FILE = Path.of("leaderboard.csv");
    private static final int MAX_ENTRIES = 10;
    private static final Comparator<LeaderboardEntry> BEST_FIRST = Comparator
            .comparingInt(LeaderboardEntry::score).reversed()
            .thenComparingDouble(LeaderboardEntry::timeSeconds);

    private LeaderboardStore() {
    }

    public static List<LeaderboardEntry> loadTopEntries() {
        List<LeaderboardEntry> entries = loadEntries();
        return sortAndLimit(entries);
    }

    public static List<LeaderboardEntry> addEntry(LeaderboardEntry entry) {
        List<LeaderboardEntry> entries = loadEntries();
        entries.add(entry);
        List<LeaderboardEntry> topEntries = sortAndLimit(entries);
        saveEntries(topEntries);
        return topEntries;
    }

    public static String sanitizeName(String name) {
        String cleaned = name == null ? "" : name.trim().replaceAll("[^A-Za-z0-9 _-]", "");
        if (cleaned.isBlank()) {
            return "Player";
        }
        return cleaned.length() > 16 ? cleaned.substring(0, 16) : cleaned;
    }

    private static List<LeaderboardEntry> loadEntries() {
        List<LeaderboardEntry> entries = new ArrayList<>();
        if (!Files.exists(LEADERBOARD_FILE)) {
            return entries;
        }

        try {
            for (String line : Files.readAllLines(LEADERBOARD_FILE, StandardCharsets.UTF_8)) {
                String[] parts = line.split("\\|", 3);
                if (parts.length != 3) {
                    continue;
                }

                entries.add(new LeaderboardEntry(
                        sanitizeName(parts[0]),
                        Integer.parseInt(parts[1]),
                        Double.parseDouble(parts[2])
                ));
            }
        } catch (IOException | NumberFormatException e) {
            System.err.println("Could not read leaderboard: " + e.getMessage());
        }

        return entries;
    }

    private static void saveEntries(List<LeaderboardEntry> entries) {
        List<String> lines = new ArrayList<>();
        for (LeaderboardEntry entry : entries) {
            lines.add(sanitizeName(entry.name()) + "|" + entry.score() + "|" + entry.timeSeconds());
        }

        try {
            Files.write(
                    LEADERBOARD_FILE,
                    lines,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING
            );
        } catch (IOException e) {
            System.err.println("Could not save leaderboard: " + e.getMessage());
        }
    }

    private static List<LeaderboardEntry> sortAndLimit(List<LeaderboardEntry> entries) {
        return entries.stream()
                .sorted(BEST_FIRST)
                .limit(MAX_ENTRIES)
                .toList();
    }
}
