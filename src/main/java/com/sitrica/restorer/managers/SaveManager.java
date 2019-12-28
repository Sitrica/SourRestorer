package com.sitrica.restorer.managers;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import com.google.common.collect.Lists;
import com.sitrica.core.manager.Manager;
import com.sitrica.restorer.SourRestorer;
import com.sitrica.restorer.objects.InventorySave;
import com.sitrica.restorer.objects.RestorerPlayer;

public class SaveManager extends Manager {

	// Used in the API or when creating custom reasons other than damage causes.
	private final Set<String> reasons = new HashSet<>();

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
		Arrays.stream(DamageCause.values()).forEach(cause -> reasons.add(cause.name()));
		// When a user with permissions manually saves an inventory.
		reasons.add("MANUAL");
	}

	/**
	 * Use this to register a reason that will be used in sorting.
	 * This isn't required as the system will automatically add
	 * reasons that are saved but not in system.
	 * 
	 * @param reason The string ignoring caps of the reason that will be used in saves.
	 */
	public void addReason(String reason) {
		reasons.add(reason.toUpperCase());
	}

	/**
	 * @return Sorted list of reasons alphabetically.
	 */
	public List<String> getReasons() {
		List<String> list = Lists.newArrayList(reasons);
		Collections.sort(list);
		return list;
	}

	public boolean addInventorySave(Player player, String reason) {
		return addInventorySave(player.getUniqueId(), reason, player.getLocation(), player.getInventory().getContents());
	}

	public boolean addInventorySave(UUID uuid, String reason, Location location, ItemStack... contents) {
		PlayerManager playerManager = SourRestorer.getInstance().getManager(PlayerManager.class);
		Optional<RestorerPlayer> restorerPlayer = playerManager.getRestorerPlayer(uuid);
		if (!restorerPlayer.isPresent())
			return false;
		addReason(reason);
		return restorerPlayer.get().addInventorySave(new InventorySave(uuid, reason, location, contents)); 
	}

	@EventHandler
	public void onPlayerDeath(EntityDamageEvent event) {
		if (event.getEntityType() != EntityType.PLAYER)
			return;
		Player player = (Player) event.getEntity();
		PlayerInventory inventory = player.getInventory();
		if (isEmpty(inventory))
			return;
		addInventorySave(player.getUniqueId(), event.getCause() + "", player.getLocation(), inventory.getContents());
	}

	public boolean isEmpty(Inventory inventory) {
		for (ItemStack itemstack : inventory.getContents()) {
			if (itemstack != null)
				return false;
		}
		return true;
	}

}
