package com.schwinglive.aerialfishingimprovements;

import com.google.inject.Provides;
import javax.inject.Inject;

import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.ItemContainer;
import net.runelite.api.NPC;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.gameval.InventoryID;
import net.runelite.api.gameval.ItemID;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

@PluginDescriptor(
		name = "Aerial Fishing Improvements",
		description = "Adds configurable audio and visual notifications during Aerial Fishing"
)
public class MolchPearlChimePlugin extends Plugin
{
	private static final int MOLCH_ISLAND_NPC_ID = 8523;

	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private MolchPearlChimeConfig config;

	@Inject
	private MolchPearlChimeOverlay pearlOverlay;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private MolchPearlChimeAudio audio;
	private int previousPearlCount;
	private boolean aerialFishing;

	@Provides
	MolchPearlChimeConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(MolchPearlChimeConfig.class);
	}

	@Override
	protected void startUp()
	{
		previousPearlCount = 0;
		aerialFishing = false;
		overlayManager.add(pearlOverlay);
	}

	@Override
	protected void shutDown()
	{
		overlayManager.remove(pearlOverlay);

		previousPearlCount = 0;
		aerialFishing = false;
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		if (client.getGameState() != GameState.LOGGED_IN)
		{
			setActivity(false);
			previousPearlCount = 0;
			return;
		}

		boolean foundMolchNpc = false;
		for (NPC npc : client.getNpcs())
		{
			if (npc.getId() == MOLCH_ISLAND_NPC_ID)
			{
				foundMolchNpc = true;
				break;
			}
		}

		setActivity(foundMolchNpc);
	}

	private void setActivity(boolean active)
	{
		if (active == aerialFishing)
		{
			return;
		}

		aerialFishing = active;
		if (active)
		{
			// Establish a baseline so existing pearls are not counted as gains.
			previousPearlCount = getInventoryPearlCount();
		}
	}

	@Subscribe
	public void onItemContainerChanged(ItemContainerChanged event)
	{
		if (event.getContainerId() != InventoryID.INV
				|| client.getGameState() != GameState.LOGGED_IN)
		{
			return;
		}

		ItemContainer container = event.getItemContainer();
		if (container == null)
		{
			return;
		}

		int currentPearlCount = Math.max(0, container.count(ItemID.AERIAL_FISHING_PEARL));
		if (!aerialFishing)
		{
			previousPearlCount = currentPearlCount;
			return;
		}

		if (currentPearlCount < previousPearlCount)
		{
			previousPearlCount = currentPearlCount;
			return;
		}

		int gained = currentPearlCount - previousPearlCount;
		previousPearlCount = currentPearlCount;
		if (gained <= 0)
		{
			return;
		}

		pearlOverlay.showPearls(gained);
		playPearlSound();
	}

	private int getInventoryPearlCount()
	{
		ItemContainer inventory = client.getItemContainer(InventoryID.INV);
		if (inventory == null)
		{
			return 0;
		}

		return Math.max(0, inventory.count(ItemID.AERIAL_FISHING_PEARL));
	}

	private void playPearlSound()
	{
		if (!config.enabled() || config.volume() <= 0 || audio == null)
		{
			return;
		}

		MolchPearlChimeAudio.Sound sound;
		switch (config.sound())
		{
			case PING:
				sound = MolchPearlChimeAudio.Sound.PING;
				break;
			case RING:
				sound = MolchPearlChimeAudio.Sound.RING;
				break;
			case TICK:
				sound = MolchPearlChimeAudio.Sound.TICK;
				break;
			case CHIME:
			default:
				sound = MolchPearlChimeAudio.Sound.CHIME;
				break;
		}

		audio.play(sound, config.volume());
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (!"molchpearlchime".equals(event.getGroup()))
		{
			return;
		}

		if ("testNotification".equals(event.getKey()) && config.testNotification())
		{
			clientThread.invokeLater(() ->
			{
				if (client.getGameState() == GameState.LOGGED_IN)
				{
					pearlOverlay.showPearls(1);
					playPearlSound();
				}
			});
		}
	}
}

