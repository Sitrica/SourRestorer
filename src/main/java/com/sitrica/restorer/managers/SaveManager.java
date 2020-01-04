package com.sitrica.restorer.managers;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.sitrica.core.manager.Manager;
import com.sitrica.core.utils.IntervalUtils;
import com.sitrica.restorer.SourRestorer;
import com.sitrica.restorer.objects.ArmourSave;
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
		SourRestorer instance = SourRestorer.getInstance();
		FileConfiguration configuration = instance.getConfig();

		PlayerManager playerManager = SourRestorer.getInstance().getManager(PlayerManager.class);
		// Time delete task.
		if (configuration.getBoolean("delete-system.time.enabled", false)) {
			if (!configuration.getBoolean("delete-system.time.only-when-loaded", true)) {
				String time = configuration.getString("delete-system.time.task-cycle", "12 hours");
				Bukkit.getScheduler().runTaskTimerAsynchronously(instance, () -> {
					Iterator<RestorerPlayer> iterator = playerManager.getAllPlayers().iterator();
					String after = configuration.getString("delete-system.time.after", "30 days");
					long milliseconds = IntervalUtils.getMilliseconds(after);
					while (iterator.hasNext()) {
						RestorerPlayer player = iterator.next();
						player.removeSaveIf(save -> System.currentTimeMillis() - save.getTimestamp() > milliseconds);
					}
				}, 1, IntervalUtils.getInterval(time));
			}
		}
		// Banned delete task
		if (configuration.getBoolean("delete-system.banned.enabled", false)) {
			String time = configuration.getString("delete-system.banned.task-cycle", "1 day");
			Bukkit.getScheduler().runTaskTimerAsynchronously(instance, () -> {
				Iterator<RestorerPlayer> iterator = playerManager.getAllPlayers().iterator();
				while (iterator.hasNext()) {
					RestorerPlayer restorerPlayer = iterator.next();
					Optional<Player> optional = restorerPlayer.getPlayer();
					if (!optional.isPresent())
						continue;
					Player player = optional.get();
					if (!player.isBanned())
						continue;
					restorerPlayer.clearSaves();
				}
			}, 1, IntervalUtils.getInterval(time));
		}

		// Auto-save
		if (configuration.getBoolean("auto-save.enabled", false)) {
			String time = configuration.getString("auto-save.task-cycle", "1 day");
			int limit = configuration.getInt("auto-save.limit", 5);
			Bukkit.getScheduler().runTaskTimerAsynchronously(instance, () -> {
				for (Player player : Bukkit.getOnlinePlayers()) {
					RestorerPlayer restorerPlayer = playerManager.getRestorerPlayer(player);
					if (limit > 0)
						restorerPlayer.getInventorySaves().stream()
								.filter(save -> save.getReason().equalsIgnoreCase("AUTO-SAVE"))
								.sorted(Comparator.comparing(InventorySave::getTimestamp))
								.limit(limit - 1);
					addInventorySave(player, "AUTO-SAVE");
				}
			}, 1, IntervalUtils.getInterval(time));
		}
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

	/**
	 * Checks if an inventory save can be deleted based on configurations.
	 * All checks get passed through this.
	 * 
	 * @param save The InventorySave to check.
	 * @param player The RestorerPlayer that is getting the inventory save deleted from.
	 * @return boolean if the configurations allow such deletion.
	 */
	public boolean canDelete(InventorySave save, RestorerPlayer player) {
		FileConfiguration configuration = SourRestorer.getInstance().getConfig();
		if (save.isStared() && !configuration.getBoolean("delete-system.delete-stared", false))
			return false;
		if (configuration.getBoolean("delete-system.reasons.enabled", false)) {
			Set<String> reasons = configuration.getStringList("delete-system.reasons.list").stream()
					.map(reason -> reason.toUpperCase(Locale.US))
					.collect(Collectors.toSet());
			if (configuration.getBoolean("delete-system.reasons.whitelist", false)) {
				if (reasons.contains(save.getReason()))
					return false;
			} else if (!reasons.contains(save.getReason()))
				return false;
		}
		return true;
	}

	public boolean addInventorySave(Player player, String reason) {
		return addInventorySave(player.getUniqueId(), reason, player.getLocation(), new ArmourSave(player), player.getInventory().getContents());
	}

	public boolean addInventorySave(UUID uuid, String reason, Location location, ArmourSave armour, ItemStack... contents) {
		SourRestorer instance = SourRestorer.getInstance();
		PlayerManager playerManager = instance.getManager(PlayerManager.class);
		Optional<RestorerPlayer> optional = playerManager.getRestorerPlayer(uuid);
		if (!optional.isPresent())
			return false;
		addReason(reason);
		RestorerPlayer restorerPlayer = optional.get();
		// Delete system max-limit
		if (instance.getConfig().getBoolean("delete-system.max-limit.enabled", false)) {
			int size = restorerPlayer.getInventorySaves().size();
			int limit = instance.getConfig().getInt("delete-system.max-limit.limit", 1000);
			if (limit < 1)
				limit = 1;
			if (size > limit) {
				// Jank way to not remove specials that can't be deleted aswell as cutting list size.
				Set<InventorySave> specials = Sets.newConcurrentHashSet(restorerPlayer.getInventorySaves());
				specials.removeIf(save -> canDelete(save, restorerPlayer));
				restorerPlayer.getInventorySaves().subList(0, limit - 1);
				restorerPlayer.removeSaveIf(save -> canDelete(save, restorerPlayer));
				specials.forEach(save -> restorerPlayer.addInventorySave(save));
			}
		}
		return restorerPlayer.addInventorySave(new InventorySave(uuid, reason, location, armour, contents)); 
	}

	@EventHandler
	public void onPlayerDeath(EntityDamageEvent event) {
		if (event.getEntityType() != EntityType.PLAYER)
			return;
		Player player = (Player) event.getEntity();
		PlayerInventory inventory = player.getInventory();
		if (isEmpty(inventory))
			return;
		addInventorySave(player, event.getCause() + "");
	}

	public boolean isEmpty(Inventory inventory) {
		for (ItemStack itemstack : inventory.getContents()) {
			if (itemstack != null)
				return false;
		}
		return true;
	}

}
