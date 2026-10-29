
package com.schwinglive.aerialfishingimprovements;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.EnumMap;
import java.util.Map;

import javax.inject.Inject;

import net.runelite.api.Client;
import net.runelite.api.Player;
import net.runelite.api.Point;
import net.runelite.api.gameval.ItemID;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

public class MolchPearlChimeOverlay extends Overlay
{
    private static final Color OUTLINE_COLOR = new Color(45, 32, 15);
    private static final int ICON_SIZE = 28;
    private static final int ICON_SPACING = 0;
    private static final int VERTICAL_OFFSET = 35;

    private final Client client;
    private final MolchPearlChimeConfig config;
    private final BufferedImage pearlIcon;

    private final Map<MolchPearlChimeConfig.OSRSFont, Font> fonts =
            new EnumMap<>(MolchPearlChimeConfig.OSRSFont.class);

    private long notificationStartTime;
    private int pearlQuantity;

    @Inject
    public MolchPearlChimeOverlay(
            Client client,
            MolchPearlChimeConfig config,
            ItemManager itemManager)
    {
        this.client = client;
        this.config = config;
        this.pearlIcon = itemManager.getImage(ItemID.AERIAL_FISHING_PEARL);

        loadFonts();

        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.ABOVE_SCENE);
    }

    private void loadFonts()
    {
        for (MolchPearlChimeConfig.OSRSFont fontChoice
                : MolchPearlChimeConfig.OSRSFont.values())
        {
            String resource = "/fonts/" + fontChoice.getResourceName();

            try (InputStream stream = getClass().getResourceAsStream(resource))
            {
                if (stream == null)
                {
                    System.err.println(
                            "Molch Pearl Chime: Font not found: " + resource);
                    continue;
                }

                Font font = Font.createFont(Font.TRUETYPE_FONT, stream);
                fonts.put(fontChoice, font);
            }
            catch (IOException | java.awt.FontFormatException ex)
            {
                System.err.println(
                        "Molch Pearl Chime: Could not load font: " + resource);
                ex.printStackTrace();
            }
        }
    }

    public void showPearls(int quantity)
    {
        if (quantity <= 0)
        {
            return;
        }

        pearlQuantity = quantity;
        notificationStartTime = System.currentTimeMillis();
    }

    @Override
    public java.awt.Dimension render(Graphics2D graphics)
    {
        if (!config.visualNotification() || pearlQuantity <= 0)
        {
            return null;
        }

        long elapsed = System.currentTimeMillis() - notificationStartTime;
        int duration = config.animationDuration();

        if (duration <= 0 || elapsed >= duration)
        {
            pearlQuantity = 0;
            return null;
        }

        Player player = client.getLocalPlayer();

        if (player == null)
        {
            return null;
        }

        float progress = Math.min(1.0f, (float) elapsed / duration);
        int rise = (int) (config.riseDistance() * progress);
        float opacity = config.fadeOut() ? 1.0f - progress : 1.0f;

        String text = "+" + pearlQuantity;

        // Get the player's canvas position and anchor the notification above it.
        Point playerPoint = player.getCanvasTextLocation(
                graphics,
                "",
                player.getLogicalHeight() + VERTICAL_OFFSET
        );

        if (playerPoint == null)
        {
            return null;
        }

        Font baseFont = fonts.get(config.visualFont());

        if (baseFont == null)
        {
            baseFont = new Font(
                    "SansSerif",
                    Font.BOLD,
                    config.visualFontSize()
            );
        }

        Font selectedFont = baseFont.deriveFont(
                (float) config.visualFontSize()
        );

        Graphics2D g = (Graphics2D) graphics.create();

        try
        {
            g.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            g.setComposite(AlphaComposite.getInstance(
                    AlphaComposite.SRC_OVER,
                    Math.max(0.0f, Math.min(1.0f, opacity))
            ));

            g.setFont(selectedFont);

            int textWidth = g.getFontMetrics().stringWidth(text);
            int totalWidth = textWidth + ICON_SPACING + ICON_SIZE;

            // Center the entire text-and-icon group over the player.
            int startX = playerPoint.getX() - totalWidth / 2;
            int textX = startX;
            int iconX = textX + textWidth + ICON_SPACING;
            int y = playerPoint.getY() - rise;

            // Center the icon vertically against the text's visual height.
            int textHeight = g.getFontMetrics().getAscent();
            int iconY = y - (textHeight + ICON_SIZE) / 2;

            if (pearlIcon != null)
            {
                g.drawImage(
                        pearlIcon,
                        iconX,
                        iconY,
                        ICON_SIZE,
                        ICON_SIZE,
                        null
                );
            }

            // Draw the outlined text.
            g.setColor(OUTLINE_COLOR);
            g.drawString(text, textX - 1, y);
            g.drawString(text, textX + 1, y);
            g.drawString(text, textX, y - 1);
            g.drawString(text, textX, y + 1);

            // Draw the main text.
            g.setColor(config.textColor());
            g.drawString(text, textX, y);
        }
        finally
        {
            g.dispose();
        }

        return null;
    }
}
