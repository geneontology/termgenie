package org.bbop.termgenie.permissions;

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

class ComplexPermissionsData {

	private static final Logger logger = Logger.getLogger(ComplexPermissionsData.class);

	private static final GsonBuilder gsonBuilder = new GsonBuilder();
	static {
		gsonBuilder.registerTypeAdapter(ComplexPermissionsData.class, new PermissionsDataHandler());
		gsonBuilder.registerTypeAdapter(TermGeniePermissions.class,
				new TermGeniePermissionsHandler());
	}
	private static final Gson gson = gsonBuilder.create();

	final Map<String, Map<String, TermGeniePermissions>> userPermissions;

	ComplexPermissionsData() {
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

	static ComplexPermissionsData loadFromJson(String jsonString) {
		try {
			return gson.fromJson(jsonString, ComplexPermissionsData.class);
		} catch (JsonParseException exception) {
			logger.error("Could not parse jsonString: " + jsonString, exception);
		}
		return null;
	}

	static String writeToJson(ComplexPermissionsData data) {
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

		static TermGeniePermissions fromPermissionsString(String string) {
			TermGeniePermissions permissions = new TermGeniePermissions();
			if (string != null && string.length() > 2) {
				string = string.trim();
				for (int start = pos('(', 0, string); start >= 0;) {
					int end = pos(')', start, string);
					if (end < 0) {
						break;
					}
					if ((start + 1) < (end)) {
						parseStanza(string.substring(start + 1, end), permissions);
					}
					start = pos('(', end, string);
				}
			}
			return permissions;
		}

		static int pos(char c, int offset, String s) {
			int pos = s.indexOf(c, offset);
			if (pos > 0) {
				char before = s.charAt(pos - 1);
				if (before == '\\') {
					pos = s.indexOf(c, pos + 1);
				}
			}
			return pos;
		}

		private static void parseStanza(String stanza, TermGeniePermissions permissions) {
			String[] split = stanza.split(",");
			if (split != null && split.length == 3) {
				String ontology = unescape(split[0].trim());
				String flagKey = unescape(split[1].trim());
				String flagValue = unescape(split[2].trim());
				permissions.addPermissions(ontology, flagKey, flagValue);
			}
			else {
				logger.warn("Could not parse permission: " + stanza);
			}
		}

		void addPermissions(String ontology, String flagKey, String flagValue) {
			Map<String, String> flags = ontologyPermissions.get(ontology);
			if (flags == null) {
				flags = new HashMap<String, String>();
				ontologyPermissions.put(ontology, flags);
			}
			flags.put(flagKey, flagValue);
		}

		static String toPermissionsString(TermGeniePermissions permissions) {
			if (permissions != null && !permissions.ontologyPermissions.isEmpty()) {
				StringBuilder sb = new StringBuilder();
				for (Entry<String, Map<String, String>> entry : permissions.ontologyPermissions.entrySet()) {
					Map<String, String> flags = entry.getValue();
					for (Entry<String, String> flagEntry : flags.entrySet()) {
						if (sb.length() > 0) {
							sb.append(',');
						}
						sb.append('(');
						sb.append(escape(entry.getKey()));
						sb.append(", ");
						sb.append(escape(flagEntry.getKey()));
						sb.append(", ");
						sb.append(escape(flagEntry.getValue()));
						sb.append(')');
					}
				}
				return sb.toString();
			}
			return null;
		}

		static CharSequence escape(String string) {
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < string.length(); i++) {
				char c = string.charAt(i);
				if (c == '(' || c == ')' || c == ',' || c == '\\') {
					sb.append('\\');
				}
				sb.append(c);
			}
			return sb;
		}

		static String unescape(String string) {
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < string.length(); i++) {
				char c = string.charAt(i);
				if (c != '\\') {
					sb.append(c);
				}
			}
			return sb.toString();
		}
	}

	private static class PermissionsDataHandler implements
			JsonSerializer<ComplexPermissionsData>,
			JsonDeserializer<ComplexPermissionsData>
	{

		@Override
		public ComplexPermissionsData deserialize(JsonElement json,
				Type typeOfT,
				JsonDeserializationContext context) throws JsonParseException
		{
			ComplexPermissionsData data = new ComplexPermissionsData();
			JsonObject jsonObject = json.getAsJsonObject();
			for (Entry<String, JsonElement> entry : jsonObject.entrySet()) {
				Map<String, TermGeniePermissions> applicationsMap = new HashMap<String, TermGeniePermissions>();
				data.userPermissions.put(entry.getKey(), applicationsMap);
				JsonObject applicationsJsonObject = entry.getValue().getAsJsonObject();
				for (Entry<String, JsonElement> applicationsEntry : applicationsJsonObject.entrySet()) {
					String application = applicationsEntry.getKey();
					TermGeniePermissions permissions = context.deserialize(applicationsEntry.getValue(),
							TermGeniePermissions.class);
					applicationsMap.put(application, permissions);
				}
			}
			return data;
		}

		@Override
		public JsonElement serialize(ComplexPermissionsData src,
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
			return TermGeniePermissions.fromPermissionsString(json.getAsString());
		}

		@Override
		public JsonElement serialize(TermGeniePermissions src,
				Type typeOfSrc,
				JsonSerializationContext context)
		{
			return context.serialize(TermGeniePermissions.toPermissionsString(src), String.class);
		}

	}
}
