package org.aslstd.api.ability;

import javax.annotation.Nullable;

import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public enum AbilityType {
	LEFT_CLICK,
	LEFT_CLICK_BLOCK,
	RIGHT_CLICK,
	RIGHT_CLICK_BLOCK,
	SHIFT_CLICK,
	SHIFT_HOLD,
	PASSIVE,
	BLOCK_BREAK,
	PLAYER_RECEIVE_DAMAGE,
	ENTITY_KILL;

	public static @Nullable AbilityType from(String value) {
		if (value == null) return null;
		for (final AbilityType type : values())
			if (type.name().equalsIgnoreCase(value))
				return type;
		return null;
	}

	public static @Nullable AbilityType fromAction(Action a) {
		switch(a) {
		case LEFT_CLICK_AIR:
			return LEFT_CLICK;
		case LEFT_CLICK_BLOCK:
			return LEFT_CLICK_BLOCK;
		case RIGHT_CLICK_AIR:
			return RIGHT_CLICK;
		case RIGHT_CLICK_BLOCK:
			return RIGHT_CLICK_BLOCK;
		default:
			return null;
		}
	}

	public static @Nullable AbilityType fromEvent(Event e) {
		switch(e.getEventName()) {
		case "PlayerInteractEvent":
			return AbilityType.fromAction(((PlayerInteractEvent)e).getAction());
		case "BlockBreakEvent":
			return AbilityType.BLOCK_BREAK;
		default: return null;
		}
	}
}
