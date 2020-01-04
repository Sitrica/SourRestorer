package com.sitrica.restorer.serializers;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.sitrica.core.database.Serializer;
import com.sitrica.restorer.objects.ArmourSave;
import com.sitrica.restorer.objects.OfflineSave;

public class OfflineSaveSerializer implements Serializer<OfflineSave> {

	@Override
	public JsonElement serialize(OfflineSave save, Type typeOfSrc, JsonSerializationContext context) {
		JsonObject json = new JsonObject();
		if (save == null)
			return json;
		json.add("location", context.serialize(save.getLogoutLocation(), Location.class));
		json.addProperty("timestamp", save.getLastLogout());
		JsonArray contents = new JsonArray();
		Arrays.stream(save.getContents()).forEach(itemstack -> contents.add(context.serialize(itemstack, ItemStack.class)));
		json.add("contents", contents);
		JsonArray enderchest = new JsonArray();
		Arrays.stream(save.getEnderchestContents()).forEach(itemstack -> enderchest.add(context.serialize(itemstack, ItemStack.class)));
		json.add("enderchest", enderchest);
		json.add("armour", context.serialize(save.getArmourSave(), ArmourSave.class));
		return json;
	}

	@Override
	public OfflineSave deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
		JsonObject object = json.getAsJsonObject();
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
		JsonElement enderchestElement = object.get("contents");
		List<ItemStack> enderchest = new ArrayList<>();
		if (enderchestElement != null && !enderchestElement.isJsonNull() && enderchestElement.isJsonArray()) {
			JsonArray array = enderchestElement.getAsJsonArray();
			array.forEach(element -> {
				ItemStack itemstack = context.deserialize(element, ItemStack.class);
				if (itemstack == null)
					return;
				enderchest.add(itemstack);
			});
		}
		JsonElement armourElement = object.get("armour");
		if (armourElement == null)
			return null;
		ArmourSave armour = context.deserialize(armourElement, ArmourSave.class);
		return new OfflineSave(timestamp, location, enderchest.toArray(new ItemStack[enderchest.size()]), armour, contents.toArray(new ItemStack[contents.size()]));
	}

}
