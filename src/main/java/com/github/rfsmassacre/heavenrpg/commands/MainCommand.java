package com.github.rfsmassacre.heavenrpg.commands;

import com.github.rfsmassacre.heavenlibrary.paper.commands.PaperCommand;
import com.github.rfsmassacre.heavenlibrary.paper.items.HeavenItem;
import com.github.rfsmassacre.heavenrpg.HeavenRPG;
import com.github.rfsmassacre.heavenrpg.classes.OriginClass;
import com.github.rfsmassacre.heavenrpg.items.HeavenRPGItem;
import com.github.rfsmassacre.heavenrpg.players.Origin;
import com.github.rfsmassacre.heavenrpg.races.OriginRace;
import com.github.rfsmassacre.heavenrpg.spells.Spell;
import com.github.rfsmassacre.heavenrpg.utils.TaskUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class MainCommand extends PaperCommand
{
    public MainCommand()
    {
        super(HeavenRPG.getInstance(), "heavenrpg");
    }

    private class ReloadCommand extends PaperSubCommand
    {
        public ReloadCommand()
        {
            super("reload");
        }

        @Override
        protected void onRun(CommandSender sender, String... args)
        {
            config.reload();
            locale.reload();
            OriginRace.initialize();
            OriginClass.initialize();
            Origin.initialize();
            HeavenRPGItem.initialize();
            Spell.loadSpells();
            TaskUtil.reload();
            locale.sendLocale(sender, "reloaded");
            playSound(sender, SoundKey.SUCCESS);
        }
    }

    private class ItemCommand extends PaperSubCommand
    {
        public ItemCommand()
        {
            super("item");
        }

        @Override
        protected void onRun(CommandSender sender, String... args)
        {
            if (!(sender instanceof Player player))
            {
                onConsole(sender);
                return;
            }

            if (args.length < 2)
            {
                onInvalidArgs(player, "<item>", "[player]");
                return;
            }

            String itemName = args[1];
            HeavenRPGItem item = HeavenRPGItem.getItem(itemName);
            if (item == null)
            {
                locale.sendLocale(player, "admin.item.invalid", "{item}", itemName);
                playSound(player, SoundKey.INCOMPLETE);
                return;
            }

            Player target = player;
            if (args.length > 2)
            {
                String playerName = args[2];
                target = Bukkit.getPlayer(playerName);
                if (target == null)
                {
                    locale.sendLocale(player, "invalid.player", "{player}", playerName);
                    playSound(player, SoundKey.INCOMPLETE);
                    return;
                }
            }

            ItemStack itemStack = item.getItemStack();
            if (target.getInventory().firstEmpty() == -1)
            {
                target.getWorld().dropItemNaturally(target.getLocation(), itemStack);
            }
            else
            {
                target.getInventory().addItem(itemStack);
            }

            if (!player.equals(target))
            {
                locale.sendLocale(player, "admin.item.sender", "{amount}",
                        Integer.toString(itemStack.getAmount()), "{item}", item.getDisplayName(), "{player}",
                        target.getDisplayName());
            }

            locale.sendLocale(target, "admin.item.success.target", "{amount}",
                    Integer.toString(itemStack.getAmount()), "{item}", item.getDisplayName());
            playSound(player, SoundKey.SUCCESS);
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String... args)
    {
        List<String> suggestions = new ArrayList<>();
        if (args.length == 2)
        {
            suggestions.addAll(HeavenRPGItem.getItems().stream()
                    .map(HeavenItem::getName)
                    .toList());
        }
        else if (args.length == 3)
        {
            suggestions.addAll(Bukkit.getOnlinePlayers().stream()
                    .filter((online) -> !sender.equals(online))
                    .map(Player::getName)
                    .toList());
        }

        return suggestions;
    }
}
