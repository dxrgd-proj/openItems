package org.dxrgd.api.item;

import org.dxrgd.api.bukkit.message.Texts;
import org.dxrgd.ei.EI;

import lombok.Getter;

public enum ItemType {
	ONE_HANDED(EI.getLang().TYPE_ONE_HANDED),
	TWO_HANDED(EI.getLang().TYPE_TWO_HANDED),
	ARMOR_HELMET(EI.getLang().TYPE_ARMOR_HELMET),
	ARMOR_CHESTPLATE(EI.getLang().TYPE_ARMOR_CHESTPLATE),
	ARMOR_LEGGINGS(EI.getLang().TYPE_ARMOR_LEGGINGS),
	ARMOR_BOOTS(EI.getLang().TYPE_ARMOR_BOOTS),
	TOOL(EI.getLang().TYPE_TOOL),
	SHIELD(EI.getLang().TYPE_SHIELD);

	@Getter private String visualName;

	ItemType(String visualName) {
		this.visualName = visualName;
	}

	public static boolean isArmor(ItemType type) {
		switch(type) {
			case ARMOR_HELMET:
			case ARMOR_CHESTPLATE:
			case ARMOR_LEGGINGS:
			case ARMOR_BOOTS:
				return true;
			default:
				return false;
		}
	}

	public static ItemType getByKey(String value) {
		if (value == null)  return null;

		for (final ItemType type : values())
			if (type.name().equalsIgnoreCase(value) || Texts.e(type.getVisualName()).equalsIgnoreCase(Texts.e(value)))
				return type;

		return null;
	}

}
