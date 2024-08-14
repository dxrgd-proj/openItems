package org.aslstd.api.durability;

import org.aslstd.api.durability.material.RepairMaterial;
import org.bukkit.inventory.ItemStack;
import org.dxrgd.api.open.value.Pair;
import org.dxrgd.api.open.value.util.MathUtil;
import org.dxrgd.api.open.value.util.NumUtil;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RepairParams {

	@NonNull private Pair<ItemStack,ItemStack> ingr;

	@Getter private ItemStack result;

	@Getter private RepairMaterial material;

	public void processRepair() {
		if (!checkConditions()) return;

		String[] durability;

		durability = DManager.getDurabilityString(ingr.getFirst()).split("/");

		if (durability.length < 2 && durability[1].equalsIgnoreCase("0")) return;

		if (!NumUtil.isNumber(durability[0]) || !NumUtil.isNumber(durability[1])) return;

		final int curr = NumUtil.parseInteger(durability[0]);
		int repair = 0;
		final int max = NumUtil.parseInteger(durability[1]);

		if (repair >= max) return;

		if (material.getRepairValue().getType().isPercent())
			repair = (int) MathUtil.getPercentsOfValue(NumUtil.parseDouble(material.getRepairValue().getValue()), max);
		else
			repair = NumUtil.parseDouble(material.getRepairValue().getValue()).intValue();

		if (curr + repair >= max)
			repair = max-curr;

		result = DManager.changeDurability(ingr.getFirst().clone(), repair, max);
	}

	public boolean checkConditions() {
		if (ingr.getFirst() == null || ingr.getSecond() == null) return false;
		if (!DManager.isRepairable(ingr.getFirst())) return false;
		material = RepairMaterial.isMaterial(ingr.getSecond());
		if (material == null) return false;

		return true;
	}


}
