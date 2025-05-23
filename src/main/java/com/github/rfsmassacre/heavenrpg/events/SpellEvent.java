package com.github.rfsmassacre.heavenrpg.events;

import com.github.rfsmassacre.heavenrpg.spells.Spell;
import lombok.Getter;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public abstract class SpellEvent extends Event implements Cancellable
{
    //Handler List
    private static final HandlerList HANDLERS = new HandlerList();

    @Override
    public @NotNull HandlerList getHandlers()
    {
        return HANDLERS;
    }

    public static HandlerList getHandlerList()
    {
        return HANDLERS;
    }

    @Getter
    private final LivingEntity caster;
    @Getter
    private final Spell spell;
    private boolean cancel;

    public SpellEvent(LivingEntity caster, Spell spell)
    {
        this.caster = caster;
        this.spell = spell;
    }

    @Override
    public boolean isCancelled()
    {
        return cancel;
    }

    @Override
    public void setCancelled(boolean cancel)
    {
        this.cancel = cancel;
    }
}
