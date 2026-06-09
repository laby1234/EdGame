package org.example.audio;

import com.almasb.fxgl.audio.Music;
import com.almasb.fxgl.audio.Sound;

import static com.almasb.fxgl.dsl.FXGL.getAssetLoader;
import static com.almasb.fxgl.dsl.FXGL.getAudioPlayer;

public final class AudioManager {

    private static Music currentMusic;
    private static String currentMusicPath;

    private AudioManager() {
    }

    public static void playSound(String assetPath) {
        Sound sound = getAssetLoader().loadSound(assetPath);
        sound.getAudio().setVolume(AudioSettings.getSoundVolume());
        getAudioPlayer().playSound(sound);
    }

    public static void loopMusic(String assetPath) {
        if (assetPath == null || assetPath.isBlank()) {
            return;
        }

        if (assetPath.equals(currentMusicPath) && currentMusic != null) {
            refreshMusicVolume();
            return;
        }

        stopMusic();

        currentMusic = getAssetLoader().loadMusic(assetPath);
        currentMusicPath = assetPath;

        currentMusic.getAudio().setVolume(AudioSettings.getMusicVolume());

        getAudioPlayer().loopMusic(currentMusic);
    }

    public static void stopMusic() {
        if (currentMusic != null) {
            getAudioPlayer().stopMusic(currentMusic);
            currentMusic = null;
        }
        currentMusicPath = null;
    }

    public static void refreshMusicVolume() {
        if (currentMusic != null) {
            currentMusic.getAudio().setVolume(AudioSettings.getMusicVolume());
        }
    }
}