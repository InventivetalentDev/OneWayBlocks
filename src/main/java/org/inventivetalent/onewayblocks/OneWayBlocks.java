/*
 * Copyright 2015-2016 inventivetalent. All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without modification, are
 *  permitted provided that the following conditions are met:
 *
 *     1. Redistributions of source code must retain the above copyright notice, this list of
 *        conditions and the following disclaimer.
 *
 *     2. Redistributions in binary form must reproduce the above copyright notice, this list
 *        of conditions and the following disclaimer in the documentation and/or other materials
 *        provided with the distribution.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE AUTHOR ''AS IS'' AND ANY EXPRESS OR IMPLIED
 *  WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 *  FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE AUTHOR OR
 *  CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 *  CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 *  SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 *  ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 *  NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 *  ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 *  The views and conclusions contained in the software and documentation are those of the
 *  authors and contributors and should not be interpreted as representing official policies,
 *  either expressed or implied, of anybody else.
 */

package org.inventivetalent.onewayblocks;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.inventivetalent.itembuilder.ItemBuilder;
import org.inventivetalent.pluginannotations.PluginAnnotations;
import org.inventivetalent.pluginannotations.command.Command;
import org.inventivetalent.pluginannotations.command.OptionalArg;
import org.inventivetalent.pluginannotations.command.Permission;
import org.inventivetalent.pluginannotations.config.ConfigValue;
import org.inventivetalent.vectors.d3.Vector3DDouble;
import org.mcstats.MetricsLite;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class OneWayBlocks extends JavaPlugin implements Listener {

	@ConfigValue(path = "radius.x") double radiusX;
	@ConfigValue(path = "radius.y") double radiusY;
	@ConfigValue(path = "radius.z") double radiusZ;

	ItemStack wandItem;

	@Override
	public void onEnable() {
		saveDefaultConfig();
		PluginAnnotations.loadAll(this, this);
		Bukkit.getPluginManager().registerEvents(this, this);

		wandItem = new ItemBuilder(Material.NAME_TAG).fromConfig(getConfig().getConfigurationSection("item")).build();

		try {
			MetricsLite metrics = new MetricsLite(this);
			if (metrics.start()) {
				getLogger().info("Metrics started");
			}
		} catch (Exception e) {
		}
	}

	@Command(name = "onewayblockwand",
			 aliases = {
					 "owbw",
					 "blockwand",
					 "onewaywand",
					 "oww"
			 },
			 usage = "[material] [inverted]",
			 description = "Give yourself the one-way-block-wand",
			 min = 0,
			 max = 2,
			 fallbackPrefix = "onewayblocks")
	@Permission("onewayblocks.wand")
	public void oneWayWand(Player sender, @OptionalArg(def = "AIR:0") String material, @OptionalArg(def = "not inverted") String inverted) {
		ItemStack itemStack = wandItem.clone();
		setLoreIndex(itemStack, 0, "inverted".equalsIgnoreCase(inverted) ? "inverted" : "not inverted");
		setLoreIndex(itemStack, 1, material.toUpperCase());
		sender.getInventory().addItem(itemStack);
	}

	@EventHandler
	public void on(PlayerMoveEvent event) {
		if (event.getFrom().distanceSquared(event.getTo()) < 0.004) { return; }

		Vector3DDouble playerVector = new Vector3DDouble(event.getPlayer().getEyeLocation());

		for (OneWayBlock block : getNearbyOneWayBlocks(event.getPlayer())) {
			Block bukkitBlock = block.getBlock(event.getPlayer().getWorld());

			// Blame Bukkit for not properly hiding invisible ArmorStands...
			ArmorStand tempMarker = bukkitBlock.getWorld().spawn(new Location(event.getPlayer().getWorld(), 0, 0, 0), ArmorStand.class);
			tempMarker.setMarker(true);
			tempMarker.setVisible(false);
			tempMarker.setGravity(false);
			tempMarker.setSmall(true);
			tempMarker.teleport(bukkitBlock.getRelative(block.getDirection()).getLocation().add(.5, -.5, .5));

			if (!block.faceVisibleFrom(playerVector) && tempMarker.hasLineOfSight(event.getPlayer())) {
				event.getPlayer().sendBlockChange(bukkitBlock.getLocation(), block.getMaterial(), block.getData());
			} else {
				event.getPlayer().sendBlockChange(bukkitBlock.getLocation(), bukkitBlock.getType(), bukkitBlock.getData());
			}

			tempMarker.remove();
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void on(BlockBreakEvent event) {
		if (event.isCancelled()) { return; }
		for (Entity entity : event.getPlayer().getNearbyEntities(16, 16, 16)) {
			if (entity.getType() == EntityType.ARMOR_STAND) {
				if (entity.getCustomName().startsWith("OneWayBlock-")) {
					if (entity.getLocation().getBlock().equals(event.getBlock())) {
						entity.remove();
					}
				}
			}
		}
	}

	@EventHandler
	public void on(PlayerInteractEvent event) {
		if (event.isCancelled()) { return; }
		if (!event.getPlayer().hasPermission("onewayblocks.create")) { return; }
		if (event.getItem() == null) { return; }
		if (event.getItem().getType() != wandItem.getType()) { return; }
		if (!event.getItem().hasItemMeta()) { return; }
		if (!wandItem.getItemMeta().getDisplayName().equals(event.getItem().getItemMeta().getDisplayName())) { return; }
		if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			boolean inverted = "inverted".equals(getLoreIndex(event.getItem(), 0));

			Material material = null;
			byte data = 0;
			try {
				String materialString = getLoreIndex(event.getItem(), 1);
				if (materialString.contains(":")) {
					String[] materialSplit = materialString.split(":");
					if (materialSplit.length != 2) {
						return;
					}
					material = Material.valueOf(materialSplit[0]);
					data = Byte.parseByte(materialSplit[1]);
				} else {
					material = Material.valueOf(materialString);
				}
			} catch (Exception ignored) {
			}

			if (material == null || material == Material.AIR) {
				event.getPlayer().sendMessage("§cPlease left-click to select another material first");
				return;
			}
			event.setCancelled(true);

			// Kill old ArmorStands
			for (Entity entity : event.getPlayer().getNearbyEntities(16, 16, 16)) {
				if (entity.getType() == EntityType.ARMOR_STAND) {
					if (entity.getCustomName().startsWith("OneWayBlock-")) {
						if (entity.getLocation().getBlock().equals(event.getClickedBlock())) {
							entity.remove();
						}
					}
				}
			}

			BlockFace face = event.getBlockFace();
			if (inverted) { face = face.getOppositeFace(); }
			Location location = event.getClickedBlock().getLocation().add(.5, .5, .5);
			ArmorStand blockMarker = location.getWorld().spawn(location, ArmorStand.class);
			blockMarker.setMarker(true);
			blockMarker.setVisible(false);
			blockMarker.setGravity(false);
			blockMarker.setSmall(true);
			blockMarker.setBasePlate(false);
			blockMarker.setCustomName("OneWayBlock-" + face.name() + "-" + material + ":" + data + "-"/* + (inverted ? "inverted" : "")*/);

			event.getPlayer().sendMessage("§aBlock converted");
		} else if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
			Block clicked = event.getClickedBlock();
			String materialString = clicked.getType() + ":" + clicked.getData();
			setLoreIndex(event.getItem(), 1, materialString);
			event.getPlayer().sendMessage("§aMaterial changed to §b" + materialString);
			event.setCancelled(true);
		}
	}

	ItemStack setLoreIndex(ItemStack itemStack, int index, String text) {
		ItemMeta meta = itemStack.getItemMeta();
		List<String> lore = !meta.hasLore() ? new ArrayList<String>() : new ArrayList<>(meta.getLore());
		if (index >= lore.size()) {
			lore.add(index, text);
		} else {
			lore.set(index, text);
		}
		meta.setLore(lore);
		itemStack.setItemMeta(meta);
		return itemStack;
	}

	String getLoreIndex(ItemStack itemStack, int index) {
		if (index >= itemStack.getItemMeta().getLore().size()) { return ""; }
		return itemStack.getItemMeta().getLore().get(index);
	}

	public Set<OneWayBlock> getNearbyOneWayBlocks(Player player) {
		Set<OneWayBlock> blocks = new HashSet<>();
		for (Entity entity : player.getNearbyEntities(radiusX, radiusY, radiusZ)) {
			if (entity.getType() == EntityType.ARMOR_STAND) {
				if (entity.getCustomName() != null && entity.getCustomName().startsWith("OneWayBlock-")) {
					OneWayBlock oneWayBlock = OneWayBlock.of(entity);
					oneWayBlock.setEntity((ArmorStand) entity);
					blocks.add(oneWayBlock);
				}
			}
		}

		return blocks;
	}

}
