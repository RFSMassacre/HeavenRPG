package com.github.rfsmassacre.heavenrpg.items;

import org.bukkit.Material;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;

public abstract class CastItem extends HeavenRPGItem
{
    public CastItem(Material material, String name, String displayName)
    {
        super(material, 1, name, displayName, new ArrayList<>());

        ItemMeta meta = item.getItemMeta();
        meta.setMaxStackSize(1);
        item.setItemMeta(meta);
    }
}
