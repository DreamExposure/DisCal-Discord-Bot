package com.cloudcraftgaming.discal.logger;

import com.cloudcraftgaming.discal.api.DisCalAPI;
import com.cloudcraftgaming.discal.api.message.MessageManager;
import com.cloudcraftgaming.discal.api.object.BotSettings;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.EmbedBuilder;

import javax.annotation.Nullable;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Logger {
	private static Logger instance;
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

	public void exception(@Nullable IUser author, @Nullable String message, Exception e, Class clazz, boolean post) {
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

		if (DisCalAPI.getAPI().getClient().getOurUser() != null && DisCalAPI.getAPI().getClient().isLoggedIn()) {
			IUser bot = DisCalAPI.getAPI().getClient().getOurUser();

			if (post) {
				String shortError = error;
				if (error.length() > 1250)
					shortError = error.substring(0, 1250);


				EmbedBuilder em = new EmbedBuilder();
				if (bot != null)
					em.withAuthorIcon(bot.getAvatarURL());

				if (author != null) {
					em.withAuthorName(author.getName());
					em.withThumbnail(author.getAvatarURL());
				}
				em.withColor(239, 15, 0);
				em.withFooterText(clazz.getName());

				//Send to discord!
				em.appendField("Time", timeStamp, true);
				if (e.getMessage() != null) {
					if (e.getMessage().length() > 1024)
						em.appendField("Exception", e.getMessage().substring(0, 1024), true);
					else
						em.appendField("Exception", e.getMessage(), true);
				}
				if (message != null)
					em.appendField("Message", message, true);


				//Get DisCal guild and channel..
				IGuild guild = DisCalAPI.getAPI().getClient().getGuildByID(DisCalAPI.getAPI().serverId);
				IChannel channel = guild.getChannelByID(DisCalAPI.getAPI().errorLog);

				MessageManager.sendMessage(em.build(), "```" + shortError + "```", channel);
			}
		}

		//ALWAYS LOG TO FILE!
		try {
			FileWriter exceptions = new FileWriter(exceptionsFile, true);
			exceptions.write("ERROR --- " + timeStamp + " ---" + MessageManager.lineBreak);
			if (author != null)
				exceptions.write("user: " + author.getName() + "#" + author.getDiscriminator() + MessageManager.lineBreak);

			if (message != null)
				exceptions.write("message: " + message + MessageManager.lineBreak);

			exceptions.write(error + MessageManager.lineBreak);
			exceptions.close();
		} catch (IOException io) {
			io.printStackTrace();
		}
	}

	public void debug(@Nullable IUser author, String message, @Nullable String info, Class clazz, boolean post) {
		String timeStamp = new SimpleDateFormat("yyyy/MM/dd-HH:mm:ss").format(Calendar.getInstance().getTime());

		if (DisCalAPI.getAPI().getClient() != null) {
			if (post) {
				IUser bot = DisCalAPI.getAPI().getClient().getOurUser();
				EmbedBuilder em = new EmbedBuilder();
				assert bot != null;
				em.withAuthorIcon(bot.getAvatarURL());
				if (author != null) {
					em.withAuthorName(author.getName());
					em.withThumbnail(author.getAvatarURL());
				}
				em.withColor(239, 15, 0);
				em.withFooterText(clazz.getName());


				em.appendField("Time", timeStamp, true);
				if (info != null)
					em.appendField("Additional Info", info, true);


				//Get DisCal guild and channel..
				IGuild guild = DisCalAPI.getAPI().getClient().getGuildByID(DisCalAPI.getAPI().serverId);
				IChannel channel = guild.getChannelByID(DisCalAPI.getAPI().errorLog);

				MessageManager.sendMessage(em.build(), "```" + message + "```", channel);
			}
		}

		//ALWAYS LOG TO FILE!
		try {
			FileWriter file = new FileWriter(debugFile, true);
			file.write("DEBUG --- " + timeStamp + " ---" + MessageManager.lineBreak);
			if (author != null)
				file.write("user: " + author.getName() + "#" + author.getDiscriminator() + MessageManager.lineBreak);

			if (message != null)
				file.write("message: " + message + MessageManager.lineBreak);

			if (info != null)
				file.write("info: " + info + MessageManager.lineBreak);

			file.close();
		} catch (IOException io) {
			io.printStackTrace();
		}
	}

	public void debug(String message) {
		String timeStamp = new SimpleDateFormat("yyyy/MM/dd-HH:mm:ss").format(Calendar.getInstance().getTime());

		try {
			FileWriter file = new FileWriter(debugFile, true);
			file.write("DEBUG --- " + timeStamp + " ---" + MessageManager.lineBreak);
			if (message != null)
				file.write("info: " + message + MessageManager.lineBreak);

			file.close();
		} catch (IOException io) {
			io.printStackTrace();
		}
	}

	public void api(String message) {
		String timeStamp = new SimpleDateFormat("yyyy/MM/dd-HH:mm:ss").format(Calendar.getInstance().getTime());

		try {
			FileWriter file = new FileWriter(apiFile, true);
			file.write("API --- " + timeStamp + " ---" + MessageManager.lineBreak);
			file.write("info: " + message + MessageManager.lineBreak);
			file.close();
		} catch (IOException io) {
			io.printStackTrace();
		}
	}

	public void api(String message, String ip) {
		String timeStamp = new SimpleDateFormat("yyyy/MM/dd-HH:mm:ss").format(Calendar.getInstance().getTime());

		try {
			FileWriter file = new FileWriter(apiFile, true);
			file.write("API --- " + timeStamp + " ---" + MessageManager.lineBreak);
			file.write("info: " + message + MessageManager.lineBreak);
			file.write("IP: " + ip + MessageManager.lineBreak);
			file.close();
		} catch (IOException io) {
			io.printStackTrace();
		}
	}

	public void api(String message, String ip, String host, String endpoint) {
		String timeStamp = new SimpleDateFormat("yyyy/MM/dd-HH:mm:ss").format(Calendar.getInstance().getTime());

		try {
			FileWriter file = new FileWriter(apiFile, true);
			file.write("API --- " + timeStamp + " ---" + MessageManager.lineBreak);
			file.write("info: " + message + MessageManager.lineBreak);
			file.write("IP: " + ip + MessageManager.lineBreak);
			file.write("Host: " + host + MessageManager.lineBreak);
			file.write("Endpoint: " + endpoint + MessageManager.lineBreak);
			file.close();
		} catch (IOException io) {
			io.printStackTrace();
		}
	}

	public void announcement(String message) {
		String timeStamp = new SimpleDateFormat("yyyy/MM/dd-HH:mm:ss").format(Calendar.getInstance().getTime());

		try {
			FileWriter file = new FileWriter(announcementsFile, true);
			file.write("ANNOUNCEMENT --- " + timeStamp + " ---" + MessageManager.lineBreak);
			file.write("info: " + message + MessageManager.lineBreak);
			file.close();
		} catch (IOException io) {
			io.printStackTrace();
		}
	}

	public void announcement(String message, String guildId, String announcementId, String eventId) {
		String timeStamp = new SimpleDateFormat("yyyy/MM/dd-HH:mm:ss").format(Calendar.getInstance().getTime());

		try {
			FileWriter file = new FileWriter(announcementsFile, true);
			file.write("ANNOUNCEMENT --- " + timeStamp + " ---" + MessageManager.lineBreak);
			file.write("info: " + message + MessageManager.lineBreak);
			file.write("guild Id: " + guildId + MessageManager.lineBreak);
			file.write("announcement Id: " + announcementId + MessageManager.lineBreak);
			file.write("event id: " + eventId + MessageManager.lineBreak);
			file.close();
		} catch (IOException io) {
			io.printStackTrace();
		}
	}
}