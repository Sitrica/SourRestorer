package com.sitrica.restorer.inventories;

import java.util.function.Consumer;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import com.sitrica.core.items.ItemStackBuilder;
import com.sitrica.core.messaging.MessageBuilder;
import com.sitrica.core.sounds.SoundPlayer;
import com.sitrica.restorer.SourRestorer;
import com.sitrica.restorer.managers.PlayerManager;
import com.sitrica.restorer.objects.RestorerPlayer;

import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;

public class ConfirmationInventory implements InventoryProvider {

	private final FileConfiguration inventories;
	private final PlayerManager playerManager;
	private final Consumer<Boolean> consumer;
	private final SourRestorer instance;

	public ConfirmationInventory(Consumer<Boolean> consumer) {
		this.instance = SourRestorer.getInstance();
		inventories = instance.getConfiguration("inventories").get();
		playerManager = instance.getManager(PlayerManager.class);
		this.consumer = consumer;
	}

	@Override
	public void init(Player player, InventoryContents contents) {
		RestorerPlayer restorerPlayer = playerManager.getRestorerPlayer(player);
		contents.fillBorders(ClickableItem.empty(new ItemStackBuilder(instance, "inventories.confirmation-inventory.border")
				.setPlaceholderObject(restorerPlayer)
				.build()));
		contents.set(1, 3, ClickableItem.of(new ItemStackBuilder(instance, "inventories.confirmation-inventory.confirm")
				.setPlaceholderObject(restorerPlayer)
				.build(), e -> {
					consumer.accept(true);
					new SoundPlayer(instance, "click").playTo(player);
				}
		));
		contents.set(1, 5, ClickableItem.of(new ItemStackBuilder(instance, "inventories.confirmation-inventory.decline")
				.setPlaceholderObject(restorerPlayer)
				.build(), e -> {
					consumer.accept(false);
					new SoundPlayer(instance, "click").playTo(player);
				}
		));
	}

	@Override
	public void update(Player player, InventoryContents contents) {}

	public void open(Player player) {
		getInventory(player).open(player);
	}

	public SmartInventory getInventory(Player player) {
		return SmartInventory.builder()
				.title(new MessageBuilder(instance, false, "inventories.confirmation-inventory.title")
						.replace("%player%", player.getName())
						.fromConfiguration(inventories)
						.setPlaceholderObject(player)
						.get())
				.manager(SourRestorer.getInventoryManager())
				.id("confirmation")
				.provider(this)
				.size(3, 9)
				.build();
	}

}
