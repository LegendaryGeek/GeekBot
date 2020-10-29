package bot.events;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.util.TimerTask;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.MalformedJsonException;

import bot.GeekBot;
import bot.commands.Minecraft;
import net.dv8tion.jda.api.entities.TextChannel;

public class MinecraftUpdateEvent extends TimerTask {
	static Logger log = LogManager.getLogger(MinecraftUpdateEvent.class);

	TextChannel channel[];

	Gson gson = new GsonBuilder().setPrettyPrinting().create();

	File BotPath = new File("/GeekBot");

	File minecraftVerionJson = new File("/GeekBot/MinecraftVersionManifest.json");

	@Override
	public void run() {
		String oldRelease = "";
		String oldSnapshot = "";
		String newRelease = "";
		String newSnapshot = "";

		try (BufferedReader data = Files.newBufferedReader(minecraftVerionJson.toPath())) {

			final JsonElement json = JsonParser.parseReader(data);

			if (!json.isJsonObject()) {
				throw new MalformedJsonException("Not a JsonObject");
			}
			final JsonObject root = json.getAsJsonObject();
			final JsonObject latest = root.get("latest").getAsJsonObject();

			oldRelease = latest.get(Minecraft.RELEASE).getAsString();
			oldSnapshot = latest.get(Minecraft.SNAPSHOT).getAsString();
			log.info("Old Release: {}", oldRelease);
			log.info("Old Snapshot: {}", oldSnapshot);

		} catch (Exception e) {
			log.catching(Level.ERROR, e);
		}
		try {
			URL minecraftVersionUrl = new URL("https://launchermeta.mojang.com/mc/game/version_manifest.json");
			BotPath.mkdir();
			if (!minecraftVerionJson.exists()) {
				minecraftVerionJson.createNewFile();
			}
			FileUtils.copyURLToFile(minecraftVersionUrl, minecraftVerionJson);

		} catch (MalformedURLException e1) {
			log.catching(e1);
		} catch (IOException e) {
			log.catching(e);
		}

		try (BufferedReader data = Files.newBufferedReader(minecraftVerionJson.toPath())) {

			final JsonElement json = JsonParser.parseReader(data);

			if (!json.isJsonObject()) {
				throw new MalformedJsonException("Not a JsonObject");
			}
			final JsonObject root = json.getAsJsonObject();
			final JsonObject latest = root.get("latest").getAsJsonObject();

			newRelease = latest.get(Minecraft.RELEASE).getAsString();
			newSnapshot = latest.get(Minecraft.SNAPSHOT).getAsString();

			log.info("New Release: {}", newRelease);
			log.info("New Snapshot: {}", newSnapshot);
		} catch (Exception e) {
			log.catching(Level.ERROR, e);
		}
		if (!oldRelease.equals(newRelease)) {
			// mchan.createMessage("New Release Version of Minecraft is out! Version: " +
			// newRelease).block();
		}
		if (!oldSnapshot.equals(newSnapshot)) {
			// mchan.createMessage("New Snapshot Version of Minecraft is out! Version: " +
			// newSnapshot).block();
		}
	}

}
