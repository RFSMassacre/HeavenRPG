package com.github.rfsmassacre.heavenrpg.data;

import com.github.rfsmassacre.heavenlibrary.paper.managers.PaperYamlStorage;
import com.github.rfsmassacre.heavenrpg.HeavenRPG;
import com.github.rfsmassacre.heavenrpg.races.OriginRace;

public class RaceYaml extends PaperYamlStorage<OriginRace>
{
    public RaceYaml()
    {
        super(HeavenRPG.getInstance(), "races", OriginRace.class);
    }
}
