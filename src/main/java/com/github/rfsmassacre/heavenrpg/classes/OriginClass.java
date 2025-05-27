package com.github.rfsmassacre.heavenrpg.classes;

import com.github.rfsmassacre.heavenlibrary.interfaces.LocaleData;
import com.github.rfsmassacre.heavenlibrary.paper.configs.PaperConfiguration;
import com.github.rfsmassacre.heavenrpg.HeavenRPG;
import com.github.rfsmassacre.heavenrpg.data.ClassYaml;
import com.github.rfsmassacre.heavenrpg.items.HeavenRPGItem;
import com.github.rfsmassacre.heavenrpg.players.Origin;
import com.github.rfsmassacre.heavenrpg.spells.Spell;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;

import java.util.*;

@Getter
@Setter
public class OriginClass
{
    private static final String CLASS_KEY = "heavenrpg_class";
    private static final Map<String, OriginClass> CACHE = new HashMap<>();
    private static ClassYaml DATA;

    public static void initialize()
    {
        DATA = new ClassYaml();
        loadClasses();
        if (CACHE.isEmpty())
        {
            OriginClass priest = new OriginClass("Survivor", "&fSurvivor");
            priest.addSpell("Prayer");
            priest.addSpell("Curse");
            priest.setCastItemName("PriestBook");
            registerClass(priest);
            saveClass(priest);
        }
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

    public static void loadClasses()
    {
        CACHE.clear();
        for (OriginClass originClass : DATA.allDynamic("com.github.rfsmassacre.heavenrpg.classes."))
        {
            registerClass(originClass);
        }
    }

    public static void saveClass(OriginClass originClass)
    {
        DATA.writeDynamic(originClass.name, originClass);
    }

    public static void saveClasses()
    {
        for (OriginClass originClass : CACHE.values())
        {
            saveClass(originClass);
        }
    }

    public static OriginClass getDefaultClass()
    {
        PaperConfiguration config = HeavenRPG.getInstance().getConfiguration();
        return getClass(config.getString("default.class", "Survivor"));
    }

    protected String name;
    protected String displayName;
    protected String castItemName;
    protected List<String> spellNames;
    protected List<Origin.AttributeStat> attributeStats;

    public OriginClass()
    {
        this.spellNames = new ArrayList<>();
        this.attributeStats = new ArrayList<>();
    }

    public OriginClass(String name)
    {
        this();

        this.name = name;
        setDisplayName(name);
    }

    public OriginClass(String name, String displayName)
    {
        this(name);

        setDisplayName(displayName);
    }

    public OriginClass(String name, String displayName, String castItemName, String... spellNames)
    {
        this(name, displayName);

        setCastItem(castItemName);
        for (String spellName : spellNames)
        {
            addSpell(spellName);
        }
    }

    public void setDisplayName(String displayName)
    {
        this.displayName = LocaleData.undoFormat(displayName);
    }

    public String getFormatDisplay()
    {
        return LocaleData.format(displayName);
    }

    public void addSpell(String spellName)
    {
        Spell spell = Spell.getSpell(spellName);
        if (spell != null && !spellNames.contains(spell.getInternalName()))
        {
            spellNames.add(spell.getInternalName());
        }
    }

    public void removeSpell(Spell spell)
    {
        spellNames.removeIf((name) -> spell.getInternalName().equals(name));
    }

    public List<? extends Spell> getSells()
    {
        return spellNames.stream()
                .map(Spell::getSpell)
                .toList();
    }

    public void addStat(Origin.AttributeStat stat)
    {
        removeStat(stat.getAttribute());
        attributeStats.add(stat);
    }

    public void removeStat(Attribute attribute)
    {
        attributeStats.removeIf((otherStat) -> otherStat.getAttribute().equals(attribute));
    }

    public void updateStats(Origin origin)
    {
        clearStats(origin);
        Player player = origin.getPlayer();
        if (player == null)
        {
            return;
        }

        for (Origin.AttributeStat stat : attributeStats)
        {
            try
            {
                Attribute attribute = stat.getAttribute();
                double amount = stat.getAmount();
                AttributeModifier.Operation operation = stat.getOperation();
                NamespacedKey namespacedKey = new NamespacedKey(CLASS_KEY, name.toLowerCase() + "." +
                        attribute.getKey().value());
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

    public void setCastItem(String itemName)
    {
        HeavenRPGItem item = HeavenRPGItem.getItem(itemName);
        if (item != null)
        {
            this.castItemName = item.getName();
        }
    }

    public HeavenRPGItem getCastItem()
    {
        return HeavenRPGItem.getItem(castItemName);
    }

    public boolean isClassSpell(Spell spell)
    {
        return spellNames.contains(spell.getInternalName());
    }
}
