package org.dxrgd.ei.commands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.dxrgd.api.bukkit.message.Texts;
import org.dxrgd.api.bukkit.utility.IStatus;
import org.dxrgd.api.bukkit.utility.InventoryUtil;
import org.dxrgd.api.bukkit.utility.ItemStackUtil;
import org.dxrgd.api.durability.material.RepairMaterial;
import org.dxrgd.api.item.ESimpleItem;
import org.dxrgd.api.open.command.OCommand;
import org.dxrgd.api.open.command.impl.CommandHandler;
import org.dxrgd.api.open.command.impl.CommandNode;
import org.dxrgd.api.open.value.util.NumUtil;
import org.dxrgd.ei.EI;

public class EICommandHandler extends CommandHandler {

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		final List<String> result = new ArrayList<>();
		if (command.getName().equalsIgnoreCase(handler.label())) {
			if (args.length == 1) {
				for (final OCommand cmd : getRegisteredCommands())
					if (cmd.label().startsWith(args[0]))
						if (cmd.testConditions(sender))
							result.add(cmd.label());
			}

			if (args.length == 2) {
				if (args[0].equalsIgnoreCase(list.label()))
					if (list.testConditions(sender))
						for (int i = 0 ; i < 1 ; i++)
							if (args[1].equalsIgnoreCase(String.valueOf(i)))
								result.add(String.valueOf(i));

				if (args[0].equalsIgnoreCase(hash.label()))
					if (hash.testConditions(sender))
						for (final Player p : Bukkit.getOnlinePlayers())
							if (p.getName().startsWith(args[1]))
								result.add(p.getName());

				if (args[0].equalsIgnoreCase(give.label()))
					if (give.testConditions(sender))
						for (final ESimpleItem item : ESimpleItem.getItems())
							if (item.getKey().startsWith(args[1]))
								result.add(item.getKey());
				if (args[0].equals(material.label()))
					if (material.testConditions(sender))
						for (final RepairMaterial mat : RepairMaterial.getMaterials())
							if (mat.getKey().startsWith(args[1]))
								result.add(mat.getKey());
			}

			if (args.length == 3)
				if (args[0].equalsIgnoreCase(give.label()))
					if (give.testConditions(sender))
						for (final Player p : Bukkit.getOnlinePlayers())
							if (p.getName().toLowerCase().startsWith(args[2].toLowerCase()))
								result.add(p.getName());
		}

		return result;
	}

	private static EIListCommand	list;
	private static EIGiveCommand	give;
	private static EIHashCommand	hash;
	private static EIMaterialCommand material;
	private static EICommandHandler	handler;

	private static CommandNode getGiveCommand() {
		return give == null ? give = new EIGiveCommand(handler, "give", (s, args) -> {

			if (s instanceof CommandSender) {
				if (args.length() <= 0)  return "&7You missed args: &e/eitems give &4<eitem-id> [player-name]";
			} else if (args.length() <= 0) return "&7You missed args: &e/eitems give &4<eitem-id> &2[player-name]";

			if (s instanceof ConsoleCommandSender)
				if (args.length() <= 1) return "&7You missed args: &e/eitems give <eitem-id> &4<player-name>";

			final ESimpleItem item = ESimpleItem.getById(args.arg(0).toLowerCase());

			if (item == null)
				return "&4Item '" + args.arg(0) + "' not found";

			Player player;
			if (args.length() >= 2) player = args.player(1);
			else player = (Player) s;

			if (player == null)  return "&4Player is not online!";

			InventoryUtil.addItem(item.toStack(), player);
			return null;

		}) : give;
	}

	private static CommandNode getListCommand() { // TODO Добавить JSON
		return list == null ? list = new EIListCommand(handler, "list", (s, args) -> {
			Texts.send(s, "»------>[&6ElephantItems&5&l]");
			final List<String> items = new ArrayList<>(ESimpleItem.getRegisteredID());
			int v = 1;
			if (args.length() >= 1 && NumUtil.isNumber(args.arg(0))) v = NumUtil.parseInteger(args.arg(0));

			try {
				for (int i = v * 10 - 10; i != v * 10 && i < items.size(); i++)
					Texts.send(s, "&6 • " + items.get(i));

			} catch (final ArrayIndexOutOfBoundsException e) {
				Texts.send(s, "&c»------>&5[&6ElephantItems&5&l]");
				return null;
			}
			Texts.send(s, "&c»------>&5[&6ElephantItems&5&l]");
			return null;
		}) : list;
	}

	private static CommandNode getHashCommand() {
		return hash == null ? hash = new EIHashCommand(handler, "hash", (s, args) -> {
			if (args.length() < 1) return "&7You missed args: &e/eitems hash &4<player> <itemhash>";

			if (args.length() < 2) return "&7You missed args: &e/eitems hash <player> &4<itemhash>";

			final Player player = args.player(0);

			if (player == null) return "&4Player is not online!";

			String pre = args.arg(1);

			if (args.length() > 2) for (int i = 2; i < args.length(); i++)
				pre = pre + " " + args.arg(i);

			final ItemStack stack = ItemStackUtil.toStack(pre);
			if (!ItemStackUtil.validate(stack, IStatus.HAS_MATERIAL)) return "&4Incorrect item hash! " + args.arg(1);

			InventoryUtil.addItem(stack, player);
			return null;
		}) : hash;
	}

	public EICommandHandler() {
		super(EI.instance(), "eitems");
		handler = this;
		attachReload();
		attachHelp();
		attachNode(getGiveCommand());
		attachNode(getListCommand());
		attachNode(getHashCommand());
		attachNode(new EIDebugCommand(this));
		attachNode(material = new EIMaterialCommand(this));
	}

}
