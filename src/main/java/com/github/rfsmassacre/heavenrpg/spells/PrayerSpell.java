package com.github.rfsmassacre.heavenrpg.spells;

import com.github.rfsmassacre.heavenlibrary.interfaces.LocaleData;
import com.github.rfsmassacre.heavenrpg.utils.RomanNumeralUtil;
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
        this.blessingSent = getString("blessing-sent",
                "&7You blessed &f{target}&7 with &b{potion} {amplify}&7!");
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
                        potionType.getCategory().equals(PotionEffectTypeCategory.BENEFICIAL))
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
        if (potionType.isInstant())
        {
            duration = 1;
        }

        int amplify = random.nextInt(amplifyMin, amplifyMax + 1);
        String romanNumeral = RomanNumeralUtil.toRomanNumeral(amplify + 1);
        target.addPotionEffect(new PotionEffect(potionType, duration, amplify));
        sendActionMessage(target, blessingReceived, "{sender}", getDisplayName(entity), "{potion}",
                LocaleData.capitalize(potionType.getKey().value()), "{amplify}", romanNumeral);
        sendActionMessage(entity, blessingSent, "{target}", getDisplayName(target), "{potion}",
                LocaleData.capitalize(potionType.getKey().value()), "{amplify}", romanNumeral);
        return true;
    }
}
