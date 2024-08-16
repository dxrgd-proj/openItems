package org.dxrgd.ei.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.dxrgd.api.bukkit.utility.InventoryUtil;
import org.dxrgd.api.durability.material.RepairMaterial;
import org.dxrgd.api.open.command.impl.CommandHandler;
import org.dxrgd.api.open.command.impl.CommandNode;

public class EIMaterialCommand extends CommandNode {

	public EIMaterialCommand(CommandHandler handler) {
		super(handler, "mat", 1, (s,args) -> {
			if (s instanceof CommandSender) {
				if (args.length() <= 0)
					return "&7You missed args: &e/eitems mat &4<id> <player-name>";
			} else if (args.length() <= 0)
				return "&7You missed args: &e/eitems mat &4<id> &2[player-name]";

			if (s instanceof ConsoleCommandSender)
				if (args.length() <= 1) return "&7You missed args: &e/eitems mat <id> &4<player-name>";

			final RepairMaterial mat = RepairMaterial.getMaterial(args.arg(0));

			if (mat == null) return "&4Repair material '" + args.arg(0) + "' not found";

			Player player;
			if (args.length() >= 2) player = args.player(1);
			else player = (Player) s;

			if (player == null) return "&4Player " + args.arg(1) + " is not online!";

			InventoryUtil.addItem(mat.toStack(), player);
			return null;
		});
	}

	@Override
	public String description() {
		return "Gives repair material";
	}

	@Override
	public String usage() {
		return "/eitems mat <id> [player]";
	}

	@Override
	public String permission() {
		return "ei.commands.material";
	}

}
