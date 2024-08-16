package org.dxrgd.ei.config;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.dxrgd.api.bukkit.message.Texts;
import org.dxrgd.api.open.file.configuration.OConf;
import org.dxrgd.api.open.plugin.OPlugin;
import org.dxrgd.api.open.value.util.NumUtil;
import org.dxrgd.ei.EI;

public class GConfig extends OConf {

	public boolean TYPE_RESTRICT, LEVEL_RESTRICT, USE_VANILLA_DMG;
	public int MAX_ITEM_UPGRADES;

	public GConfig(String file, OPlugin plugin) {
		super(file, plugin);
	}

	@Override
	public void loadConfig() {
		TYPE_RESTRICT = getBoolean("type-restriction", true, true);
		//LEVEL_RESTRICT = getBoolean("level-restriction", false, true);
		USE_VANILLA_DMG = getBoolean("use-vanilla-damage", true, true);
		MAX_ITEM_UPGRADES = getInt("item-settings.max-item-upgrades", 12, true);

		if (!EI.datas.isEmpty()) EI.datas.clear();
		if (!EI.rangedWeapon.isEmpty()) EI.rangedWeapon.clear();
		if (!EI.predefined.isEmpty()) EI.predefined.clear();

		for (final String key : getStringList("block-tool"))
			EI.datas.add(key.toUpperCase());

		for (final String key : getStringList("predefined-cmd")) {
			String[] split = key.split(":");
			Material mat = null;
			final List<Integer> cmds = new ArrayList<>();

			if ((mat = Material.matchMaterial(split[0])) != null) {
				split = split[1].split(",");

				for (final String element : split) {
					if (element.matches("^\\d+\\-\\d+$")) {
						final String[] range = element.split("-");
						int start = NumUtil.parseInteger(range[0]);
						if (start < 1) {
							Texts.warn("Don't use negative values for CustomModelData! | " + key + ":" + start );
							continue;
						}
						final int end = NumUtil.parseInteger(range[1]);
						for ( ; start < end+1 ; start++)
							cmds.add(start);
					} else {
						final int value = NumUtil.parseInteger(element);
						if (value < 1) {
							Texts.warn("Don't use negative values for CustomModelData! | " + key + ":" + value );
							continue;
						}
						cmds.add(value);
					}
				}
				EI.predefined.put(mat, cmds);
			} else
				Texts.warn("Predefined custom model data: " + key + " has incorrect material");
		}

		for (final String key : getStringList("ranged-weapon"))
			EI.rangedWeapon.add(key.toUpperCase());
	}

}
