package com.github.rfsmassacre.heavenrpg.events;

import com.github.rfsmassacre.heavenrpg.spells.Spell;
import org.bukkit.entity.LivingEntity;

public class SpellCastEvent extends SpellEvent
{
    public SpellCastEvent(LivingEntity caster, Spell spell)
    {
        super(caster, spell);
    }
}
