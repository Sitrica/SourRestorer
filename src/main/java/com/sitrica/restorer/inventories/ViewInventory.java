package com.sitrica.restorer.inventories;

import java.util.Optional;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

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

public class ViewInventory implements InventoryProvider {

	private final FileConfiguration inventories;
	private final PlayerManager playerManager;
	private final SourRestorer instance;
	private final InventorySave save;

	public ViewInventory(InventorySave save) {
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
		int column = 0, row = 0;
		for (ItemStack itemstack : save.getContents()) {
			if (itemstack == null)
				column++;
			if (column % 9 == 1) {
				column = -1;
				row++;
			}
			column++;
			contents.set(column, row, ClickableItem.of(itemstack, e -> {
				if (!player.hasPermission("sourrestorer.grab"))
					e.setCancelled(false);
			}));
		}
		contents.set(4, 3, ClickableItem.of(new ItemStackBuilder(instance, "inventories.view-inventory.restore-history")
				.setPlaceholderObject(save)
				.build(), e -> {
					new LogViewerInventory(save).open(player);
					new SoundPlayer(instance, "click").playTo(player);
				}
		));
		contents.set(4, 4, ClickableItem.of(new ItemStackBuilder(instance, "inventories.view-inventory.back")
				.setPlaceholderObject(save)
				.build(), e -> {
					new SavesInventory(save.getPlayer().get()).open(player);
					new SoundPlayer(instance, "click").playTo(player);
				}
		));
		contents.set(4, 5, ClickableItem.of(new ItemStackBuilder(instance, "inventories.view-inventory.restore-icon")
				.setPlaceholderObject(save)
				.build(), e -> {
					new RestoreSelectorInventory(save).open(player);
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
				.title(new MessageBuilder(instance, false, "inventories.view-inventory.title")
						.replace("%player%", optional.get().getPlayer().get().getName())
						.fromConfiguration(inventories)
						.setPlaceholderObject(save)
						.get())
				.manager(SourRestorer.getInventoryManager())
				.provider(this)
				.id("view-inventory")
				.size(5, 9)
				.build();
	}

}
