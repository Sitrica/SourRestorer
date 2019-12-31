package com.sitrica.restorer.managers;

import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.google.common.collect.ImmutableMap;
import com.sitrica.core.database.Database;
import com.sitrica.core.database.Serializer;
import com.sitrica.core.manager.Manager;
import com.sitrica.core.utils.IntervalUtils;
import com.sitrica.restorer.SourRestorer;
import com.sitrica.restorer.objects.InventorySave;
import com.sitrica.restorer.objects.OfflineSave;
import com.sitrica.restorer.objects.RestorerPlayer;
import com.sitrica.restorer.serializers.InventorySaveSerializer;
import com.sitrica.restorer.serializers.RestorerPlayerSerializer;

public class PlayerManager extends Manager {

	private final Set<RestorerPlayer> players = new HashSet<>();
	private final Database<RestorerPlayer> database;

	public PlayerManager() throws IllegalAccessException {
		super(true);
		SourRestorer instance = SourRestorer.getInstance();
		FileConfiguration configuration = instance.getConfig();
		Map<Type, Serializer<?>> map = ImmutableMap.of(RestorerPlayer.class, new RestorerPlayerSerializer(), InventorySave.class, new InventorySaveSerializer());
		database = getNewDatabase(instance, "player-table", RestorerPlayer.class, map);
		String interval = configuration.getString("database.autosave", "5 miniutes");
		Bukkit.getScheduler().runTaskTimerAsynchronously(instance, () -> players.forEach(player -> database.put(player.getUniqueId() + "", player)), 0, IntervalUtils.getInterval(interval));
	}

	public RestorerPlayer getRestorerPlayer(Player player) {
		return getRestorerPlayer(player.getUniqueId()).orElseGet(() -> {
					RestorerPlayer p = new RestorerPlayer(player.getUniqueId());
					database.put(player.getUniqueId() + "", p);
					players.add(p);
					return p;
				});
	}

	public Optional<RestorerPlayer> getRestorerPlayer(OfflinePlayer player) {
		if (player.isOnline())
			return Optional.of(getRestorerPlayer(player.getPlayer()));
		return getRestorerPlayer(player.getUniqueId());
	}

	public Optional<RestorerPlayer> getRestorerPlayer(UUID uuid) {
		return Optional.ofNullable(players.parallelStream()
				.filter(p -> p.getUniqueId().equals(uuid))
				.findFirst()
				.orElseGet(() -> {
					RestorerPlayer player = database.get(uuid + "");
					if (player != null)
						players.add(player);
					return player;
				}));
	}

	public Set<RestorerPlayer> getAllPlayers() {
		return database.getKeys().stream()
				.map(key -> database.get(key))
				.collect(Collectors.toSet());
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		RestorerPlayer restorerPlayer = getRestorerPlayer(event.getPlayer());
		SourRestorer instance = SourRestorer.getInstance();
		instance.debugMessage("Loaded player " + player.getUniqueId());
		InventorySave load = restorerPlayer.getOnlineLoad();
		if (load != null) {
			load.restoreInventory();
			return;
		}
		OfflineSave offline = restorerPlayer.getOfflineSave();
		offline.load(player);
		if (instance.getConfig().getBoolean("delete-system.login", false))
			restorerPlayer.clearSaves();
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		RestorerPlayer restorerPlayer = getRestorerPlayer(player);
		restorerPlayer.setOfflineSave(new OfflineSave(player));
		SourRestorer instance = SourRestorer.getInstance();
		if (instance.getConfig().getBoolean("delete-system.logout", false))
			restorerPlayer.clearSaves();
		database.put(player.getUniqueId() + "", restorerPlayer);
		Bukkit.getScheduler().runTaskLaterAsynchronously(instance, () -> players.removeIf(p -> p.getUniqueId().equals(player.getUniqueId())), 1);
	}

}
