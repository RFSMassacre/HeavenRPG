package com.github.rfsmassacre.heavenrpg.events;

import com.github.rfsmassacre.heavenrpg.spells.Spell;
import org.bukkit.entity.LivingEntity;

public class SpellCooldownEvent extends SpellEvent
{
    public SpellCooldownEvent(LivingEntity caster, Spell spell)
    {
        super(caster, spell);
    }
}
