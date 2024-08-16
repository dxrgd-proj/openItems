package org.dxrgd.api.ability.action.mining;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;

public class MiningBlockEvent extends BlockBreakEvent {

	public MiningBlockEvent(Block theBlock, Player player) {
		super(theBlock, player);
	}

}
