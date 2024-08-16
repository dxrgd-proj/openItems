package org.dxrgd.ei.listener;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.inventory.PrepareSmithingEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.SmithingInventory;
import org.dxrgd.api.bukkit.utility.IStatus;
import org.dxrgd.api.bukkit.utility.InventoryUtil;
import org.dxrgd.api.bukkit.utility.ItemStackUtil;
import org.dxrgd.api.open.plugin.listener.BukkitListener;
import org.dxrgd.api.open.plugin.listener.Named;
import org.dxrgd.ei.EI;
import org.dxrgd.ei.config.ConvertConfig;

@Named(key = "conversion")
public class ConversionListener implements BukkitListener {

	private static final Map<UUID, ItemStack> invisibleItem = new ConcurrentHashMap<>();

	@EventHandler
	public void onCraftPrepare(PrepareItemCraftEvent e) {
		if (!ItemStackUtil.validate(e.getInventory().getResult(), IStatus.HAS_MATERIAL)) return;

		final ItemStack result = e.getInventory().getResult();

		if (result == null) return;

		if (!ConvertConfig.convert.containsKey(result.getType())) return;

		final ItemStack conv = ConvertConfig.attemptConversion(result, true);
		invisibleItem.put(e.getView().getPlayer().getUniqueId(), e.getInventory().getResult());
		e.getInventory().setResult(conv);
	}

	@EventHandler
	public void onSmithingPrepare(PrepareSmithingEvent e) {
		if (!ItemStackUtil.validate(e.getInventory().getResult(), IStatus.HAS_MATERIAL)) return;

		final SmithingInventory inv = e.getInventory();

		final ItemStack result = inv.getResult();

		if (result == null) return;

		if (!ConvertConfig.convert.containsKey(result.getType())) return;

		final ItemStack conv = ConvertConfig.attemptConversion(result, true);
		invisibleItem.put(e.getView().getPlayer().getUniqueId(), inv.getResult());
		Bukkit.getScheduler().scheduleSyncDelayedTask(EI.instance(), () -> { inv.setResult(conv); inv.getViewers().forEach(h -> { ((Player)h).updateInventory(); } ); });
	}

	@EventHandler
	public void onItemPickup(EntityPickupItemEvent e) {
		if (e.getEntity().getType() != EntityType.PLAYER) return;

		final ItemStack item = e.getItem().getItemStack();

		if (!ItemStackUtil.validate(item, IStatus.HAS_MATERIAL)) return;

		if (!ConvertConfig.convert.containsKey(item.getType())) return;

		final ItemStack converted = ConvertConfig.attemptConversion(item, false);

		if (!ItemStackUtil.isEquals(item, converted)) {
			e.getItem().remove();
			e.setCancelled(true);
			InventoryUtil.addItem(converted, (Player) e.getEntity());
		}
	}

	@EventHandler
	public void onItemClick(InventoryClickEvent e) {
		if (e.isCancelled()) return;
		if (e.getClickedInventory() == null) return;

		final ItemStack item = e.getCurrentItem();
		final InventoryType type = e.getClickedInventory().getType();

		if (type == InventoryType.ANVIL) return;

		if (!ItemStackUtil.validate(item, IStatus.HAS_MATERIAL)) return;

		if (!ConvertConfig.convert.containsKey(item.getType())) return;

		if ((type == InventoryType.WORKBENCH || type == InventoryType.CRAFTING || type == InventoryType.SMITHING) && e.getSlotType() == SlotType.RESULT) {

			if (invisibleItem.containsKey(e.getWhoClicked().getUniqueId())) {
				final ItemStack result = ConvertConfig.attemptConversion(invisibleItem.get(e.getWhoClicked().getUniqueId()), true);

				if (e.getAction() == InventoryAction.DROP_ALL_SLOT ||
						e.getAction() == InventoryAction.DROP_ONE_SLOT || e.isShiftClick()) {
					e.setCurrentItem(result);
					return;
				}

				if (!ItemStackUtil.isEquals(item, result))
					Bukkit.getScheduler().scheduleSyncDelayedTask(EI.instance(), () -> {
						e.getView().setCursor(result);
						((Player)e.getWhoClicked()).updateInventory();
					});
				return;
			}
		}

		if (e.getAction() == InventoryAction.DROP_ALL_SLOT || e.getAction() == InventoryAction.DROP_ONE_SLOT) return;

		final ItemStack converted = ConvertConfig.attemptConversion(item, false);
		if (!ItemStackUtil.isEquals(item, converted)) {
			if (e.isShiftClick()) {
				e.setCurrentItem(converted);
			} else
				Bukkit.getScheduler().scheduleSyncDelayedTask(EI.instance(), () -> {
					e.getView().setCursor(converted);
					((Player)e.getWhoClicked()).updateInventory();
				});
		}

	}

}
