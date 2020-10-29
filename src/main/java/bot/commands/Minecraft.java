package bot.commands;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import bot.json.models.ForgeMapping;
import bot.json.models.MinecraftVersion;

public class Minecraft {
	static Logger log = LogManager.getLogger(Minecraft.class);
	public static List<ForgeMapping> mappingsList = new ArrayList<>();
	public static List<MinecraftVersion> versionList = new ArrayList<>();

	// Constants
	public static final String RELEASE = "release";
	public static final String SNAPSHOT = "snapshot";
	public static final String OLDBETA = "old_beta";
	public static final String OLDALPHA = "old_alpha";

	public static String MinecraftVersion() {
		Gson gson = new Gson();
		String message = null;
		File backupJsonVersion = new File("/GeekBot/MinecraftVersionManifest.json");

		try (BufferedReader data = Files.newBufferedReader(backupJsonVersion.toPath())) {

			final JsonElement json = JsonParser.parseReader(data);

			if (!json.isJsonObject()) {
				throw new Exception();
			}
			final JsonObject root = json.getAsJsonObject();
			final JsonObject latest = root.get("latest").getAsJsonObject();
			final JsonArray versions = root.get("versions").getAsJsonArray();

			versions.forEach(action -> {
				MinecraftVersion versioning = gson.fromJson(action.getAsJsonObject(), MinecraftVersion.class);
				versionList.add(versioning);
			});
			message = "latest release is: " + latest.get(RELEASE) + "; " + "the latest snapshot is: "
					+ latest.get(SNAPSHOT) + " <:PixelCreeperBig:665599312134144044>";
		} catch (Exception e) {
			log.catching(Level.ERROR, e);
		}

		return message;
	}

	public static String ForgeStatus() throws IOException {
		Gson gson = new Gson();
		int snapshotVersion = 0;
		int stableVersion = 0;
		String latestMCVersion = "1.12.2";
		StringBuilder message = new StringBuilder("");
		String message2;
		File backupJsonStatus = new File("/GeekBot/MCPVersionManifest.json");

		try (BufferedReader data = Files.newBufferedReader(backupJsonStatus.toPath())) {

			final JsonElement json = JsonParser.parseReader(data);

			if (!json.isJsonObject()) {
				throw new Exception();
			}
			final JsonObject root = json.getAsJsonObject();
			for (Entry<String, JsonElement> entry : root.entrySet()) {
				ForgeMapping mappingobj = gson.fromJson(entry.getValue().toString(), ForgeMapping.class);
				mappingobj.setMCVersion(entry.getKey());
				String[] splitVersion = mappingobj.getMCVersion().split("\\.");
				mappingobj.setMCMajor(Integer.parseInt(splitVersion[0]));
				mappingobj.setMCMinor(Integer.parseInt(splitVersion[1]));
				if (splitVersion.length == 3) {
					mappingobj.setMCPatch(Integer.parseInt(splitVersion[2]));
				}
				mappingsList.add(mappingobj);
			}
			Collections.sort(mappingsList);
			if (versionList.isEmpty()) {
				MinecraftVersion();
			}

		} catch (Exception e) {
			log.catching(Level.ERROR, e);
		}
		int listSize = mappingsList.size();
		for (int i = 0; i < versionList.size(); i++) {

			String version_i = versionList.get(i).getType();
			if (RELEASE.equals(versionList.get(i).getType())) {
				for (int i2 = 0; i2 < listSize; i2++) {
					String version_i2 = mappingsList.get(i2).getMCVersion();
					if (version_i.equals(version_i2)) {
						log.info("minecraft version list id: {}, mcp mappings list id: {};", versionList.get(i).getId(),
								mappingsList.get(i2).getMCVersion());
					}
				}
			}
		}
		ForgeMapping forgeMappingList = mappingsList.get(listSize - 1);
		message.append("latest MCVersion there is mappings for: " + forgeMappingList.getMCVersion());
		if (forgeMappingList.getStable().length - 1 >= 1) {
			message.append(
					"; latest Stable Mapping " + forgeMappingList.getStable()[forgeMappingList.getStable().length - 1]);
		}
		if (forgeMappingList.getSnapshot().length - 1 >= 1) {
			message.append("; latest snapshot Mapping "
					+ forgeMappingList.getSnapshot()[forgeMappingList.getSnapshot().length - 1]);
		}
		if (message.toString().equals("")) {
			message.append("nope, didnt work right kenz");
		}
		return message.toString();
	}

}
