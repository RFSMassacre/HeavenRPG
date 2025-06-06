package com.github.rfsmassacre.heavenrpg;

import com.github.rfsmassacre.heavenlibrary.paper.HeavenPaperPlugin;
import com.github.rfsmassacre.heavenlibrary.paper.configs.PaperConfiguration;
import com.github.rfsmassacre.heavenlibrary.paper.configs.PaperLocale;
import com.github.rfsmassacre.heavenrpg.classes.OriginClass;
import com.github.rfsmassacre.heavenrpg.commands.KeybindCommand;
import com.github.rfsmassacre.heavenrpg.commands.MainCommand;
import com.github.rfsmassacre.heavenrpg.items.HeavenRPGItem;
import com.github.rfsmassacre.heavenrpg.listeners.KeybindListener;
import com.github.rfsmassacre.heavenrpg.listeners.OriginListener;
import com.github.rfsmassacre.heavenrpg.players.Origin;
import com.github.rfsmassacre.heavenrpg.races.OriginRace;
import com.github.rfsmassacre.heavenrpg.spells.Spell;
import com.github.rfsmassacre.heavenrpg.utils.TaskUtil;
import lombok.Getter;
import org.bukkit.plugin.PluginManager;

@Getter
public final class HeavenRPG extends HeavenPaperPlugin
{
    @Getter
    public enum ConfigType
    {
        ITEMS("items.yml");

        private final String fileName;

        ConfigType(String fileName)
        {
            this.fileName = fileName;
        }
    }

    @Getter
    private static HeavenRPG instance;

    @Override
    public void onEnable()
    {
        instance = this;
        getDataFolder().mkdir();
        addYamlManager(new PaperConfiguration(this, "", "config.yml", true));
        addYamlManager(new PaperLocale(this, "", "locale.yml", true));
        addYamlManager(new PaperConfiguration(this, "", "items.yml"));
        HeavenRPGItem.initialize();
        Spell.initialize();
        OriginRace.initialize();
        OriginClass.initialize();
        Origin.initialize();
        TaskUtil.initialize();
        PluginManager plugins = getServer().getPluginManager();
        plugins.registerEvents(new OriginListener(), this);
        plugins.registerEvents(new KeybindListener(), this);
        getCommand("heavenrpg").setExecutor(new MainCommand());
        getCommand("keybind").setExecutor(new KeybindCommand());
    }

    @Override
    public void onDisable()
    {
        OriginRace.saveRaces();
        OriginClass.saveClasses();
        Origin.saveOrigins();
    }

    public PaperConfiguration getConfiguration(ConfigType type)
    {
        return getYamlManager(type.getFileName(), PaperConfiguration.class);
    }
}
