package com.sitrica.restorer.placeholders;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.sitrica.core.SourPlugin;
import com.sitrica.core.messaging.MessageBuilder;
import com.sitrica.core.placeholders.Placeholder;
import com.sitrica.core.placeholders.Placeholders;
import com.sitrica.restorer.managers.PlayerManager;
import com.sitrica.restorer.objects.InventorySave;
import com.sitrica.restorer.objects.RestorerPlayer;

public class DefaultPlaceholders {

	public static void register(SourPlugin instance) {

		// InventorySave
		Placeholders.registerPlaceholder(new Placeholder<InventorySave>("%timestamp%", "%date%", "%time%") {
			@Override
			public String replace(InventorySave save) {
				Date date = new Date(save.getTimestamp());
				String pattern = instance.getConfig().getString("date-format", "MM/dd/yyyy HH:mm:ss");
				return new SimpleDateFormat(pattern).format(date);
			}
		});
		Placeholders.registerPlaceholder(new Placeholder<InventorySave>("%cause%", "%damagecause%", "%reason%") {
			@Override
			public String replace(InventorySave save) {
				return save.getReason().toLowerCase();
			}
		});
		Placeholders.registerPlaceholder(new Placeholder<InventorySave>("%location%") {
			@Override
			public String replace(InventorySave save) {
				Location location = save.getDeathLocation();
				return location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ() + ", " + location.getWorld().getName();
			}
		});
		Placeholders.registerPlaceholder(new Placeholder<InventorySave>("%player%") {
			@Override
			public String replace(InventorySave save) {
				Optional<RestorerPlayer> restorerPlayer = instance.getManager(PlayerManager.class).getRestorerPlayer(save.getOwnerUUID());
				if (!restorerPlayer.isPresent())
					return new MessageBuilder(instance, "messages.not-player").get();
				Optional<Player> player = restorerPlayer.get().getPlayer();
				if (player.isPresent())
					return player.get().getName();
				return new MessageBuilder(instance, "messages.not-player").get();
			}
		});

		// RestorerPlayer
		Placeholders.registerPlaceholder(new Placeholder<RestorerPlayer>("%player%") {
			@Override
			public String replace(RestorerPlayer restorerPlayer) {
				Optional<Player> player = restorerPlayer.getPlayer();
				if (player.isPresent())
					return player.get().getName();
				return new MessageBuilder(instance, "messages.not-player").get();
			}
		});
	}

}
