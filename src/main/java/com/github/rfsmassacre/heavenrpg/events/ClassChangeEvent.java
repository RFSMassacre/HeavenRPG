package com.github.rfsmassacre.heavenrpg.events;

import com.github.rfsmassacre.heavenrpg.classes.OriginClass;
import com.github.rfsmassacre.heavenrpg.players.Origin;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;


public class ClassChangeEvent extends Event implements Cancellable
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
    @Setter
    private Class<? extends OriginClass> originClass;
    private boolean cancel;

    public ClassChangeEvent(Origin origin, Class<? extends OriginClass> originClass)
    {
        this.origin = origin;
        this.originClass = originClass;
        this.cancel = false;
    }

    public OriginClass getOriginClass()
    {
        return OriginClass.getClass(originClass);
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
