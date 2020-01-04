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
import com.sitrica.restorer.managers.PlayerManager;
import com.sitrica.restorer.managers.SaveManager;
import com.sitrica.restorer.objects.OfflineSave;
import com.sitrica.restorer.objects.RestorerPlayer;

public class SaveCommand extends AdminCommand {

	public SaveCommand(SourPlugin instance) {
		super(instance, true, "s", "save");
	}

	@Override
	protected ReturnType runCommand(String command, CommandSender sender, String... arguments) {
		if (arguments.length >= 2 || arguments.length == 0)
			return ReturnType.SYNTAX_ERROR;
		OfflinePlayer target = DeprecationUtils.getSkullOwner(arguments[0]);
		if (target == null) {
			new MessageBuilder(instance, "commands.save.no-player-found")
					.replace("%input%", arguments[0])
					.setPlaceholderObject(sender)
					.send(sender);
			return ReturnType.FAILURE;
		}
		SourRestorer instance = SourRestorer.getInstance();
		Optional<RestorerPlayer> restorerPlayer = instance.getManager(PlayerManager.class).getRestorerPlayer(target);
		if (!restorerPlayer.isPresent()) {
			new MessageBuilder(instance, "commands.save.never-played")
					.replace("%input%", arguments[0])
					.setPlaceholderObject(sender)
					.send(sender);
			return ReturnType.FAILURE;
		}
		Optional<Player> player = restorerPlayer.get().getPlayer();
		SaveManager saveManager = instance.getManager(SaveManager.class);
		if (!player.isPresent()) {
			OfflineSave offline = restorerPlayer.get().getOfflineSave();
			saveManager.addInventorySave(target.getUniqueId(), "MANUAL", offline.getLogoutLocation(), offline.getArmourSave(), offline.getContents());
		} else {
			saveManager.addInventorySave(player.get(), "MANUAL");
		}
		new MessageBuilder(instance, "commands.save.saved")
				.setPlaceholderObject(restorerPlayer.get())
				.send(sender);
		return ReturnType.SUCCESS;
	}

	@Override
	public String getConfigurationNode() {
		return "save";
	}

	@Override
	public String[] getPermissionNodes() {
		return new String[] {"sourrestorer.save", "sourrestorer.admin"};
	}

}
