package org.aslstd.api.rarity;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.dxrgd.api.open.file.configuration.type.Yaml;
import org.dxrgd.api.open.value.ModifierType;
import org.dxrgd.api.open.value.util.NumUtil;
import org.dxrgd.api.rpg.attributes.AttrBase;
import org.dxrgd.api.rpg.attributes.AttrManager;

import lombok.Getter;

public class ERarity {

	private ConcurrentMap<String, String> upgradeValues = new ConcurrentHashMap<>();

	@Getter private ModifierType 	upgradeAllAttrPercentsMod;
	@Getter private double 			upgradeAllAttrPercents;
	@Getter private ModifierType 	upgradeAllAttrValueMod;
	@Getter private double 			upgradeAllAttrValue;

	@Getter private String key;
	@Getter private String visualName;

	public ERarity(String key, Yaml file) {
		this.key = key;
		visualName = file.getString("visual-name");

		final String percentAllStats = file.getString("upgrade.percent-stats.all-stats");
		if (percentAllStats != null) {
			upgradeAllAttrPercentsMod = ModifierType.getFromValue(percentAllStats);
			upgradeAllAttrPercents = NumUtil.parseDouble(percentAllStats.replaceAll("%", ""));
		}

		final String valueAllStats = file.getString("upgrade.value-stats.all-stats");
		if (valueAllStats != null) {
			upgradeAllAttrValueMod = ModifierType.getFromValue(valueAllStats);
			upgradeAllAttrValue = NumUtil.parseDouble(valueAllStats.replaceAll("%", ""));
		}

		for (final String str : file.getSection("upgrade.percent-stats").getKeys(false)) {
			if (str.equalsIgnoreCase("all-stats")) continue;

			final AttrBase attr = AttrManager.get(str);
			if (attr != null) {
				upgradeValues.put("percent-" + attr.getKey(), file.getString("upgrade.percent-stats." + str));
			}
		}

		for (final String str : file.getSection("upgrade.value-stats").getKeys(false)) {
			if (str.equalsIgnoreCase("all-stats")) continue;

			final AttrBase attr = AttrManager.get(str);
			if (attr != null) {
				upgradeValues.put("value-" + attr.getKey(), file.getString("value.percent-stats." + str));
			}
		}

	}

}
