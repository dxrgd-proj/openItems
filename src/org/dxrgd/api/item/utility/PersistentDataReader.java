package org.dxrgd.api.item.utility;

import java.util.List;

import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataHolder;
import org.bukkit.persistence.PersistentDataType;
import org.dxrgd.api.item.meta.persistent.MetaPersistentDataType;

public class PersistentDataReader {

	public static PersistentDataReader of(PersistentDataHolder holder) {
		return new PersistentDataReader(holder);
	}

	private PersistentDataHolder holder;

	private PersistentDataReader(PersistentDataHolder holder) { this.holder = holder; }

	public boolean hasContainer(NamespacedKey key) {
		return holder.getPersistentDataContainer().has(key, PersistentDataType.TAG_CONTAINER);
	}

	public PersistentDataContainer container(NamespacedKey key) {
		return holder.getPersistentDataContainer().get(key, PersistentDataType.TAG_CONTAINER);
	}

	public PersistentDataReader container(NamespacedKey key, PersistentDataContainer value) {
		holder.getPersistentDataContainer().set(key, PersistentDataType.TAG_CONTAINER, value);
		return this;
	}

	public boolean hasString(NamespacedKey key) {
		return holder.getPersistentDataContainer().has(key, PersistentDataType.STRING);
	}

	public String string(NamespacedKey key) {
		return holder.getPersistentDataContainer().get(key, PersistentDataType.STRING);
	}

	public PersistentDataReader string(NamespacedKey key, String value) {
		holder.getPersistentDataContainer().set(key, PersistentDataType.STRING, value);
		return this;
	}

	public boolean hasStringList(NamespacedKey key) {
		return holder.getPersistentDataContainer().has(key, MetaPersistentDataType.STRING_LIST);
	}

	public List<String> stringList(NamespacedKey key) {
		return holder.getPersistentDataContainer().get(key, MetaPersistentDataType.STRING_LIST);
	}

	public PersistentDataReader stringList(NamespacedKey key, List<String> value) {
		holder.getPersistentDataContainer().set(key, MetaPersistentDataType.STRING_LIST, value);
		return this;
	}

	public boolean hasInteger(NamespacedKey key) {
		return holder.getPersistentDataContainer().has(key, PersistentDataType.INTEGER);
	}

	public int integer(NamespacedKey key) {
		return holder.getPersistentDataContainer().get(key, PersistentDataType.INTEGER);
	}

	public PersistentDataReader integer(NamespacedKey key, int value) {
		holder.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, value);
		return this;
	}

	public boolean hasDecimal(NamespacedKey key) {
		return holder.getPersistentDataContainer().has(key, PersistentDataType.DOUBLE);
	}

	public double decimal(NamespacedKey key) {
		return holder.getPersistentDataContainer().get(key, PersistentDataType.DOUBLE);
	}

	public PersistentDataReader decimal(NamespacedKey key, double value) {
		holder.getPersistentDataContainer().set(key, PersistentDataType.DOUBLE, value);
		return this;
	}

	public boolean hasBool(NamespacedKey key) {
		return holder.getPersistentDataContainer().has(key, MetaPersistentDataType.BOOLEAN);
	}

	public boolean bool(NamespacedKey key) {
		return holder.getPersistentDataContainer().get(key, MetaPersistentDataType.BOOLEAN);
	}

	public PersistentDataReader bool(NamespacedKey key, boolean value) {
		holder.getPersistentDataContainer().set(key, MetaPersistentDataType.BOOLEAN, value);
		return this;
	}

}
