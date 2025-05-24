package com.github.rfsmassacre.heavenrpg.commands;

import com.github.rfsmassacre.heavenlibrary.paper.commands.PaperCommand;
import com.github.rfsmassacre.heavenrpg.HeavenRPG;
import com.github.rfsmassacre.heavenrpg.classes.OriginClass;
import com.github.rfsmassacre.heavenrpg.events.ClassChangeEvent;
import com.github.rfsmassacre.heavenrpg.events.RaceChangeEvent;
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
            for (HeavenRPG.ConfigType configType : HeavenRPG.ConfigType.values())
            {
                HeavenRPG.getInstance().getConfiguration(configType).reload();
            }

            HeavenRPGItem.initialize();
            Spell.initialize();
            OriginRace.initialize();
            OriginClass.initialize();
            Origin.initialize();
            TaskUtil.reload();
            locale.sendLocale(sender, "admin.reloaded");
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
                playSound(player, SoundKey.INCOMPLETE);
                return;
            }

            String itemName = args[1];
            HeavenRPGItem item = HeavenRPGItem.getItem(itemName);
            if (item == null)
            {
                locale.sendLocale(player, "invalid.item", "{item}", itemName);
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
                locale.sendLocale(player, "admin.item.success.sender", "{amount}",
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
                    .map(HeavenRPGItem::getName)
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

    private class RaceCommand extends PaperSubCommand
    {
        public RaceCommand()
        {
            super("race");
        }

        @Override
        protected void onRun(CommandSender sender, String[] args)
        {
            //race change
            if (args.length < 2)
            {
                onInvalidArgs(sender, "<race>", "[player]");
            }
            //race change <race>
            else if (args.length == 2)
            {
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

                String raceName = args[1];
                OriginRace originRace = OriginRace.getRace(raceName);
                if (originRace == null)
                {
                    locale.sendLocale(sender, "invalid.race", "{race}", raceName);
                    playSound(player, SoundKey.INCOMPLETE);
                    return;
                }

                change(sender, origin, originRace.getClass());
            }
            else
            {
                String raceName = args[1];
                OriginRace originRace = OriginRace.getRace(raceName);
                if (originRace == null)
                {
                    locale.sendLocale(sender, "invalid.race", "{race}", raceName);
                    playSound(sender, SoundKey.INCOMPLETE);
                    return;
                }

                String playerName = args[2];
                Origin.getOrLoadOrigin(playerName, (origin) ->
                {
                    if (origin == null)
                    {
                        locale.sendLocale(sender, true, "invalid.player", "{name}",
                                playerName);
                        playSound(sender, SoundKey.INCOMPLETE);
                        return;
                    }

                    change(sender, origin, originRace.getClass());
                });
            }
        }

        private void change(CommandSender sender, Origin origin, Class<? extends OriginRace> clazz)
        {
            if (clazz == null || OriginRace.getRace(clazz) == null)
            {
                locale.sendLocale(sender, true, "admin.race.failed");
                return;
            }

           TaskUtil.runTaskAsync(() ->
           {
                try
                {
                    TaskUtil.runTask(() ->
                    {
                        RaceChangeEvent event = new RaceChangeEvent(origin, clazz);
                        if (!event.callEvent())
                        {
                            locale.sendLocale(sender, "admin.race.failed");
                            playSound(sender, SoundKey.INCOMPLETE);
                            return;
                        }

                        origin.setOriginRace(clazz);
                        locale.sendLocale(origin.getPlayer(), "admin.race.success.target", "{race}",
                                origin.getOriginRace().getDisplayName());
                        if (!sender.equals(origin.getPlayer()))
                        {
                            locale.sendLocale(sender, "admin.race.success.sender", "{player}",
                                    origin.getDisplayName(), "{race}", origin.getOriginRace().getDisplayName());
                        }

                        playSound(sender, SoundKey.SUCCESS);
                    });
                }
                catch (IllegalArgumentException exception)
                {
                    playSound(sender, SoundKey.INCOMPLETE);
                }
            });
        }

        @Override
        public List<String> onTabComplete(CommandSender sender, String[] args)
        {
            List<String> suggestions = new ArrayList<>();
            if (args.length == 2)
            {
                suggestions.addAll(OriginRace.getRaces().stream()
                        .map(OriginRace::getName)
                        .toList());
            }
            else if (args.length == 3)
            {
                suggestions.addAll(Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .toList());
            }

            return suggestions;
        }
    }

    private class ClassCommand extends PaperSubCommand
    {
        public ClassCommand()
        {
            super("class");
        }

        @Override
        protected void onRun(CommandSender sender, String[] args)
        {
            //race change
            if (args.length < 2)
            {
                onInvalidArgs(sender, "<class>", "[player]");
            }
            //race change <race>
            else if (args.length == 2)
            {
                if (!(sender instanceof Player player))
                {
                    locale.sendLocale(sender, true, "invalid.console");
                    return;
                }

                Origin origin = Origin.getOrigin(player.getUniqueId());
                if (origin == null)
                {
                    playSound(player, SoundKey.INCOMPLETE);
                    return;
                }

                String className = args[1];
                OriginClass originClass = OriginClass.getClass(className);
                if (originClass == null)
                {
                    locale.sendLocale(sender, "invalid.class", "{class}", className);
                    playSound(player, SoundKey.INCOMPLETE);
                    return;
                }

                change(sender, origin, originClass.getClass());
            }
            else
            {
                String className = args[1];
                OriginClass originClass = OriginClass.getClass(className);
                if (originClass == null)
                {
                    locale.sendLocale(sender, "invalid.class", "{class}", className);
                    playSound(sender, SoundKey.INCOMPLETE);
                    return;
                }

                String playerName = args[2];
                Origin.getOrLoadOrigin(playerName, (origin) ->
                {
                    if (origin == null)
                    {
                        locale.sendLocale(sender, true, "invalid.player", "{name}",
                                playerName);
                        playSound(sender, SoundKey.INCOMPLETE);
                        return;
                    }

                    change(sender, origin, originClass.getClass());
                });
            }
        }

        private void change(CommandSender sender, Origin origin, Class<? extends OriginClass> clazz)
        {
            if (clazz == null || OriginClass.getClass(clazz) == null)
            {
                locale.sendLocale(sender, true, "admin.class.failed");
                return;
            }

            TaskUtil.runTaskAsync(() ->
            {
                try
                {
                    TaskUtil.runTask(() ->
                    {
                        ClassChangeEvent event = new ClassChangeEvent(origin, clazz);
                        if (!event.callEvent())
                        {
                            locale.sendLocale(sender, "admin.class.failed", "{player}",
                                    origin.getDisplayName(), "{class}", event.getOriginClass().getDisplayName());
                            playSound(sender, SoundKey.INCOMPLETE);
                            return;
                        }

                        origin.setOriginClass(clazz);
                        locale.sendLocale(origin.getPlayer(), true, "admin.class.success.target",
                                "{class}", origin.getOriginClass().getDisplayName());
                        if (!sender.equals(origin.getPlayer()))
                        {
                            locale.sendLocale(sender, true, "admin.class.success.sender",
                                    "{player}", origin.getDisplayName(), "{class}",
                                    origin.getOriginClass().getDisplayName());
                        }

                        playSound(sender, SoundKey.SUCCESS);
                    });
                }
                catch (IllegalArgumentException exception)
                {
                    playSound(sender, SoundKey.INCOMPLETE);
                }
            });
        }

        @Override
        public List<String> onTabComplete(CommandSender sender, String[] args)
        {
            List<String> suggestions = new ArrayList<>();
            if (args.length == 2)
            {
                suggestions.addAll(OriginClass.getClasses().stream()
                        .map(OriginClass::getName)
                        .toList());
            }
            else if (args.length == 3)
            {
                suggestions.addAll(Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .toList());
            }

            return suggestions;
        }
    }
}
