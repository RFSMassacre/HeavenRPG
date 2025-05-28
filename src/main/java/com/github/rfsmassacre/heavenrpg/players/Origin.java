package com.github.rfsmassacre.heavenrpg.players;

import com.github.rfsmassacre.heavenlibrary.interfaces.LocaleData;
import com.github.rfsmassacre.heavenlibrary.paper.configs.PaperConfiguration;
import com.github.rfsmassacre.heavenrpg.HeavenRPG;
import com.github.rfsmassacre.heavenrpg.classes.OriginClass;
import com.github.rfsmassacre.heavenrpg.data.OriginGson;
import com.github.rfsmassacre.heavenrpg.races.OriginRace;
import com.github.rfsmassacre.heavenrpg.spells.Spell;
import com.github.rfsmassacre.heavenrpg.utils.TaskUtil;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.KeybindComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Registry;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Consumer;

@Getter
@Setter
@SuppressWarnings("deprecation")
public final class Origin
{
    public enum KeyBind
    {
        //Default
        DROP("key.drop"),
        SWAP("key.swapOffhand"),
        LEFT_CLICK("key.attack"),
        RIGHT_CLICK("key.use"),
        SNEAK("key.sneak");

        private final String key;

        KeyBind(String key)
        {
            this.key = key;
        }

        public KeybindComponent getComponent()
        {
            return Component.keybind(key);
        }

        public static KeyBind fromString(String name)
        {
            try
            {
                return KeyBind.valueOf(name);
            }
            catch (IllegalArgumentException exception)
            {
                return null;
            }
        }
    }

    @Getter
    @Setter
    public static class AttributeStat
    {
        private String attributeName;
        private double amount;
        private String operationName;

        public AttributeStat()
        {
            this.amount = 0.0;
        }

        private AttributeStat(String attributeName, double amount, String operationName)
        {
            this.attributeName = attributeName;
            this.amount = amount;
            this.operationName = operationName;
        }

        public AttributeStat(Attribute attribute, double amount, AttributeModifier.Operation operation)
        {
            this(attribute.getKey().value(), amount, operation.name());
        }

        public Attribute getAttribute()
        {
            try
            {
                return Registry.ATTRIBUTE.getOrThrow(Key.key("minecraft", attributeName.toLowerCase()));
            }
            catch (Exception exception)
            {
                return null;
            }
        }

        public AttributeModifier.Operation getOperation()
        {
            try
            {
                return AttributeModifier.Operation.valueOf(operationName);
            }
            catch (IllegalArgumentException exception)
            {
                return null;
            }
        }
    }

    private static final Map<UUID, Origin> CACHE = new HashMap<>();
    private static OriginGson DATA;

    public static void initialize()
    {
        DATA = new OriginGson();
    }

    public static void addOrigin(Origin origin)
    {
        CACHE.put(origin.playerId, origin);
    }

    public static Origin getOrigin(UUID playerId)
    {
        return CACHE.get(playerId);
    }

    public static Origin getOrigin(String playerName)
    {
        for (Origin origin : CACHE.values())
        {
            if (origin.getName().equals(playerName))
            {
                return origin;
            }
        }

        return null;
    }

    public static void removeOrigin(UUID playerId)
    {
        CACHE.remove(playerId);
    }

    public static void saveOrigin(Origin origin, boolean async)
    {
        if (async)
        {
            DATA.writeAsync(origin.playerId.toString(), origin);
        }
        else
        {
            DATA.write(origin.playerId.toString(), origin);
        }
    }

    public static void saveOrigins()
    {
        for (Origin origin : CACHE.values())
        {
            saveOrigin(origin, false);
        }
    }

    public static Origin loadOrigin(UUID playerId)
    {
        return DATA.read(playerId.toString());
    }

    public static void getOrLoadOrigin(UUID playerId, Consumer<Origin> callback)
    {
        Origin origin = getOrigin(playerId);
        if (origin != null)
        {
            callback.accept(origin);
            return;
        }

        TaskUtil.runTaskAsync(() ->
        {
            Origin offline = loadOrigin(playerId);
            callback.accept(offline);
        });
    }

    public static void getOrLoadOrigin(String playerName, Consumer<Origin> callback)
    {
        Origin online = getOrigin(playerName);
        if (online != null)
        {
            callback.accept(online);
            return;
        }

        TaskUtil.runTaskAsync(() ->
        {
            for (Origin offline : DATA.all())
            {
                if (offline.getPlayer() == null && offline.getName().equals(playerName))
                {
                    callback.accept(offline);
                    return;
                }
            }
        });
    }

    private UUID playerId;
    private String originRace;
    private String originClass;
    private String displayName;
    private String name;
    private double lastHealth;
    private double lastMaxHealth;
    private long lastLogin;
    private Map<String, Double> raceLevels;
    private Map<String, Double> classLevels;
    private Map<KeyBind, String> spells;

    public Origin()
    {
        this(OriginRace.getDefaultRace(), OriginClass.getDefaultClass());
    }

    public Origin(OriginRace originRace, OriginClass originClass)
    {
        this.originRace = originRace.getName();
        this.originClass = originClass.getName();
        this.lastHealth = 0.0;
        this.lastMaxHealth = 0.0;
        this.lastLogin = 0L;
        this.raceLevels = new HashMap<>();
        this.classLevels = new HashMap<>();
        this.spells = new HashMap<>();
    }

    public Origin(Player player)
    {
        this();

        this.playerId = player.getUniqueId();
        setDisplayName(player.getDisplayName());
        this.name = player.getName();
    }

    public Origin(Player player, OriginRace originRace, OriginClass originClass)
    {
        this(originRace, originClass);

        this.playerId = player.getUniqueId();
        setDisplayName(player.getDisplayName());
        this.name = player.getName();
    }

    public Player getPlayer()
    {
        return Bukkit.getPlayer(playerId);
    }

    public void setPlayer(Player player)
    {
        this.playerId = player.getUniqueId();
        this.name = player.getName();
        setDisplayName(player.getDisplayName());
    }

    public String getDisplayName()
    {
        Player player = getPlayer();
        if (player != null)
        {
            this.displayName = player.getDisplayName();
        }

        return displayName != null ? LocaleData.format(displayName) : null;
    }

    public void setDisplayName(String displayName)
    {
        this.displayName = LocaleData.undoFormat(displayName);
    }

    public String getName()
    {
        Player player = getPlayer();
        if (player != null)
        {
            this.name = player.getName();
        }

        return name;
    }

    public Spell getSpell(KeyBind key)
    {
        String spellName = spells.get(key);
        if (spellName == null)
        {
            return null;
        }

        return Spell.getSpell(spellName);
    }

    public List<Material> getDiet()
    {
        PaperConfiguration config = HeavenRPG.getInstance().getConfiguration();
        return config.getStringList("diet." + originRace.toLowerCase()).stream()
                .map(Material::getMaterial)
                .filter(Objects::nonNull)
                .toList();
    }

    public boolean canEat(Material material)
    {
        OriginRace originRace = getOriginRace();
        if (originRace == null)
        {
            return false;
        }

        List<String> diet = originRace.getFoodNames();
        return diet.contains("ALL") || diet.contains(material.name());
    }

    public boolean offlineFor(long hours)
    {
        if (getPlayer() != null)
        {
            return false;
        }

        // Calculate days between last login and now
        long offlineHours = Instant.ofEpochMilli(lastLogin).until(Instant.now(), ChronoUnit.HOURS);
        return offlineHours > hours;
    }

    public double getRaceLevel()
    {
        return getRaceLevel(originRace);
    }

    public double getRaceLevel(String originRaceName)
    {
        OriginRace originRace = OriginRace.getRace(originRaceName);
        if (originRace == null)
        {
            return 0.0;
        }

        return raceLevels.getOrDefault(originRace.getName(), 0.0);
    }

    public void addRaceLevel(double level)
    {
        addRaceLevel(level, originRace);
    }

    public void addRaceLevel(double level, String originRaceName)
    {
        OriginRace originRace = OriginRace.getRace(originRaceName);
        if (originRace != null)
        {
            raceLevels.put(originRace.getName(), this.getRaceLevel() + level);
        }
    }

    public double getClassLevel()
    {
        return getClassLevel(originClass);
    }

    public double getClassLevel(String name)
    {
        OriginClass originClass = OriginClass.getClass(name);
        if (originClass == null)
        {
            return 0.0;
        }

        return classLevels.getOrDefault(originClass.getName(), 0.0);
    }

    public void addClassLevel(double level)
    {
        addClassLevel(level, originClass);
    }

    public void addClassLevel(double level, String originClassName)
    {
        OriginClass originClass = OriginClass.getClass(originClassName);
        if (originClass != null)
        {
            raceLevels.put(originClass.getName(), this.getRaceLevel() + level);
        }
    }

    public void setOriginRace(OriginRace originRace)
    {
        if (originRace != null)
        {
            this.originRace = originRace.getName();
        }
    }

    public OriginRace getOriginRace()
    {
        return OriginRace.getRace(originRace);
    }

    public void setOriginClass(OriginClass originClass)
    {
        if (originClass != null)
        {
            this.originClass = originClass.getName();
        }
    }

    public OriginClass getOriginClass()
    {
        return OriginClass.getClass(originClass);
    }

    public void updateStats()
    {
        OriginRace originRace = getOriginRace();
        if (originRace != null)
        {
            originRace.updateStats(this);
        }

        OriginClass originClass = getOriginClass();
        if (originClass != null)
        {
            originClass.updateStats(this);
        }
    }
}
