package org.aslstd.api.entity;

import java.util.List;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.dxrgd.api.bukkit.utility.BasicMetaAdapter;
import org.dxrgd.api.bukkit.utility.IStatus;
import org.dxrgd.api.bukkit.utility.ItemStackUtil;
import org.dxrgd.api.open.player.OPlayer;
import org.dxrgd.api.open.value.util.NumUtil;
import org.dxrgd.api.rpg.attributes.AttrBase;
import org.dxrgd.api.rpg.attributes.AttrManager;
import org.dxrgd.api.rpg.equipment.EquipSlot;

import net.kyori.adventure.text.Component;

public class RPGPlayer {

	public static double getAveragePercent(OPlayer ej, AttrBase stat) {
		final double[] mult = { 0D, 0D };

		for (final EquipSlot slot : EquipSlot.values())
			if (ej.options().checkValue("player.equip." + slot.name().toLowerCase() + "." + stat.getKey() + "-multiplier")) {
				final Double[] val = ej.options().readRange("player.equip." + slot.name().toLowerCase() + "." + stat.getKey() + "-multiplier").toArray();
				mult[0] = mult[0] + val[0];
				mult[1] = mult[1] + val[1];
			}

		if (mult[1] < mult[0]) return mult[0];
		return (mult[0] + mult[1]) / 2;
	}

	public static void calculateEquip(OPlayer ej, EquipSlot slot) {
		final ItemStack stack = ej.equip().get(slot);

		if (!ItemStackUtil.validate(stack, IStatus.HAS_LORE)) {
			ej.equip().remove(slot);
			return;
		}

		RPGPlayer.calculateStats(ej, stack, slot);
	}

	private static void calculateStats(OPlayer p, ItemStack stack, EquipSlot slot) {
		final ItemMeta meta = stack.getItemMeta();
		final List<Component> lore = meta.lore();

		for (final AttrBase stat : AttrManager.getAttributes())
			if (BasicMetaAdapter.contains(lore, AttrBase.getRegexPattern(stat))) {
				final String pref = BasicMetaAdapter.getStringValue(AttrBase.getRegexPattern(stat), stack);

				final boolean isModifier = pref.endsWith("%");
				final boolean isNegative = pref.startsWith("-");

				final String[] value = pref.replaceFirst("[+-]*", "").replace("%", "").split("-");

				double first = 0D;
				double second = 0D;

				if ((value.length > 0) && NumUtil.isNumber(value[0])) {
					first = NumUtil.parseDouble(value[0]);
					second = NumUtil.parseDouble(value[0]);
				}

				if (value.length > 1)
					if (NumUtil.isNumber(value[1]))
						second = NumUtil.parseDouble(value[1]);

				if (isNegative) {
					first *= -1; second *= -1;
				}

				if ((first != 0D) && (second != 0D))
					if (!isModifier)
						p.options().writeRange("player.equip." + slot.name().toLowerCase() + "." + stat.getKey(), first, second);
					else
						p.options().writeRange("player.equip." + slot.name().toLowerCase() + "." + stat.getKey() + "-multiplier", first, second);
			}
	}

}
