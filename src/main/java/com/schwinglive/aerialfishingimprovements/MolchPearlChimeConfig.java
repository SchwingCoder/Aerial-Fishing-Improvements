
package com.schwinglive.aerialfishingimprovements;

import java.awt.Color;

import net.runelite.client.config.Alpha;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Range;

@ConfigGroup("molchpearlchime")
public interface MolchPearlChimeConfig extends Config
{
	// Audio section
	@ConfigSection(
			name = "Audio",
			description = "Configure pearl notification sounds",
			position = 0
	)
	String audioSection = "audioSection";

	@ConfigItem(
			keyName = "enabled",
			name = "Enable sound",
			description = "Play a sound when gaining Molch Pearls",
			section = audioSection,
			position = 0
	)
	default boolean enabled()
	{
		return true;
	}

	@ConfigItem(
			keyName = "sound",
			name = "Sound effect",
			description = "Select the sound played when receiving pearls",
			section = audioSection,
			position = 1
	)
	default PearlSound sound()
	{
		return PearlSound.RING;
	}

	@Range(min = 0, max = 100)
	@ConfigItem(
			keyName = "volume",
			name = "Volume",
			description = "Volume of pearl notification sounds",
			section = audioSection,
			position = 2
	)
	default int volume()
	{
		return 25;
	}

	// Visual section
	@ConfigSection(
			name = "Visual",
			description = "Customize the floating pearl notification",
			position = 1
	)
	String visualSection = "visualSection";



	// Developer section — intended for development builds
	@ConfigSection(
			name = "Developer",
			description = "Development and testing tools",
			position = 2,
			closedByDefault = true
	)
	String developerSection = "developerSection";


	@ConfigItem(
			keyName = "testNotification",
			name = "Test Notification",
			description = "Play the selected sound and preview the floating pearl notification",
			section = developerSection,
			position = 0
	)
	default boolean testNotification()
	{
		return false;
	}

	@ConfigItem(
			keyName = "visualNotification",
			name = "Enable visual notification",
			description = "Display floating text when gaining pearls",
			section = visualSection,
			position = 0
	)
	default boolean visualNotification()
	{
		return true;
	}


	@ConfigItem(
			keyName = "visualFont",
			name = "Font",
			description = "Choose the OSRS font for the floating notification",
			section = visualSection,
			position = 1
	)
	default OSRSFont visualFont()
	{
		return OSRSFont.PLAIN_11;
	}


	@ConfigItem(
			keyName = "visualFontSize",
			name = "Font size",
			description = "Size of the floating notification text",
			section = visualSection,
			position = 2
	)
	@Range(min = 8, max = 32)
	default int visualFontSize()
	{
		return 16;
	}

	@ConfigItem(
			keyName = "riseDistance",
			name = "Rise distance",
			description = "How far the notification travels upward, in pixels",
			section = visualSection,
			position = 3
	)
	@Range(min = 0, max = 100)
	default int riseDistance()
	{
		return 16;
	}

	@ConfigItem(
			keyName = "animationDuration",
			name = "Animation duration",
			description = "Time for the notification to rise and fade, in milliseconds",
			section = visualSection,
			position = 4
	)
	@Range(min = 250, max = 3000)
	default int animationDuration()
	{
		return 1000;
	}

	@ConfigItem(
			keyName = "fadeOut",
			name = "Fade out",
			description = "Gradually fade the notification away",
			section = visualSection,
			position = 5
	)
	default boolean fadeOut()
	{
		return true;
	}

	@Alpha
	@ConfigItem(
			keyName = "textColor",
			name = "Text color",
			description = "Color of the floating notification",
			section = visualSection,
			position = 6
	)
	default Color textColor() {
		return new Color(255, 220, 120);
	}

	enum PearlSound
	{
		PING("Pluck"),
		RING("Ding"),
		TICK("Tick"),
		CHIME("Chime");

		private final String label;

		PearlSound(String label)
		{
			this.label = label;
		}

		@Override
		public String toString()
		{
			return label;
		}
	}


	enum OSRSFont
	{
		QUILL_8("Quill 8", "RuneScape-Quill-8.ttf"),
		BOLD_12("Bold 12", "RuneScape-Bold-12.ttf"),
		PLAIN_11("Plain 11", "RuneScape-Plain-11.ttf"),
		PLAIN_12("Plain 12", "RuneScape-Plain-12.ttf");

		private final String label;
		private final String resourceName;

		OSRSFont(String label, String resourceName)
		{
			this.label = label;
			this.resourceName = resourceName;
		}

		public String getResourceName()
		{
			return resourceName;
		}

		@Override
		public String toString()
		{
			return label;
		}
	}

}
