package com.sitrica.restorer.managers;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import com.sitrica.core.manager.Manager;
import com.sitrica.restorer.SourRestorer;
import com.sitrica.restorer.objects.InventorySave;
import com.sitrica.restorer.objects.RestorerPlayer;

public class SaveManager extends Manager {

	public static enum SortType {

		DATE(true, Comparator.comparingLong(InventorySave::getTimestamp)),
		LOCATION(null) {
			@Override
			public void sort(Player player, List<InventorySave> list) {
				list.removeIf(save -> !save.getDeathLocation().getWorld().equals(player.getLocation().getWorld()));
				Collections.sort(list, new Comparator<InventorySave>() {
					@Override
					public int compare(InventorySave save, InventorySave other) {
						if (save.getDeathLocation() == null)
							return -1;
						return Double.compare(player.getLocation().distance(save.getDeathLocation()), player.getLocation().distance(other.getDeathLocation()));
					}
				});
			}
		},
		SIZE(new Comparator<InventorySave>() {
			@Override
			public int compare(InventorySave save, InventorySave other) {
				return Double.compare(other.getContents().length, save.getContents().length);
			}
		});

		private final Comparator<? super InventorySave> comparator;
		private final boolean reverse;

		SortType(Comparator<? super InventorySave> comparator) {
			this.comparator = comparator;
			this.reverse = false;
		}

		SortType(boolean reverse, Comparator<? super InventorySave> comparator) {
			this.comparator = comparator;
			this.reverse = reverse;
		}

		public void sort(Player player, List<InventorySave> list) {
			Collections.sort(list, comparator);
			if (reverse)
				Collections.reverse(list);
		}

	}

	public SaveManager() throws IllegalAccessException {
		super(true);
	}

	@EventHandler
	public void onPlayerDeath(EntityDamageEvent event) {
		if (event.getEntityType() != EntityType.PLAYER)
			return;
		Player player = (Player) event.getEntity();
		PlayerInventory inventory = player.getInventory();
		if (isEmpty(inventory))
			return;
		RestorerPlayer restorerPlayer = SourRestorer.getInstance().getManager(PlayerManager.class).getRestorerPlayer(player);
		restorerPlayer.addInventorySave(new InventorySave(player.getUniqueId(), event.getCause(), player.getLocation(), inventory.getContents()));
	}

	public boolean isEmpty(Inventory inventory) {
		for (ItemStack itemstack : inventory.getContents()) {
			if (itemstack != null)
				return false;
		}
		return true;
	}

}
