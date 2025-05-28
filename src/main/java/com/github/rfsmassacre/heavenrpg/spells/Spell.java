package com.github.rfsmassacre.heavenrpg.spells;

import com.github.rfsmassacre.heavenlibrary.interfaces.LocaleData;
import com.github.rfsmassacre.heavenlibrary.paper.configs.PaperLocale;
import com.github.rfsmassacre.heavenrpg.HeavenRPG;
import com.github.rfsmassacre.heavenrpg.data.SpellYaml;
import com.github.rfsmassacre.heavenrpg.events.SpellCastEvent;
import com.github.rfsmassacre.heavenrpg.events.SpellCooldownEvent;
import com.github.rfsmassacre.heavenrpg.events.SpellTargetEvent;
import com.github.rfsmassacre.heavenrpg.players.Origin;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionEffectTypeCategory;
import org.bukkit.util.RayTraceResult;

import java.util.*;
import java.util.function.Predicate;

@Getter
public abstract class Spell implements Listener
{
    private static final Map<String, Spell> SPELLS = new HashMap<>();
    private static SpellYaml DATA;

    public static void initialize()
    {
        DATA = new SpellYaml();
        loadSpells();
        if (SPELLS.isEmpty())
        {
            PotionSpell prayer = new PotionSpell("Prayer", "&ePrayer",
                    PotionEffectTypeCategory.BENEFICIAL);
            prayer.setTargetSelf(true);
            prayer.setBeneficial(true);
            PotionSpell curse = new PotionSpell("Curse", "&cCurse",
                    PotionEffectTypeCategory.HARMFUL);
            PotionSpell defense = new PotionSpell("Defense", "&aDefense",
                    PotionEffectType.RESISTANCE);
            defense.setPotionReceived(null);
            defense.setPotionSent(null);
            defense.setTargetSelf(true);
            defense.setBeneficial(true);
            defense.setBindable(false);
            PassiveSpell orcPassive = new PassiveSpell("OrcDefense", "&aOrc Defense");
            orcPassive.addInnerSpell(defense);
            orcPassive.setTargetSelf(true);
            orcPassive.setBeneficial(true);
            orcPassive.setBindable(false);
            addSpell(prayer);
            addSpell(curse);
            addSpell(defense);
            addSpell(orcPassive);
            saveSpells();
        }
    }

    public static <T extends Spell> T getSpell(Class<T> clazz)
    {
        for (Spell spell : SPELLS.values())
        {
            if (clazz.isInstance(spell))
            {
                return clazz.cast(spell);
            }
        }

        return null;
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
        SPELLS.put(spell.internalName, spell);
        Bukkit.getPluginManager().registerEvents(spell, HeavenRPG.getInstance());
    }

    public static void loadSpells()
    {
        for (Spell spell : SPELLS.values())
        {
            switch (spell)
            {
                case BuffSpell buffSpell -> buffSpell.deactivateTimers();
                case PassiveSpell passiveSpell -> passiveSpell.deactivateTimers();
                default ->
                {
                    //Do nothing.
                }
            }

            HandlerList.unregisterAll(spell);
        }

        SPELLS.clear();
        for (Spell spell : DATA.allDynamic("com.github.rfsmassacre.heavenrpg.spells."))
        {
            addSpell(spell);
        }
    }

    public static <T extends Spell> void saveSpell(T spell)
    {
        DATA.writeDynamic(spell.internalName, spell);
    }

    public static void saveSpells()
    {
        for (Spell spell : SPELLS.values())
        {
            saveSpell(spell);
        }
    }

    private final PaperLocale locale;
    protected final Map<UUID, Long> cooldowns;
    @Getter
    @Setter
    protected String internalName;
    protected String displayName;
    @Getter
    @Setter
    protected long cooldown;
    @Getter
    @Setter
    protected int level, customModelId;
    @Getter
    @Setter
    protected boolean beneficial, bindable, targetSelf;
    @Getter
    @Setter
    protected double range, dot;
    @Getter
    @Setter
    protected String cooldownMessage, itemMessage, levelMessage, noTargetMessage;

    protected List<String> description;

    public Spell()
    {
        this.locale = HeavenRPG.getInstance().getLocale();
        this.cooldowns = new HashMap<>();
    }

    public Spell(String internalName)
    {
        this();

        this.internalName = internalName;
        this.displayName = internalName;
        this.cooldown = 0;
        this.level = 0;
        this.beneficial = false;
        this.bindable = true;
        this.targetSelf = false;
        this.customModelId = 0;
        this.description = new ArrayList<>();
        this.cooldownMessage =  "&f{spell}&r &cis on cooldown! &4(&e{time}&4)";
        this.range = 0.0;
        this.dot = 0.2;
        this.itemMessage = "&f{spell}&r &cis missing an item!";
        this.levelMessage = "&f{spell}&r &crequires you to be &eLVL {level}&c!";
        this.noTargetMessage = "&f{spell}&r &crequires a target!";
    }

    public Spell(String internalName, String displayName)
    {
        this(internalName);

        setDisplayName(displayName);
    }

    public void setDisplayName(String displayName)
    {
        this.displayName = LocaleData.undoFormat(displayName);
    }

    public String getDisplayName()
    {
        return LocaleData.format(displayName);
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

        if (!origin.getOriginRace().isRaceSpell(this) & !origin.getOriginClass().isClassSpell(this))
        {
            return false;
        }

        if (origin.getRaceLevel() < level)
        {
            sendActionMessage(entity, levelMessage, "{spell}", getDisplayName());
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
        if (System.currentTimeMillis() - cooldown < this.cooldown)
        {
            sendActionMessage(entity, cooldownMessage, "{spell}", getDisplayName(), "{time}",
                    LocaleData.formatTime((double) getCooldown(entity) / 1000));
            return false;
        }

        SpellCastEvent event = new SpellCastEvent(entity, this);
        if (event.callEvent() && activate(entity))
        {
            cooldowns.put(entity.getUniqueId(), System.currentTimeMillis());
            SpellCooldownEvent cooldownEvent = new SpellCooldownEvent(entity, this);
            Bukkit.getPluginManager().callEvent(cooldownEvent);
            return true;
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

        if (range == 0.0)
        {
            AttributeInstance reach = entity.getAttribute(Attribute.ENTITY_INTERACTION_RANGE);
            if (reach != null)
            {
                range = reach.getValue();
            }
        }

        RayTraceResult result = entity.getWorld().rayTraceEntities(entity.getEyeLocation(),
                entity.getEyeLocation().getDirection(), range, dot, filter);
        if (result == null)
        {
            return null;
        }

        return (LivingEntity) result.getHitEntity();
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
