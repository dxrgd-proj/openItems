package org.aslstd.api.item.meta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aslstd.api.item.utility.PersistentDataReader;
import org.aslstd.ei.NKeys;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.dxrgd.api.rpg.attributes.AttrBase;
import org.dxrgd.api.rpg.attributes.AttrManager;

import lombok.experimental.Accessors;

@Accessors(fluent = true)
public class ItemPersistentData {

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

	public ItemPersistentData(ItemStack from) {
		reader = PersistentDataReader.of(from.getItemMeta());
		attributes = new HashMap<>();
		abilities = new ArrayList<>();

		if (reader.hasContainer(NKeys.PDC))
			ei = reader.container(NKeys.PDC);
		else
			ei = from.getItemMeta().getPersistentDataContainer().getAdapterContext().newPersistentDataContainer();
	}

	public void unpack() {
		if (reader.hasStringList(NKeys.ABILITIES))
			abilities = reader.stringList(NKeys.ABILITIES);

		for (final AttrBase attr : AttrManager.getAttributes())
			if (reader.hasString(attr.getKey()))
				attributes.put(attr.getKey(), reader.string(attr.getKey()));

		if (reader.hasString(NKeys.RARITY))
			rarity = reader.string(NKeys.RARITY);

		if (reader.hasString(NKeys.ITEM_TYPE))
			type = reader.string(NKeys.ITEM_TYPE);

		if (reader.hasBool(NKeys.REPAIRABLE))
			repairable = reader.bool(NKeys.REPAIRABLE);

		if (reader.hasInteger(NKeys.LEVEL))
			level = reader.integer(NKeys.LEVEL);

		if (reader.hasInteger(NKeys.UPGRADES))
			upgrades = reader.integer(NKeys.UPGRADES);

		if (reader.hasInteger(NKeys.CUR_DURABILITY))
			curdurability = reader.integer(NKeys.CUR_DURABILITY);

		if (reader.hasInteger(NKeys.MAX_DURABILITY))
			maxDurability = reader.integer(NKeys.MAX_DURABILITY);
	}



}
