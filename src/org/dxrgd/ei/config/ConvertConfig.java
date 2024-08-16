package org.dxrgd.ei.config;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.dxrgd.api.bukkit.message.Texts;
import org.dxrgd.api.bukkit.utility.IStatus;
import org.dxrgd.api.bukkit.utility.ItemStackUtil;
import org.dxrgd.api.item.ESimpleItem;
import org.dxrgd.api.open.file.configuration.OConf;
import org.dxrgd.api.open.plugin.OPlugin;
import org.dxrgd.api.open.value.Pair;
import org.dxrgd.api.open.value.util.NumUtil;
import org.dxrgd.ei.config.conversion.Conversion;

import de.tr7zw.changeme.nbtapi.NBTItem;

public class ConvertConfig extends OConf {

	public static final ConcurrentMap<Material, List<Conversion>> convert = new ConcurrentHashMap<>();

	public ConvertConfig(String fileName, OPlugin plugin) {
		super(fileName, plugin);
	}

	@Override
	public void loadConfig() {
		if (!convert.isEmpty()) convert.clear();

		for (final String key : getKeys(false)) {
			final Material mat = Material.matchMaterial(key);

			if (mat == null) { Texts.warn("Incorrect Material id! " + key + ", skipped!"); continue; }

			final List<Conversion> conversion = new ArrayList<>();

			for (int i = 0 ; contains(key + "."  + i) ; i++) {
				final String section = key + "." + i;
				final String displayName = getString(section + ".display-name");
				final List<String> itemID = getStringList(section + ".eitem");

				boolean incorrect = false;

				if (itemID.isEmpty()) {
					final String singleVar = getString(section + ".eitem");

					if (singleVar != null)
						itemID.add(singleVar + " 100%");
				}


				for (final String id : itemID)
					if (ESimpleItem.getById(id.split(" ")[0]) == null) {
						Texts.warn("Incorrect EItem id! " + section + ": " + id + ", skipped!");
						incorrect = true;
					}

				if (displayName == null) {
					Texts.warn("DisplayName cannot be null! " + section + ", skipped!");
					incorrect = true;
				}

				if (incorrect) return;
				else {
					final List<Pair<String, Double>> ids = new ArrayList<>();

					for (final String id : itemID) {
						final Pair<String, Double> pair = getPair(id);

						if (pair != null)
							ids.add(pair);
					}

					ids.sort(Comparator.comparingDouble(Pair::getSecond));

					conversion.add(new Conversion(Texts.e(displayName), ids));
				}
			}

			convert.put(mat, conversion);
		}
	}

	private static Pair<String, Double> getPair(String line) {
		final String[] args = line.replaceAll("%", "").split(" ");

		double chance;

		if (args.length > 1 && args[1] != null)
			chance = NumUtil.parseDouble(args[1]);
		else
			chance = 100d;

		return new Pair<>(args[0], chance);
	}


	public static ItemStack attemptConversion(ItemStack stack, boolean crafted) {
		final Material mat = stack.getType();

		final List<Conversion> conversion = convert.get(mat);

		if (!ItemStackUtil.validate(stack, IStatus.HAS_DISPLAYNAME))
			for (final Conversion conv : conversion) {
				if (conv.getTrigger_displayName().equalsIgnoreCase("none")) {
					final ESimpleItem eitem = ESimpleItem.getById(conv.roll());

					if (eitem == null)
						return stack;
					final ItemStack converted = eitem.toStack().clone();

					converted.setAmount(stack.getAmount());

					return converted;
				}
			}

		final String displayName = Texts.e(stack.getItemMeta().getDisplayName());

		for (final Conversion conv : conversion) {
			if (conv.getTrigger_displayName().equalsIgnoreCase(displayName)) {
				final ESimpleItem eitem = ESimpleItem.getById(conv.roll());

				if (eitem == null)
					return stack;
				final ItemStack converted = eitem.toStack().clone();

				converted.setAmount(stack.getAmount());

				return converted;
			}
		}

		final NBTItem item = new NBTItem(stack);
		if (crafted)
			item.setBoolean("ei-cratfed", true);

		return item.getItem();
	}

}
