package com.github.rfsmassacre.heavenrpg.classes;

import com.github.rfsmassacre.heavenrpg.items.PriestBook;
import com.github.rfsmassacre.heavenrpg.players.Origin;
import com.github.rfsmassacre.heavenrpg.spells.PrayerSpell;

public class Priest extends OriginClass
{
    public Priest()
    {
        super("Priest", "&ePriest");

        addSpell(0, PrayerSpell.class);
        setCastItem(PriestBook.class);
    }

    @Override
    public void updateStats(Origin origin)
    {
        clearStats(origin);
        updateStats(origin, "class");
    }
}
