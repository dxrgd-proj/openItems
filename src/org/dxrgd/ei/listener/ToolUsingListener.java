package org.dxrgd.ei.listener;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.dxrgd.api.CustomParams;
import org.dxrgd.api.ability.AbilityType;
import org.dxrgd.api.ability.EAbility;
import org.dxrgd.api.bukkit.utility.IStatus;
import org.dxrgd.api.bukkit.utility.ItemStackUtil;
import org.dxrgd.api.durability.DManager;
import org.dxrgd.api.item.ESimpleItem;
import org.dxrgd.api.item.ItemType;
import org.dxrgd.api.open.plugin.listener.BukkitListener;
import org.dxrgd.api.open.plugin.listener.Named;
import org.dxrgd.api.rpg.equipment.EquipSlot;
import org.dxrgd.ei.EI;

import de.tr7zw.changeme.nbtapi.NBTItem;

@Named(key = "toolsUsing")
public class ToolUsingListener implements BukkitListener {

	public void processAbilities(ItemStack stack, Event e) {
		final NBTItem item = new NBTItem(stack);

		if (!item.hasTag("ei-id")) return;

		final List<EAbility> abilities = ESimpleItem.getById(item.getString("ei-id")).getAbilities();

		if (abilities.isEmpty()) return;

		final AbilityType type = AbilityType.fromEvent(e);

		if (type != null)
			abilities.forEach(ab -> {
				if (!ab.getAction().requiresThisEvent(e)) return;

				if (ab.getType() == type)
					ab.getAction().accept(e);
			});

	}

	@EventHandler
	public void onHoeUse(PlayerInteractEvent e) {
		final ItemStack stack = e.getItem();
		if (stack == null || stack.getType() == Material.AIR) return;

		processAbilities(stack, e);

		if (EI.containsData(stack)) e.setCancelled(true);

		if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
			switch(e.getClickedBlock().getType()) {
				case DIRT:
				case GRASS_BLOCK:
					if (ItemStackUtil.isHoe(stack.getType()))
						DManager.decreaseDurability(e.getPlayer(), EquipSlot.HAND, 1);
				default:
			}
		}
	}

	@EventHandler
	public void onBlockBreak(BlockBreakEvent e) {
		final ItemStack stack = e.getPlayer().getInventory().getItemInMainHand();
		if (stack == null || stack.getType() == Material.AIR) return;

		processAbilities(stack, e);

		if (EI.containsData(stack)) e.setCancelled(true);

		if (ItemStackUtil.isTool(stack.getType()) && !ItemStackUtil.isHoe(stack.getType()))
			DManager.decreaseDurability(e.getPlayer(), EquipSlot.HAND, 1);
	}

	@EventHandler
	public void onBlockDamage(BlockDamageEvent e) {
		final ItemStack stack = e.getPlayer().getInventory().getItemInMainHand();
		if (stack == null || stack.getType() == Material.AIR) return;

		if (EI.containsData(stack)) e.setCancelled(true);
	}

	@EventHandler
	public void bowShooting(EntityShootBowEvent e) {
		if (e.getEntityType() == EntityType.PLAYER) {
			final Player p = (Player) e.getEntity();
			final ItemStack hand = p.getInventory().getItemInMainHand();

			if (hand != null && (hand.getType() == Material.BOW || hand.getType() == Material.CROSSBOW))
				DManager.decreaseDurability(p, EquipSlot.HAND, 1);
		}
	}

	@EventHandler
	public void onWeaponUse(EntityDamageByEntityEvent e) {
		final Entity attacker = e.getDamager();
		final Entity receiver = e.getEntity();
		boolean ranged = false;

		if (attacker instanceof AbstractArrow) {
			ranged = true;
		}

		if (attacker.getType() == EntityType.PLAYER) {
			final Player p = (Player)attacker;
			final ItemStack hand = p.getInventory().getItemInMainHand(),
			offhand = p.getInventory().getItemInOffHand();

			boolean isShield = false;
			if (ItemStackUtil.validate(offhand, IStatus.HAS_LORE)) {
				final String type = CustomParams.TYPE.getValue(offhand.getItemMeta().getLore().toString());
				final ItemType it = ItemType.getByKey(type);
				if (it != null && it == ItemType.SHIELD)
					isShield = true;
			}

			if (hand != null && !EI.containsData(hand) && !ranged)
				DManager.decreaseDurability(p, EquipSlot.HAND, 1);

			if (offhand != null && !EI.containsData(offhand) && !isShield && !ranged)
				DManager.decreaseDurability(p, EquipSlot.OFF, 1);
		}

		if (receiver.getType() == EntityType.PLAYER) {
			final Player p = (Player)receiver;
			final ItemStack offhand = p.getInventory().getItemInOffHand();

			DManager.decreaseArmorDurability(p, 1);

			boolean isShield = false;
			if (ItemStackUtil.validate(offhand, IStatus.HAS_LORE)) {
				final String type = CustomParams.TYPE.getValue(offhand.getItemMeta().getLore().toString());
				final ItemType it = ItemType.getByKey(type);
				if (it != null && it == ItemType.SHIELD)
					isShield = true;
			}

			if (isShield)
				DManager.decreaseDurability(p, EquipSlot.OFF, 1);
		}
	}

}
