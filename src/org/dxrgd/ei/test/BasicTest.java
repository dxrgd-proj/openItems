package org.dxrgd.ei.test;

import java.util.Arrays;
import java.util.regex.Pattern;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.dxrgd.api.CustomParams;
import org.dxrgd.api.bukkit.message.Texts;
import org.dxrgd.api.bukkit.utility.BasicMetaAdapter;
import org.dxrgd.api.durability.DManager;
import org.dxrgd.api.item.utils.ItemUtils;
import org.dxrgd.api.open.value.util.NumUtil;
import org.dxrgd.api.rpg.attributes.AttrBase;
import org.dxrgd.api.rpg.attributes.AttrManager;
import org.dxrgd.api.rpg.attributes.Attributes;
import org.dxrgd.ei.EI;

import de.tr7zw.changeme.nbtapi.NBTItem;

public class BasicTest {

	public static final void test() {
		Texts.fine("Durability System: Checking..", EI.prefix);
		ItemStack stack = new ItemStack(Material.DIAMOND_SWORD);
		final ItemMeta meta = stack.getItemMeta();

		meta.setLore(Arrays.asList(AttrManager.get(Attributes.PHYS_DAMAGE).convertToLore("+", "1"),CustomParams.LEVEL.convert("8"), DManager.getDurabilityLore("10")));

		stack.setItemMeta(meta);

		final Pattern patt = DManager.checkDurabilityType(stack);

		final String bef = BasicMetaAdapter.getStringValue(patt, stack);
		if (bef == null || bef.equals("")) { Texts.warn("Durability is null on test item! Lore Manager not works, report it to author", EI.prefix); return; }
		if (NumUtil.parseInteger(bef.split("/")[0]) < 1) Texts.warn("Durability on test item less than 1, wtf? Lore Manager not works, report it to author", EI.prefix);
		final int before = NumUtil.parseInteger(bef.split("/")[0]);

		stack = DManager.changeDurability(stack, -1, 0);
		stack = DManager.changeDurability(stack, -1, 0);
		stack = DManager.changeDurability(stack, -1, 0);
		stack = DManager.changeDurability(stack, -1, 0);
		final String af = BasicMetaAdapter.getStringValue(patt, stack);

		if (af == null || af.equals("")) { Texts.warn("Durability is null after changing durability! Durability Manager has errors, report it to author", EI.prefix); return; }
		if (NumUtil.parseInteger(af.split("/")[0]) < 1) Texts.warn("Durability less than 1 after changing durability! Durability Manager has errors, report it to author", EI.prefix);

		final int after = NumUtil.parseInteger(af.split("/")[0]);

		if (after < before) Texts.fine("Durability System: No any problem found, have fun using this plugin!", EI.prefix);
		else if (before == after) Texts.warn("Durability change method not works properly, report it to author!", EI.prefix);
		else if (after > before)
			Texts.warn(
			"Durability increased after changing.. maybe not bad, but now you has infinite items, report this problem to author!",
			EI.prefix);

		final NBTItem item = new NBTItem(stack);

		item.setString("ei-id", "id");
		item.setString("ei-rarity", "trash");
		item.setInteger("ei-level", 8);

		final ItemStack upgrade = ItemUtils.upgrade(item.getItem());

		if (BasicMetaAdapter.getStringValue(AttrBase.getRegexPattern(AttrManager.get(Attributes.PHYS_DAMAGE)), upgrade).contains("3") &&
		BasicMetaAdapter.getStringValue(CustomParams.LEVEL.pattern(), upgrade).contains("9"))
			Texts.fine("Item Upgrading System works correctly :)");
		else {
			Texts.warn("Item Upgrading System not works correctly! Send this message to plugin author " + EI.instance().getDescription().getAuthors().get(0));
			Texts.warn(upgrade.getItemMeta().getLore().toString());
		}

	}

}
