package com.github.rfsmassacre.heavenrpg.listeners;

import com.github.rfsmassacre.heavenrpg.events.ClassChangeEvent;
import com.github.rfsmassacre.heavenrpg.events.RaceChangeEvent;
import com.github.rfsmassacre.heavenrpg.players.Origin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.time.Instant;

public class OriginListener implements Listener
{
    @EventHandler(ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event)
    {
        Player player = event.getPlayer();
        Origin.getOrLoadOrigin(player.getUniqueId(), (origin) ->
        {
            if (origin == null)
            {
                origin = new Origin(player);
            }

            origin.setLastLogin(Instant.now().toEpochMilli());
            Origin.addOrigin(origin);
            Origin.saveOrigin(origin, false);
        });
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerQuit(PlayerQuitEvent event)
    {
        Player player = event.getPlayer();
        Origin origin = Origin.getOrigin(player.getUniqueId());
        if (origin == null)
        {
            return;
        }

        origin.setLastHealth(player.getHealth());
        origin.setLastLogin(Instant.now().toEpochMilli());
        Origin.saveOrigin(origin, true);
        Origin.removeOrigin(player.getUniqueId());
    }

    /**
     *  Each origin has a race depending on their race.
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPlayerFoodChange(FoodLevelChangeEvent event)
    {
        Player player = (Player) event.getEntity();
        Origin origin = Origin.getOrigin(player.getUniqueId());
        if (origin == null || event.getItem() == null || player.getFoodLevel() > event.getFoodLevel())
        {
            return;
        }

        if (!origin.getDiet().contains(event.getItem().getType()))
        {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onRaceChange(RaceChangeEvent event)
    {
        Origin origin = event.getOrigin();
        origin.getOriginRace().clearStats(origin);
    }

    @EventHandler(ignoreCancelled = true)
    public void onClassChange(ClassChangeEvent event)
    {
        Origin origin = event.getOrigin();
        origin.getOriginClass().clearStats(origin);
    }
}
