package com.sitrica.restorer.objects;

import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import com.sitrica.restorer.SourRestorer;
import com.sitrica.restorer.managers.PlayerManager;

public class OfflineSave {

	private final ItemStack[] contents;
	private final Location logout;
	private final long timestamp;
	private final UUID uuid;

	public OfflineSave(Player player) {
		this(player.getUniqueId(), player.getLocation(), player.getInventory().getContents());
	}

	public OfflineSave(UUID uuid, Location logout, ItemStack... contents) {
		this(System.currentTimeMillis(), uuid, logout, contents);
	}

	public OfflineSave(long timestamp, UUID uuid, Location logout, ItemStack... contents) {
		this.timestamp = timestamp;
		this.contents = contents;
		this.logout = logout;
		this.uuid = uuid;
	}

	public UUID getOwnerUUID() {
		return uuid;
	}

	public Location getLogoutLocation() {
		return logout;
	}

	public Optional<RestorerPlayer> getPlayer() {
		return SourRestorer.getInstance().getManager(PlayerManager.class).getRestorerPlayer(uuid);
	}

	public ItemStack[] getContents() {
		return contents;
	}

	/**
	 * @return The timestamp the player last logged out.
	 */
	public long getLastLogout() {
		return timestamp;
	}

	public void load(Player player) {
		PlayerInventory inventory = player.getInventory();
		if (!Arrays.equals(inventory.getContents(), contents)) {
			// TODO support armour properly.
			inventory.clear();
			inventory.setContents(contents);
		}
	}

}
