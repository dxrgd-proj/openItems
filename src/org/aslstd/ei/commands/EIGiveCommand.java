package org.aslstd.ei.commands;

import org.dxrgd.api.open.command.Executor;
import org.dxrgd.api.open.command.SenderType;
import org.dxrgd.api.open.command.impl.CommandHandler;
import org.dxrgd.api.open.command.impl.CommandNode;

public class EIGiveCommand extends CommandNode {

	public EIGiveCommand(CommandHandler handler, String label, Executor func) {
		super(handler, label, 1, func);
		senderType = SenderType.ALL;
	}

	@Override
	public String description() {
		if (senderType == SenderType.CONSOLE_ONLY)
			return "Gives <eitem-id> to <player-name> player";
		else
			return "Gives <eitem-id> to you or to [player-name] player";
	}

	@Override
	public String usage() {
		if (senderType == SenderType.CONSOLE_ONLY)
			return "/eitems give <eitem-id> <player-name>";
		else
			return "/eitems give <eitem-id> [player-name]";
	}

	@Override
	public String permission() {
		return "ei.admin.give";
	}

}
