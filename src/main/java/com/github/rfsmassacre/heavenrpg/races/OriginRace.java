package com.github.rfsmassacre.heavenrpg.races;

import com.github.rfsmassacre.heavenlibrary.interfaces.LocaleData;
import com.github.rfsmassacre.heavenlibrary.paper.configs.PaperConfiguration;
import com.github.rfsmassacre.heavenrpg.HeavenRPG;
import com.github.rfsmassacre.heavenrpg.data.RaceYaml;
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
public class OriginRace
{
    private static final String RACE_KEY = "heavenrpg_race";
    private static final Map<String, OriginRace> CACHE = new HashMap<>();
    private static RaceYaml DATA;

    public static void initialize()
    {
        DATA = new RaceYaml();
        loadRaces();
        if (CACHE.isEmpty())
        {
            OriginRace human = new OriginRace("Human", "&fHuman");
            OriginRace orc = new OriginRace("Orc", "&6Orc");
            orc.addStat(new Origin.AttributeStat(Attribute.SCALE, 0.5,
                    AttributeModifier.Operation.ADD_SCALAR));
            orc.addStat(new Origin.AttributeStat(Attribute.ENTITY_INTERACTION_RANGE, 0.5,
                    AttributeModifier.Operation.ADD_SCALAR));
            orc.addStat(new Origin.AttributeStat(Attribute.BLOCK_INTERACTION_RANGE, 0.5,
                    AttributeModifier.Operation.ADD_SCALAR));
            orc.addSpell("Defense");
            orc.addSpell("OrcDefense");
            registerRace(human);
            registerRace(orc);
            saveRaces();
        }
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

    public static Set<OriginRace> getRaces()
    {
        return new HashSet<>(CACHE.values());
    }

    public static void loadRaces()
    {
        CACHE.clear();
        for (OriginRace originRace : DATA.allDynamic("com.github.rfsmassacre.heavenrpg.races."))
        {
            registerRace(originRace);
        }
    }

    public static void saveRace(OriginRace originRace)
    {
        DATA.writeDynamic(originRace.name, originRace);
    }

    public static void saveRaces()
    {
        for (OriginRace originRace : CACHE.values())
        {
            saveRace(originRace);
        }
    }

    public static OriginRace getDefaultRace()
    {
        PaperConfiguration config = HeavenRPG.getInstance().getConfiguration();
        return getRace(config.getString("default.race", "Human"));
    }

    protected List<String> spellNames;
    protected List<Origin.AttributeStat> attributeStats;
    protected String name;
    protected String displayName;

    public OriginRace()
    {
        this.spellNames = new ArrayList<>();
        this.attributeStats = new ArrayList<>();
    }

    public OriginRace(String name)
    {
        this();

        this.name = name;
        setDisplayName(name);
    }

    public OriginRace(String name, String displayName)
    {
        this(name);

        setDisplayName(displayName);
    }

    @SafeVarargs
    public OriginRace(String name, String displayName, String... spellNames)
    {
        this(name, displayName);

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
                NamespacedKey namespacedKey = new NamespacedKey(RACE_KEY, name.toLowerCase() + "." +
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
                    if (modifier.getKey().getNamespace().equals(RACE_KEY))
                    {
                        instance.removeModifier(modifier);
                    }
                }
            }
        }
    }

    public boolean isRaceSpell(Spell spell)
    {
        return spellNames.contains(spell.getInternalName());
    }
}
