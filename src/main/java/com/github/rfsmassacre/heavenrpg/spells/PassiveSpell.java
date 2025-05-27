package com.github.rfsmassacre.heavenrpg.spells;

import com.github.rfsmassacre.heavenrpg.HeavenRPG;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

@Getter
@Setter
public class PassiveSpell extends Spell
{
    private final transient Set<BukkitTask> tasks;

    private int interval;
    private List<String> spellNames;

    public PassiveSpell()
    {
        this.tasks = new HashSet<>();

        this.bindable = false;
        this.interval = 20;
        this.spellNames = new ArrayList<>();
        activateTimer();
    }

    public PassiveSpell(String internalName, String displayName)
    {
        super(internalName);

        this.tasks = new HashSet<>();

        setDisplayName(displayName);
        this.bindable = false;
        this.interval = 20;
        this.spellNames = new ArrayList<>();
        activateTimer();
    }

    public List<Spell> getInnerSpells()
    {
        return new ArrayList<>(spellNames.stream()
                .map(Spell::getSpell)
                .filter(Objects::nonNull)
                .toList());
    }

    public void addInnerSpell(Spell spell)
    {
        if (!spellNames.contains(spell.getInternalName()))
        {
            spellNames.add(spell.getInternalName());
        }
    }

    public void removeInnerSpell(Spell spell)
    {
        spellNames.remove(spell.getInternalName());
    }

    public void activateTimer()
    {
        tasks.add(new BukkitRunnable()
        {
            @Override
            public void run()
            {
                Bukkit.getOnlinePlayers().forEach((player) -> cast(player));
            }
        }.runTaskTimer(HeavenRPG.getInstance(), 0L, interval));
    }

    protected void deactivateTimers()
    {
        for (BukkitTask task : tasks)
        {
            task.cancel();
        }

        tasks.clear();
    }

    @Override
    public boolean activate(LivingEntity entity)
    {
        for (Spell spell : getInnerSpells())
        {
            if (!spell.activate(entity))
            {
                return false;
            }
        }

        return true;
    }
}
