package com.cloudcraftgaming.discal.logger;

import com.cloudcraftgaming.discal.Main;
import com.cloudcraftgaming.discal.api.message.Message;
import com.cloudcraftgaming.discal.api.object.BotSettings;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.EmbedBuilder;

import javax.annotation.Nullable;
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
		if (instance == null) {
			instance = new Logger();
		}
		return instance;
	}

	public void init() {
		//Create files...
		String now = String.valueOf(System.currentTimeMillis());

		SimpleDateFormat format = new SimpleDateFormat("dd-M-yyyy hh:mm:ss");
		String timestamp = format.format(System.currentTimeMillis());

		exceptionsFile = BotSettings.LOG_FOLDER.get() + "/" + now + "/exceptions.log";
		apiFile = BotSettings.LOG_FOLDER.get() + "/" + now + "/api.log";
		announcementsFile = BotSettings.LOG_FOLDER.get() + "/" + now + "/announcements.log";
		debugFile = BotSettings.LOG_FOLDER.get() + "/" + now + "/debug.log";

		try {
			PrintWriter exceptions = new PrintWriter(exceptionsFile, "UTF-8");
			exceptions.println("INIT --- " + timestamp + " ---");
			exceptions.close();

			PrintWriter api = new PrintWriter(apiFile, "UTF-8");
			api.println("INIT --- " + timestamp + " ---");
			api.close();

			PrintWriter announcement = new PrintWriter(apiFile, "UTF-8");
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

		if (Main.getSelfUser() != null) {
			IUser bot = Main.getSelfUser();

			if (post) {
				String shortError = error;
				if (error.length() > 1250) {
					shortError = error.substring(0, 1250);
				}

				EmbedBuilder em = new EmbedBuilder();
				if (bot != null) {
					em.withAuthorIcon(bot.getAvatarURL());
				}
				if (author != null) {
					em.withAuthorName(author.getName());
					em.withThumbnail(author.getAvatarURL());
				}
				em.withColor(239, 15, 0);
				em.withFooterText(clazz.getName());

				//Send to discord!
				em.appendField("Time", timeStamp, true);
				if (e.getMessage() != null) {
					if (e.getMessage().length() > 1024) {
						em.appendField("Exception", e.getMessage().substring(0, 1024), true);
					} else {
						em.appendField("Exception", e.getMessage(), true);
					}
				}
				if (message != null) {
					em.appendField("Message", message, true);
				}

				//Get DisCal guild and channel..
				IGuild guild = Main.client.getGuildByID(266063520112574464L);
				IChannel channel = guild.getChannelByID(302249332244217856L);

				Message.sendMessage(em.build(), "```" + shortError + "```", channel);
			}
		}

		//ALWAYS LOG TO FILE!
		try {
			PrintWriter exceptions = new PrintWriter(exceptionsFile, "UTF-8");
			exceptions.println("ERROR --- " + timeStamp + " ---");
			if (author != null) {
				exceptions.println("user: " + author.getName() + "#" + author.getDiscriminator());
			}
			if (message != null) {
				exceptions.println("message: " + message);
			}
			exceptions.println(error);
			exceptions.close();
		} catch (IOException io) {
			io.printStackTrace();
		}
	}

	public void debug(@Nullable IUser author, String message, @Nullable String info, Class clazz, boolean post) {
		String timeStamp = new SimpleDateFormat("yyyy/MM/dd-HH:mm:ss").format(Calendar.getInstance().getTime());

		if (Main.client != null) {
			if (post) {
				IUser bot = Main.getSelfUser();
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
				if (info != null) {
					em.appendField("Additional Info", info, true);
				}

				//Get DisCal guild and channel..
				IGuild guild = Main.client.getGuildByID(266063520112574464L);
				IChannel channel = guild.getChannelByID(302249332244217856L);

				Message.sendMessage(em.build(), "```" + message + "```", channel);
			}
		}

		//ALWAYS LOG TO FILE!
		try {
			PrintWriter file = new PrintWriter(debugFile, "UTF-8");
			file.println("DEBUG --- " + timeStamp + " ---");
			if (author != null) {
				file.println("user: " + author.getName() + "#" + author.getDiscriminator());
			}
			if (message != null) {
				file.println("message: " + message);
			}
			if (info != null) {
				file.println("info: " + info);
			}
			file.close();
		} catch (IOException io) {
			io.printStackTrace();
		}
	}

	public void debug(String message) {
		String timeStamp = new SimpleDateFormat("yyyy/MM/dd-HH:mm:ss").format(Calendar.getInstance().getTime());

		try {
			PrintWriter file = new PrintWriter(debugFile, "UTF-8");
			file.println("DEBUG --- " + timeStamp + " ---");
			if (message != null) {
				file.println("info: " + message);
			}
			file.close();
		} catch (IOException io) {
			io.printStackTrace();
		}
	}
}