package com.sitrica.restorer;

import java.util.Optional;
import java.util.UUID;

import org.bukkit.entity.Player;

import com.sitrica.restorer.managers.PlayerManager;
import com.sitrica.restorer.objects.RestorerPlayer;

public class SourRestorerAPI {

	private final PlayerManager players;

	public SourRestorerAPI(SourRestorer instance) {
		players = instance.getManager(PlayerManager.class);
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

}
