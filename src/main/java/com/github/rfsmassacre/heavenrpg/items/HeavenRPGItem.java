package com.github.rfsmassacre.heavenrpg.items;

import com.github.rfsmassacre.heavenlibrary.paper.configs.PaperConfiguration;
import com.github.rfsmassacre.heavenlibrary.paper.items.HeavenItem;
import com.github.rfsmassacre.heavenrpg.HeavenRPG;
import org.bukkit.Material;
import org.bukkit.inventory.Recipe;

import java.util.*;

public abstract class HeavenRPGItem extends HeavenItem
{
    private static final Map<String, HeavenRPGItem> CACHE = new HashMap<>();

    public static void initialize()
    {
        CACHE.clear();
        registerItem(new PriestBook());
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
            if (clazz.isInstance(clazz))
            {
                return clazz.cast(clazz);
            }
        }

        return null;
    }

    public static Set<HeavenRPGItem> getItems()
    {
        return new HashSet<>(CACHE.values());
    }

    public HeavenRPGItem(Material material, int amount, String name, String displayName, List<String> lore)
    {
        super(HeavenRPG.getInstance(), material, amount, name, displayName, lore);

        PaperConfiguration config = HeavenRPG.getInstance().getConfiguration(HeavenRPG.ConfigType.ITEMS);
        setDisplayName(config.getString(name + ".display-name" + displayName));
        setItemLore(config.getStringList(name + ".lore"));
        setCustomModelData(config.getInt(name + ".custom-model-data", 0));
    }

    @Override
    protected Recipe createRecipe()
    {
        return null;
    }
}
