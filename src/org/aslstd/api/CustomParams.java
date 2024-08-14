package org.aslstd.api;

import org.aslstd.api.item.ItemType;
import org.aslstd.api.rarity.RarityManager;
import org.dxrgd.api.open.value.CustomParam;
import org.dxrgd.api.open.value.util.NumUtil;

public class CustomParams {

	public static final CustomParam LEVEL 	= new CustomParam("level") {

		@Override
		public boolean isAllowedValue(String value) {
			if (NumUtil.isNumber(value))
				return true;
			else throw new IllegalArgumentException("You must set the integer value for level!");
		}

	};

	public static final CustomParam RARITY 	= new CustomParam("rarity") {

		@Override
		public boolean isAllowedValue(String value) {
			if (RarityManager.getById(value) != null)
				return true;
			else throw new IllegalArgumentException(
			"You must use one of the key listed in your plugins/ElephantItems/rarity folder, you can't set non-existed value. "
			+ "If u want to decorate Item with Rarity, set it in description");
		}

	};

	public static final CustomParam TYPE 	= new CustomParam("type") {

		@Override
		public boolean isAllowedValue(String value) {
			if (ItemType.getByKey(value) != null)
				return true;
			else throw new IllegalArgumentException("You must use one of parameters listed in ItemType! You try to use [ Type:" + value + " ]");
		}

	};

	public static final CustomParam SOCKET = new CustomParam("socket") {

		@Override
		public boolean isAllowedValue(String value) {
			return false;
		}

	};

}
