package org.dreamexposure.discal.core.logger;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import discord4j.core.object.entity.User;
import org.dreamexposure.discal.core.object.BotSettings;
import org.dreamexposure.discal.core.utils.GlobalConst;

import javax.annotation.Nullable;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Calendar;

@SuppressWarnings("Duplicates")
public class Logger {
	private static Logger instance;
	private WebhookClient debugClient;
	private WebhookClient exceptionClient;
	private WebhookClient statusClient;

	private String exceptionsFile;
	private String apiFile;
	private String announcementsFile;
	private String debugFile;

	private Logger() {
	} //Prevent initialization

	public static Logger getLogger() {
		if (instance == null)
			instance = new Logger();
		return instance;
	}

	public void init() {
		//Create webhook clients.
		if (BotSettings.USE_WEBHOOKS.get().equalsIgnoreCase("true")) {
			debugClient = WebhookClient.withUrl(BotSettings.DEBUG_WEBHOOK.get());
			exceptionClient = WebhookClient.withUrl(BotSettings.ERROR_WEBHOOK.get());
			statusClient = WebhookClient.withUrl(BotSettings.STATUS_WEBHOOK.get());
		}

		//Create files...
		String timestamp = new SimpleDateFormat("dd-MM-yyyy-hh.mm.ss").format(System.currentTimeMillis());

		exceptionsFile = BotSettings.LOG_FOLDER.get() + "/" + timestamp + "-exceptions.log";
		apiFile = BotSettings.LOG_FOLDER.get() + "/" + timestamp + "-api.log";
		announcementsFile = BotSettings.LOG_FOLDER.get() + "/" + timestamp + "-announcements.log";
		debugFile = BotSettings.LOG_FOLDER.get() + "/" + timestamp + "-debug.log";

		try {
			PrintWriter exceptions = new PrintWriter(exceptionsFile, "UTF-8");
			exceptions.println("INIT --- " + timestamp + " ---");
			exceptions.close();

			PrintWriter api = new PrintWriter(apiFile, "UTF-8");
			api.println("INIT --- " + timestamp + " ---");
			api.close();

			PrintWriter announcement = new PrintWriter(announcementsFile, "UTF-8");
			announcement.println("INIT --- " + timestamp + " ---");
			announcement.close();

			PrintWriter debug = new PrintWriter(debugFile, "UTF-8");
			debug.println("INIT --- " + timestamp + " ---");
			debug.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void exception(@Nullable User author, @Nullable String message, Exception e, boolean postWebhook, Class clazz) {
		String timeStamp = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(Calendar.getInstance().getTime());
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		String error = sw.toString(); // stack trace as a string
		pw.close();
		try {
			sw.close();
		} catch (IOException e1) {
			//Can ignore silently...
		}

		//ALWAYS LOG TO FILE!
		try {
			FileWriter exceptions = new FileWriter(exceptionsFile, true);
			exceptions.write("ERROR --- " + timeStamp + " ---" + GlobalConst.lineBreak);
			if (author != null)
				exceptions.write("user: " + author.getUsername() + "#" + author.getDiscriminator() + GlobalConst.lineBreak);

			if (message != null)
				exceptions.write("message: " + message + GlobalConst.lineBreak);

			exceptions.write("Class:" + clazz.getName() + GlobalConst.lineBreak);

			exceptions.write(error + GlobalConst.lineBreak);
			exceptions.close();
		} catch (IOException io) {
			io.printStackTrace();
		}

		//Post to webhook if wanted.
		if (BotSettings.USE_WEBHOOKS.get().equalsIgnoreCase("true") && postWebhook) {
			WebhookEmbedBuilder builder = new WebhookEmbedBuilder()
				.setTitle(new WebhookEmbed.EmbedTitle("Debug", null))
				.addField(new WebhookEmbed
					.EmbedField(true, "Shard Index", BotSettings.SHARD_INDEX.get()))
				.addField(new WebhookEmbed
					.EmbedField(false, "Class", clazz.getName()))
				.setDescription(error)
				.setColor(GlobalConst.discalColor.getRGB())
				.setTimestamp(Instant.now());

			if (author != null) {
				builder.setAuthor(new WebhookEmbed
					.EmbedAuthor(author.getUsername(), author.getAvatarUrl(), null));
			}
			if (message != null) {
				builder.addField(new WebhookEmbed.EmbedField(false, "Message", message));
			}

			exceptionClient.send(builder.build());
		}
	}

	public void debug(@Nullable User author, String message, @Nullable String info, boolean postWebhook, Class clazz) {
		String timeStamp = new SimpleDateFormat("yyyy/MM/dd-HH:mm:ss").format(Calendar.getInstance().getTime());
		//ALWAYS LOG TO FILE!
		try {
			FileWriter file = new FileWriter(debugFile, true);
			file.write("DEBUG --- " + timeStamp + " ---" + GlobalConst.lineBreak);
			if (author != null)
				file.write("user: " + author.getUsername() + "#" + author.getDiscriminator() + GlobalConst.lineBreak);

			if (message != null)
				file.write("message: " + message + GlobalConst.lineBreak);

			if (info != null)
				file.write("info: " + info + GlobalConst.lineBreak);

			file.write("Class: " + clazz.getName() + GlobalConst.lineBreak);

			file.close();
		} catch (IOException io) {
			io.printStackTrace();
		}

		//Post to webhook if wanted.
		if (BotSettings.USE_WEBHOOKS.get().equalsIgnoreCase("true") && postWebhook) {
			WebhookEmbedBuilder builder = new WebhookEmbedBuilder()
				.setTitle(new WebhookEmbed.EmbedTitle("Debug", null))
				.addField(new WebhookEmbed
					.EmbedField(true, "Shard Index", BotSettings.SHARD_INDEX.get()))
				.setDescription(message)
				.setColor(GlobalConst.discalColor.getRGB())
				.setTimestamp(Instant.now());

			if (author != null) {
				builder.setAuthor(new WebhookEmbed
					.EmbedAuthor(author.getUsername(), author.getAvatarUrl(), null));
			}
			if (info != null) {
				builder.addField(new WebhookEmbed.EmbedField(false, "Info", info));
			}

			debugClient.send(builder.build());
		}
	}

	public void debug(String message, boolean postWebhook) {
		String timeStamp = new SimpleDateFormat("yyyy/MM/dd-HH:mm:ss").format(Calendar.getInstance().getTime());

		try {
			FileWriter file = new FileWriter(debugFile, true);
			file.write("DEBUG --- " + timeStamp + " ---" + GlobalConst.lineBreak);
			if (message != null)
				file.write("info: " + message + GlobalConst.lineBreak);

			file.close();
		} catch (IOException io) {
			io.printStackTrace();
		}

		//Post to webhook if wanted.
		if (BotSettings.USE_WEBHOOKS.get().equalsIgnoreCase("true") && postWebhook) {
			WebhookEmbedBuilder builder = new WebhookEmbedBuilder()
				.setTitle(new WebhookEmbed.EmbedTitle("Debug", null))
				.addField(new WebhookEmbed
					.EmbedField(true, "Shard Index", BotSettings.SHARD_INDEX.get()))
				.setDescription(message)
				.setColor(GlobalConst.discalColor.getRGB())
				.setTimestamp(Instant.now());

			debugClient.send(builder.build());
		}
	}

	public void api(String message) {
		String timeStamp = new SimpleDateFormat("yyyy/MM/dd-HH:mm:ss").format(Calendar.getInstance().getTime());

		try {
			FileWriter file = new FileWriter(apiFile, true);
			file.write("API --- " + timeStamp + " ---" + GlobalConst.lineBreak);
			file.write("info: " + message + GlobalConst.lineBreak);
			file.close();
		} catch (IOException io) {
			io.printStackTrace();
		}
	}

	public void api(String message, String ip) {
		String timeStamp = new SimpleDateFormat("yyyy/MM/dd-HH:mm:ss").format(Calendar.getInstance().getTime());

		try {
			FileWriter file = new FileWriter(apiFile, true);
			file.write("API --- " + timeStamp + " ---" + GlobalConst.lineBreak);
			file.write("info: " + message + GlobalConst.lineBreak);
			file.write("IP: " + ip + GlobalConst.lineBreak);
			file.close();
		} catch (IOException io) {
			io.printStackTrace();
		}
	}

	public void api(String message, String ip, String host, String endpoint) {
		String timeStamp = new SimpleDateFormat("yyyy/MM/dd-HH:mm:ss").format(Calendar.getInstance().getTime());

		try {
			FileWriter file = new FileWriter(apiFile, true);
			file.write("API --- " + timeStamp + " ---" + GlobalConst.lineBreak);
			file.write("info: " + message + GlobalConst.lineBreak);
			file.write("IP: " + ip + GlobalConst.lineBreak);
			file.write("Host: " + host + GlobalConst.lineBreak);
			file.write("Endpoint: " + endpoint + GlobalConst.lineBreak);
			file.close();
		} catch (IOException io) {
			io.printStackTrace();
		}
	}

	public void announcement(String message) {
		String timeStamp = new SimpleDateFormat("yyyy/MM/dd-HH:mm:ss").format(Calendar.getInstance().getTime());

		try {
			FileWriter file = new FileWriter(announcementsFile, true);
			file.write("ANNOUNCEMENT --- " + timeStamp + " ---" + GlobalConst.lineBreak);
			file.write("info: " + message + GlobalConst.lineBreak);
			file.close();
		} catch (IOException io) {
			io.printStackTrace();
		}
	}

	public void announcement(String message, String guildId, String announcementId, String eventId) {
		String timeStamp = new SimpleDateFormat("yyyy/MM/dd-HH:mm:ss").format(Calendar.getInstance().getTime());

		try {
			FileWriter file = new FileWriter(announcementsFile, true);
			file.write("ANNOUNCEMENT --- " + timeStamp + " ---" + GlobalConst.lineBreak);
			file.write("info: " + message + GlobalConst.lineBreak);
			file.write("guild Id: " + guildId + GlobalConst.lineBreak);
			file.write("announcement Id: " + announcementId + GlobalConst.lineBreak);
			file.write("event id: " + eventId + GlobalConst.lineBreak);
			file.close();
		} catch (IOException io) {
			io.printStackTrace();
		}
	}

	public void status(String message, @Nullable String info) {
		//Post to webhook if wanted.
		if (BotSettings.USE_WEBHOOKS.get().equalsIgnoreCase("true")) {
			WebhookEmbedBuilder builder = new WebhookEmbedBuilder()
				.setTitle(new WebhookEmbed.EmbedTitle("Debug", null))
				.addField(new WebhookEmbed
					.EmbedField(true, "Shard Index", BotSettings.SHARD_INDEX.get()))
				.setDescription(message)
				.setColor(GlobalConst.discalColor.getRGB())
				.setTimestamp(Instant.now());

			if (info != null) {
				builder.addField(new WebhookEmbed
					.EmbedField(false, "Info", info));
			}

			statusClient.send(builder.build());
		}
	}
}