package org.aslstd.ei.commands;

import org.dxrgd.api.open.command.Executor;
import org.dxrgd.api.open.command.impl.CommandHandler;
import org.dxrgd.api.open.command.impl.CommandNode;

public class EIListCommand extends CommandNode {

	public EIListCommand(CommandHandler handler, String label, Executor func) {
		super(handler, label, 0, func);
	}

	@Override
	public String description() {
		return "Shows list of eitems";
	}

	@Override
	public String usage() {
		return "/eitems list [page]";
	}

	@Override
	public String permission() {
		return "ei.commands.stats";
	}

}
