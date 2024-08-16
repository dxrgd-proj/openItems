package org.dxrgd.api.item;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.dxrgd.api.bukkit.message.Texts;
import org.dxrgd.api.open.file.configuration.type.Yaml;
import org.dxrgd.ei.EI;

public class ItemManager {

	public static void preInit() {
		final File itemDB = new File(EI.instance().getDataFolder() + "/items");
		itemDB.mkdirs();
		if (itemDB.listFiles() == null) return;
		final ArrayList<File> files = new ArrayList<>(Arrays.asList(itemDB.listFiles()));

		while (files.size() > 0) {
			final File file = files.remove(0);
			if (file.isDirectory()) {
				if (file.listFiles().length > 0) files.addAll(Arrays.asList(file.listFiles()));

			} else
				if (Yaml.getFileExtension(file).equals("yml")) {
					final Yaml util = new Yaml(file);
					if (util.getKeys(false).size() > 0)
						for (final String section : util.getKeys(false)) {

							final ESimpleItem item = new ESimpleItem(util, section);

							ESimpleItem.registered.put(section.toLowerCase(), item);
						}
				}
		}
	}

	public static void process() {
		boolean first = false;

		final List<ESimpleItem> items = new ArrayList<>(ESimpleItem.getItems());

		items.sort(Comparator.comparing(ESimpleItem::getKey));

		for (final ESimpleItem item : items) {

			final String section = item.getKey();
			final Yaml util = item.getFile();

			if (util.contains(section + ".copy-from")) {
				final String copyFrom = util.getString(section + ".copy-from");
				final ESimpleItem from = ESimpleItem.getById(copyFrom);

				if (from == null) {
					Texts.warn("CANT COPY PARAMETERS FROM " + copyFrom + " FOR ITEM " + item.getKey()
					+ " BECAUSE IT NOT EXIST, THIS ITEM SKIPPED", "EI");
					continue;
				}

				item.getSettings().importFromSettings(from.getSettings());
				if (!first)
					first = true;
			}

			item.build();
		}
	}

}
