package com.schwinglive.aerialfishingimprovements;

import javax.inject.Inject;
import java.io.IOException;

import lombok.extern.slf4j.Slf4j;
import net.runelite.client.audio.AudioPlayer;

@Slf4j
public class MolchPearlChimeAudio
{
    public enum Sound
    {
        PING("pluck.wav"),
        RING("ding.wav"),
        TICK("tick.wav"),
        CHIME("chime.wav");

        private final String fileName;

        Sound(String fileName)
        {
            this.fileName = fileName;
        }
    }

    private final AudioPlayer audioPlayer;

    @Inject
    public MolchPearlChimeAudio(AudioPlayer audioPlayer)
    {
        this.audioPlayer = audioPlayer;
    }

    public void play(Sound sound, int volume)
    {
        int percentage = Math.max(0, Math.min(100, volume));
        if (percentage == 0)
        {
            return;
        }

        float gain = (float) (20.0 * Math.log10(percentage / 100.0));
        String path = "/com/schwinglive/aerialfishingimprovements/sounds/" + sound.fileName;
        try
        {
            audioPlayer.play(MolchPearlChimeAudio.class, path, gain);
        }
        catch (Exception e)
        {
            log.warn("Unable to play pearl sound {}", sound, e);
        }
    }
}


