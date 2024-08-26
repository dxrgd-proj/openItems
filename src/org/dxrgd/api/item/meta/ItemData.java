package org.dxrgd.api.item.meta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.dxrgd.api.item.utility.PersistentDataReader;
import org.dxrgd.api.rpg.attributes.AttrBase;
import org.dxrgd.api.rpg.attributes.AttrManager;

import lombok.experimental.Accessors;

@Accessors(fluent = true)
public class ItemData {

	private PersistentDataReader reader;

	private PersistentDataContainer ei;
	private List<String> abilities;
	private Map<NamespacedKey, String> attributes;
	private String rarity;
	private String type;
	private boolean repairable;
	private int level;
	private int upgrades;
	private int curdurability;
	private int maxDurability;

	public ItemData(ItemStack from) {
		reader = PersistentDataReader.of(from.getItemMeta());
		attributes = new HashMap<>();
		abilities = new ArrayList<>();

		if (reader.hasContainer(Const.PDC))
			ei = reader.container(Const.PDC);
		else
			ei = from.getItemMeta().getPersistentDataContainer().getAdapterContext().newPersistentDataContainer();
	}

	public void unpack() {
		if (reader.hasStringList(Const.ABILITIES))
			abilities = reader.stringList(Const.ABILITIES);

		for (final AttrBase attr : AttrManager.getAttributes())
			if (reader.hasString(attr.getKey()))
				attributes.put(attr.getKey(), reader.string(attr.getKey()));

		if (reader.hasString(Const.RARITY)) rarity = reader.string(Const.RARITY);

		if (reader.hasString(Const.ITEM_TYPE)) type = reader.string(Const.ITEM_TYPE);

		if (reader.hasBool(Const.REPAIRABLE)) repairable = reader.bool(Const.REPAIRABLE);

		if (reader.hasInteger(Const.LEVEL)) level = reader.integer(Const.LEVEL);

		if (reader.hasInteger(Const.UPGRADES)) upgrades = reader.integer(Const.UPGRADES);

		if (reader.hasInteger(Const.CUR_DURABILITY)) curdurability = reader.integer(Const.CUR_DURABILITY);

		if (reader.hasInteger(Const.MAX_DURABILITY)) maxDurability = reader.integer(Const.MAX_DURABILITY);
	}



}
