package com.github.rfsmassacre.heavenrpg.commands;

import com.github.rfsmassacre.heavenlibrary.paper.commands.SimplePaperCommand;
import com.github.rfsmassacre.heavenrpg.HeavenRPG;
import com.github.rfsmassacre.heavenrpg.classes.OriginClass;
import com.github.rfsmassacre.heavenrpg.players.Origin;
import com.github.rfsmassacre.heavenrpg.races.OriginRace;
import com.github.rfsmassacre.heavenrpg.spells.Spell;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class KeybindCommand extends SimplePaperCommand
{
    private static final String NONE_SPELL = "NONE";

    public KeybindCommand()
    {
        super(HeavenRPG.getInstance(), "keybind");
    }

    @Override
    protected void onRun(CommandSender sender, String... args)
    {
        //kit control <mode>
        if (!(sender instanceof Player player))
        {
            onConsole(sender);
            return;
        }

        Origin origin = Origin.getOrigin(player.getUniqueId());
        if (origin == null)
        {
            playSound(player, SoundKey.INCOMPLETE);
            return;
        }

        //kit spell <spell>
        if (args.length < 2)
        {
            onInvalidArgs(player, "<keybind>", "<ability>");
            return;
        }

        String keyName = args[0];
        String spellName = args[1];
        Origin.KeyBind keyBind = Origin.KeyBind.fromString(keyName.toUpperCase());
        if (keyBind == null)
        {
            locale.sendLocale(player, "spells.no-keybind", "{keybind}", keyName);
            return;
        }

        if (spellName.equalsIgnoreCase(NONE_SPELL))
        {
            origin.getSpells().remove(keyBind);
            locale.sendLocale(player, "spells.removed-keybind", "{keybind}",
                    keyBind.toString());
            Origin.saveOrigin(origin, true);
            return;
        }

        Spell spell = Spell.getSpell(spellName);
        if (spell == null || !spell.isBindable())
        {
            locale.sendLocale(player, "invalid.spell", "{spell}", spellName);
            return;
        }

        OriginRace originRace = origin.getOriginRace();
        OriginClass originClass = origin.getOriginClass();
        if (!originRace.isRaceSpell(spell) && !originClass.isClassSpell(spell))
        {
            locale.sendLocale(player, "spell.cant-set", "{spell}", spell.getDisplayName());
            return;
        }

        if (originRace.isRaceSpell(spell) && origin.getRaceLevel() < spell.getLevel())
        {
            locale.sendLocale(player, "spell.low-level.race", "{spell}", spell.getDisplayName(), "{race}",
                    originRace.getFormatDisplay(), "{requirement}", Integer.toString(spell.getLevel()), "{level}",
                    Integer.toString((int) origin.getRaceLevel()));
            playSound(player, SoundKey.INCOMPLETE);
            return;
        }

        if (originClass.isClassSpell(spell) && origin.getClassLevel() < spell.getLevel())
        {
            locale.sendLocale(player, "spell.low-level.class", "{spell}", spell.getDisplayName(),
                    "{class}", originClass.getFormatDisplay(), "{requirement}", Integer.toString(spell.getLevel()),
                    "{level}", Integer.toString((int) origin.getClassLevel()));
            playSound(player, SoundKey.INCOMPLETE);
            return;
        }

        origin.getSpells().put(keyBind, spellName);
        Origin.saveOrigin(origin, true);
        locale.sendLocale(player, "spells.keybind", "{keybind}", keyBind.toString(), "{spell}",
                spell.getDisplayName());
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String... args)
    {
        List<String> suggestions = new ArrayList<>();
        if (!(sender instanceof Player player))
        {
            return suggestions;
        }

        Origin origin = Origin.getOrigin(player.getUniqueId());
        if (origin == null)
        {
            return suggestions;
        }

        if (args.length == 1)
        {
            suggestions.addAll(Arrays.stream(Origin.KeyBind.values())
                    .map(Origin.KeyBind::toString)
                    .toList());
        }
        else if (args.length == 2)
        {
            suggestions.add(NONE_SPELL);
            suggestions.addAll(Spell.getSpells().stream()
                    .filter((spell) -> spell.isBindable() && (origin.getOriginRace().isRaceSpell(spell) ||
                            origin.getOriginClass().isClassSpell(spell)))
                    .map(Spell::getInternalName)
                    .toList());
        }

        return suggestions;
    }
}
