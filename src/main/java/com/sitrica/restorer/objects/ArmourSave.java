package com.sitrica.restorer.objects;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class ArmourSave {

	private final ItemStack helmet, chestplate, leggings, boots;
	private final ItemStack[] extra;

	public ArmourSave(Player player) {
		PlayerInventory inventory = player.getInventory();
		this.extra = inventory.getExtraContents();
		this.chestplate = inventory.getChestplate();
		this.leggings = inventory.getLeggings();
		this.helmet = inventory.getHelmet();
		this.boots = inventory.getBoots();
	}

	public ArmourSave(ItemStack helmet, ItemStack chestplate, ItemStack leggings, ItemStack boots, ItemStack... extra) {
		this.chestplate = chestplate;
		this.leggings = leggings;
		this.helmet = helmet;
		this.boots = boots;
		this.extra = extra;
	}

	public ItemStack[] getExtraContents( ) {
		return extra;
	}

	public ItemStack getChestplate() {
		return chestplate;
	}

	public ItemStack getLeggings() {
		return leggings;
	}

	public ItemStack getHelmet() {
		return helmet;
	}

	public ItemStack getBoots() {
		return boots;
	}

}
