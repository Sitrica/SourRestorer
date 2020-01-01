package com.sitrica.restorer;

import java.util.Optional;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.sitrica.restorer.managers.PlayerManager;
import com.sitrica.restorer.managers.SaveManager;
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
	 * Otherwise will generate it.
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
	 * @param contents The itemstacks of this inventory save.
	 * @return boolean if the save was added.
	 */
	public boolean addInventorySave(UUID uuid, String reason, Location deathLocation, ItemStack... contents) {
		return saves.addInventorySave(uuid, reason, deathLocation, contents);
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

}
