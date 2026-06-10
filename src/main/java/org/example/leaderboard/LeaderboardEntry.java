package org.example.leaderboard;

public record LeaderboardEntry(String name, int score, double timeSeconds) {

    public String formattedTime() {
        int totalTenths = (int) Math.round(timeSeconds * 10);
        int minutes = totalTenths / 600;
        int seconds = (totalTenths / 10) % 60;
        int tenths = totalTenths % 10;
        return String.format("%dm %02d.%ds", minutes, seconds, tenths);
    }
}
