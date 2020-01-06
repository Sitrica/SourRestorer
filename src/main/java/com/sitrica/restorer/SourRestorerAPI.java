package com.sitrica.restorer;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.sitrica.restorer.managers.PlayerManager;
import com.sitrica.restorer.managers.SaveManager;
import com.sitrica.restorer.objects.ArmourSave;
import com.sitrica.restorer.objects.InventorySave;
import com.sitrica.restorer.objects.RestorerPlayer;

public class SourRestorerAPI {

	private final PlayerManager players;
	private final SaveManager saves;

	public SourRestorerAPI(SourRestorer instance) {
		players = instance.getManager(PlayerManager.class);
		saves = instance.getManager(SaveManager.class);
	}

	/**
	 * Returns the RestorerPlayer object allocated to the Player.
	 * Otherwise will generate it. All info is contained in this object.
	 * 
	 * @param player The Bukkit Player to match with.
	 * @return The RestorerPlayer object allocated to the Player.
	 */
	public RestorerPlayer getRestorerPlayer(Player player) {
		return players.getRestorerPlayer(player);
	}

	/**
	 * Returns the RestorerPlayer object allocated to the UUID.
	 * If there was a data set matching the UUID will be optional.
	 * All info is contained in this object.
	 * 
	 * @param uuid The Bukkit Player UUID to match with.
	 * @return The RestorerPlayer in an optional if present in database.
	 */
	public Optional<RestorerPlayer> getRestorerPlayer(UUID uuid) {
		return players.getRestorerPlayer(uuid);
	}

	/**
	 * Manually add an inventory save through API.
	 * 
	 * @param uuid The player to have this inventory save based on.
	 * @param reason The reason of the save, if reason not registered, it will be saved.
	 * @param deathLocation The location of the inventory save.
	 * @param armour An ArmourSave snapshot, can be manually made.
	 * @param contents The itemstacks of this inventory save.
	 * @return boolean if the save was added.
	 */
	public boolean addInventorySave(UUID uuid, String reason, Location deathLocation, ArmourSave armour, ItemStack... contents) {
		return saves.addInventorySave(uuid, reason, deathLocation, armour, contents);
	}

	/**
	 * Add an inventory save using the player directly.
	 * 
	 * @param player The Player object to get info from.
	 * @param reason The reason of the save, if reason not registered, it will be saved.
	 * @return boolean if the save was added.
	 */
	public boolean addInventorySave(Player player, String reason) {
		return saves.addInventorySave(player, reason);
	}

	/**
	 * Creates a local InventorySave object that
	 * is not linked to the saving system.
	 * E.g usage: Saving an inventory before entering a minigame.
	 * 
	 * @param player The player to have their inventory contents saved.
	 * @param location The location to make the save based from.
	 * @return The unallocated InventorySave object for API usage.
	 */
	public InventorySave createInventorySave(Player player, Location location) {
		ArmourSave armour = new ArmourSave(player);
		ItemStack[] contents = player.getInventory().getStorageContents(); //try getContents if failed test.
		return new InventorySave(player.getUniqueId(), "API", location, armour, contents);
	}

	/**
	 * Creates a local InventorySave object that
	 * is not linked to the saving system.
	 * E.g usage: Saving an inventory before entering a minigame.
	 * 
	 * @param player The player to have their inventory contents saved.
	 * @return The unallocated InventorySave object for API usage.
	 */
	public InventorySave createInventorySave(Player player) {
		return new InventorySave(player, "API");
	}

	/**
	 * If you want to register a custom reason.
	 * This is optional, system will catch it regardless if registered.
	 * 
	 * @param reason The String reason of potential inventory saves.
	 */
	public void addSaveReason(String reason) {
		saves.addReason(reason);
	}

	/**
	 * Because Spigot still to this day doesn't have a proper empty inventory method.
	 * Useful if wanting to check if an inventory is empty.
	 * 
	 * @param inventory The Inventory to check if empty.
	 * @return boolean if the Inventory is empty.
	 */
	public boolean isInventoryEmpty(Inventory inventory) {
		return saves.isEmpty(inventory);
	}

	/**
	 * @return List of registered reasons sorted alphabetically.
	 */
	public List<String> getReasons() {
		return saves.getReasons();
	}

}
