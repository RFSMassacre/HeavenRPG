package com.github.rfsmassacre.heavenrpg.classes;

import com.github.rfsmassacre.heavenrpg.players.Origin;

public class Priest extends OriginClass
{
    public Priest()
    {
        super("Priest", "&ePriest");
    }

    @Override
    public void updateStats(Origin origin)
    {
        clearStats(origin);
        updateStats(origin, "class");
    }
}
