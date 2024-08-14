package org.aslstd.api.rarity;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.aslstd.ei.EI;
import org.dxrgd.api.bukkit.message.Texts;
import org.dxrgd.api.open.file.configuration.type.Yaml;

public class RarityManager {

	public static final ConcurrentMap<String,ERarity> rarities = new ConcurrentHashMap<>();

	public static ERarity getById(String id) {
		for (final Entry<String, ERarity> entry : rarities.entrySet())
			if (entry.getKey().equalsIgnoreCase(id) || Texts.e(entry.getValue().getVisualName()).equalsIgnoreCase(Texts.e(id))) return entry.getValue();
		return null;
	}

	public static void init() {
		final File itemDB = new File(EI.instance().getDataFolder() + "/rarity");
		itemDB.mkdirs();
		if (itemDB.listFiles() == null) return;

		final ArrayList<File> files = new ArrayList<>(Arrays.asList(itemDB.listFiles()));

		while (files.size() > 0) {

			final File file = files.remove(0);
			if (Yaml.getFileExtension(file).equals("yml")) {
				final Yaml util = new Yaml(file);

				if (!util.contains("id")) util.set("id", file.getName().substring(0, file.getName().length()-4));

				final ERarity rar = new ERarity(util.getString("id"), util);

				rarities.put(util.getString("id"), rar);
			}
		}
	}

}
