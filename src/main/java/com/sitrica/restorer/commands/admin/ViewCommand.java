package com.sitrica.restorer.commands.admin;

import java.util.List;
import java.util.Optional;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.sitrica.core.SourPlugin;
import com.sitrica.core.command.AdminCommand;
import com.sitrica.core.messaging.MessageBuilder;
import com.sitrica.core.utils.DeprecationUtils;
import com.sitrica.restorer.SourRestorer;
import com.sitrica.restorer.inventories.SavesInventory;
import com.sitrica.restorer.managers.PlayerManager;
import com.sitrica.restorer.objects.InventorySave;
import com.sitrica.restorer.objects.RestorerPlayer;

public class ViewCommand extends AdminCommand {

	public ViewCommand(SourPlugin instance) {
		super(instance, false, "view", "inventory", "restore");
	}

	@Override
	protected ReturnType runCommand(String command, CommandSender sender, String... arguments) {
		Player player = (Player) sender;
		if (!player.hasPermission("sourrestorer.use")) {
			new MessageBuilder(instance, "messages.no-permission")
					.setPlaceholderObject(player)
					.send(player);
			return ReturnType.FAILURE;
		}
		if (arguments.length >= 2 || arguments.length == 0)
			return ReturnType.SYNTAX_ERROR;
		OfflinePlayer target = DeprecationUtils.getSkullOwner(arguments[0]);
		if (target == null) {
			new MessageBuilder(instance, "commands.restore.no-player-found")
					.replace("%input%", arguments[0])
					.setPlaceholderObject(player)
					.send(player);
			return ReturnType.FAILURE;
		}
		Optional<RestorerPlayer> restorerPlayer = SourRestorer.getInstance().getManager(PlayerManager.class).getRestorerPlayer(target);
		if (!restorerPlayer.isPresent()) {
			new MessageBuilder(instance, "commands.restore.never-played")
					.replace("%input%", arguments[0])
					.setPlaceholderObject(player)
					.send(player);
			return ReturnType.FAILURE;
		}
		List<InventorySave> saves = restorerPlayer.get().getInventorySaves();
		if (saves.size() <= 0) {
			new MessageBuilder(instance, "commands.restore.no-saves")
					.replace("%input%", arguments[0])
					.setPlaceholderObject(player)
					.send(player);
			return ReturnType.FAILURE;
		}
		new SavesInventory(restorerPlayer.get()).open(player);
		return ReturnType.SUCCESS;
	}

	@Override
	public String getConfigurationNode() {
		return "restore";
	}

	@Override
	public String[] getPermissionNodes() {
		return new String[] {"sourrestorer.restore", "sourrestorer.admin"};
	}

}
