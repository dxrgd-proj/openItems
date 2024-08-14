package org.aslstd.api.durability;

import java.util.List;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.dxrgd.api.bukkit.message.Texts;
import org.dxrgd.api.bukkit.utility.BasicMetaAdapter;
import org.dxrgd.api.bukkit.utility.IStatus;
import org.dxrgd.api.bukkit.utility.InventoryUtil;
import org.dxrgd.api.bukkit.utility.ItemStackUtil;
import org.dxrgd.api.open.value.util.NumUtil;
import org.dxrgd.api.rpg.equipment.EquipSlot;
import org.dxrgd.api.rpg.equipment.EquipType;
import org.dxrgd.api.rpg.event.equipment.PrepareEquipEvent;
import org.dxrgd.core.OpenLib;

import net.kyori.adventure.text.Component;

public class DManager { // Durability Manager

	public static Pattern	repairableDurability	= Pattern.compile(Texts.e(OpenLib.lang().get("openlib.gameplay.durability.visual").toLowerCase()) + ".?\\s\\[([-+]?\\d+\\/\\d+)\\]");
	public static Pattern	nonRepairableDurability	= Pattern.compile(Texts.e(OpenLib.lang().get("openlib.gameplay.durability.visual").toLowerCase()) + ".?\\s\\[([-+]?\\d+)\\]");

	public static ItemStack changeDurability(ItemStack stack, int amount, int max) {
		final Pattern patt = DManager.checkDurabilityType(stack);
		if (patt == null) return stack;
		final List<Component> lore = stack.getItemMeta().lore();
		final ItemMeta meta = stack.getItemMeta();

		final int index = BasicMetaAdapter.indexOf(lore, patt);
		final String val = BasicMetaAdapter.getStringValue(patt, lore);
		if (val == null || val.equalsIgnoreCase("") || !val.matches("(-)?[0-9]*(/)?[0-9]*")) return stack;
		final String[] value = val.split("/");

		if (value.length < 1) return stack;
		final int first = NumUtil.parseInteger(value[0]);
		int second = max;

		if (value.length > 1) {
			if (second <= 0)
				second = NumUtil.parseInteger(value[1]);
			lore.set(index, Component.text(DManager.getDurabilityLore((first + amount) + "", second + "")));
		}

		if (value.length == 1)
			lore.set(index, Component.text(DManager.getDurabilityLore((first + amount) + "")));

		meta.lore(lore);
		stack.setItemMeta(meta);
		return stack;
	}

	public static Pattern checkDurabilityType(ItemStack stack) {
		if (!ItemStackUtil.validate(stack, IStatus.HAS_LORE)) return null;
		else return checkDurabilityType(stack.getItemMeta().lore());
	}

	public static boolean isRepairable(ItemStack stack) {
		return BasicMetaAdapter.contains(stack, repairableDurability);
	}

	public static Pattern checkDurabilityType(List<Component> lore) {
		if (BasicMetaAdapter.contains(lore, DManager.repairableDurability)) return DManager.repairableDurability;
		if (BasicMetaAdapter.contains(lore, DManager.nonRepairableDurability)) return DManager.nonRepairableDurability;
		else return null;
	}

	public static void decreaseArmorDurability(Player p, int amount) {
		final ItemStack[] armourSet = new ItemStack[] { p.getInventory().getHelmet(), p.getInventory().getChestplate(), p.getInventory().getLeggings(), p.getInventory().getBoots() };

		for (int i = 0; i < 4; i++) {
			if (!NumUtil.isNegative(amount + "")) amount = amount * -1;
			final Pattern patt = DManager.checkDurabilityType(armourSet[i]);
			if (patt == null) continue;

			DManager.changeDurability(armourSet[i], amount, 0);
			final String val = BasicMetaAdapter.getStringValue(patt, armourSet[i]);
			if (val != null && !val.equals(""))
				if (NumUtil.parseInteger(val.split("/")[0]) < 1) {
					armourSet[i] = null;

					final EquipSlot slot = EquipSlot.id(39-i);

					Bukkit.getPluginManager().callEvent(new PrepareEquipEvent(p, slot, null));
				}
		}

		p.getInventory().setHelmet(armourSet[0]);
		p.getInventory().setChestplate(armourSet[1]);
		p.getInventory().setLeggings(armourSet[2]);
		p.getInventory().setBoots(armourSet[3]);
	}

	public static void decreaseDurability(Player p, EquipSlot slot, int amount) {
		final ItemStack stack = EquipSlot.get(slot, p);

		if (!ItemStackUtil.validate(stack, IStatus.HAS_LORE)) return;

		final Pattern patt = DManager.checkDurabilityType(stack);
		if (patt == null) return;

		final String[] value = BasicMetaAdapter.getStringValue(patt, stack).split("/");
		if (value.length < 1) return;
		if (!NumUtil.isNegative(amount + "")) amount = amount * -1;
		if (NumUtil.parseInteger(value[0]) - amount <= 1) {
			InventoryUtil.decreaseItemAmount(stack, p, 1);

			Bukkit.getPluginManager().callEvent(new PrepareEquipEvent(p, slot, null));
		}
		else DManager.changeDurability(stack, amount, 0);

	}

	public static String getDurabilityLore(String... values) {
		final String prepaired = OpenLib.lang().get("openlib.gameplay.durability.visual") + ": " + OpenLib.lang().get("openlib.gameplay.durability.decorator");

		String converted = values.length > 1 ? Texts.c(prepaired + OpenLib.lang().get("openlib.gameplay.durability.value.repairable")) :
			Texts.c(prepaired + OpenLib.lang().get("openlib.gameplay.durability.value.non-repairable"));
		int $ = 0;
		for (final String dod : values) {
			converted = converted.replace("$" + $, dod);
			$++;
		}

		while (converted.contains("$" + $)) {
			converted = converted.replace("$" + $, "");
			$++;
		}
		return converted;
	}

	public static String getDurabilityString(Player p, EquipType eq) {
		ItemStack stack = null;
		switch (eq) {
			case BOOTS:
				stack = p.getInventory().getBoots();
				break;
			case CHESTPLATE:
				stack = p.getInventory().getChestplate();
				break;
			case HAND:
				stack = p.getInventory().getItemInMainHand();
				break;
			case HELMET:
				stack = p.getInventory().getHelmet();
				break;
			case LEGGINGS:
				stack = p.getInventory().getLeggings();
				break;
			case OFF_HAND:
				stack = p.getInventory().getItemInOffHand();
				break;
			default:
				break;
		}

		return getDurabilityString(stack);
	}

	public static String getDurabilityString(ItemStack stack) {
		if (ItemStackUtil.validate(stack, IStatus.HAS_LORE)) return "0";
		final List<Component> lore = stack.getItemMeta().lore();

		final Pattern patt = DManager.checkDurabilityType(lore);
		final String value = BasicMetaAdapter.getStringValue(patt, lore);
		if (value.equalsIgnoreCase("")) return "0";

		return value;
	}

}
