package com.sitrica.restorer.objects;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import com.google.common.collect.Lists;
import com.sitrica.restorer.managers.SaveManager.SortType;

public class RestorerPlayer {

	private final List<InventorySave> saves = new ArrayList<>();
	// sort and cause are used in the inventories.
	private SortType sort = SortType.DATE;
	private DamageCause cause;
	private final UUID uuid;

	public RestorerPlayer(UUID uuid, Collection<InventorySave> saves) {
		this.saves.addAll(saves);
		this.uuid = uuid;
	}

	public RestorerPlayer(UUID uuid) {
		this.uuid = uuid;
	}

	public UUID getUniqueId() {
		return uuid;
	}

	public SortType getSortType() {
		return sort;
	}

	public void setSortType(SortType sort) {
		this.sort = sort;
	}

	public DamageCause getDamageCause() {
		return cause;
	}

	public void setDamageCausee(DamageCause cause) {
		this.cause = cause;
	}

	public Optional<Player> getPlayer() {
		return Optional.ofNullable(Bukkit.getPlayer(uuid));
	}

	public boolean isOnline() {
		return getPlayer().isPresent();
	}

	public boolean addInventorySave(InventorySave save) {
		return saves.add(save);
	}

	public List<InventorySave> getInventorySaves() {
		return Lists.newArrayList(saves);
	}

	@Override
	public boolean equals(Object object) {
		if (!(object instanceof RestorerPlayer))
			return false;
		RestorerPlayer other = (RestorerPlayer) object;
		if (!other.getUniqueId().equals(uuid))
			return false;
		return true;
	}

}
