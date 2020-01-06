package com.sitrica.restorer.inventories;

import java.util.Map;
import java.util.Optional;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import com.sitrica.core.items.ItemStackBuilder;
import com.sitrica.core.messaging.MessageBuilder;
import com.sitrica.core.sounds.SoundPlayer;
import com.sitrica.restorer.SourRestorer;
import com.sitrica.restorer.managers.PlayerManager;
import com.sitrica.restorer.objects.InventorySave;
import com.sitrica.restorer.objects.RestorerPlayer;

import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import fr.minuskube.inv.content.Pagination;
import fr.minuskube.inv.content.SlotIterator;

public class LogViewerInventory implements InventoryProvider {

	private final FileConfiguration inventories;
	private final PlayerManager playerManager;
	private final SourRestorer instance;
	private final InventorySave save;

	public LogViewerInventory(InventorySave save) {
		this.instance = SourRestorer.getInstance();
		playerManager = instance.getManager(PlayerManager.class);
		inventories = instance.getConfiguration("inventories").get();
		this.save = save;
	}

	@Override
	public void init(Player player, InventoryContents contents) {
		RestorerPlayer restorerPlayer = playerManager.getRestorerPlayer(player);
		Optional<RestorerPlayer> optional = save.getPlayer();
		if (!optional.isPresent()) {
			new MessageBuilder(instance, "messages.player-not-present")
					.setPlaceholderObject(restorerPlayer)
					.send(player);
			return;
		}
		contents.fillBorders(ClickableItem.empty(new ItemStackBuilder(instance, "inventories.logs-inventory.border")
				.setPlaceholderObject(restorerPlayer)
				.build()));
		Pagination pagination = contents.pagination();
		ClickableItem[] items = save.getRestoreLog().entrySet().stream()
				.sorted(Map.Entry.comparingByValue())
				.map(entry -> {
					Optional<RestorerPlayer> owner = playerManager.getRestorerPlayer(entry.getKey());
					if (!owner.isPresent())
						return null;
					return ClickableItem.empty(new ItemStackBuilder(instance, "inventories.logs-inventory.player-icon")
							.replace("%player%", owner.get().getOfflinePlayer().getName())
							.setPlaceholderObject(save)
							.build());
				})
				.filter(element -> element != null)
				.toArray(size -> new ClickableItem[size]);
		pagination.setItems(items);
		pagination.setItemsPerPage(28);
		pagination.addToIterator(contents.newIterator(SlotIterator.Type.HORIZONTAL, 1, 1).allowOverride(false));
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
		contents.set(5, 4, ClickableItem.of(new ItemStackBuilder(instance, "inventories.logs-inventory.back")
				.setPlaceholderObject(save)
				.build(), e -> {
					new ViewInventory(save).open(player);
					new SoundPlayer(instance, "click").playTo(player);
				}
		));
	}

	@Override
	public void update(Player player, InventoryContents contents) {}

	public void open(Player player) {
		SmartInventory inventory = getInventory(player);
		if (inventory == null)
			return;
		inventory.open(player);
	}

	public SmartInventory getInventory(Player player) {
		Optional<RestorerPlayer> optional = save.getPlayer();
		if (!optional.isPresent())
			return null;
		return SmartInventory.builder()
				.title(new MessageBuilder(instance, false, "inventories.logs-inventory.title")
						.replace("%player%", optional.get().getPlayer().get().getName())
						.fromConfiguration(inventories)
						.setPlaceholderObject(player)
						.get())
				.manager(SourRestorer.getInventoryManager())
				.provider(this)
				.id("logs-inventory")
				.size(6, 9)
				.build();
	}

}
