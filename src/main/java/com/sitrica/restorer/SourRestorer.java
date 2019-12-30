package com.sitrica.restorer;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.ServicePriority;

import com.sitrica.core.SourPlugin;
import com.sitrica.core.command.CommandHandler;
import com.sitrica.core.manager.ExternalManager;
import com.sitrica.core.manager.Manager;
import com.sitrica.core.manager.ManagerHandler;
import com.sitrica.restorer.commands.SourRestorerCommand;
import com.sitrica.restorer.placeholders.DefaultPlaceholders;

import fr.minuskube.inv.InventoryManager;

public class SourRestorer extends SourPlugin {

	private final Map<String, FileConfiguration> configurations = new HashMap<>();
	private final static String packageName = "com.sitrica.restorer";
	private static InventoryManager inventoryManager;
	private CommandHandler commandHandler;
	private ManagerHandler managerHandler;
	private static SourRestorer instance;
	private SourRestorerAPI API;

	public SourRestorer() {
		super("&7[&aSourRestorer&7]&r ", packageName + ".managers");
	}

	@Override
	public void onEnable() {
		instance = this;
		File configFile = new File(getDataFolder(), "config.yml");
		//If newer version was found, update configuration.
		if (!getDescription().getVersion().equals(getConfig().getString("version", getDescription().getVersion()))) {
			if (configFile.exists())
				configFile.delete();
		}
		//Create all the default files.
		for (String name : Arrays.asList("config", "messages", "sounds", "inventories")) {
			File file = new File(getDataFolder(), name + ".yml");
			if (!file.exists()) {
				file.getParentFile().mkdirs();
				saveResource(file.getName(), false);
				debugMessage("Created new default file " + file.getName());
			}
			FileConfiguration configuration = new YamlConfiguration();
			try {
				configuration.load(file);
				configurations.put(name, configuration);
			} catch (IOException | InvalidConfigurationException e) {
				e.printStackTrace();
			}
		}
		managerHandler = new ManagerHandler(this);
		inventoryManager = new InventoryManager(this);
		inventoryManager.init();
		commandHandler = new CommandHandler(this, SourRestorerCommand.class, packageName + ".commands");
		getCommand("sourrestorer").setExecutor(commandHandler);
		DefaultPlaceholders.register(this);
		API = new SourRestorerAPI(this);
		Bukkit.getServicesManager().register(SourRestorerAPI.class, API, this, ServicePriority.Normal);
		consoleMessage("has been enabled!");
	}

	public <T extends ExternalManager> T getExternalManager(Class<T> expected) {
		return managerHandler.getExternalManager(expected);
	}

	public static InventoryManager getInventoryManager() {
		return inventoryManager;
	}

	/**
	 * Grab a FileConfiguration if found.
	 * Call it without it's file extension, just the simple name of the file.
	 * 
	 * @param configuration The name of the configuration to search for.
	 * @return Optional<FileConfiguration> as the file may or may not exist.
	 */
	@Override
	public Optional<FileConfiguration> getConfiguration(String configuration) {
		return Optional.ofNullable(configurations.get(configuration));
	}

	/**
	 * Grab a Manager by it's class and create it if not present.
	 * 
	 * @param <T> <T extends Manager>
	 * @param expected The expected Class that extends Manager.
	 * @return The Manager that matches the defined class.
	 */
	@Override
	public <T extends Manager> T getManager(Class<T> expected) {
		return managerHandler.getManager(expected);
	}

	/**
	 * @return The CommandManager allocated to the plugin.
	 */
	@Override
	public CommandHandler getCommandHandler() {
		return commandHandler;
	}

	public ManagerHandler getManagerHandler() {
		return managerHandler;
	}

	public static SourRestorer getInstance() {
		return instance;
	}

	public List<Manager> getManagers() {
		return managerHandler.getManagers();
	}

	public String getPackageName() {
		return packageName;
	}

}
