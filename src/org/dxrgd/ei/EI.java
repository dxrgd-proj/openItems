package org.dxrgd.ei;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.dxrgd.api.ability.EAbility;
import org.dxrgd.api.ability.action.ActionManager;
import org.dxrgd.api.bukkit.message.Texts;
import org.dxrgd.api.bukkit.utility.ItemStackUtil;
import org.dxrgd.api.durability.material.RepairMaterial;
import org.dxrgd.api.item.ESimpleItem;
import org.dxrgd.api.item.ItemManager;
import org.dxrgd.api.open.file.configuration.type.Yaml;
import org.dxrgd.api.open.plugin.OPlugin;
import org.dxrgd.api.open.registers.Listeners.Collector;
import org.dxrgd.api.rarity.RarityManager;
import org.dxrgd.api.rpg.attributes.AttrManager;
import org.dxrgd.ei.commands.EICommandHandler;
import org.dxrgd.ei.config.ConvertConfig;
import org.dxrgd.ei.config.GConfig;
import org.dxrgd.ei.config.LangConfig;
import org.dxrgd.ei.listener.ConversionListener;
import org.dxrgd.ei.listener.InventoryChangeListener;
import org.dxrgd.ei.listener.RepairMaterialsListener;
import org.dxrgd.ei.listener.ToolUsingListener;
import org.dxrgd.ei.test.BasicTest;

import lombok.Getter;

public class EI extends OPlugin {

	public static final String prefix = "EIT";

	@Getter private static LangConfig		lang			= null;
	@Getter private static GConfig			gconfig			= null;
	@Getter private static ConvertConfig	convertConfig	= null;
	@Getter private static Yaml 			guiConfig		= null;
	@Getter private static ActionManager	actionManager 	= null;
	private static EI						instance		= null;
	public static EI instance()	   { return instance; }

	public static List<String>	datas						= new ArrayList<>();
	public static List<String>	rangedWeapon				= new ArrayList<>();

	public static Map<Material,List<Integer>> predefined = new ConcurrentHashMap<>();

	public static boolean containsData(ItemStack stack) {
		return datas.contains(stack.getType().toString() + ":" + ItemStackUtil.getDamage(stack)) || datas.contains(stack.getType().toString());
	}

	public static boolean isRanged(Material mat) { return rangedWeapon.contains(mat.toString()); }

	@Override
	public int priority() { return 3; }

	@Override
	public void initialize() {
		instance = this;
		lang = new LangConfig(getDataFolder() + "/lang.yml", this);
		gconfig = new GConfig(getDataFolder() + "/config.yml", this);
		guiConfig = new Yaml(getDataFolder() + "/gui.yml", this);

		exportDefaults();
		final Metrics metrics = new Metrics(instance, 524);

		Collector.forPlugin(this).collect(new InventoryChangeListener(), new ToolUsingListener(), new ConversionListener(), new RepairMaterialsListener());

		actionManager = new ActionManager();
		actionManager.register();

		EAbility.init();
		RepairMaterial.init();
		RarityManager.init();
		ItemManager.preInit();

		BasicTest.test();

		new EICommandHandler().register();

		ItemManager.process();
		convertConfig  = new ConvertConfig(getDataFolder() + "/conversion.yml", this);
		Texts.fine("Currently " + RarityManager.rarities.size() + " rarities is registered", EI.prefix);
		Texts.fine("Currently " + AttrManager.getAttributes().size() + " attributes is registered", EI.prefix);
		Texts.fine("Currently " + ESimpleItem.getRegisteredID().size() + " items is registered", EI.prefix);

		metrics.addCustomChart(new SimplePie("itemsCreated", () -> {
			final int itemsCount = ESimpleItem.getRegisteredID().size();

			if (itemsCount > 1000 ) return ">1000";
			if (itemsCount > 750  ) return ">750";
			if (itemsCount > 500  ) return ">500";
			if (itemsCount > 250  ) return ">250";
			if (itemsCount > 100  ) return ">100";
			if (itemsCount > 50   ) return ">50";
			if (itemsCount > 25   ) return ">25";
			if (itemsCount > 10   ) return ">10";
			if (itemsCount > 1    ) return ">1";

			return "0/1";
		}));
	}

	@Override
	public void reloadPlugin() {
		AttrManager.reloadAttributes();
		//Collector.forPlugin(this).collect(new InventoryChangeListener(), new ToolUsingListener(), new ConversionListener(), new RepairMaterialsListener());

		ESimpleItem.unregisterAll();

		EI.getGconfig().reload();
		EI.getGuiConfig().reload();
		EI.getLang().reload();
		EI.getConvertConfig().reload();

		EI.instance().exportDefaults();

		ItemManager.preInit();

		actionManager = new ActionManager();
		actionManager.register();

		EAbility.init();
		RepairMaterial.init();
		RarityManager.init();
		ItemManager.preInit();

		ItemManager.process();

		EI.getConvertConfig().reload();

		Texts.fine("Currently " + RarityManager.rarities.size() + " rarities is registered", EI.prefix);
		Texts.fine("Currently " + EAbility.getRegistered().size() + " abilities is registered", EI.prefix);
		Texts.fine("Currently " + AttrManager.getAttributes().size() + " attributes is registered", EI.prefix);
		Texts.fine("Currently " + ESimpleItem.getRegisteredID().size() + " items is registered", EI.prefix);
		Texts.fine("Currently " + RepairMaterial.getMaterials().size() + " repair materials is registered", EI.prefix);
		Texts.fine("ElephantItems succesfully reloaded!", EI.prefix);
	}

	public final void exportDefaults() {
		final File itemsFolder = new File(getDataFolder() + "/items");
		final File rarityFolder = new File(getDataFolder() + "/rarity");
		final File abilityFolder = new File(getDataFolder() + "/abilities");

		Yaml.exportFile("defaults/exampleItemTemplate.yml", this, getDataFolder());

		Yaml.exportFile("defaults/allowedMaterials.txt", this, getDataFolder());

		if (!itemsFolder.exists()) {
			itemsFolder.mkdirs();

			Yaml.exportFile("defaults/example.yml", this, itemsFolder);
		}

		if (!rarityFolder.exists()) {
			rarityFolder.mkdirs();

			Yaml.exportFile("defaults/trash.yml", this, rarityFolder);
			Yaml.exportFile("defaults/common.yml", this, rarityFolder);
			Yaml.exportFile("defaults/uncommon.yml", this, rarityFolder);
			Yaml.exportFile("defaults/rare.yml", this, rarityFolder);
			Yaml.exportFile("defaults/epic.yml", this, rarityFolder);
			Yaml.exportFile("defaults/mythical.yml", this, rarityFolder);
			Yaml.exportFile("defaults/legendary.yml", this, rarityFolder);
		}

		if (!abilityFolder.exists()) {
			abilityFolder.mkdirs();

			Yaml.exportFile("defaults/ability-mining.yml", this, abilityFolder);
			Yaml.exportFile("defaults/ability-harvest.yml", this, abilityFolder);
		}
	}

}
