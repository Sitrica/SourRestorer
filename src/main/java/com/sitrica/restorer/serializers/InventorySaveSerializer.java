package com.sitrica.restorer.serializers;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.sitrica.core.database.Serializer;
import com.sitrica.core.utils.IntervalUtils;
import com.sitrica.restorer.SourRestorer;
import com.sitrica.restorer.managers.SaveManager;
import com.sitrica.restorer.objects.ArmourSave;
import com.sitrica.restorer.objects.InventorySave;

public class InventorySaveSerializer implements Serializer<InventorySave> {

	@Override
	public JsonElement serialize(InventorySave save, Type typeOfSrc, JsonSerializationContext context) {
		JsonObject json = new JsonObject();
		if (save == null)
			return json;
		json.add("location", context.serialize(save.getDeathLocation(), Location.class));
		json.addProperty("stared", save.isStared());
		json.addProperty("reason", save.getReason());
		json.addProperty("timestamp", save.getTimestamp());
		json.addProperty("uuid", save.getOwnerUUID() + "");
		JsonArray contents = new JsonArray();
		Arrays.stream(save.getContents()).forEach(itemstack -> contents.add(context.serialize(itemstack, ItemStack.class)));
		json.add("contents", contents);
		JsonArray log = new JsonArray();
		save.getRestoreLog().entrySet().forEach(entry -> {
			JsonObject logJson = new JsonObject();
			logJson.addProperty("uuid", entry.getKey() + "");
			logJson.addProperty("timestamp", entry.getValue());
			log.add(logJson);
		});
		json.add("log", log);
		json.add("armour", context.serialize(save.getArmourSave(), ArmourSave.class));
		return json;
	}

	@Override
	public InventorySave deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
		JsonObject object = json.getAsJsonObject();
		String stringUUID = object.get("uuid").getAsString();
		if (stringUUID == null)
			return null;
		UUID uuid = UUID.fromString(stringUUID);
		if (uuid == null)
			return null;
		JsonElement locationElement = object.get("location");
		if (locationElement == null)
			return null;
		Location location = context.deserialize(locationElement, Location.class);
		if (location == null)
			return null;
		JsonElement timestampElement = object.get("timestamp");
		if (timestampElement == null)
			return null;
		long timestamp = timestampElement.getAsLong();
		JsonElement reasonElement = object.get("reason");
		if (reasonElement == null)
			return null;
		String reason = reasonElement.getAsString();
		SourRestorer.getInstance().getManager(SaveManager.class).addReason(reason);
		JsonElement contentsElement = object.get("contents");
		List<ItemStack> contents = new ArrayList<>();
		if (contentsElement != null && !contentsElement.isJsonNull() && contentsElement.isJsonArray()) {
			JsonArray array = contentsElement.getAsJsonArray();
			array.forEach(element -> {
				ItemStack itemstack = context.deserialize(element, ItemStack.class);
				if (itemstack == null)
					return;
				contents.add(itemstack);
			});
		}
		JsonElement logElement = object.get("log");
		Map<UUID, Long> log = new HashMap<>();
		if (logElement != null && !logElement.isJsonNull() && logElement.isJsonArray()) {
			JsonArray array = logElement.getAsJsonArray();
			array.forEach(element -> {
				JsonObject logObject = element.getAsJsonObject();
				String logStringUUID = logObject.get("uuid").getAsString();
				if (logStringUUID == null)
					return;
				UUID logUuid = UUID.fromString(logStringUUID);
				if (logUuid == null)
					return;
				JsonElement logTimestampElement = object.get("timestamp");
				if (logTimestampElement == null)
					return;
				long logTimestamp = logTimestampElement.getAsLong();
				log.put(logUuid, logTimestamp);
			});
		}
		JsonElement armourElement = object.get("armour");
		if (armourElement == null)
			return null;
		ArmourSave armour = context.deserialize(armourElement, ArmourSave.class);
		InventorySave save = new InventorySave(timestamp, uuid, reason, location, armour, contents.toArray(new ItemStack[contents.size()]));
		save.addAllRestoreLog(log);
		JsonElement staredElement = object.get("stared");
		if (staredElement != null)
			save.setStared(staredElement.getAsBoolean());

		// Loaded deletion check.
		SourRestorer instance = SourRestorer.getInstance();
		FileConfiguration configuration = instance.getConfig();
		if (configuration.getBoolean("delete-system.time.enabled", false)) {
			if (configuration.getBoolean("delete-system.time.only-when-loaded", true)) {
				String after = configuration.getString("delete-system.time.after", "30 days");
				long milliseconds = IntervalUtils.getMilliseconds(after);
				if (System.currentTimeMillis() - save.getTimestamp() > milliseconds)
					return null;
			}
		}
		return save;
	}

}
