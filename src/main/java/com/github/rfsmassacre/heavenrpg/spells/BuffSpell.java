package com.github.rfsmassacre.heavenrpg.spells;

import com.github.rfsmassacre.heavenlibrary.interfaces.LocaleData;
import com.github.rfsmassacre.heavenrpg.HeavenRPG;
import com.github.rfsmassacre.heavenrpg.events.RaceChangeEvent;
import com.github.rfsmassacre.heavenrpg.events.SpellCastEvent;
import com.github.rfsmassacre.heavenrpg.events.SpellCooldownEvent;
import com.github.rfsmassacre.heavenrpg.players.Origin;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public abstract class BuffSpell extends Spell
{
    private final transient Set<BukkitTask> tasks;

    protected Map<UUID, Long> casters;
    protected int duration, effectInterval;
    protected boolean toggle;

    public BuffSpell(String internalName)
    {
        super(internalName);

        this.tasks = new HashSet<>();
        this.casters = new HashMap<>();
        this.duration = 1;
        this.effectInterval = 20;
        this.toggle = false;
    }

    public boolean isActive(LivingEntity entity)
    {
        return casters.containsKey(entity.getUniqueId());
    }

    public long getDuration(LivingEntity entity)
    {
        Long lastCast = casters.get(entity.getUniqueId());
        if (lastCast != null)
        {
            return ((this.duration / 20L) * 1000L) - (System.currentTimeMillis() - lastCast);
        }

        return 0L;
    }

    public void activateTimer()
    {
        tasks.add(new BukkitRunnable()
        {
            @Override
            public void run()
            {
                for (UUID casterId : casters.keySet())
                {
                    if (Bukkit.getEntity(casterId) instanceof LivingEntity caster)
                    {
                        effect(caster);
                    }
                }
            }
        }.runTaskTimer(HeavenRPG.getInstance(), 0L, effectInterval));
    }

    public void deactivateTimers()
    {
        for (BukkitTask task : tasks)
        {
            task.cancel();
        }

        tasks.clear();
    }

    public void end(LivingEntity entity)
    {
        if (!deactivate(entity))
        {
            return;
        }

        casters.remove(entity.getUniqueId());
        cooldowns.put(entity.getUniqueId(), System.currentTimeMillis());
        Bukkit.getPluginManager().callEvent(new SpellCooldownEvent(entity, this));
    }

    public abstract boolean deactivate(LivingEntity entity);

    @Override
    public boolean cast(LivingEntity entity)
    {
        if (isActive(entity) && toggle)
        {
            end(entity);
            return true;
        }

        if (!canCast(entity))
        {
            return false;
        }

        long cooldown = cooldowns.getOrDefault(entity.getUniqueId(), 0L);
        if (System.currentTimeMillis() - cooldown > this.cooldown)
        {
            SpellCastEvent event = new SpellCastEvent(entity, this);
            if (event.callEvent() && activate(entity))
            {
                if (!entity.isValid())
                {
                    end(entity);
                    return false;
                }

                casters.put(entity.getUniqueId(), System.currentTimeMillis());
                if (duration <= 0)
                {
                    return true;
                }

                Bukkit.getScheduler().runTaskLater(HeavenRPG.getInstance(), () -> end(entity), duration);
                return true;
            }
        }
        else
        {
            sendActionMessage(entity, cooldownMessage, "{spell}", getDisplayName(), "{time}",
                    LocaleData.formatTime((double) getCooldown(entity) / 1000));
        }

        return false;
    }

    @EventHandler(ignoreCancelled = true)
    public void onRaceChange(RaceChangeEvent event)
    {
        Origin origin = event.getOrigin();
        if (origin == null)
        {
            return;
        }

        Player player = origin.getPlayer();
        if (player != null && isActive(player))
        {
            end(player);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent event)
    {
        Player player = event.getPlayer();
        if (isActive(player))
        {
            end(player);
        }
    }
}
