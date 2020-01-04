package com.sitrica.restorer.serializers;

import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.sitrica.core.database.Serializer;
import com.sitrica.restorer.objects.InventorySave;
import com.sitrica.restorer.objects.OfflineSave;
import com.sitrica.restorer.objects.RestorerPlayer;

public class RestorerPlayerSerializer implements Serializer<RestorerPlayer> {

	@Override
	public JsonElement serialize(RestorerPlayer player, Type typeOfSrc, JsonSerializationContext context) {
		JsonObject json = new JsonObject();
		if (player == null)
			return json;
		json.addProperty("uuid", player.getUniqueId() + "");
		JsonArray saves = new JsonArray();
		player.getInventorySaves().forEach(save -> saves.add(context.serialize(save, InventorySave.class)));
		json.add("saves", saves);
		json.add("save", context.serialize(player.getOnlineLoad(), InventorySave.class));
		json.add("offline", context.serialize(player.getOfflineSave(), OfflineSave.class));
		return json;
	}

	@Override
	public RestorerPlayer deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
		JsonObject object = json.getAsJsonObject();
		String stringUUID = object.get("uuid").getAsString();
		if (stringUUID == null)
			return null;
		UUID uuid = UUID.fromString(stringUUID);
		if (uuid == null)
			return null;
		JsonElement savesElement = object.get("saves");
		Set<InventorySave> saves = new HashSet<>();
		if (savesElement != null && !savesElement.isJsonNull() && savesElement.isJsonArray()) {
			JsonArray array = savesElement.getAsJsonArray();
			array.forEach(element -> {
				InventorySave save = context.deserialize(element, InventorySave.class);
				if (save == null)
					return;
				saves.add(save);
			});
		}
		RestorerPlayer player = new RestorerPlayer(uuid, saves);
		JsonElement saveElement = object.get("save");
		if (saveElement != null) {
			InventorySave save = context.deserialize(saveElement, InventorySave.class);
			if (save != null)
				player.setOnlineLoad(save);
		}
		JsonElement offlineSaveElement = object.get("offline");
		if (offlineSaveElement != null) {
			OfflineSave offline = context.deserialize(offlineSaveElement, OfflineSave.class);
			player.setOfflineSave(offline);
		}
		return player;
	}

}
