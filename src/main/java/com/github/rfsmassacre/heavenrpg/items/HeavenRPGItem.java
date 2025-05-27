package com.github.rfsmassacre.heavenrpg.items;

import com.github.rfsmassacre.heavenlibrary.interfaces.LocaleData;
import com.github.rfsmassacre.heavenlibrary.paper.configs.PaperConfiguration;
import com.github.rfsmassacre.heavenlibrary.paper.items.HeavenItem;
import com.github.rfsmassacre.heavenrpg.HeavenRPG;
import org.bukkit.Material;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class HeavenRPGItem extends HeavenItem
{
    private static final Map<String, HeavenRPGItem> CACHE = new HashMap<>();
    private static PaperConfiguration config;
    
    public static void initialize()
    {
        CACHE.clear();
        config = HeavenRPG.getInstance().getConfiguration(HeavenRPG.ConfigType.ITEMS);
        registerItem(new HeavenRPGItem("PriestBook"));
    }

    public static void registerItem(HeavenRPGItem item)
    {
        CACHE.put(item.name, item);
    }

    public static void unregisterItem(String name)
    {
        CACHE.remove(name);
    }

    public static HeavenRPGItem getItem(String name)
    {
        return CACHE.get(name);
    }

    public static <T extends HeavenRPGItem> T getItem(Class<T> clazz)
    {
        for (HeavenRPGItem item : CACHE.values())
        {
            if (clazz.isInstance(item))
            {
                return clazz.cast(item);
            }
        }

        return null;
    }

    public static Set<HeavenRPGItem> getItems()
    {
        return new HashSet<>(CACHE.values());
    }

    private static Material getMaterial(String materialName)
    {
        try
        {
            return Material.getMaterial(materialName);
        }
        catch (Exception exception)
        {
            return Material.STICK;
        }
    }

    public HeavenRPGItem(String name)
    {
        super(HeavenRPG.getInstance(),
                getMaterial(config.getString(name + ".material", "STICK").toUpperCase()),
                config.getInt(name + ".amount", 1),
                name,
                LocaleData.format(config.getString(name + ".display-name", name)),
                new ArrayList<>());

        setItemLore(config.getStringList(name + ".lore"));
        setCustomModelData(config.getInt(name + ".custom-model-data", 0));
        setMaxSize(config.getInt(name + ".amount", 1));
    }

    public void setMaxSize(int size)
    {
        ItemMeta meta = item.getItemMeta();
        meta.setMaxStackSize(size);
        item.setItemMeta(meta);
    }

    @Override
    protected Recipe createRecipe()
    {
        return null;
    }
}
