package com.sitrica.restorer.objects;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import com.sitrica.restorer.SourRestorer;
import com.sitrica.restorer.managers.SaveManager;
import com.sitrica.restorer.managers.SaveManager.SortType;

public class RestorerPlayer {

	private final List<InventorySave> saves = new ArrayList<>();
	// sort and reason are used in the inventory sorting.
	private SortType sort = SortType.DATE;
	private String reason, search;
	private OfflineSave offline;
	private InventorySave save;
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

	public String getSearch() {
		return search;
	}

	public void setSearch(String search) {
		this.search = search;
	}

	public OfflineSave getOfflineSave() {
		return offline;
	}

	public void setOfflineSave(OfflineSave offline) {
		this.offline = offline;
	}

	/**
	 * @return The String to compare against for the reasons of InventorySave's
	 */
	public String getSortingReason() {
		return reason;
	}

	public void setSortingReason(String reason) {
		this.reason = reason;
	}

	public Optional<Player> getPlayer() {
		return Optional.ofNullable(Bukkit.getPlayer(uuid));
	}

	public OfflinePlayer getOfflinePlayer() {
		return Bukkit.getOfflinePlayer(uuid);
	}

	public boolean isOnline() {
		return getPlayer().isPresent();
	}

	public boolean addInventorySave(InventorySave save) {
		return saves.add(save);
	}

	public void clearSaves() {
		SaveManager saveManager = SourRestorer.getInstance().getManager(SaveManager.class);
		saves.removeIf(save -> saveManager.canDelete(save, this));
	}

	public void removeSaveIf(Predicate<InventorySave> predicate) {
		SaveManager saveManager = SourRestorer.getInstance().getManager(SaveManager.class);
		saves.removeIf(save -> saveManager.canDelete(save, this) && saveManager.canDelete(save, this));
	}

	public void removeInventorySave(InventorySave save) {
		if (!SourRestorer.getInstance().getManager(SaveManager.class).canDelete(save, this))
			return;
		saves.remove(save);
	}

	public List<InventorySave> getInventorySaves() {
		return saves;
	}

	/**
	 * This is the inventory that will be loaded when the player joins the server.
	 * Used when an admin commands it to be restored on their next login.
	 * 
	 * @return The InventorySave to load on their next login.
	 */
	public InventorySave getOnlineLoad() {
		return save;
	}

	public void setOnlineLoad(InventorySave save) {
		Validate.isTrue(this.save.getOwnerUUID().equals(save.getOwnerUUID()), "The uuid of the setOnlineLoad being set " +
				"must match the same uuid as the save.");
		this.save = save;
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
