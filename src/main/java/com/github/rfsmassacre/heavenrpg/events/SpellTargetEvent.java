package com.github.rfsmassacre.heavenrpg.events;

import com.github.rfsmassacre.heavenrpg.spells.Spell;
import lombok.Getter;
import org.bukkit.entity.LivingEntity;

@Getter
public class SpellTargetEvent extends SpellEvent
{
    private final LivingEntity target;

    public SpellTargetEvent(LivingEntity caster, Spell spell, LivingEntity target)
    {
        super(caster, spell);

        this.target = target;
    }
}
