package org.dxrgd.api.item;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.dxrgd.api.CustomParams;
import org.dxrgd.api.ability.EAbility;
import org.dxrgd.api.bukkit.message.Texts;
import org.dxrgd.api.bukkit.setting.impl.FileSettings;
import org.dxrgd.api.bukkit.utility.BasicMetaAdapter;
import org.dxrgd.api.bukkit.utility.IStatus;
import org.dxrgd.api.bukkit.utility.ItemStackUtil;
import org.dxrgd.api.durability.DManager;
import org.dxrgd.api.open.file.configuration.type.Yaml;
import org.dxrgd.api.open.value.ModifierType;
import org.dxrgd.api.open.value.Pair;
import org.dxrgd.api.open.value.Value;
import org.dxrgd.api.open.value.random.RandomBool;
import org.dxrgd.api.open.value.random.RandomVal;
import org.dxrgd.api.open.value.util.MathUtil;
import org.dxrgd.api.open.value.util.NumUtil;
import org.dxrgd.api.open.value.util.ValueParser;
import org.dxrgd.api.rarity.ERarity;
import org.dxrgd.api.rarity.RandomRarity;
import org.dxrgd.api.rarity.RarityManager;
import org.dxrgd.api.rpg.attributes.AttrBase;
import org.dxrgd.api.rpg.attributes.AttrManager;
import org.dxrgd.api.rpg.attributes.AttrType;
import org.dxrgd.ei.EI;

import de.tr7zw.changeme.nbtapi.NBTItem;
import lombok.Getter;
import net.kyori.adventure.text.Component;

public final class ESimpleItem extends ItemStack {

	public static ConcurrentHashMap<String, ESimpleItem> registered = new ConcurrentHashMap<>();

	public static void unregisterAll() { registered.clear(); }
	public static Set<String> getRegisteredID() { return ESimpleItem.registered.keySet(); }
	public static Collection<ESimpleItem> getItems() { return registered.values(); }

	public static ESimpleItem getById(String id) {
		id = id.toLowerCase();
		if (ESimpleItem.registered.containsKey(id))
			return ESimpleItem.registered.get(id);
		return null;
	}

	public static boolean isEItem(ItemStack stack) {
		if (!ItemStackUtil.validate(stack, IStatus.HAS_LORE)) return false;
		List<Component> lore;
		if ((lore = stack.getItemMeta().lore()) == null) return false;
		for (final AttrBase stat : AttrManager.getAttributes())
			if (BasicMetaAdapter.contains(lore, AttrBase.getRegexPattern(stat))) return true;
		return false;
	}

	@Getter protected final FileSettings 	settings;
	@Getter private final ConcurrentMap<String,RandomVal> random;
	@Getter private RandomRarity 			randomRarity;

	@Getter public final String 			key;
	@Getter private final Yaml				file;
	protected ItemMeta						meta;
	public boolean 							isUnbreakable, isRepairable;
	public boolean[] 						hasAttr;

	@Getter private ItemType 				itemType = ItemType.ONE_HANDED;
	private int 							level = 0;
	protected ERarity rarity;

	protected List<String>	lore;
	protected List<String>	attributes;
	protected List<String>	desc;
	@Getter protected List<EAbility> abilities;

	public void build() {
		settings.importYaml(file, key);

		isUnbreakable = Boolean.parseBoolean(settings.getValue("is-unbreakable-flag"));

		if (isUnbreakable)
			ItemStackUtil.setUnbreakable(this, isUnbreakable);

		meta = getItemMeta();

		setMaterial(settings.getValue("material"))
		.setData(settings.getValue("data"))
		.setDisplayName(settings.getValue("display.display-name"))
		.preInitRandom()
		.setEnchantments(settings.exportArray("enchantments"))
		.setHideFlags(settings.exportArray("item-flags"))
		.initLevel(settings.getValue("level"))
		.setAttributes(settings.getKey("attributes"))
		.setRandomAttributes()
		.setAbilities(settings.exportArray("abilities"))
		.processDescription();

		setItemMeta(meta);
	}

	public ESimpleItem randomize() {
		initLevel(settings.getValue("level"))
		.setAttributes(settings.getKey("attributes"))
		.setRandomAttributes()
		.processDescription();

		return this;
	}

	public ESimpleItem(Yaml util, String section) {
		super(new ItemStack(Material.matchMaterial("IRON_SWORD")));
		settings = new FileSettings();
		file = util;
		key = section;
		desc = new ArrayList<>();
		attributes = new ArrayList<>();
		lore = new ArrayList<>();
		abilities = new ArrayList<>();
		random = new ConcurrentHashMap<>();
		hasAttr = new boolean[AttrManager.getAttributes().size()];

		if (getById(section.toLowerCase()) != null) {
			Texts.warn("ITEM WITH ID &a" + section + " NOW EXISTS", EI.prefix);
		}
	}

	public ESimpleItem setRandomAttributes() {
		for (final Entry<String,RandomVal> entry : random.entrySet()) {
			final AttrBase stat = AttrManager.get(entry.getKey());
			if (stat == null || hasAttr[stat.uniquePosition()]) continue;

			String[] split = new String[] { "" };
			final Value val = entry.getValue().roll(level);
			if (val == null) continue;
			String value = val.getValue();
			String dot = "+";

			if (value.startsWith("-") || value.startsWith("+")) {
				dot = value.substring(0, 1);
				value = value.substring(1);
			}

			final boolean percent = NumUtil.isPercent(value);
			boolean single = stat.type() == AttrType.RANGE ? false : true;
			value = value.replace("%", "");
			split = value.split("-");

			try {
				NumUtil.parseDouble(split[0]);
				if (split.length >= 2)
					NumUtil.parseDouble(split[1]);
				else
					single = true;
			} catch (final NumberFormatException e) {
				Texts.warn("&4STAT IS BROKEN :&e" + getKey() + ":" + entry.getKey() + "&4, SKIPPED", EI.prefix);
				continue;
			}

			double fResultValue = NumUtil.parseDouble(split[0]);

			if (single)
				attributes.add( Texts.c( stat.convertToLore( dot, Texts.df.format(fResultValue), percent ? "%" : "" ) ) );
			else {
				split = value.split("-");
				fResultValue = NumUtil.parseDouble(split[0]);
				final double sResultValue = NumUtil.parseDouble(split[1]);
				attributes.add( Texts.c( stat.convertToLore( dot, Texts.df.format(fResultValue), "-", Texts.df.format(sResultValue), percent ? "%" : "" ) ) );
			}
		}

		return this;
	}

	public ESimpleItem setAttributes(List<Entry<String,String>> attrSet) {
		if (attrSet == null || attrSet.isEmpty()) return this;
		attributes.clear();

		for (final Entry<String,String> key: attrSet) {
			final AttrBase stat = AttrManager.get(key.getKey().substring(11));
			if (stat == null) continue;

			String[] split = new String[] { "" };
			String value = key.getValue();
			String dot = "+";

			if (value.startsWith("-") || value.startsWith("+")) {
				dot = value.substring(0, 1);
				value = value.substring(1);
			}

			final boolean percent = NumUtil.isPercent(value);
			boolean single = stat.type() != AttrType.RANGE;
			value = value.replace("%", "");
			split = value.split("-");

			try {
				NumUtil.parseDouble(split[0]);
				if (split.length >= 2)
					NumUtil.parseDouble(split[1]);
				else
					single = true;
			} catch (final NumberFormatException e) {
				Texts.warn("&4ATTRIBUTE IS BROKEN :&e" + getKey() + ":" + key.getKey().substring(6) + "&4, SKIPPED", EI.prefix);
				continue;
			}

			double fResultValue = NumUtil.parseDouble(split[0]);

			if (random.containsKey(stat.getKey())) {

				final Value val = random.get(stat.getKey()).roll(level);

				if (val != null) {
					val.setValue(val.getValue().replaceAll("%", ""));
					if (val.getType() == ModifierType.POSITIVE_PERCENTS || val.getType() == ModifierType.NEGATIVE_PERCENTS) {

						final double percents = NumUtil.parseDouble(val.getValue());

						if (single) {
							fResultValue = MathUtil.incrementByPercents(fResultValue, percents);
						} else
							value = MathUtil.incrementRangeByPercents((dot.equalsIgnoreCase("-") ? "-" : "") + value, NumUtil.parseDouble(val.getValue().split("-")[0]));
					} else {
						if (single) {
							if (dot.equalsIgnoreCase("-"))
								fResultValue *= -1;

							fResultValue += NumUtil.parseDouble(val.getValue());

							if (dot.equalsIgnoreCase("-")) {
								if (fResultValue > 0)
									dot = "+";
								else
									fResultValue *= -1;
							}
						} else
							value = MathUtil.incrementRangeValue((dot.equalsIgnoreCase("-") ? "-" : "") + value, val.getValue());
					}
				}

			}

			hasAttr[stat.uniquePosition()] = true;
			if (single)
				attributes.add( Texts.c( stat.convertToLore( dot, Texts.df.format(fResultValue), percent ? "%" : "" ) ) );
			else {
				split = value.split("-");
				fResultValue = NumUtil.parseDouble(split[0]);
				final double sResultValue = NumUtil.parseDouble(split[1]);
				attributes.add( Texts.c( stat.convertToLore( dot, Texts.df.format(fResultValue), "-", Texts.df.format(sResultValue), percent ? "%" : "" ) ) );
			}
		}

		return this;
	}


	public ESimpleItem preInitRandom() {
		for (final Entry<String, String> str : getSettings().getKeys() ) {
			if (str.getKey().equalsIgnoreCase("level")) {
				if (str.getValue().startsWith("from")) {
					final RandomVal val = ValueParser.getSingleValue(str.getValue());
					if (val == null) {
						Texts.warn("Some trouble happens with item: " + getKey() + " check level line.");
						continue;
					}

					random.put("level", val);
				}
				continue;
			}

			if (str.getKey().equalsIgnoreCase("repairable")) {
				if (str.getValue().equalsIgnoreCase("random")){
					random.put("repairable", new RandomBool());
				} else
					isRepairable = Boolean.parseBoolean(settings.getValue("repairable"));
				continue;
			}

			if (str.getKey().startsWith("random-rarity")) {
				if (!getSettings().hasKey("default-rarity")) {
					Texts.warn("You can't add 'random-rarity' without 'default-rarity'! Rarity initialisation skipped for item: " + key);
					continue;
				}

				final List<Pair<ERarity, Double>> list = new ArrayList<>();
				for (final Entry<String, String> rar : getSettings().getKey("random-rarity")) { //14
					if (rar.getKey().contains("chance")) continue;
					final ERarity rarity = RarityManager.getById(rar.getValue().substring(14));
					if (rarity == null) {
						Texts.warn("Unknown rarity type: " + rar, "EI");
						continue;
					}
					list.add( new Pair<>(rarity, NumUtil.parseDouble(getSettings().getValue(rar.getKey() + ".chance")) ));
				}
				list.add(new Pair<>(RarityManager.getById(getSettings().getValue("default-rarity")), 100d));
				list.sort(Comparator.comparingDouble(Pair::getSecond));
				randomRarity = new RandomRarity(list);

				continue;
			}

			if (str.getKey().equalsIgnoreCase("max-durability")) {
				if (str.getValue().startsWith("from")) {
					final RandomVal val = ValueParser.getSingleValue(str.getValue());
					if (val == null) {
						Texts.warn("Some trouble happens with item: " + getKey() + " check max durability line.");
						continue;
					}
					random.put("max-durability", ValueParser.getSingleValue(str.getValue()));
				}

				continue;
			}

			if (str.getKey().startsWith("random-attributes")) {
				for (final Entry<String, String> attr : getSettings().getKey("random-attributes")) {
					if (AttrManager.get(attr.getKey().substring(18)) == null) continue;

					final RandomVal val = ValueParser.getRandomValue(attr.getValue());

					if (val == null) {
						Texts.warn("Some trouble happens with item: " + getKey() + " check attribute:  " + attr.getKey().substring(18).toUpperCase() + " line.");
						continue;
					}

					random.put(attr.getKey().substring(18).toUpperCase(), ValueParser.getRandomValue(attr.getValue()));
				}

				continue;
			}
		}

		return this;
	}

	public void processDescription() {
		final List<String> description = new ArrayList<>();

		desc = settings.exportArray("display.description");
		setLevel();
		setRarity(settings.getValue("default-rarity"));
		setItemType(ItemType.getByKey(settings.getValue("type")));
		setDurability(settings.getValue("max-durability"));
		lore = settings.exportArray("display.lore");

		if (!desc.isEmpty())
			if (EI.getLang().HEADER_DESC != null && !EI.getLang().HEADER_DESC.equalsIgnoreCase("")) {
				description.add(Texts.c(EI.getLang().HEADER_DESC));

				for (final String s : desc)
					description.add(Texts.c(s));
			}

		if (!attributes.isEmpty())
			if (EI.getLang().HEADER_ATTR != null && !EI.getLang().HEADER_ATTR.equalsIgnoreCase("")) {
				description.add(Texts.c(EI.getLang().HEADER_ATTR));

				for (final String s : attributes)
					description.add(Texts.c(s));
			}

		boolean loreEmpty = lore.isEmpty();

		for (final EAbility ability : abilities)
			if (ability.getToLore() != null && !ability.getToLore().isEmpty()) {
				loreEmpty = false;
				break;
			}

		if (!loreEmpty)
			if (EI.getLang().HEADER_LORE != null && !EI.getLang().HEADER_LORE.equalsIgnoreCase("")) {
				description.add(Texts.c(EI.getLang().HEADER_LORE));


				for (final EAbility ability : abilities)
					if (ability.getToLore() != null && !ability.getToLore().isEmpty())
						description.addAll(ability.getToLore());

				if (!lore.isEmpty())
					for (final String s : lore)
						description.add(Texts.c(s));
			}

		meta.setLore(description);
		setItemMeta(meta);
	}

	public ESimpleItem setData(String data) {
		if (data == null) return this;
		if (NumUtil.isNumber(data));
		int d = NumUtil.parseInteger(data);
		if (d < 0) d = 0;

		meta.setCustomModelData(d);

		setItemMeta(meta);

		return this;
	}

	public ESimpleItem setDisplayName(String displayName) {
		if (displayName == null) return this;

		meta.setDisplayName(Texts.c(displayName));

		setItemMeta(meta);
		return this;
	}

	public ESimpleItem setDurability(String maxDurability) {
		if (maxDurability == null) return this;

		if (random.containsKey("max-durability"))
			maxDurability = random.get("max-durability").roll(0).getValue();

		if (random.containsKey("repairable")) {
			isRepairable = MathUtil.randomBoolean();
		}

		if (!NumUtil.isNumber(maxDurability)) return this;

		final int c = NumUtil.parseDouble(maxDurability).intValue();

		if (c < 1) return this;

		if (isRepairable) desc.add(Texts.c( DManager.getDurabilityLore( String.valueOf(c), String.valueOf(c) ) ));
		else desc.add(Texts.c(DManager.getDurabilityLore(String.valueOf(c))));

		return this;
	}

	public ESimpleItem setEnchantments(List<String> ench) {
		if (ench == null) return this;

		for (final String enchantment : ench) {
			final String[] params = enchantment.split(":");
			final Enchantment e = Enchantment.getByKey(NamespacedKey.minecraft(params[0]));
			if (e == null)
				continue;
			if (NumUtil.isNumber(params[1]))
				meta.addEnchant(e, Integer.parseInt(params[1]), true);
		}

		return this;
	}

	public ESimpleItem setHideFlags(List<String> flags) {
		if (flags == null) return this;
		for (final String flag : flags) {
			final ItemFlag f = ItemStackUtil.getFlagByName(flag);
			if (f == null)
				continue;
			else
				meta.addItemFlags(f);
		}
		return this;
	}

	public void changeAttr(final AttrBase attr, String value) {
		final double[] values = new double[2] ;
		final boolean isPercents = value.contains("%");
		value = value.replaceAll("[%]?", "");

		if (attr.type() == AttrType.RANGE) {
			final String[] split = value.split("-");

			values[0] = NumUtil.parseDouble(split[0].replaceAll("[\\+\\-%]?", ""));

			if (split.length > 1) {
				values[1] = NumUtil.parseDouble(split[1].replaceAll("[%]?", ""));
			}
		}

		if (attr.type() == AttrType.RANGE && values.length > 1 && values[1] != 0 && values[1] >= values[0])
			settings.setValue("attributes." + attr.toString().toLowerCase(), Texts.df.format(values[0]) + "-" + Texts.df.format(values[1]) + (isPercents ? "%" : ""));
		else
			settings.setValue("attributes." + attr.toString().toLowerCase(), Texts.df.format(NumUtil.parseDouble(value)) + (isPercents ? "%" : ""));

		setAttributes(settings.getKey("attributes"));
		processDescription();
	}

	public void removeAttr(final AttrBase stat, boolean random) {
		if (random) {
			if (settings.hasKey("random-attributes." + stat.toString().toLowerCase())) {
				settings.remove("random-attributes." + stat.toString().toLowerCase());
				getFile().set(getKey() + ".random-attributes." + stat.toString().toLowerCase(), null);
			}
		} else
			if (settings.hasKey("attributes." + stat.toString().toLowerCase())) {
				settings.remove("attributes." + stat.toString().toLowerCase());
				getFile().set(getKey() + ".attributes." + stat.toString().toLowerCase(), null);
			}

		setAttributes(settings.getKey("attributes"));
		processDescription();
	}

	public ESimpleItem initLevel(String level) {
		if (level != null) {
			if (random.containsKey("level"))
				level = random.get("level").roll(0).getValue();

			this.level = NumUtil.parseDouble(level).intValue();
		}

		return this;
	}

	public ESimpleItem setLevel() {
		try {
			if (level > 0)
				desc.add(CustomParams.LEVEL.convert(String.valueOf( level )));
		} catch (final IllegalArgumentException e) {
			e.printStackTrace();
			return this;
		}

		return this;
	}

	public ESimpleItem setMaterial(String material) {
		if (material == null) {
			Texts.warn("&4Incorrect Material for key:&a'" + key + "' &4in file:&a'" + file.getFile().getName() + "'", EI.prefix);
			return this;
		}

		final Material mat = Material.matchMaterial(material);
		if (mat == null) return this;
		setType(mat);
		return this;
	}

	public ESimpleItem setItemType(ItemType type) {
		if (type == null) return this;
		else itemType = type;

		desc.add(CustomParams.TYPE.convert(itemType.getVisualName()));

		return this;
	}


	public ESimpleItem setRarity(String rar) {
		if (rar == null) return this;
		ERarity rarity;

		if (randomRarity != null)
			rarity = randomRarity.roll();
		else
			rarity = RarityManager.getById(rar.toLowerCase());

		if (rarity != null)
			desc.add(CustomParams.RARITY.convert(rarity.getVisualName()));

		this.rarity = rarity;

		return this;
	}

	public ESimpleItem setAbilities(List<String> abil) {
		if (abil == null || abil.isEmpty()) return this;

		final List<EAbility> abilities = new ArrayList<>();

		for (final String key : abil) {
			final EAbility a = EAbility.getAbility(key);
			if (a != null)
				abilities.add(a);
		}

		this.abilities = abilities;

		return this;
	}


	public ItemStack toStack() {
		final NBTItem item = new NBTItem(randomize());

		item.setString ("ei-id", 			key				);
		item.setString ("ei-rarity", 		rarity.getKey()	);
		item.setInteger("ei-level", 		level			);

		return item.getItem();
	}

	public void export() {
		settings.exportYaml(getFile(), getKey());
	}

}
