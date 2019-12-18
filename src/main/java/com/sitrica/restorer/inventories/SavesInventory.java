package com.sitrica.restorer.inventories;

import java.util.List;
import java.util.Locale;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

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

	public SavesInventory() {
		this.instance = SourRestorer.getInstance();
		playerManager = instance.getManager(PlayerManager.class);
		inventories = instance.getConfiguration("inventories").get();
	}

	@Override
	public void init(Player player, InventoryContents contents) {
		RestorerPlayer restorerPlayer = playerManager.getRestorerPlayer(player);
		contents.fillBorders(ClickableItem.empty(new ItemStackBuilder(instance, "inventories.save-inventory.border")
				.setPlaceholderObject(restorerPlayer)
				.build()));
		List<InventorySave> saves = restorerPlayer.getInventorySaves();
		SortType sort = restorerPlayer.getSortType();
		sort.sort(player, saves);
		if (saves.size() <= 0)
			return;
		DamageCause cause = restorerPlayer.getDamageCause();
		if (cause != null)
			saves.removeIf(save -> save.getDamageCause() != cause);
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
					return ClickableItem.of(itemstack, event -> {
							Inventory inventory = Bukkit.createInventory(null, 6 * 9, new MessageBuilder(instance, false, "inventories.view-inventory.title")
									.fromConfiguration(inventories)
									.setPlaceholderObject(save)
									.get());
							inventory.clear();
							inventory.setContents(save.getContents());
							player.openInventory(inventory);
							new SoundPlayer(instance, "click").playTo(player);
						});
				})
				.toArray(size -> new ClickableItem[size]);
		pagination.setItems(items);
		pagination.setItemsPerPage(28);
		pagination.addToIterator(contents.newIterator(SlotIterator.Type.HORIZONTAL, 1, 1).allowOverride(false));
		contents.set(0, 5, ClickableItem.of(new ItemStackBuilder(instance, "inventories.save-inventory.damage-cause")
				.withAdditionalLoresIf(restorerPlayer.getDamageCause() != null, new ListMessageBuilder(instance, false, "inventories.save-inventory.damage-cause.additional-lore")
						.withPlaceholder(restorerPlayer.getDamageCause(), new Placeholder<DamageCause>("%cause%") {
							@Override
							public Object replace(DamageCause cause) {
								if (cause == null)
									return null;
								return restorerPlayer.getDamageCause().name().toLowerCase(Locale.US);
							}
						})
						.setPlaceholderObject(restorerPlayer)
						.fromConfiguration(inventories)
						.get())
				.glowingIf(restorerPlayer.getDamageCause() != null)
				.setPlaceholderObject(restorerPlayer)
				.build(), e -> {
					if (e.isRightClick() && restorerPlayer.getDamageCause() != null) {
						restorerPlayer.setDamageCausee(null);
						new SoundPlayer(instance, "damage-cause-clear").playTo(player);
						getInventory(player).open(player);
						return;
					}
					new DamageCauseInventory().open(player);
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
