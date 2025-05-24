package com.github.rfsmassacre.heavenrpg.spells;

import org.bukkit.Registry;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionEffectTypeCategory;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

public class PrayerSpell extends Spell
{
    private final String noBlessingsFound, blessingReceived, blessingSent;
    private final int durationMin, durationMax, amplifyMin, amplifyMax;

    public PrayerSpell()
    {
        super("Prayer");

        this.noBlessingsFound = getString("no-blessings-message", "&f{target}&c has every blessing!");
        this.blessingReceived = getString("blessing-received",
                "&f{sender}&e has blessed you with &b{potion} {amplify}&e!");
        this.blessingSent = getString("blessing-received",
                "&7You blessed &f{sender}&7 with &b{potion} {amplify}&7!");
        this.durationMin = getInt("duration.min", 3000);
        this.durationMax = getInt("duration.max", 6000);
        this.amplifyMin = getInt("amplify.min", 0);
        this.amplifyMax = getInt("amplify.max", 1);
    }

    @Override
    public boolean activate(LivingEntity entity)
    {
        LivingEntity target = getTarget(entity);
        if (target == null)
        {
            sendActionMessage(entity, noTargetMessage);
            return false;
        }

        List<PotionEffectType> potionTypes = new ArrayList<>(Registry.POTION_EFFECT_TYPE.stream()
                .filter((potionType) ->
                        potionType.getCategory().equals(PotionEffectTypeCategory.BENEFICIAL) ||
                                potionType.getCategory().equals(PotionEffectTypeCategory.NEUTRAL))
                .toList());
        potionTypes.removeIf(target::hasPotionEffect);
        if (potionTypes.isEmpty())
        {
            sendActionMessage(entity, noBlessingsFound, "{target}", getDisplayName(target));
            return false;
        }

        SecureRandom random = new SecureRandom();
        PotionEffectType potionType = potionTypes.get(random.nextInt(potionTypes.size()));
        int duration = random.nextInt(durationMin, durationMax);
        int amplify = random.nextInt(amplifyMin, amplifyMax);
        target.addPotionEffect(new PotionEffect(potionType, duration, amplify));
        sendActionMessage(target, blessingReceived, "{sender}", getDisplayName(entity), "{potion}",
                potionType.getName(), "{amplify}", Integer.toString(amplify + 1));
        sendActionMessage(entity, blessingSent, "{target}", getDisplayName(target),
                potionType.getName(), "{amplify}", Integer.toString(amplify + 1));
        return true;
    }
}
