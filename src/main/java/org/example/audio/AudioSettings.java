package org.example.audio;

public final class AudioSettings {

    private static double soundVolume = 0.7;
    private static double musicVolume = 0.7;

    private AudioSettings() {
    }

    public static double getSoundVolume() {
        return soundVolume;
    }

    public static void setSoundVolume(double value) {
        soundVolume = clamp(value);
    }

    public static double getMusicVolume() {
        return musicVolume;
    }

    public static void setMusicVolume(double value) {
        musicVolume = clamp(value);
    }

    private static double clamp(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }
}