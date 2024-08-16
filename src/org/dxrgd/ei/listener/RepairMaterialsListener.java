package org.dxrgd.ei.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.dxrgd.api.bukkit.message.Texts;
import org.dxrgd.api.bukkit.utility.IStatus;
import org.dxrgd.api.bukkit.utility.ItemStackUtil;
import org.dxrgd.api.durability.RepairParams;
import org.dxrgd.api.open.plugin.listener.BukkitListener;
import org.dxrgd.api.open.plugin.listener.Named;
import org.dxrgd.api.open.value.Pair;

import de.tr7zw.changeme.nbtapi.NBTItem;

@Named(key = "repairMaterials")
public class RepairMaterialsListener implements BukkitListener {

	@EventHandler
	public void onAnvil(PrepareAnvilEvent e) {
		final AnvilInventory inv = e.getInventory();

		final ItemStack target = inv.getItem(0);
		final ItemStack ingredient = inv.getItem(1);

		if (!ItemStackUtil.validate(target, IStatus.HAS_META) || !ItemStackUtil.validate(ingredient, IStatus.HAS_META)) return;

		final RepairParams repair = new RepairParams(new Pair<>(target,ingredient));

		repair.processRepair();

		if (repair.getResult() != null) {
			e.setResult(repair.getResult());
			inv.setRepairCost(1);
			inv.setRepairCost(repair.getMaterial().getRepairCost());
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onClick(InventoryClickEvent e) {
		if (e.getCursor() == null || e.getCursor().getType().name().contains("AIR")) return;

		final NBTItem item = new NBTItem(e.getCursor());

		if (item.hasTag("ei-repair"))
			if (e.getClickedInventory() != null)
				if (isRestricted(e.getClickedInventory().getType())) {
					e.setCancelled(true);
					Texts.send(e.getWhoClicked(), "&4You can't do it");
				}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onDrag(InventoryDragEvent e) {
		if (e.getOldCursor() == null || e.getOldCursor().getType().name().contains("AIR")) return;

		final NBTItem item = new NBTItem(e.getOldCursor());

		if (item.hasTag("ei-repair"))
			if (e.getInventory() != null)
				if (isRestricted(e.getInventory().getType())) {
					e.setCancelled(true);
					Texts.send(e.getWhoClicked(), "&4You can't do it");
				}
	}

	public boolean isRestricted(InventoryType type) {
		switch (type) {
			case BEACON:
			case BLAST_FURNACE:
			case BREWING:
			case CARTOGRAPHY:
			case CRAFTING:
			case ENCHANTING:
			case FURNACE:
			case GRINDSTONE:
			case HOPPER:
			case LECTERN:
			case LOOM:
			case MERCHANT:
			case SMITHING:
			case SMOKER:
			case STONECUTTER:
				return true;
			default:
				return false;
		}
	}

}
