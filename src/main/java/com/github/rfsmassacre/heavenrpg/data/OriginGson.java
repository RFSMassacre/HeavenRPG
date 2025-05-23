package com.github.rfsmassacre.heavenrpg.data;

import com.github.rfsmassacre.heavenlibrary.paper.managers.PaperGsonManager;
import com.github.rfsmassacre.heavenrpg.HeavenRPG;
import com.github.rfsmassacre.heavenrpg.players.Origin;

public class OriginGson extends PaperGsonManager<Origin>
{
    public OriginGson()
    {
        super(HeavenRPG.getInstance(), "players", Origin.class);
    }
}
