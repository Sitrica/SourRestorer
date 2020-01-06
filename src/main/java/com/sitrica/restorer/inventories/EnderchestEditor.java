package com.sitrica.restorer.inventories;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.sitrica.core.messaging.MessageBuilder;
import com.sitrica.restorer.SourRestorer;
import com.sitrica.restorer.objects.OfflineSave;
import com.sitrica.restorer.objects.RestorerPlayer;

public class EnderchestEditor {

	private final ListenUp listener = new ListenUp();
	private final RestorerPlayer owner;
	private Inventory enderchest;
	private final Player viewer;

	public EnderchestEditor(Player viewer, RestorerPlayer owner) {
		this.viewer = viewer;
		this.owner = owner;

		SourRestorer instance = SourRestorer.getInstance();
		Bukkit.getPluginManager().registerEvents(listener, instance);

		FileConfiguration inventories = instance.getConfiguration("inventories").get();
		enderchest = Bukkit.createInventory(null, 3 * 9, new MessageBuilder(instance, "inventories.enderchest-editor.title")
				.fromConfiguration(inventories)
				.setPlaceholderObject(owner)
				.get());
		enderchest.setContents(owner.getOfflineSave().getEnderchestContents());
		if (owner.isOnline())
			enderchest = owner.getPlayer().get().getEnderChest();
		viewer.openInventory(enderchest);
	}

	private class ListenUp implements Listener {

		@EventHandler
		public void onInventoryClose(InventoryCloseEvent event) {
			if (!event.getInventory().equals(enderchest))
				return;
			if (owner.isOnline()) // it's handled when they leave the server.
				return;
			OfflineSave old = owner.getOfflineSave();
			ItemStack[] contents = enderchest.getContents();
			OfflineSave offline = new OfflineSave(old.getLastLogout(), old.getLogoutLocation(), contents, old.getArmourSave(), old.getContents());
			owner.setOfflineSave(offline);
			new MessageBuilder(SourRestorer.getInstance(), "saves.enderchest-edited")
					.setPlaceholderObject(owner)
					.send(viewer);
		}

	}

	public Inventory getEnderChest() {
		return enderchest;
	}

}
