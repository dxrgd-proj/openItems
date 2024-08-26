package org.dxrgd.api.item.utility;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.dxrgd.api.CustomParams;
import org.dxrgd.api.bukkit.utility.BasicMetaAdapter;
import org.dxrgd.api.bukkit.utility.IStatus;
import org.dxrgd.api.bukkit.utility.ItemStackUtil;
import org.dxrgd.api.item.ESimpleItem;
import org.dxrgd.api.open.value.Pair;
import org.dxrgd.api.rarity.ERarity;
import org.dxrgd.api.rarity.RarityManager;
import org.dxrgd.api.rpg.attributes.AttrBase;
import org.dxrgd.api.rpg.attributes.AttrManager;
import org.dxrgd.ei.EI;

import de.tr7zw.changeme.nbtapi.NBTItem;
import net.kyori.adventure.text.Component;

public class ItemUtils {

	public static ItemStack reroll(ItemStack source) {
		if (!ItemStackUtil.validate(source, IStatus.HAS_LORE)) return null;

		final NBTItem item = new NBTItem(source);

		if (!item.hasTag("ei-id")) return null;

		final ESimpleItem eItem = ESimpleItem.getById(item.getString("ei-id"));

		return eItem.toStack();
	}

	public static Pair<AttrBase,String> upgradeStat(Pair<AttrBase,String> attribute, ERarity rarity) {
		attribute.setSecond(attribute.getSecond().replace('1', '3'));
		return attribute;
	}

	public static ItemStack upgrade(ItemStack source) {
		if (!ItemStackUtil.validate(source, IStatus.HAS_LORE)) return null;
		if (!isUpgradable(source)) return null;
		final NBTItem item = new NBTItem(source);
		final ERarity rarity = RarityManager.getById(item.getString("ei-rarity"));
		if (rarity == null) return null;
		final int level = item.getInteger("ei-level");
		final List<Pair<AttrBase,String>> collectedStats = new ArrayList<>();

		final ItemStack target = source.clone();
		final ItemMeta targetMeta = target.getItemMeta();

		final List<Component> lore = source.getItemMeta().lore();

		final int lvIndex = BasicMetaAdapter.indexOf(lore, CustomParams.LEVEL.pattern());

		if (lvIndex == -1) return null;

		for (final AttrBase attr : AttrManager.getAttributes()) {
			final String val = BasicMetaAdapter.getStringValue(AttrBase.getRegexPattern(attr), lore);

			if (!val.equalsIgnoreCase("")) collectedStats.add(new Pair<>(attr,val));
		}

		collectedStats.forEach(pair -> upgradeStat(pair, rarity));

		for (final Pair<AttrBase,String> attr : collectedStats) {
			final int index = BasicMetaAdapter.indexOf(lore, AttrBase.getRegexPattern(attr.getFirst()));

			if (index != -1)
				lore.set(index, Component.text(attr.getFirst().convertToLore(attr.getSecond())));
		}

		lore.set(lvIndex, Component.text(CustomParams.LEVEL.convert(String.valueOf(level + 1))));

		targetMeta.lore(lore);
		target.setItemMeta(targetMeta);

		return target;
	}

	public static boolean isUpgradable(ItemStack source) {
		final NBTItem item = new NBTItem(source);

		if (!item.hasTag("ei-rarity")) return false;
		if (!item.hasTag("ei-level")) return false;
		if (!item.hasTag("ei-upgrades")) return true;

		if (EI.getGconfig().MAX_ITEM_UPGRADES == -1)
			return true;

		return item.getInteger("ei-upgrades") < EI.getGconfig().MAX_ITEM_UPGRADES;
	}

}
