package org.aslstd.api.ability;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.aslstd.ei.EI;
import org.dxrgd.api.bukkit.message.Texts;
import org.dxrgd.api.bukkit.setting.impl.FileSettings;
import org.dxrgd.api.open.file.configuration.type.Yaml;

import lombok.Getter;

public class EAbility {

	private static ConcurrentMap<String, EAbility> registered = new ConcurrentHashMap<>();

	public static EAbility getAbility(String key) {
		return registered.get(key.toLowerCase());
	}

	public static Collection<EAbility> getRegistered() {
		return registered.values();
	}

	public static void init() {
		final File abilityFolder = new File(EI.instance().getDataFolder() + "/abilities");
		abilityFolder.mkdirs();
		if (abilityFolder.listFiles() == null) return;
		final ArrayList<File> files = new ArrayList<>(Arrays.asList(abilityFolder.listFiles()));

		while (files.size() > 0) {
			final File file = files.remove(0);
			if (file.isDirectory()) {
				if (file.listFiles().length > 0) files.addAll(Arrays.asList(file.listFiles()));

			} else
				if (Yaml.getFileExtension(file).equals("yml")) {
					final Yaml util = new Yaml(file);
					if (util.getKeys(false).size() > 0) {
						for (String section : util.getKeys(false)) {
							section = section.toLowerCase();
							final EAbility ability = new EAbility(section, util);

							EAbility.registered.put(section, ability);
							Texts.debug("Ability: &5" + section + "&e successfully registered");
						}
					}
				}
		}
	}

	@Getter private AbilityType type;

	@Getter private BasicAction action;

	@Getter private String key;

	@Getter private List<String> toLore;

	@Getter private FileSettings settings;

	public EAbility(String key, Yaml file) {
		this.key = key;

		settings = new FileSettings();
		settings.importYaml(file, key);

		type = AbilityType.from(settings.getValue("type"));

		if (type == null) {
			Texts.warn("Incorrect Ability Type provided : " + key + " : " + settings.getValue("type") == null ? "null" : settings.getValue("type"));
			return;
		}

		toLore = new ArrayList<>();
		for (final String line : settings.exportArray("lore"))
			toLore.add(Texts.c(line));

		action = EI.getActionManager().getAction(settings.getValue("action"));

		if (action == null) {
			Texts.warn("Incorrect Ability Action provided : " + key + " : " + settings.getValue("type") == null ? "null" : settings.getValue("type"));
			return;
		}

		action.acceptSettings(settings);
	}

}
