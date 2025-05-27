package com.github.rfsmassacre.heavenrpg.data;

import com.github.rfsmassacre.heavenlibrary.paper.managers.PaperYamlStorage;
import com.github.rfsmassacre.heavenrpg.HeavenRPG;
import com.github.rfsmassacre.heavenrpg.spells.Spell;

public class SpellYaml extends PaperYamlStorage<Spell>
{
    public SpellYaml()
    {
        super(HeavenRPG.getInstance(), "spells", Spell.class);
    }
}
