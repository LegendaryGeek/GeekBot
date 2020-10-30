package bot;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import com.github.koraktor.steamcondenser.steam.servers.SourceServer;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.LowLevelHttpRequest;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.jagrosh.jdautilities.command.CommandClient;
import com.jagrosh.jdautilities.command.CommandClientBuilder;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import bot.commands.CmdChironHistory;
import bot.commands.CmdChironJob;
import bot.commands.CmdContribute;
import bot.commands.CmdEnderHistory;
import bot.commands.CmdEnderJob;
import bot.commands.CmdExitVoice;
import bot.commands.CmdHug;
import bot.commands.CmdInvite;
import bot.commands.CmdJoinVoice;
import bot.commands.CmdPing;
import bot.commands.CmdStarBoundRestart;
import bot.commands.CmdStarboundRole;
import bot.commands.CmdStopBot;
import bot.commands.CmdUserInfo;
import bot.events.EventStarboudServerReset;
import bot.events.WelcomeEvent;
import edu.cmu.sphinx.api.Configuration;
import edu.cmu.sphinx.api.StreamSpeechRecognizer;
import net.dv8tion.jda.api.AccountType;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;

public class GeekBot {
	private static String BASEURL = "https://www.googleapis.com/youtube/v3";
	private static String GOOGLE_API_KEY;
	private static String DISCORD_TOKEN;
	private static String DISCORD_ID;
	private static String DISCORD_SECRET;
	private static String YOUTUBE_ID;
	private static String OWNER_ID;
	private static String LABRINTH_ID;
	private static String LABUPDATE;
	private static String CHIRON_KEY;
	private static String CHIRON_URL;
	private static String MINDUSTRY_URL;
	private static String DATABASE_URL;
	private static String DATABASE_USER;
	private static String DATABASE_PASS;
	private static String ENDER_KEY;
	private static String ENDER_URL;

	private static URL url1;
	private static GsonFactory factory;
	private static SearchListResponse sr;
	public static JDABuilder builder;
	public static JDA DisClient;
	final static CommandClientBuilder commandBuilder = new CommandClientBuilder();
	public static YouTube YTClient;
	private static String result;
	public static String botname = "GeekBot";
	private static String BotPrefix = "!gb";
	private static long id;
	// public List<ServerSettings> settingsList = new ArrayList<>();
	private static JsonElement serverSettings;
	private static Logger log = LogManager.getLogger(GeekBot.class);
	public static final File BotPath = new File("/GeekBot/ServerSettings");
	public static final File ConfigPath = new File("/GeekBot/Config/Config.properties");
	private static Timer timer = new Timer();
	private static Configuration config = new Configuration();
	private static StreamSpeechRecognizer recog;
	public static TimerTask task = new EventStarboudServerReset();
	public static SourceServer starboundServer;

	private static Set<GatewayIntent> intents = new HashSet<>();

	static {
		intents.add(GatewayIntent.DIRECT_MESSAGES);
		intents.add(GatewayIntent.DIRECT_MESSAGE_TYPING);
		intents.add(GatewayIntent.GUILD_MEMBERS);
		intents.add(GatewayIntent.GUILD_MESSAGES);
		intents.add(GatewayIntent.GUILD_MESSAGE_TYPING);
	}

	public static void main(String[] args) throws IOException {
		Gson gson = new GsonBuilder().setPrettyPrinting().create();

		config.setAcousticModelPath("resource:/edu/cmu/sphinx/models/en-us/en-us");
		config.setDictionaryPath("resource:/edu/cmu/sphinx/models/en-us/cmudict-en-us.dict");
		config.setLanguageModelPath("resource:/edu/cmu/sphinx/models/en-us/en-us.lm.bin");

		recog = new StreamSpeechRecognizer(config);

		if (!BotPath.exists()) {
			BotPath.mkdirs();
			ConfigPath.mkdirs();
		}

		try (InputStream input = FileUtils.openInputStream(ConfigPath)) {

			Properties prop = new Properties();
			if (input == null) {
				log.error("unable to find Config.properties");
				return;
			}

			log.info("Loading keys");
			prop.load(input);
			setGOOGLE_API_KEY(prop.getProperty("key.google"));
			setCHIRON_KEY(prop.getProperty("id.discord"));
			setDISCORD_SECRET(prop.getProperty("secret.discord"));
			setDISCORD_TOKEN(prop.getProperty("token.discord"));
			setYOUTUBE_ID(prop.getProperty("id.youtube"));
			setOWNER_ID(prop.getProperty("id.owner"));
			setLABRINTH_ID(prop.getProperty("id.labrinth"));
			setLABUPDATE(prop.getProperty("id.labupdate"));
			setCHIRON_KEY(prop.getProperty("key.chiron"));
			setCHIRON_URL(prop.getProperty("url.chiron"));
			setMINDUSTRY_URL(prop.getProperty("url.mindustry"));
			setDATABASE_URL(prop.getProperty("url.database"));
			setDATABASE_PASS(prop.getProperty("password.database"));
			setDATABASE_USER(prop.getProperty("username.database"));
			setENDER_KEY(prop.getProperty("key.ender"));
			setENDER_URL(prop.getProperty("url.ender"));
		}



		factory = new GsonFactory();
		builder = new JDABuilder(AccountType.BOT).setToken(DISCORD_TOKEN);

		YTClient = new YouTube.Builder(new HttpTransport() {

			@Override
			protected LowLevelHttpRequest buildRequest(String method, String url) throws IOException {
				// TODO Auto-generated method stub
				return null;
			}
		}, factory, new HttpRequestInitializer() {

			@Override
			public void initialize(HttpRequest request) throws IOException {
			}
		}).setApplicationName(botname).build();
		YouTube.Search.List request = YTClient.search().list("id,snippet");

		request.setChannelId(getID());
		request.buildHttpRequest();
		log.info("request json content: {}", request.getJsonContent());

		// result = get(getBaseurl() + "/search?" + "part=snippet" + "&order=date" +
		// "&channelId=" + getID() + "&key="
		// + getYTApiKey());

		// sr = gson.fromJson(result, SearchListResponse.class);

		// set discord events
		builder.addEventListeners(new WelcomeEvent());
		commandBuilder.setPrefix("!gb ");
		commandBuilder.setOwnerId(OWNER_ID);
		commandBuilder.addCommand(new CmdPing());
		commandBuilder.addCommand(new CmdInvite());
		commandBuilder.addCommand(new CmdContribute());
		commandBuilder.addCommand(new CmdHug());
		commandBuilder.addCommand(new CmdUserInfo());

		// commands not used by the public
		commandBuilder.addCommand(new CmdStopBot());
		commandBuilder.addCommand(new CmdStarboundRole());
		//commandBuilder.addCommand(new CmdStarBoundRestart());
		commandBuilder.addCommand(new CmdChironHistory());
		commandBuilder.addCommand(new CmdChironJob());
		commandBuilder.addCommand(new CmdJoinVoice());
		commandBuilder.addCommand(new CmdExitVoice());
		commandBuilder.addCommand(new CmdEnderHistory());
		commandBuilder.addCommand(new CmdEnderJob());

		commandBuilder.setHelpWord("help");

		//timer.schedule(task, get24());
		final CommandClient commandListener = commandBuilder.build();
		builder.addEventListeners(commandListener);
		builder.enableIntents(intents);

		try {
			DisClient = builder.build();
			DisClient.setAutoReconnect(true);
			DisClient.getPresence().setActivity(Activity.watching("for !gb help"));
		} catch (Exception e) {
			e.printStackTrace();
		}

		
		DisClient.getGuilds().forEach(action -> {

		log.info(result);
		log.info("End Of Program");
	});
}

	/**
	 * @return the MINDUSTRY_URL
	 */
	public static String getMINDUSTRY_URL() {
		return MINDUSTRY_URL;
	}

	public static String getCHIRON_KEY() {
		return CHIRON_KEY;
	}

	public static String getCHIRON_URL() {
		return CHIRON_URL;
	}

	public static String getLABRINTH_ID() {
		return LABRINTH_ID;
	}

	public static void setLABRINTH_ID(String lABRINTH_ID) {
		LABRINTH_ID = lABRINTH_ID;
	}

	public static String getLABUPDATE() {
		return LABUPDATE;
	}

	public static void setLABUPDATE(String lABUPDATE) {
		LABUPDATE = lABUPDATE;
	}

	public static Configuration getConfig() {
		return config;
	}

	public static StreamSpeechRecognizer getRecog() {
		return recog;
	}

	public static String get(String url) throws IOException {
		// URL declaration
		URL obj = new URL(url);

		// URL connection
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();

		// Request Settings
		con.setRequestMethod("GET");
		con.setRequestProperty("User-Agent", "Mozilla/5.0");
		// check response code for an okay
		int responseCode = con.getResponseCode();
		log.info("GET Response Code: " + responseCode);
		if (responseCode == HttpURLConnection.HTTP_OK) { // success
			// Read the Response from the site
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();

			// Generate a response to return
			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			// Close Reader
			in.close();
			con.disconnect();

			// print result
			return response.toString();
		}
		return null;
	}

	public static String getOWNER_ID() {
		return OWNER_ID;
	}

	public static String get(String url, String additionalPropertyKey, String additionalPropertyValue)
			throws IOException {
		// URL declaration
		URL obj = new URL(url);

		// URL connection
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();

		// Request Settings
		con.setRequestMethod("GET");
		con.setRequestProperty("User-Agent", "Mozilla/5.0");
		con.addRequestProperty(additionalPropertyKey, additionalPropertyValue);
		// check response code for an okay
		int responseCode = con.getResponseCode();
		log.info("GET Response Code: " + responseCode);
		if (responseCode == HttpURLConnection.HTTP_OK) { // success
			// Read the Response from the site
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();

			// Generate a response to return
			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			// Close Reader
			in.close();
			con.disconnect();

			// print result
			return response.toString();
		}
		return null;
	}

	public static int post(String hostUrl, String send) throws IOException {
		URL obj = new URL(hostUrl);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		con.setRequestMethod("POST");
		con.setDoOutput(true);

		try (OutputStream os = con.getOutputStream()) {
			os.write(send.getBytes());
		}
		return con.getResponseCode();
	}

	// -----GETTERS-&-SETTERS----- //

	/**
	 * 
	 * @return the YouTube Data API's Base URL as a string
	 */
	public static String getBaseurl() {
		return BASEURL;
	}

	// public static EventDispatcher getDispatcher() {
	// return dispatcher;
	// }

	public static URL getUrl1() {
		return url1;
	}

	public static String getID() {
		return YOUTUBE_ID;
	}

	public static void setUrl1(URL url1) {
		GeekBot.url1 = url1;
	}

	public static JDA getClient() {
		return DisClient;
	}

	public static String getYTApiKey() {
		return GOOGLE_API_KEY;
	}

	public static String getDiscordToken() {
		return DISCORD_TOKEN;
	}

	public static String getDiscordId() {
		return DISCORD_ID;
	}

	public static String getDisordSecret() {
		return DISCORD_SECRET;
	}

	public static String getBotPrefix() {
		return BotPrefix;
	}

	public static Date get24() {
		Date date = new Date();
		date.setHours(24);
		date.setMinutes(0);
		return date;
	}

	/**
	 * @return the gOOGLE_API_KEY
	 */
	public static String getGOOGLE_API_KEY() {
		return GOOGLE_API_KEY;
	}

	/**
	 * @param gOOGLE_API_KEY the gOOGLE_API_KEY to set
	 */
	public static void setGOOGLE_API_KEY(String gOOGLE_API_KEY) {
		GOOGLE_API_KEY = gOOGLE_API_KEY;
	}

	/**
	 * @return the dISCORD_TOKEN
	 */
	public static String getDISCORD_TOKEN() {
		return DISCORD_TOKEN;
	}

	/**
	 * @param dISCORD_TOKEN the dISCORD_TOKEN to set
	 */
	public static void setDISCORD_TOKEN(String dISCORD_TOKEN) {
		DISCORD_TOKEN = dISCORD_TOKEN;
	}

	/**
	 * @return the dISCORD_ID
	 */
	public static String getDISCORD_ID() {
		return DISCORD_ID;
	}

	/**
	 * @param dISCORD_ID the dISCORD_ID to set
	 */
	public static void setDISCORD_ID(String dISCORD_ID) {
		DISCORD_ID = dISCORD_ID;
	}

	/**
	 * @return the dISCORD_SECRET
	 */
	public static String getDISCORD_SECRET() {
		return DISCORD_SECRET;
	}

	/**
	 * @param dISCORD_SECRET the dISCORD_SECRET to set
	 */
	public static void setDISCORD_SECRET(String dISCORD_SECRET) {
		DISCORD_SECRET = dISCORD_SECRET;
	}

	/**
	 * @return the yOUTUBE_ID
	 */
	public static String getYOUTUBE_ID() {
		return YOUTUBE_ID;
	}

	/**
	 * @param yOUTUBE_ID the yOUTUBE_ID to set
	 */
	public static void setYOUTUBE_ID(String yOUTUBE_ID) {
		YOUTUBE_ID = yOUTUBE_ID;
	}

	/**
	 * @param oWNER_ID the oWNER_ID to set
	 */
	public static void setOWNER_ID(String oWNER_ID) {
		OWNER_ID = oWNER_ID;
	}

	/**
	 * @param cHIRON_KEY the cHIRON_KEY to set
	 */
	public static void setCHIRON_KEY(String cHIRON_KEY) {
		CHIRON_KEY = cHIRON_KEY;
	}

	/**
	 * @param cHIRON_URL the cHIRON_URL to set
	 */
	public static void setCHIRON_URL(String cHIRON_URL) {
		CHIRON_URL = cHIRON_URL;
	}

	/**
	 * @param mINDUSTRY_URL the mINDUSTRY_URL to set
	 */
	public static void setMINDUSTRY_URL(String mINDUSTRY_URL) {
		MINDUSTRY_URL = mINDUSTRY_URL;
	}

	/**
	 * @return the dATABASE_URL
	 */
	public static String getDATABASE_URL() {
		return DATABASE_URL;
	}

	/**
	 * @param dATABASE_URL the dATABASE_URL to set
	 */
	public static void setDATABASE_URL(String dATABASE_URL) {
		DATABASE_URL = dATABASE_URL;
	}

	/**
	 * @return the dATABASE_USER
	 */
	public static String getDATABASE_USER() {
		return DATABASE_USER;
	}

	/**
	 * @param dATABASE_USER the dATABASE_USER to set
	 */
	public static void setDATABASE_USER(String dATABASE_USER) {
		DATABASE_USER = dATABASE_USER;
	}

	/**
	 * @return the dATABASE_PASS
	 */
	public static String getDATABASE_PASS() {
		return DATABASE_PASS;
	}

	/**
	 * @param dATABASE_PASS the dATABASE_PASS to set
	 */
	public static void setDATABASE_PASS(String dATABASE_PASS) {
		DATABASE_PASS = dATABASE_PASS;
	}

	/**
	 * @return the eNDER_KEY
	 */
	public static String getENDER_KEY() {
		return ENDER_KEY;
	}

	/**
	 * @param eNDER_KEY the eNDER_KEY to set
	 */
	public static void setENDER_KEY(String eNDER_KEY) {
		ENDER_KEY = eNDER_KEY;
	}

	/**
	 * @return the eNDER_URL
	 */
	public static String getENDER_URL() {
		return ENDER_URL;
	}

	/**
	 * @param eNDER_URL the eNDER_URL to set
	 */
	public static void setENDER_URL(String eNDER_URL) {
		ENDER_URL = eNDER_URL;
	}
}
