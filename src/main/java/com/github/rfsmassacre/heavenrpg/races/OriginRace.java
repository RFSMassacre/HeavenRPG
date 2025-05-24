package com.github.rfsmassacre.heavenrpg.races;

import com.github.rfsmassacre.heavenlibrary.interfaces.LocaleData;
import com.github.rfsmassacre.heavenlibrary.paper.configs.PaperConfiguration;
import com.github.rfsmassacre.heavenrpg.HeavenRPG;
import com.github.rfsmassacre.heavenrpg.players.Origin;
import com.github.rfsmassacre.heavenrpg.spells.Spell;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.key.Key;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.*;

@Getter
@Setter
public abstract class OriginRace
{
    /**
     * Implement this if your race has a transforming mechanic to make it streamlined.
     */
    public interface Transform
    {
        boolean transform(Origin origin, boolean form);

        boolean isTransformed(Origin origin);

        default boolean transform(Origin origin)
        {
            return transform(origin, !isTransformed(origin));
        }
    }

    private static final String RACE_KEY = "heavenrpg_race";
    public static final Map<String, OriginRace> CACHE = new HashMap<>();

    public static void initialize()
    {
        CACHE.clear();
        registerRace(new Human());
    }

    public static void registerRace(OriginRace originRace)
    {
        CACHE.put(originRace.name, originRace);
    }

    public static void unregisterRace(String name)
    {
        CACHE.remove(name);
    }

    public static OriginRace getRace(String name)
    {
        return CACHE.get(name);
    }

    public static <T extends OriginRace> T getRace(Class<T> clazz)
    {
        for (OriginRace originRace : CACHE.values())
        {
            if (clazz.isInstance(originRace))
            {
                return clazz.cast(originRace);
            }
        }

        return null;
    }

    public static Set<OriginRace> getRaces()
    {
        return new HashSet<>(CACHE.values());
    }

    private final String name;
    private final Map<Integer, Class<? extends Spell>> spells;
    private String displayName;

    public OriginRace(String name)
    {
        this.name = name;
        this.spells = new HashMap<>();
    }

    public void setDisplayName(String displayName)
    {
        this.displayName = LocaleData.undoFormat(displayName);
    }

    public <T extends Spell> void registerSpell(int level, Class<T> clazz)
    {
        spells.put(level, clazz);
    }

    public Spell getSpell(int level)
    {
        return Spell.getSpell(spells.get(level));
    }

    public abstract void updateStats(Origin origin);

    protected void updateStats(Origin origin, String statsKey)
    {
        clearStats(origin);
        Player player = origin.getPlayer();
        if (player == null)
        {
            return;
        }

        PaperConfiguration config = HeavenRPG.getInstance().getConfiguration();
        ConfigurationSection section = config.getSection("attributes." + name.toLowerCase() + "." +
                statsKey);
        if (section == null)
        {
            return;
        }

        for (String key : section.getKeys(false))
        {
            try
            {
                AttributeModifier.Operation operation = AttributeModifier.Operation.valueOf(section.getString(key +
                        ".operation"));
                double amount = section.getDouble(key + ".amount");
                NamespacedKey namespacedKey = new NamespacedKey(RACE_KEY, name.toLowerCase() + "." +
                        key);
                Attribute attribute = Registry.ATTRIBUTE.getOrThrow(Key.key("minecraft", key.toLowerCase()));
                AttributeModifier modifier = new AttributeModifier(namespacedKey, amount, operation);
                AttributeInstance attributeInstance = player.getAttribute(attribute);
                if (attributeInstance != null)
                {
                    attributeInstance.addTransientModifier(modifier);
                }
            }
            catch (Exception exception)
            {
                //Do nothing.
            }
        }
    }

    public void clearStats(Origin origin)
    {
        Player player = origin.getPlayer();
        if (player == null)
        {
            return;
        }

        for (Attribute attribute : Registry.ATTRIBUTE)
        {
            AttributeInstance instance = player.getAttribute(attribute);
            if (instance != null)
            {
                for (AttributeModifier modifier : new ArrayList<>(instance.getModifiers()))
                {
                    if (modifier.getKey().getNamespace().equals(RACE_KEY))
                    {
                        instance.removeModifier(modifier);
                    }
                }
            }
        }
    }
}
