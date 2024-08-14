package org.aslstd.api.durability.material;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.aslstd.ei.EI;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.dxrgd.api.bukkit.message.Texts;
import org.dxrgd.api.bukkit.setting.impl.FileSettings;
import org.dxrgd.api.open.file.configuration.type.Yaml;
import org.dxrgd.api.open.value.ModifierType;
import org.dxrgd.api.open.value.Value;
import org.dxrgd.api.open.value.util.NumUtil;

import de.tr7zw.changeme.nbtapi.NBTItem;
import lombok.Getter;

public class RepairMaterial extends ItemStack {

	private static ConcurrentMap<String, RepairMaterial> materials = new ConcurrentHashMap<>();

	public static final Yaml MATERIALS_DB = new Yaml(EI.instance().getDataFolder() + "/repair-materials.yml", EI.instance());

	public static RepairMaterial getMaterial(String key) {
		return materials.get(key.toLowerCase());
	}

	public static Collection<RepairMaterial> getMaterials() {
		return materials.values();
	}

	public static RepairMaterial isMaterial(ItemStack stack) {
		if (stack == null || stack.getType().name().contains("AIR")) return null;

		RepairMaterial mat = null;
		final NBTItem item = new NBTItem(stack);

		if (item.hasTag("ei-repair"))
			mat = materials.get(item.getString("ei-repair"));

		return mat;
	}

	public static void init() {
		if (!materials.isEmpty())
			materials.clear();

		for (final String key : MATERIALS_DB.getKeys(false)) {
			if (key == null) continue;

			final RepairMaterial mat = new RepairMaterial(key);

			materials.put(key.toLowerCase(), mat);
		}

	}

	@Getter private String key;
	private ItemMeta meta;

	private FileSettings settings;

	@Getter private Value repairValue;
	@Getter private int repairCost;

	public RepairMaterial(String key) {
		super(Material.STICK);
		settings = new FileSettings();
		settings.importYaml(MATERIALS_DB, key);
		this.key = key;
		meta = getItemMeta();

		setMaterial(settings.getValue("material"))
		.setData(settings.getValue("data"))
		.setDisplayName(settings.getValue("display.name"))
		.writeLore(settings.exportArray("display.lore"));

		repairCost = NumUtil.parseInteger(settings.getValue("cost-levels"));

		final String value = settings.getValue("repair-value");
		repairValue = new Value(value, ModifierType.getFromValue(value));
	}

	public RepairMaterial writeLore(List<String> prepaired) {
		if (prepaired == null || prepaired.isEmpty()) return this;

		final List<String> lore = new ArrayList<>();

		for (final String prep : prepaired)
			if (prep != null) lore.add(Texts.c(prep));

		meta.setLore(lore);
		setItemMeta(meta);

		return this;
	}

	public RepairMaterial setData(String data) {
		if (data == null) return this;
		if (NumUtil.isNumber(data));
		int d = NumUtil.parseInteger(data);
		if (d < 0) d = 0;

		meta.setCustomModelData(d);

		setItemMeta(meta);

		return this;
	}

	public RepairMaterial setDisplayName(String displayName) {
		if (displayName == null) return this;

		meta.setDisplayName(Texts.c(displayName));

		setItemMeta(meta);
		return this;
	}

	public RepairMaterial setMaterial(String material) {
		if (material == null) {
			Texts.warn("&4Incorrect Material for key:&a'" + key, EI.prefix);
			return this;
		}

		final Material mat = Material.matchMaterial(material);
		if (mat == null) return this;
		setType(mat);
		return this;
	}

	public ItemStack toStack() {
		final NBTItem item = new NBTItem(this);

		item.setString("ei-repair", key);

		return item.getItem();
	}

}
