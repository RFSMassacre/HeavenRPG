package com.github.rfsmassacre.heavenrpg.spells;

import com.github.rfsmassacre.heavenlibrary.interfaces.LocaleData;
import com.github.rfsmassacre.heavenlibrary.paper.configs.PaperConfiguration;
import com.github.rfsmassacre.heavenlibrary.paper.configs.PaperLocale;
import com.github.rfsmassacre.heavenrpg.HeavenRPG;
import com.github.rfsmassacre.heavenrpg.events.SpellCastEvent;
import com.github.rfsmassacre.heavenrpg.events.SpellCooldownEvent;
import com.github.rfsmassacre.heavenrpg.events.SpellTargetEvent;
import com.github.rfsmassacre.heavenrpg.players.Origin;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.RayTraceResult;
import org.jetbrains.annotations.NotNull;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;

import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
public abstract class Spell implements Listener
{
    private static final Map<Class<? extends Spell>, Spell> SPELLS = new HashMap<>();

    public static void loadSpells()
    {
        for (Spell spell : SPELLS.values())
        {
            if (spell instanceof BuffSpell buff)
            {
                buff.deactivateTimers();
            }

            HandlerList.unregisterAll(spell);
        }

        SPELLS.clear();
        Reflections reflections = new Reflections("com.github.rfsmassacre.heavenrpg.spells", Scanners.SubTypes);
        Set<Class<? extends Spell>> spellClasses = reflections.getSubTypesOf(Spell.class);
        for (Class<? extends Spell> spellClass : spellClasses)
        {
            if (!Modifier.isAbstract(spellClass.getModifiers()))
            {
                try
                {
                    Spell spell = spellClass.getDeclaredConstructor().newInstance();
                    addSpell(spell);
                }
                catch (Exception ignored)
                {
                    //Do nothing
                }
            }
        }
    }

    public static <T extends Spell> @NotNull T getSpell(Class<T> clazz)
    {
        if (Modifier.isAbstract(clazz.getModifiers()))
        {
            throw new IllegalArgumentException();
        }

        return clazz.cast(SPELLS.get(clazz));
    }

    public static Spell getSpell(String internalName)
    {
        for (Spell spell : SPELLS.values())
        {
            if (spell.internalName.equals(internalName))
            {
                return spell;
            }
        }

        return null;
    }

    public static Set<Spell> getSpells()
    {
        return new HashSet<>(SPELLS.values());
    }

    public static void addSpell(Spell spell)
    {
        SPELLS.put(spell.getClass(), spell);
        Bukkit.getPluginManager().registerEvents(spell, HeavenRPG.getInstance());
    }

    private final PaperConfiguration config;
    protected final PaperLocale locale;
    private final Set<BukkitTask> tasks;
    protected final Map<UUID, Long> cooldowns;

    @Getter
    protected final String internalName;
    @Getter
    protected final String displayName;
    @Getter
    protected final long cooldown;
    @Getter
    protected final String race;
    @Getter
    protected final int level, customModelId;
    @Getter
    protected final boolean beneficial, bindable;
    @Getter
    protected final double range, dot;
    @Getter
    protected String cooldownMessage, itemMessage, levelMessage, raceMessage, noTargetMessage;

    protected final List<String> description;

    public Spell(String internalName)
    {
        this.config = HeavenRPG.getInstance().getConfiguration(HeavenRPG.ConfigType.SPELLS);
        this.locale = HeavenRPG.getInstance().getLocale();
        this.tasks = new HashSet<>();
        this.cooldowns = new HashMap<>();
        this.internalName = internalName;
        this.displayName = getString("display-name", internalName);
        this.cooldown = getInt("cooldown", 0);
        this.race = getString("race", null);
        this.level = getInt("level", 0);
        this.beneficial = getBoolean("beneficial", false);
        this.bindable = getBoolean("bindable", true);
        this.customModelId = getInt("custom-model-data", 0);
        this.description = getStringList("description");
        this.cooldownMessage = getString("cooldown-message", displayName + "&r &cis on cooldown!");
        this.range = getDouble("range", 0);
        this.dot = getDouble("dot", 0.0);
        this.itemMessage = getString("no-item-message", displayName + "&r &cis missing an item!");
        this.levelMessage = getString("level-message", displayName + "&r &crequires you to be &eLVL "
                + level + "&c!");
        this.raceMessage = getString("race-message", displayName + "&r &cis a &e" +
                LocaleData.capitalize(race) + "&c ability!");
        this.noTargetMessage = getString("no-target-message", displayName + "&r &crequires a target!");
    }

    private String replaceValues(String input)
    {
        // Regular expression pattern for placeholder format {key}
        Pattern pattern = Pattern.compile("\\{([^}]+)}");
        Matcher matcher = pattern.matcher(input);
        StringBuilder result = new StringBuilder();
        while (matcher.find())
        {
            String key = matcher.group(1);
            if (key.equals("cooldown"))
            {
                matcher.appendReplacement(result, Integer.toString(config.getSection(internalName).getInt(key)
                        / 1000));
                continue;
            }
            else if (key.equals("duration") || key.equals("delay") || key.equals("animation"))
            {
                matcher.appendReplacement(result, String.format("%.1f", (double) config.getSection(internalName)
                        .getInt(key) / 20.0));
                continue;
            }

            Object value = config.getSection(internalName).get(key);
            String replacement = (value != null) ? value.toString() : matcher.group(0);
            matcher.appendReplacement(result, replacement);
        }

        matcher.appendTail(result);
        return result.toString();
    }

    public List<String> getDescription()
    {
        return description.stream()
                .map(LocaleData::format)
                .toList();
    }

    public abstract boolean activate(LivingEntity entity);

    /**
     * Override this so it does an effect when called.
     * @param entity Caster.
     */
    public void effect(LivingEntity entity)
    {
        //Do nothing yet.
    }

    public boolean canCast(LivingEntity entity)
    {
        Origin origin = Origin.getOrigin(entity.getUniqueId());
        if (origin == null)
        {
            return false;
        }

        if (!origin.getOriginRace().getName().equals(race))
        {
            locale.sendActionMessage(origin.getPlayer(), raceMessage);
            return false;
        }

        if (origin.getRaceLevel() < level)
        {
            locale.sendActionMessage(origin.getPlayer(), levelMessage);
            return false;
        }

        return true;
    }

    public boolean cast(LivingEntity entity)
    {
        if (!canCast(entity))
        {
            return false;
        }

        long cooldown = cooldowns.getOrDefault(entity.getUniqueId(), 0L);
        if (System.currentTimeMillis() - cooldown > this.cooldown)
        {
            SpellCastEvent event = new SpellCastEvent(entity, this);
            if (event.callEvent() && activate(entity))
            {
                cooldowns.put(entity.getUniqueId(), System.currentTimeMillis());
                SpellCooldownEvent cooldownEvent = new SpellCooldownEvent(entity, this);
                Bukkit.getPluginManager().callEvent(cooldownEvent);
                return true;
            }
        }
        else if (entity instanceof Player player)
        {
            locale.sendActionLocale(player, false, "spells.cooldown", "{spell}", displayName,
                    "{cooldown}", LocaleData.formatTime((double) getCooldown(player) / 1000.0));
        }

        return false;
    }

    public long getCooldown(LivingEntity entity)
    {
        return Math.max(0L, this.cooldown - (System.currentTimeMillis() - cooldowns.getOrDefault(entity.getUniqueId(),
                0L)));
    }

    public LivingEntity getTarget(LivingEntity entity)
    {
        Predicate<Entity> filter = other ->
        {
            if (other instanceof LivingEntity target && !target.equals(entity))
            {
                SpellTargetEvent targetEvent = new SpellTargetEvent(entity, this, target);
                return targetEvent.callEvent();
            }

            return false;
        };

        RayTraceResult result = entity.getWorld().rayTraceEntities(entity.getEyeLocation(),
                entity.getEyeLocation().getDirection(), range, dot, filter);
        if (result == null)
        {
            return null;
        }

        return (LivingEntity) result.getHitEntity();
    }

    protected String getString(String key, String defaultValue)
    {
        return config.getString(internalName + "." + key, defaultValue);
    }

    protected int getInt(String key, int defaultValue)
    {
        return config.getInt(internalName + "." + key, defaultValue);
    }

    protected double getDouble(String key, double defaultValue)
    {
        return config.getDouble(internalName + "." + key, defaultValue);
    }

    protected long getLong(String key, long defaultValue)
    {
        return config.getLong(internalName + "." + key, defaultValue);
    }

    protected boolean getBoolean(String key, boolean defaultValue)
    {
        return config.getBoolean(internalName + "." + key, defaultValue);
    }

    protected List<String> getStringList(String key)
    {
        return config.getStringList(internalName + "." + key);
    }

    protected List<Integer> getIntegerList(String key)
    {
        return config.getIntegerList(internalName + "." + key);
    }

    protected List<Double> getDoubleList(String key)
    {
        return config.getDoubleList(internalName + "." + key);
    }

    protected List<Long> getLongList(String key)
    {
        return config.getLongList(internalName + "." + key);
    }

    protected void sendActionMessage(LivingEntity entity, String message, String... holders)
    {
        if (entity instanceof Player player)
        {
            locale.sendActionMessage(player, message, holders);
        }
    }

    protected String getDisplayName(LivingEntity entity)
    {
        return entity instanceof Player player ? player.getDisplayName() : entity.getName();
    }
}
