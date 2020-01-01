package com.sitrica.restorer.inventories;

import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import com.google.common.collect.Lists;
import com.sitrica.core.items.ItemStackBuilder;
import com.sitrica.core.messaging.ListMessageBuilder;
import com.sitrica.core.messaging.MessageBuilder;
import com.sitrica.core.placeholders.Placeholder;
import com.sitrica.core.sounds.SoundPlayer;
import com.sitrica.restorer.SourRestorer;
import com.sitrica.restorer.managers.PlayerManager;
import com.sitrica.restorer.managers.SaveManager.SortType;
import com.sitrica.restorer.objects.InventorySave;
import com.sitrica.restorer.objects.RestorerPlayer;

import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import fr.minuskube.inv.content.Pagination;
import fr.minuskube.inv.content.SlotIterator;

public class SavesInventory implements InventoryProvider {

	private final FileConfiguration inventories;
	private final PlayerManager playerManager;
	private final SourRestorer instance;
	private final RestorerPlayer owner;

	public SavesInventory(RestorerPlayer owner) {
		this.instance = SourRestorer.getInstance();
		playerManager = instance.getManager(PlayerManager.class);
		inventories = instance.getConfiguration("inventories").get();
		this.owner = owner;
	}

	@Override
	public void init(Player player, InventoryContents contents) {
		RestorerPlayer restorerPlayer = playerManager.getRestorerPlayer(player);
		contents.fillBorders(ClickableItem.empty(new ItemStackBuilder(instance, "inventories.save-inventory.border")
				.setPlaceholderObject(restorerPlayer)
				.build()));
		List<InventorySave> saves = Lists.newArrayList(owner.getInventorySaves());
		SortType sort = restorerPlayer.getSortType();
		sort.sort(player, saves);
		if (saves.size() <= 0)
			return;
		String reason = restorerPlayer.getSortingReason();
		if (reason != null)
			saves.removeIf(save -> save.getReason().equalsIgnoreCase(reason));
		List<InventorySave> stars = saves.stream()
				.filter(save -> save.isStared())
				.collect(Collectors.toList());
		saves.removeIf(save -> save.isStared());
		saves.addAll(stars);
		String search = restorerPlayer.getSearch();
		if (search != null)
			saves.removeIf(save -> {
				for (ItemStack itemstack : save.getContents()) {
					if (itemstack == null)
						continue;
					if (itemstack.getType().name().contains(search.toUpperCase(Locale.US)))
						return false;
					String display = itemstack.getItemMeta().getDisplayName().toLowerCase(Locale.US);
					if (display.contains(search.toLowerCase(Locale.US)))
						return false;
					for (String lore : itemstack.getItemMeta().getLore()) {
						if (lore.toLowerCase(Locale.US).contains(search.toLowerCase(Locale.US)))
							return false;
					}
				}
				return true;
			});
		Pagination pagination = contents.pagination();
		ClickableItem[] items = saves.stream()
				.map(save -> {
					ItemStack itemstack = new ItemStackBuilder(instance, "inventories.save-inventory.inventory-icon")
							.withPlaceholder(save.getDeathLocation(), new Placeholder<Location>("%distance%") {
								@Override
								public Object replace(Location location) {
									if (!location.getWorld().equals(player.getWorld()))
										return new MessageBuilder(instance, false, "messages.not-same-world").get();
									return Math.round(player.getLocation().distance(location));
								}
							})
							.setPlaceholderObject(save)
							.build();
					if (save.isStared())
						itemstack.setType(Material.NETHER_STAR);
					return ClickableItem.of(itemstack, event -> {
						if (event.getClick() == ClickType.MIDDLE) {
							save.setStared(!save.isStared());
							open(player);
						} else if (event.getClick() == ClickType.DROP) {
							if (!player.hasPermission("sourrestorer.delete")) {
								new MessageBuilder(instance, "messages.no-permission").send(player);
								return;
							}
							Consumer<Boolean> consumer = accept -> {
								if (accept == true)
									owner.removeInventorySave(save);
								open(player);
								return;
							};
							if (!instance.getConfig().getBoolean("disable-confirmation-delete", false)) {
								new ConfirmationInventory(consumer);
							} else {
								consumer.accept(true);
								new SoundPlayer(instance, "click").playTo(player);
							}
						} else {
							new ViewInventory(save).open(player);
							new SoundPlayer(instance, "click").playTo(player);
						}
					});
				})
				.toArray(size -> new ClickableItem[size]);
		pagination.setItems(items);
		pagination.setItemsPerPage(28);
		pagination.addToIterator(contents.newIterator(SlotIterator.Type.HORIZONTAL, 1, 1).allowOverride(false));
		contents.set(0, 5, ClickableItem.of(new ItemStackBuilder(instance, "inventories.save-inventory.damage-cause")
				.withAdditionalLoresIf(reason != null, new ListMessageBuilder(instance, false, "inventories.save-inventory.damage-cause.additional-lore")
						.setPlaceholderObject(restorerPlayer)
						.fromConfiguration(inventories)
						.replace("%reason%", reason)
						.replace("%cause%", reason)
						.get())
				.glowingIf(reason != null)
				.setPlaceholderObject(restorerPlayer)
				.build(), e -> {
					if (e.isRightClick() && reason != null) {
						restorerPlayer.setSortingReason(null);
						new SoundPlayer(instance, "damage-cause-clear").playTo(player);
						getInventory(player).open(player);
						return;
					}
					new ReasonsInventory(owner).open(player);
					new SoundPlayer(instance, "click").playTo(player);
				}));
		contents.set(0, 4, ClickableItem.of(new ItemStackBuilder(instance, "inventories.save-inventory.search")
				.replace("%search%", search == null ? "Not set" : search)
				.setPlaceholderObject(owner)
				.build(),
				e -> {
					new AnvilMenu(new ItemStackBuilder(instance, "inventories.search-anvil.search")
							.build(), player, result -> restorerPlayer.setSearch(result));
					open(player);
					new SoundPlayer(instance, "click").playTo(player);
				}));
		contents.set(0, 3, ClickableItem.of(new ItemStackBuilder(instance, "inventories.save-inventory.sort." + sort.name().toLowerCase(Locale.US)).build(),
				e -> {
					switch (sort) {
						case DATE:
							restorerPlayer.setSortType(SortType.LOCATION);
							break;
						case LOCATION:
							restorerPlayer.setSortType(SortType.SIZE);
							break;
						case SIZE:
							restorerPlayer.setSortType(SortType.DATE);
							break;
					}
					open(player);
					new SoundPlayer(instance, "click").playTo(player);
				}));
		if (!pagination.isFirst())
			contents.set(5, 3, ClickableItem.of(new ItemStackBuilder(instance, "previous").build(),
					e -> {
						getInventory(player).open(player, pagination.previous().getPage());
						new SoundPlayer(instance, "click").playTo(player);
					}));
		if (!pagination.isLast())
			contents.set(5, 5, ClickableItem.of(new ItemStackBuilder(instance, "next").build(),
					e -> { 
						getInventory(player).open(player, pagination.next().getPage());
						new SoundPlayer(instance, "click").playTo(player);
					}));
	}

	@Override
	public void update(Player player, InventoryContents contents) {}

	public void open(Player player) {
		getInventory(player).open(player);
	}

	public SmartInventory getInventory(Player player) {
		return SmartInventory.builder()
				.title(new MessageBuilder(instance, false, "inventories.save-inventory.title")
						.replace("%player%", player.getName())
						.fromConfiguration(inventories)
						.setPlaceholderObject(player)
						.get())
				.manager(SourRestorer.getInventoryManager())
				.provider(this)
				.id("save-inventory")
				.size(6, 9)
				.build();
	}

}
