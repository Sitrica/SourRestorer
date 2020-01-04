package com.sitrica.restorer.objects;

import java.util.Arrays;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

/**
 * Should always be attached to a RestorerPlayer object.
 */
public class OfflineSave {

	private final ItemStack[] contents, enderchest;
	private final ArmourSave armour;
	private final Location logout;
	private final long timestamp;

	public OfflineSave(Player player) {
		this(player.getLocation(), player.getEnderChest().getContents(), new ArmourSave(player), player.getInventory().getContents());
	}

	public OfflineSave(Location logout, ItemStack[] enderchest, ArmourSave armour, ItemStack... contents) {
		this(System.currentTimeMillis(), logout, enderchest, armour, contents);
	}

	public OfflineSave(long timestamp, Location logout, ItemStack[] enderchest, ArmourSave armour, ItemStack... contents) {
		this.enderchest = enderchest;
		this.timestamp = timestamp;
		this.contents = contents;
		this.logout = logout;
		this.armour = armour;
	}

	public ArmourSave getArmourSave() {
		return armour;
	}

	public Location getLogoutLocation() {
		return logout;
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

	public ItemStack[] getEnderchestContents() {
		return enderchest;
	}

	public void load(Player player) {
		PlayerInventory inventory = player.getInventory();
		if (!Arrays.equals(inventory.getContents(), contents)) {
			// TODO support armour properly.
			inventory.clear();
			inventory.setContents(contents);
		}
		if (!Arrays.equals(player.getEnderChest().getContents(), enderchest)) {
			player.getEnderChest().clear();
			player.getEnderChest().setContents(enderchest);
		}
	}

}
