package com.sitrica.restorer.inventories;

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

public class RestoreSelectorInventory implements InventoryProvider {

	private final FileConfiguration inventories;
	private final PlayerManager playerManager;
	private final SourRestorer instance;
	private final InventorySave save;

	public RestoreSelectorInventory(InventorySave save) {
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
		contents.set(2, 4, ClickableItem.of(new ItemStackBuilder(instance, "inventories.restore-selector.back")
				.setPlaceholderObject(save)
				.build(), e -> {
					new ViewInventory(save).open(player);
					new SoundPlayer(instance, "click").playTo(player);
				}
		));
		contents.set(1, 3, ClickableItem.of(new ItemStackBuilder(instance, "inventories.restore-selector.instant")
				.setPlaceholderObject(save)
				.build(), e -> {
					save.restoreInventory();
					save.addRestoreLog(player.getUniqueId());
					new SoundPlayer(instance, "restore").playTo(player);
					player.closeInventory();
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
				.title(new MessageBuilder(instance, false, "inventories.restore-selector.title")
						.replace("%player%", optional.get().getPlayer().get().getName())
						.fromConfiguration(inventories)
						.setPlaceholderObject(player)
						.get())
				.manager(SourRestorer.getInventoryManager())
				.provider(this)
				.id("restore-selector")
				.size(3, 9)
				.build();
	}

}
