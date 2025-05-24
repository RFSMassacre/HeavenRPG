package com.github.rfsmassacre.heavenrpg.races;

import com.github.rfsmassacre.heavenrpg.players.Origin;

public final class Human extends OriginRace
{
    Human()
    {
        super("Human", "&fHuman");
    }

    @Override
    public void updateStats(Origin origin)
    {
        clearStats(origin);
    }
}
