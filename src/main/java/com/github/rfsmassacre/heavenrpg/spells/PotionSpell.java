package com.github.rfsmassacre.heavenrpg.spells;

import com.github.rfsmassacre.heavenlibrary.interfaces.LocaleData;
import com.github.rfsmassacre.heavenrpg.utils.RomanNumeralUtil;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.key.Key;
import org.bukkit.Bukkit;
import org.bukkit.Registry;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionEffectTypeCategory;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class PotionSpell extends Spell
{
    private String noPotionFound, potionReceived, potionSent;
    private int durationMin, durationMax, amplifyMin, amplifyMax;
    private String potionTypeName;
    private String potionCategoryName;
    private boolean override;

    public PotionSpell()
    {
        super();

        this.noPotionFound = "&f{target}&c has every potion effect!";
        this.potionReceived = "&f{sender}&e has given you &b{potion} {amplify}&e!";
        this.potionSent = "&7You gave &f{target}&7 &b{potion} {amplify}&7!";
        this.durationMin = 3000;
        this.durationMax = 6000;
        this.amplifyMin = 0;
        this.amplifyMax = 1;
        this.override = false;
    }

    public PotionSpell(String name)
    {
        super(name);

        this.noPotionFound = "&f{target}&c has every potion effect!";
        this.potionReceived = "&f{sender}&e has given you &b{potion} {amplify}&e!";
        this.potionSent = "&7You gave &f{target}&7 &b{potion} {amplify}&7!";
        this.durationMin = 3000;
        this.durationMax = 6000;
        this.amplifyMin = 0;
        this.amplifyMax = 1;
        this.override = false;
    }

    public PotionSpell(String name, String displayName, PotionEffectType potionType)
    {
        this(name);

        setDisplayName(displayName);
        this.potionTypeName = potionType.getKey().value().toUpperCase();
    }

    public PotionSpell(String name, String displayName, PotionEffectTypeCategory potionCategory)
    {
        this(name);

        setDisplayName(displayName);
        this.potionCategoryName = potionCategory.name();
    }

    @Override
    public boolean activate(LivingEntity entity)
    {
        LivingEntity target = entity;
        if (!targetSelf)
        {
            target = getTarget(entity);
        }

        if (target == null)
        {
            sendActionMessage(entity, noTargetMessage, "{spell}", getDisplayName());
            return false;
        }

        List<PotionEffectType> potionTypes = new ArrayList<>(Registry.POTION_EFFECT_TYPE.stream().toList());
        if (potionTypeName != null)
        {
            try
            {
                PotionEffectType potionType = Registry.POTION_EFFECT_TYPE.getOrThrow(Key.key(Key.MINECRAFT_NAMESPACE,
                        potionTypeName.toLowerCase()));
                potionTypes.removeIf((otherType) -> !otherType.equals(potionType));
            }
            catch (Exception exception)
            {
                //Do nothing.
                Bukkit.getLogger().severe(potionTypeName + " is not a valid potion effect type!");
            }
        }
        else if (potionCategoryName != null)
        {
            try
            {
                PotionEffectTypeCategory potionCategory = PotionEffectTypeCategory.valueOf(
                        potionCategoryName.toUpperCase());
                potionTypes.removeIf((otherType) -> !otherType.getCategory().equals(potionCategory));
            }
            catch (IllegalArgumentException exception)
            {
                //Do nothing.
                Bukkit.getLogger().severe(potionCategoryName + " is not a valid potion effect category!");
            }
        }

        if (!override)
        {
            potionTypes.removeIf(target::hasPotionEffect);
        }

        if (potionTypes.isEmpty())
        {
            sendActionMessage(entity, noPotionFound, "{target}", getDisplayName(target));
            return false;
        }

        SecureRandom random = new SecureRandom();
        PotionEffectType potionType = potionTypes.get(random.nextInt(0, potionTypes.size()));
        int duration = random.nextInt(durationMin, durationMax + 1);
        if (potionType.isInstant())
        {
            duration = 1;
        }

        int amplify = random.nextInt(amplifyMin, amplifyMax + 1);
        String romanNumeral = RomanNumeralUtil.toRomanNumeral(amplify + 1);
        target.addPotionEffect(new PotionEffect(potionType, duration, amplify));
        sendActionMessage(target, potionReceived, "{sender}", getDisplayName(entity), "{potion}",
                LocaleData.capitalize(potionType.getKey().value()), "{amplify}", romanNumeral);
        if (!entity.equals(target))
        {
            sendActionMessage(entity, potionSent, "{target}", getDisplayName(target), "{potion}",
                    LocaleData.capitalize(potionType.getKey().value()), "{amplify}", romanNumeral);
        }

        return true;
    }
}
