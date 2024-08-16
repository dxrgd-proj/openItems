package org.dxrgd.ei.commands;

import org.dxrgd.api.open.command.Executor;
import org.dxrgd.api.open.command.SenderType;
import org.dxrgd.api.open.command.impl.CommandHandler;
import org.dxrgd.api.open.command.impl.CommandNode;

public class EIHashCommand extends CommandNode {

	public EIHashCommand(CommandHandler handler, String label, Executor func) {
		super(handler, label, 2, func);
		senderType = SenderType.ALL;
	}

	@Override
	public String description() {
		return "Gives <itemHash> to <player> player";
	}

	@Override
	public String usage() {
		return "/eitems hash  <player-name> <itemHash>";
	}

	@Override
	public String permission() {
		return "ei.admin.hash";
	}

}
