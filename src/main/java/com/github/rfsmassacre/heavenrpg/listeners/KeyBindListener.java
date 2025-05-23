package com.github.rfsmassacre.heavenrpg.listeners;

import com.github.rfsmassacre.heavenrpg.players.Origin;
import com.github.rfsmassacre.heavenrpg.spells.Spell;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class KeyBindListener implements Listener
{
    private static final long THRESHOLD = 400L;
    private static final Map<Origin.KeyBind, Map<UUID, Long>> LAST_CLICKED = new HashMap<>();

    private static void clicked(Origin.KeyBind keyBind, UUID playerId)
    {
        Map<UUID, Long> lastClicked = LAST_CLICKED.getOrDefault(keyBind, new HashMap<>());
        lastClicked.put(playerId, System.currentTimeMillis());
        LAST_CLICKED.put(keyBind, lastClicked);
    }

    public static long getClicked(Origin.KeyBind keyBind, UUID playerId)
    {
        Map<UUID, Long> lastClicked = LAST_CLICKED.getOrDefault(keyBind, new HashMap<>());
        return lastClicked.getOrDefault(playerId, 0L);
    }

    public static void removeClick(Origin.KeyBind keyBind, UUID playerId)
    {
        Map<UUID, Long> lastClicked = LAST_CLICKED.get(keyBind);
        if (lastClicked != null)
        {
            lastClicked.remove(playerId);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onAnimation(PlayerDropItemEvent event)
    {
        clicked(Origin.KeyBind.DROP, event.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDrop(PlayerDropItemEvent event)
    {
        Player player = event.getPlayer();
        Origin origin = Origin.getOrigin(player.getUniqueId(), Origin.class);
        if (origin == null)
        {
            return;
        }

        Spell spell = origin.getSpell(Origin.KeyBind.DROP);
        if (spell != null)
        {
            event.setCancelled(true);
            spell.cast(player);
        }
    }

    @EventHandler
    public void onSwap(PlayerSwapHandItemsEvent event)
    {
        Player player = event.getPlayer();
        Origin origin = Origin.getOrigin(player.getUniqueId());
        if (origin == null)
        {
            return;
        }

        long lastClicked = getClicked(Origin.KeyBind.SWAP, player.getUniqueId());
        if (System.currentTimeMillis() - lastClicked < THRESHOLD)
        {
            return;
        }

        Spell spell = origin.getSpell(Origin.KeyBind.SWAP);
        if (spell != null)
        {
            event.setCancelled(true);
            spell.cast(player);
            clicked(Origin.KeyBind.SWAP, player.getUniqueId());
        }
    }

    @EventHandler
    public void onClick(PlayerInteractEvent event)
    {
        Player player = event.getPlayer();
        long dropTime = getClicked(Origin.KeyBind.DROP, player.getUniqueId());
        if (System.currentTimeMillis() - dropTime < THRESHOLD)
        {
            return;
        }

        removeClick(Origin.KeyBind.DROP, player.getUniqueId());
        Origin origin = Origin.getOrigin(player.getUniqueId());
        if (origin == null)
        {
            return;
        }

        if (!EquipmentSlot.HAND.equals(event.getHand()))
        {
            return;
        }

        if (event.getAction().isLeftClick())
        {
            long lastClicked = getClicked(Origin.KeyBind.LEFT_CLICK, player.getUniqueId());
            if (System.currentTimeMillis() - lastClicked < THRESHOLD)
            {
                return;
            }

            Spell spell = origin.getSpell(Origin.KeyBind.LEFT_CLICK);
            if (spell != null)
            {
                event.setCancelled(true);
                spell.cast(player);
                clicked(Origin.KeyBind.LEFT_CLICK, player.getUniqueId());
            }
        }
        else if (event.getAction().isRightClick())
        {
            long lastClicked = getClicked(Origin.KeyBind.RIGHT_CLICK, player.getUniqueId());
            if (System.currentTimeMillis() - lastClicked < THRESHOLD)
            {
                return;
            }

            Spell spell = origin.getSpell(Origin.KeyBind.RIGHT_CLICK);
            if (spell != null)
            {
                event.setCancelled(true);
                spell.cast(player);
                clicked(Origin.KeyBind.RIGHT_CLICK, player.getUniqueId());
            }
        }
    }

    @EventHandler
    public void onDoubleSneak(PlayerToggleSneakEvent event)
    {
        Player player = event.getPlayer();
        Origin origin = Origin.getOrigin(player.getUniqueId());
        if (origin == null)
        {
            return;
        }

        long lastClicked = getClicked(Origin.KeyBind.SNEAK, player.getUniqueId());
        if (System.currentTimeMillis() - lastClicked < THRESHOLD)
        {
            return;
        }

        Spell spell = origin.getSpell(Origin.KeyBind.SNEAK);
        if (spell != null)
        {
            event.setCancelled(true);
            spell.cast(player);
            clicked(Origin.KeyBind.SNEAK, player.getUniqueId());
        }
    }
}
