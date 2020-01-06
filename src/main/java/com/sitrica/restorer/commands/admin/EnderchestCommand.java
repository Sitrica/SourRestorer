package com.sitrica.restorer.commands.admin;

import java.util.Optional;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.sitrica.core.SourPlugin;
import com.sitrica.core.command.AdminCommand;
import com.sitrica.core.messaging.MessageBuilder;
import com.sitrica.core.utils.DeprecationUtils;
import com.sitrica.restorer.SourRestorer;
import com.sitrica.restorer.inventories.EnderchestEditor;
import com.sitrica.restorer.managers.PlayerManager;
import com.sitrica.restorer.objects.RestorerPlayer;

public class EnderchestCommand extends AdminCommand {

	public EnderchestCommand(SourPlugin instance) {
		super(instance, false, "e", "ec", "enderchest");
	}

	@Override
	protected ReturnType runCommand(String command, CommandSender sender, String... arguments) {
		Player player = (Player) sender;
		if (arguments.length >= 2 || arguments.length == 0)
			return ReturnType.SYNTAX_ERROR;
		OfflinePlayer target = DeprecationUtils.getSkullOwner(arguments[0]);
		if (target == null) {
			new MessageBuilder(instance, "commands.enderchest.no-player-found")
					.replace("%input%", arguments[0])
					.setPlaceholderObject(player)
					.send(player);
			return ReturnType.FAILURE;
		}
		Optional<RestorerPlayer> restorerPlayer = SourRestorer.getInstance().getManager(PlayerManager.class).getRestorerPlayer(target);
		if (!restorerPlayer.isPresent()) {
			new MessageBuilder(instance, "commands.enderchest.never-played")
					.replace("%input%", arguments[0])
					.setPlaceholderObject(player)
					.send(player);
			return ReturnType.FAILURE;
		}
		new EnderchestEditor(player, restorerPlayer.get());
		return ReturnType.SUCCESS;
	}

	@Override
	public String getConfigurationNode() {
		return "enderchest";
	}

	@Override
	public String[] getPermissionNodes() {
		return new String[] {"sourrestorer.enderchest", "sourrestorer.admin"};
	}

}
