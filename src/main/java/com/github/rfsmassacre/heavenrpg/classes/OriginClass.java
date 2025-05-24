package com.github.rfsmassacre.heavenrpg.classes;

import com.github.rfsmassacre.heavenlibrary.paper.configs.PaperConfiguration;
import com.github.rfsmassacre.heavenrpg.HeavenRPG;
import com.github.rfsmassacre.heavenrpg.items.CastItem;
import com.github.rfsmassacre.heavenrpg.items.HeavenRPGItem;
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
public abstract class OriginClass
{
    private static final String CLASS_KEY = "heavenrpg_class";
    public static final Map<String, OriginClass> CACHE = new HashMap<>();

    public static void initialize()
    {
        CACHE.clear();
        registerClass(new Priest());
    }

    public static void registerClass(OriginClass originClass)
    {
        CACHE.put(originClass.name, originClass);
    }

    public static void unregisterClass(String name)
    {
        CACHE.remove(name);
    }

    public static OriginClass getClass(String name)
    {
        return CACHE.get(name);
    }

    public static <T extends OriginClass> T getClass(Class<T> clazz)
    {
        for (OriginClass originClass : CACHE.values())
        {
            if (clazz.isInstance(originClass))
            {
                return clazz.cast(originClass);
            }
        }

        return null;
    }

    public static Set<OriginClass> getClasses()
    {
        return new HashSet<>(CACHE.values());
    }

    private final Set<String> spells;
    private String name;
    private String displayName;
    private String castItem;

    public OriginClass()
    {
        this.spells = new HashSet<>();
    }

    public OriginClass(String name)
    {
        this();

        this.name = name;
        this.displayName = name;
    }

    public OriginClass(String name, String displayName)
    {
        this(name);

        this.displayName = displayName;
    }

    public OriginClass(String name, String displayName, Class<? extends CastItem> clazz)
    {
        this(name, displayName);

        //Make sure to register your cast item!
        CastItem castItem = HeavenRPGItem.getItem(clazz);
        if (castItem != null)
        {
            this.castItem = castItem.getName();
        }
    }

    public <T extends Spell> T getSpell(Class<T> clazz)
    {
        try
        {
            return clazz.cast(getSpells().stream()
                    .filter(clazz::isInstance)
                    .findFirst()
                    .orElse(null));
        }
        catch (ClassCastException exception)
        {
            return null;
        }
    }

    public void addSpell(Spell spell)
    {
        spells.add(spell.getInternalName());
    }

    public void removeSpell(Spell spell)
    {
        spells.removeIf((name) -> spell.getInternalName().equals(name));
    }

    public List<? extends Spell> getSpells()
    {
        return spells.stream()
                .map(Spell::getSpell)
                .toList();
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

        PaperConfiguration config = HeavenRPG.getInstance().getConfiguration(HeavenRPG.ConfigType.CLASSES);
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
                double amount = section.getDouble(key + ".amount") * origin.getRaceLevel();
                NamespacedKey namespacedKey = new NamespacedKey(CLASS_KEY, name.toLowerCase() + "." +
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
                    if (modifier.getKey().getNamespace().equals(CLASS_KEY))
                    {
                        instance.removeModifier(modifier);
                    }
                }
            }
        }
    }

    public CastItem getCastItem()
    {
        if (HeavenRPGItem.getItem(castItem) instanceof CastItem item)
        {
            return item;
        }

        return null;
    }
}
