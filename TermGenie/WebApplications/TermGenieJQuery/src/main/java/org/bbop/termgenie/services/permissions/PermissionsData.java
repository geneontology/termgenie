package org.bbop.termgenie.services.permissions;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

class PermissionsData {
	
	private static final Logger logger = Logger.getLogger(PermissionsData.class);

	private static final GsonBuilder gsonBuilder = new GsonBuilder();
	static {
		gsonBuilder.registerTypeAdapter(PermissionsData.class, new PermissionsDataHandler());
		gsonBuilder.registerTypeAdapter(TermGeniePermissions.class, new TermGeniePermissionsHandler());
	}
	private static final Gson gson = gsonBuilder.create();

	final Map<String, Map<String, TermGeniePermissions>> userPermissions;

	PermissionsData() {
		userPermissions = new HashMap<String, Map<String, TermGeniePermissions>>();
	}

	TermGeniePermissions getPermissions(String guid, String application) {
		if (guid != null && application != null) {
			Map<String, TermGeniePermissions> applicationPermissions = userPermissions.get(guid);
			if (applicationPermissions != null) {
				return applicationPermissions.get(application);
			}
		}
		return null;
	}

	static PermissionsData loadFromJson(String jsonString) {
		try {
			return gson.fromJson(jsonString, PermissionsData.class);
		} catch (JsonParseException exception) {
			logger.error("Could not parse jsonString: "+jsonString, exception);
		}
		return null;
	}

	static String writeToJson(PermissionsData data) {
		return gson.toJson(data);
	}

	static class TermGeniePermissions {
	
		final Map<String, Map<String, String>> ontologyPermissions;
	
		TermGeniePermissions() {
			ontologyPermissions = new HashMap<String, Map<String, String>>();
		}
	
		Map<String, String> getPermissionFlags(String ontologyName) {
			Map<String, String> flags = ontologyPermissions.get(ontologyName);
			if (flags != null) {
				return Collections.unmodifiableMap(flags);
			}
			return null;
		}
	}

	private static class PermissionsDataHandler implements
			JsonSerializer<PermissionsData>,
			JsonDeserializer<PermissionsData>
	{
	
		@Override
		public PermissionsData deserialize(JsonElement json,
				Type typeOfT,
				JsonDeserializationContext context) throws JsonParseException
		{
			PermissionsData data = new PermissionsData();
			JsonObject jsonObject = json.getAsJsonObject();
			for(Entry<String, JsonElement> entry :jsonObject.entrySet()) {
				Map<String, TermGeniePermissions> applicationsMap = new HashMap<String, TermGeniePermissions>();
				data.userPermissions.put(entry.getKey(), applicationsMap);
				JsonObject applicationsJsonObject = entry.getValue().getAsJsonObject();
				for(Entry<String, JsonElement> applicationsEntry : applicationsJsonObject.entrySet()) {
					String application = applicationsEntry.getKey();
					TermGeniePermissions permissions = context.deserialize(applicationsEntry.getValue(), TermGeniePermissions.class);
					applicationsMap.put(application, permissions);
				}
			}
			return data;
		}
	
		@Override
		public JsonElement serialize(PermissionsData src,
				Type typeOfSrc,
				JsonSerializationContext context)
		{
			return context.serialize(src.userPermissions, src.userPermissions.getClass());
		}
	
	}

	private static class TermGeniePermissionsHandler implements
		JsonSerializer<TermGeniePermissions>,
		JsonDeserializer<TermGeniePermissions>
	{
	
		@Override
		public TermGeniePermissions deserialize(JsonElement json,
				Type typeOfT,
				JsonDeserializationContext context) throws JsonParseException
		{
			TermGeniePermissions termGeniePermissions = new TermGeniePermissions();
			JsonObject jsonObject = json.getAsJsonObject();
			for(Entry<String, JsonElement> entry :jsonObject.entrySet()) {
				Map<String, String> flags = new HashMap<String, String>();
				termGeniePermissions.ontologyPermissions.put(entry.getKey(), flags);
				JsonObject flagsJsonObject = entry.getValue().getAsJsonObject();
				for(Entry<String, JsonElement> flagsEntry : flagsJsonObject.entrySet()) {
					flags.put(flagsEntry.getKey(), flagsEntry.getValue().getAsString());
				}
			}
			return termGeniePermissions;
		}
	
		@Override
		public JsonElement serialize(TermGeniePermissions src,
				Type typeOfSrc,
				JsonSerializationContext context)
		{
			return context.serialize(src.ontologyPermissions, src.ontologyPermissions.getClass());
		}
		
	}
}
