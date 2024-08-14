package org.aslstd.ei.commands;

import org.aslstd.api.durability.DManager;
import org.bukkit.entity.Player;
import org.dxrgd.api.bukkit.message.Texts;
import org.dxrgd.api.open.command.SenderType;
import org.dxrgd.api.open.command.impl.CommandHandler;
import org.dxrgd.api.open.command.impl.CommandNode;
import org.dxrgd.api.rpg.equipment.EquipType;

public class EIDebugCommand extends CommandNode {

	public EIDebugCommand(CommandHandler handler) {
		super(handler, "debug", 1, (s, args) -> {
			if (args.length() <= 0)
				return "Incorrect argument: /eitems debug <debugType>";

			final Player p = (Player) s;

			switch (args.arg(0).toLowerCase()) {
				case "durability":
					Texts.send(p, DManager.getDurabilityString(p, EquipType.HAND));
					break;
			}
			return null;
		});
		senderType = SenderType.PLAYER_ONLY;
	}

}
