package org.aslstd.api.ability.action.mining;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.dxrgd.api.bukkit.utility.Tags;

public class MiningLevel {

	private static final List<Material> basic = new ArrayList<>(Tags.MINEABLE_PICKAXE.values());

	static {
		basic.removeIf(m -> Tags.NEEDS_DIAMOND_TOOL.values().contains(m));
		basic.removeIf(m -> Tags.NEEDS_IRON_TOOL.values().contains(m));
		basic.removeIf(m -> Tags.NEEDS_STONE_TOOL.values().contains(m));
	}

	public static final MiningLevel LEVEL_0 = new MiningLevel(null);
	public static final MiningLevel LEVEL_1 = new MiningLevel(Tags.NEEDS_STONE_TOOL);
	public static final MiningLevel LEVEL_2 = new MiningLevel(Tags.NEEDS_IRON_TOOL);
	public static final MiningLevel LEVEL_3 = new MiningLevel(Tags.NEEDS_DIAMOND_TOOL);

	private List<Material> materials = new ArrayList<>();

	private MiningLevel(Tags<Material> toAdd) {
		materials.addAll(basic);

		if (toAdd == null) return;

		materials.addAll(toAdd.values());
	}


	public static List<Material> getByPickaxe(ItemStack stack) {

		switch(stack.getType()) {

			case NETHERITE_PICKAXE:
			case DIAMOND_PICKAXE:
				return LEVEL_3.materials;
			case IRON_PICKAXE:
				return LEVEL_2.materials;
			case STONE_PICKAXE:
			case GOLDEN_PICKAXE:
				return LEVEL_1.materials;
			case WOODEN_PICKAXE:
				return LEVEL_0.materials;
			default:
				return null;
		}

	}



}
