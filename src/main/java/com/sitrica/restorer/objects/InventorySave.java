package com.sitrica.restorer.objects;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import com.sitrica.restorer.SourRestorer;
import com.sitrica.restorer.managers.PlayerManager;

public class InventorySave {

	// This map, maps out the times that a player has restored this inventory.
	private final Map<UUID, Long> logs = new HashMap<>();
	private final Location deathLocation;
	private final ItemStack[] contents;
	private final long timestamp;
	private final String reason;
	private final UUID uuid;
	private boolean stared;

	public InventorySave(UUID uuid, String reason, Location deathLocation, ItemStack... contents) {
		this(System.currentTimeMillis(), uuid, reason, deathLocation, contents);
	}

	public InventorySave(long timestamp, UUID uuid, String reason, Location deathLocation, ItemStack... contents) {
		this.deathLocation = deathLocation;
		this.timestamp = timestamp;
		this.contents = contents;
		this.reason = reason;
		this.uuid = uuid;
	}

	public String getReason() {
		return reason;
	}

	public void setStared(boolean stared) {
		this.stared = stared;
	}

	public boolean isStared() {
		return stared;
	}

	public UUID getOwnerUUID() {
		return uuid;
	}

	public Location getDeathLocation() {
		return deathLocation;
	}

	public Optional<RestorerPlayer> getPlayer() {
		return SourRestorer.getInstance().getManager(PlayerManager.class).getRestorerPlayer(uuid);
	}

	public ItemStack[] getContents() {
		return contents;
	}

	public void addRestoreLog(UUID uuid) {
		logs.put(uuid, System.currentTimeMillis());
	}

	public void addAllRestoreLog(Map<UUID, Long> map) {
		logs.putAll(map);
	}

	public Map<UUID, Long> getRestoreLog() {
		return Collections.unmodifiableMap(logs);
	}

	public long getTimestamp() {
		return timestamp;
	}

	public boolean restoreInventory() {
		Optional<RestorerPlayer> restorerPlayer = getPlayer();
		if (!restorerPlayer.isPresent())
			return false;
		Optional<Player> player = restorerPlayer.get().getPlayer();
		if (!player.isPresent()) //TODO do offline inventory restoring.
			return false;
		PlayerInventory playerInventory = player.get().getInventory();
		playerInventory.clear();
		playerInventory.setContents(contents);
		//TODO handle armour

		// Automatic delete
		FileConfiguration configuration = SourRestorer.getInstance().getConfig();
		if (configuration.getBoolean("delete-system.restored.enabled", false)) {
			int amount = configuration.getInt("delete-system.restored.amount", 1);
			if (logs.keySet().size() >= amount)
				restorerPlayer.get().removeInventorySave(this);
		}
		return true;
	}

}
