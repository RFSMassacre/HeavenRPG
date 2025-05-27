package com.github.rfsmassacre.heavenrpg.events;

import com.github.rfsmassacre.heavenrpg.players.Origin;
import com.github.rfsmassacre.heavenrpg.races.OriginRace;
import lombok.Getter;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class RaceChangeEvent extends Event implements Cancellable
{
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
    private final Origin origin;
    private String originRace;
    private boolean cancel;

    public RaceChangeEvent(Origin origin, String originRace)
    {
        this.origin = origin;
        this.originRace = originRace;
        this.cancel = false;
    }

    public void setOriginRace(OriginRace originRace)
    {
        this.originRace = originRace.getName();
    }

    public OriginRace getOriginRace()
    {
        return OriginRace.getRace(originRace);
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
