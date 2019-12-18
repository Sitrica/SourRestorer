package com.sitrica.restorer.inventories;

import java.util.Arrays;
import java.util.Locale;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.ItemStack;

import com.google.common.collect.Ordering;
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
import fr.minuskube.inv.content.Pagination;
import fr.minuskube.inv.content.SlotIterator;

public class DamageCauseInventory implements InventoryProvider {

	private final FileConfiguration inventories;
	private final PlayerManager playerManager;
	private final SourRestorer instance;

	public DamageCauseInventory() {
		this.instance = SourRestorer.getInstance();
		playerManager = instance.getManager(PlayerManager.class);
		inventories = instance.getConfiguration("inventories").get();
	}

	@Override
	public void init(Player player, InventoryContents contents) {
		RestorerPlayer restorerPlayer = playerManager.getRestorerPlayer(player);
		contents.fillBorders(ClickableItem.empty(new ItemStackBuilder(instance, "inventories.damage-causes.border")
				.setPlaceholderObject(restorerPlayer)
				.build()));
		Pagination pagination = contents.pagination();
		ClickableItem[] items = Arrays.stream(DamageCause.values())
				.sorted(Ordering.usingToString())
				.map(cause -> {
					ItemStack itemstack = new ItemStackBuilder(instance, "inventories.damage-causes.cause-icon")
							.replace("%cause%", cause.name().toLowerCase(Locale.US))
							.glowingIf(restorerPlayer.getDamageCause() == cause)
							.setPlaceholderObject(restorerPlayer)
							.build();
					return ClickableItem.of(itemstack, event -> {
							restorerPlayer.setDamageCausee(cause);
							new SavesInventory().open(player);
							new SoundPlayer(instance, "click").playTo(player);
						});
				})
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
		contents.set(5, 4, ClickableItem.of(new ItemStackBuilder(instance, "inventories.damage-causes.back").build(),
				e -> {
					new SavesInventory().open(player);
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
				.title(new MessageBuilder(instance, false, "inventories.damage-causes.title")
						.replace("%player%", player.getName())
						.fromConfiguration(inventories)
						.setPlaceholderObject(player)
						.get())
				.manager(SourRestorer.getInventoryManager())
				.provider(this)
				.id("damage-causes-inventory")
				.size(6, 9)
				.build();
	}

}
