package org.dxrgd.api.ability.action;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.dxrgd.api.ability.BasicAction;

public class ActionManager {

	public final BasicAction MINING = new MiningAction();

	public final BasicAction HARVEST = new HarvestAction();

	private final Map<String, BasicAction> actions = new ConcurrentHashMap<>();

	public final Collection<BasicAction> getRegistered() { return actions.values(); }

	public BasicAction getAction(String key) {
		if (actions.containsKey(key))
			return actions.get(key).clone();
		else
			return null;
	}

	public final void register(BasicAction action) {
		if (action != null && !actions.containsKey(action.getKey()))
			actions.put(action.getKey().toUpperCase(), action);
	}

	public final void register() {
		if (!actions.isEmpty()) { return; }

		for (final Field f : ActionManager.class.getFields()) {
			if (!f.isAccessible())
				f.setAccessible(true);

			if (BasicAction.class.isAssignableFrom(f.getType()))
				try {
					register((BasicAction) f.get(this));
				} catch (final Exception e) { e.printStackTrace(); }
		}
	}

}
