package org.aslstd.ei.config;

import org.dxrgd.api.open.file.configuration.OConf;
import org.dxrgd.api.open.plugin.OPlugin;

public class LangConfig extends OConf {

	public String HEADER_ATTR, HEADER_DESC, HEADER_LORE, NAME_LEVEL, NAME_RARITY;

	public String TYPE_ONE_HANDED, TYPE_TWO_HANDED, TYPE_ARMOR_HELMET, TYPE_ARMOR_CHESTPLATE, TYPE_ARMOR_LEGGINGS, TYPE_ARMOR_BOOTS, TYPE_TOOL, TYPE_SHIELD;

	public LangConfig(String file, OPlugin plugin) {
		super(file, plugin);
	}

	@Override
	public void loadConfig() {
		HEADER_DESC = getString("eimodule.header.custom", "&5&l»------>[&6DESC&5&l]", true);
		HEADER_ATTR = getString("eimodule.header.stats", "&5&l»------>[&6STATS&5&l]", true);
		HEADER_LORE = getString("eimodule.header.lore", "&5&l»------>[&6LORE&5&l]", true);

		TYPE_TWO_HANDED = getString("eimodule.type.two-handed", "&7Two-Handed", true);
		TYPE_ONE_HANDED = getString("eimodule.type.one-handed", "&7One-Handed", true);
		TYPE_ARMOR_HELMET = getString("eimodule.type.helmet", "&7Helmet", true);
		TYPE_ARMOR_CHESTPLATE = getString("eimodule.type.chestplate", "&7Chestplate", true);
		TYPE_ARMOR_LEGGINGS = getString("eimodule.type.leggings", "&7Leggings", true);
		TYPE_ARMOR_BOOTS = getString("eimodule.type.boots", "&7Boots", true);
		TYPE_TOOL = getString("eimodule.type.tool", "&7Tool", true);
		TYPE_SHIELD = getString("eimodule.type.shield", "&7Shield", true);
	}

}
