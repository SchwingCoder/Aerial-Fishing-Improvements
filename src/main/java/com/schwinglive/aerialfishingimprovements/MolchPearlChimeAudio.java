
package com.schwinglive.aerialfishingimprovements;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.EnumMap;
import java.util.Map;

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

    private final Map<Sound, Clip> clips = new EnumMap<>(Sound.class);

    public void load()
    {
        close();

        for (Sound sound : Sound.values())
        {
            String path = "/com/schwinglive/aerialfishingimprovements/sounds/" + sound.fileName;

            try (InputStream resource = getClass().getResourceAsStream(path))
            {
                if (resource == null)
                {
                    throw new IllegalStateException(
                            "Missing audio resource: " + path);
                }

                try (AudioInputStream source =
                             AudioSystem.getAudioInputStream(
                                     new BufferedInputStream(resource)))
                {
                    System.out.println(
                            "Molch Pearl Chime: Loading original WAV format: "
                                    + source.getFormat());

                    Clip clip = AudioSystem.getClip();
                    clip.open(source);
                    clips.put(sound, clip);
                }
            }
            catch (Exception e)
            {
                System.err.println(
                        "Molch Pearl Chime: Could not load "
                                + path + ": " + e.getMessage());
            }
        }
    }

    public synchronized void play(Sound sound, int volume)
    {
        Clip clip = clips.get(sound);
        int percentage = Math.max(0, Math.min(100, volume));

        if (clip == null || percentage == 0)
        {
            return;
        }

        try
        {
            if (clip.isRunning())
            {
                clip.stop();
            }

            if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN))
            {
                FloatControl gain = (FloatControl) clip.getControl(
                        FloatControl.Type.MASTER_GAIN);
                float requestedGain = percentage == 100
                        ? 0.0f
                        : (float) (20.0 * Math.log10(percentage / 100.0));
                gain.setValue(Math.max(
                        gain.getMinimum(),
                        Math.min(gain.getMaximum(), requestedGain)));
            }

            clip.setFramePosition(0);
            clip.start();
        }
        catch (Exception e)
        {
            System.err.println(
                    "Molch Pearl Chime: Could not play sound: "
                            + e.getMessage());
        }
    }

    public synchronized void close()
    {
        for (Clip clip : clips.values())
        {
            clip.stop();
            clip.close();
        }

        clips.clear();
    }
}
