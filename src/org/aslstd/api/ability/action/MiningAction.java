package org.aslstd.api.ability.action;

import java.util.ArrayList;
import java.util.List;

import org.aslstd.api.ability.BasicAction;
import org.aslstd.api.ability.action.mining.MiningBlockEvent;
import org.aslstd.api.ability.action.mining.MiningLevel;
import org.aslstd.ei.EI;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.dxrgd.api.bukkit.message.Texts;
import org.dxrgd.api.bukkit.setting.impl.FileSettings;
import org.dxrgd.api.bukkit.utility.BlockUtil;
import org.dxrgd.api.open.value.util.NumUtil;

public class MiningAction extends BasicAction {

	private int radius;
	private List<Material> blockedBlocks = new ArrayList<>();
	private boolean blockedAsWhiteList = false;
	private boolean throwPerBlock = false;

	public MiningAction() {
		super("MINING");
		radius = 0;
		setFunc(e -> func(this, (BlockBreakEvent) e));
	}

	@Override
	public void acceptSettings(FileSettings settings) {
		if (!settings.hasKey("settings.radius")) { Texts.warn("Mining action has incorrect settings"); return; }

		radius = NumUtil.parseInteger(settings.getValue("settings.radius"));

		if (settings.hasKey("settings.blocked-as-whitelist"))
			blockedAsWhiteList = Boolean.parseBoolean(settings.getValue("settings.blocked-as-whitelist"));
		if (settings.hasKey("settings.block-break-event-per-block"))
			throwPerBlock = Boolean.parseBoolean(settings.getValue("settings.block-break-event-per-block"));

		for (final String param : settings.exportArray("settings.blocked-blocks")) {
			final Material mat = Material.matchMaterial(param);

			if (mat == null)
				Texts.warn("Incorrect material in blocked-blocks-list provided: " + param);
			else
				blockedBlocks.add(mat);
		}
	}

	private static void func(MiningAction a, BlockBreakEvent e) {
		if (e.getPlayer().getGameMode() == GameMode.CREATIVE) return;
		final List<Material> toBreak = MiningLevel.getByPickaxe(e.getPlayer().getInventory().getItemInMainHand());
		if (toBreak == null) return;
		final List<Block> list = BlockUtil.getBlocksCuboid(e.getBlock(), a.radius);
		list.remove(e.getBlock());
		list.removeIf(b -> !toBreak.contains(b.getType()) );

		if (!toBreak.contains(e.getBlock().getType())) return;

		final ItemStack cloned = e.getPlayer().getInventory().getItemInMainHand().clone();

		for (final Block b : list) {
			if (!a.blockedAsWhiteList) {
				if (a.blockedBlocks.contains(b.getType())) continue;
				if (!toBreak.contains(b.getType())) continue;
			} else
				if (!a.blockedBlocks.contains(b.getType())) continue;

			if (a.throwPerBlock) {
				final MiningBlockEvent event = new MiningBlockEvent(b, e.getPlayer());

				Bukkit.getPluginManager().callEvent(event);
				if (event.isCancelled()) continue;
			}

			Bukkit.getScheduler().scheduleSyncDelayedTask(EI.instance(), () -> b.breakNaturally(cloned));

		}
	}

	@Override
	public boolean requiresThisEvent(Event e) {
		return e instanceof BlockBreakEvent;
	}

	@Override
	public BasicAction clone() {
		return new MiningAction();
	}

}
