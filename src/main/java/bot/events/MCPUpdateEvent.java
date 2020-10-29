package bot.events;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.TimerTask;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import bot.GeekBot;
import bot.commands.Minecraft;
import bot.json.models.ForgeMapping;

public class MCPUpdateEvent extends TimerTask {
	static Logger log = LogManager.getLogger(MCPUpdateEvent.class);

	// Channel channel =
	// GeekBot.getClient().getGuildById(Snowflake.of("632708637122625538")).block()
	// .getChannelById(Snowflake.of("637651124530446366")).block();
	// MessageChannel mchan = (MessageChannel) channel;

	Gson gson = new GsonBuilder().setPrettyPrinting().create();

	File BotPath = new File("/GeekBot");
	
	File mcpVerionJson = new File("/GeekBot/MCPVersionManifest.json");

	@Override
	public void run() {
		String oldRelease = "";
		String oldSnapshot = "";
		String newRelease = "";
		String newSnapshot = "";

		// -+-+-+- BEFORE CHANGING FILE -+-+-+-
		try {
			Minecraft.ForgeStatus();
		} catch (IOException e2) {
			log.catching(e2);
		}

		int listSize = Minecraft.mappingsList.size();
		ForgeMapping forgeMappingList = Minecraft.mappingsList.get(listSize - 1);
		if (forgeMappingList.getStable().length - 1 >= 1) {
			oldRelease = Integer.toString(forgeMappingList.getStable()[forgeMappingList.getStable().length - 1]);
		}
		if (forgeMappingList.getSnapshot().length - 1 >= 1) {
			oldSnapshot = Integer.toString(forgeMappingList.getSnapshot()[0]);
		}

		log.info("Old Release: {}", oldRelease);
		log.info("Old Snapshot: {}", oldSnapshot);

		// -+-+-+- CHANGE FILE -+-+-+-

		try {
			URL minecraftVersionUrl = new URL("https://files.minecraftforge.net/maven/de/oceanlabs/mcp/versions.json");
			BotPath.mkdirs();
			if (!mcpVerionJson.exists()) {
				mcpVerionJson.createNewFile();
			}
			FileUtils.copyURLToFile(minecraftVersionUrl, mcpVerionJson);

		} catch (MalformedURLException e1) {
			log.catching(e1);
		} catch (IOException e) {
			log.catching(e);
		}

		// -+-+-+- AFTER CHANGING FILE -+-+-+-
		try {
			Minecraft.ForgeStatus();
		} catch (IOException e2) {
			log.catching(e2);
		}

		if (forgeMappingList.getStable().length - 1 >= 1) {
			newRelease = Integer.toString(forgeMappingList.getStable()[forgeMappingList.getStable().length - 1]);
		}
		if (forgeMappingList.getSnapshot().length - 1 >= 1) {
			newSnapshot = Integer.toString(forgeMappingList.getSnapshot()[0]);
		}

		log.info("New Release: {}", newRelease);
		log.info("New Snapshot: {}", newSnapshot);

		if (!oldRelease.equals(newRelease)) {
			// mchan.createMessage("New Stable Version of MCP Mappings is out! Version: " +
			// newRelease);
		}
		if (!oldSnapshot.equals(newSnapshot)) {
			// mchan.createMessage("New Snapshot Version of MCP Mappings is out! Version: "
			// + newSnapshot);
		}
	}

}
