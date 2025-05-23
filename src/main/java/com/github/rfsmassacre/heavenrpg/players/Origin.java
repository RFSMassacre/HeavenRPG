package com.github.rfsmassacre.heavenrpg.players;

import com.github.rfsmassacre.heavenlibrary.interfaces.LocaleData;
import com.github.rfsmassacre.heavenlibrary.paper.configs.PaperConfiguration;
import com.github.rfsmassacre.heavenrpg.HeavenRPG;
import com.github.rfsmassacre.heavenrpg.classes.OriginClass;
import com.github.rfsmassacre.heavenrpg.data.OriginGson;
import com.github.rfsmassacre.heavenrpg.races.Human;
import com.github.rfsmassacre.heavenrpg.races.OriginRace;
import com.github.rfsmassacre.heavenrpg.spells.Spell;
import com.github.rfsmassacre.heavenrpg.utils.TaskUtil;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.KeybindComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Consumer;

@Getter
@Setter
@SuppressWarnings("deprecation")
public class Origin
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

    private static final Map<UUID, Origin> CACHE = new HashMap<>();
    private static OriginGson DATA;

    public static void initialize()
    {
        DATA = new OriginGson();
    }

    public static <T extends Origin> void registerRace(Class<T> clazz)
    {
        DATA.registerType(clazz);
    }

    public static void addOrigin(Origin origin)
    {
        CACHE.put(origin.playerId, origin);
    }

    public static Origin getOrigin(UUID playerId)
    {
        return CACHE.get(playerId);
    }

    public static <T extends Origin> T getOrigin(UUID playerId, Class<T> clazz)
    {
        Origin origin = getOrigin(playerId);
        if (clazz.isInstance(origin))
        {
            return clazz.cast(origin);
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

    public static Origin loadOrigin(UUID playerId)
    {
        return DATA.read(playerId.toString());
    }

    public static <T extends Origin> T loadOrigin(UUID playerId, Class<T> clazz)
    {
        Origin origin = loadOrigin(playerId);
        if (clazz.isInstance(origin))
        {
            return clazz.cast(origin);
        }

        return null;
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

    public static <T extends Origin> void getOrLoadOrigin(UUID playerId, Class<T> clazz, Consumer<T> callback)
    {
        getOrLoadOrigin(playerId, (origin) ->
        {
            if (clazz.isInstance(origin))
            {
                callback.accept(clazz.cast(origin));
            }
            else
            {
                callback.accept(null);
            }
        });
    }

    protected UUID playerId;
    protected String originRace;
    protected String originClass;
    protected String displayName;
    protected String name;
    protected double lastHealth;
    protected double lastMaxHealth;
    protected long lastLogin;
    protected Map<String, Double> raceLevels;
    protected Map<String, Double> classLevels;
    protected Map<KeyBind, String> spells;

    public Origin()
    {
        this.originRace = OriginRace.getRace(Human.class).getName(); //This is never null. <3
        this.lastHealth = 0.0;
        this.lastMaxHealth = 0.0;
        this.lastLogin = 0L;
        this.raceLevels = new HashMap<>();
    }

    public Origin(Player player)
    {
        this();

        this.playerId = player.getUniqueId();
        setDisplayName(player.getDisplayName());
        this.name = player.getName();
    }

    public Origin(Origin origin)
    {
        this.playerId = origin.playerId;
        this.originRace = origin.originRace;
        this.originClass = origin.originClass;
        this.displayName = origin.displayName;
        this.name = origin.name;
        this.lastHealth = origin.lastHealth;
        this.lastMaxHealth = origin.lastMaxHealth;
        this.lastLogin = origin.lastLogin;
        this.raceLevels = origin.raceLevels;
        this.classLevels = origin.classLevels;
        this.spells = origin.spells;
    }

    public Player getPlayer()
    {
        return Bukkit.getPlayer(playerId);
    }

    public void setPlayer(Player player)
    {
        this.playerId = player.getUniqueId();
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

    public void addRaceLevel(double level)
    {
        addRaceLevel(level, getOriginRace().getClass());
    }

    public void addRaceLevel(double level, Class<? extends OriginRace> clazz)
    {
        OriginRace originRace = OriginRace.getRace(clazz);
        if (originRace != null)
        {
            raceLevels.put(originRace.getName(), getLevel() + level);
        }
    }

    public void addClassLevel(double level)
    {
        addClassLevel(level, getOriginClass().getClass());
    }

    public void addClassLevel(double level, Class<? extends OriginClass> clazz)
    {
        OriginClass originClass = OriginClass.getClass(clazz);
        if (originClass != null)
        {
            raceLevels.put(originClass.getName(), getLevel() + level);
        }
    }

    public double getLevel()
    {
        return getLevel(getOriginRace().getClass());
    }

    public double getLevel(Class<? extends OriginRace> raceClass)
    {
        OriginRace race = OriginRace.getRace(raceClass);
        if (race == null)
        {
            return 0.0;
        }

        return raceLevels.getOrDefault(race.getName(), 0.0);
    }

    public OriginRace getOriginRace()
    {
        return OriginRace.getRace(originRace);
    }

    public OriginClass getOriginClass()
    {
        return OriginClass.getClass(originClass);
    }
}
