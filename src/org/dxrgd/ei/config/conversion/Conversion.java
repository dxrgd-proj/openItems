package org.dxrgd.ei.config.conversion;

import java.util.List;

import org.dxrgd.api.open.value.Pair;
import org.dxrgd.api.open.value.util.NumUtil;

import lombok.Getter;

public class Conversion {

	@Getter private String trigger_displayName;
	//@Getter private String trigger_lore;
	//@Getter private Enchantment convertFrom;
	//@Getter private List<Pair<String,String>> convertTo;
	@Getter private List<Pair<String, Double>> itemIDs;
	//@Getter private boolean convertDisplayName = true;

	public Conversion(String trigger_displayName, List<Pair<String,Double>> itemIDs) {
		this.trigger_displayName = trigger_displayName;
		this.itemIDs = itemIDs;
	}

	public String roll() {

		String result = null;
		for (final Pair<String,Double> entry : itemIDs) {
			if (NumUtil.isTrue(entry.getSecond()*100, 10000)) {
				result = entry.getFirst();
				break;
			}
		}

		return result == null ? itemIDs.get(itemIDs.size()-1).getFirst() : result;
	}

}
