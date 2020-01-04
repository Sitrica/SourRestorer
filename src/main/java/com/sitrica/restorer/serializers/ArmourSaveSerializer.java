package com.sitrica.restorer.serializers;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.inventory.ItemStack;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.sitrica.core.database.Serializer;
import com.sitrica.restorer.objects.ArmourSave;

public class ArmourSaveSerializer implements Serializer<ArmourSave> {

	@Override
	public JsonElement serialize(ArmourSave save, Type typeOfSrc, JsonSerializationContext context) {
		JsonObject json = new JsonObject();
		if (save == null)
			return json;
		json.add("helmet", context.serialize(save.getHelmet(), ItemStack.class));
		json.add("chestplate", context.serialize(save.getChestplate(), ItemStack.class));
		json.add("leggings", context.serialize(save.getLeggings(), ItemStack.class));
		json.add("boots", context.serialize(save.getBoots(), ItemStack.class));
		JsonArray extra = new JsonArray();
		Arrays.stream(save.getExtraContents()).forEach(itemstack -> extra.add(context.serialize(itemstack, ItemStack.class)));
		json.add("extra", extra);
		return json;
	}

	@Override
	public ArmourSave deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
		JsonObject object = json.getAsJsonObject();
		JsonElement extraElement = object.get("extra");
		List<ItemStack> extra = new ArrayList<>();
		if (extraElement != null && !extraElement.isJsonNull() && extraElement.isJsonArray()) {
			JsonArray array = extraElement.getAsJsonArray();
			array.forEach(element -> {
				ItemStack itemstack = context.deserialize(element, ItemStack.class);
				if (itemstack == null)
					return;
				extra.add(itemstack);
			});
		}
		JsonElement helmetElement = object.get("helmet");
		if (helmetElement == null)
			return null;
		ItemStack helmet = context.deserialize(helmetElement, ItemStack.class);
		JsonElement chestplateElement = object.get("chestplate");
		if (chestplateElement == null)
			return null;
		ItemStack chestplate = context.deserialize(chestplateElement, ItemStack.class);
		JsonElement leggingsElement = object.get("leggings");
		if (leggingsElement == null)
			return null;
		ItemStack leggings = context.deserialize(leggingsElement, ItemStack.class);
		JsonElement bootsElement = object.get("boots");
		if (bootsElement == null)
			return null;
		ItemStack boots = context.deserialize(bootsElement, ItemStack.class);
		return new ArmourSave(helmet, chestplate, leggings, boots, extra.toArray(new ItemStack[extra.size()]));
	}

}
