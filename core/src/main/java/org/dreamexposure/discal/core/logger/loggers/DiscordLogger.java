package org.dreamexposure.discal.core.logger.loggers;

import org.dreamexposure.discal.core.logger.interfaces.Logger;
import org.dreamexposure.discal.core.logger.object.LogObject;
import org.dreamexposure.discal.core.object.BotSettings;
import org.dreamexposure.discal.core.utils.GlobalConst;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Instant;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;

public class DiscordLogger implements Logger {
	private final WebhookClient debugClient;
	private final WebhookClient exceptionClient;
	private final WebhookClient statusClient;

	public DiscordLogger(WebhookClient debug, WebhookClient exception, WebhookClient status) {
		this.debugClient = debug;
		this.exceptionClient = exception;
		this.statusClient = status;
	}

	@Override
	public void write(LogObject log) {
		switch (log.getType()) {
			case STATUS:
				writeStatus(log);
				break;
			case DEBUG:
				writeDebug(log);
				break;
			case EXCEPTION:
				writeException(log);
				break;
			default:
				break;
		}
	}

	private void writeStatus(LogObject log) {
		WebhookEmbedBuilder builder = new WebhookEmbedBuilder()
				.setTitle(new WebhookEmbed.EmbedTitle("Status", null))
				.addField(new WebhookEmbed
						.EmbedField(true, "Shard Index", BotSettings.SHARD_INDEX.get()))
				.addField(new WebhookEmbed
						.EmbedField(false, "Time", log.getTimestamp()))
				.setDescription(log.getMessage())
				.setColor(GlobalConst.discalColor.getRGB())
				.setTimestamp(Instant.now());

		if (log.getInfo() != null) {
			builder.addField(new WebhookEmbed
					.EmbedField(false, "Info", log.getInfo()));
		}

		statusClient.send(builder.build());
	}

	private void writeDebug(LogObject log) {
		WebhookEmbedBuilder builder = new WebhookEmbedBuilder()
				.setTitle(new WebhookEmbed.EmbedTitle("Debug", null))
				.addField(new WebhookEmbed
						.EmbedField(true, "Shard Index", BotSettings.SHARD_INDEX.get()))
				.addField(new WebhookEmbed
						.EmbedField(false, "Time", log.getTimestamp()))
				.setDescription(log.getMessage())
				.setColor(GlobalConst.discalColor.getRGB())
				.setTimestamp(Instant.now());

		if (log.getInfo() != null) {
			builder.addField(new WebhookEmbed.EmbedField(false, "Info", log.getInfo()));
		}

		debugClient.send(builder.build());
	}

	private void writeException(LogObject log) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		log.getException().printStackTrace(pw);
		String error = sw.toString(); // stack trace as a string
		pw.close();
		try {
			sw.close();
		} catch (IOException e1) {
			//Can ignore silently...
		}

		//Shorten error message...
		if (error.length() > 1500)
			error = error.substring(0, 1500);

		WebhookEmbedBuilder builder = new WebhookEmbedBuilder()
				.setTitle(new WebhookEmbed.EmbedTitle("Exception", null))
				.addField(new WebhookEmbed
						.EmbedField(true, "Shard Index", BotSettings.SHARD_INDEX.get()))
				.addField(new WebhookEmbed
						.EmbedField(false, "Class", log.getClazz().getName()))
				.addField(new WebhookEmbed
						.EmbedField(false, "Time", log.getTimestamp()))
				.addField(new WebhookEmbed
						.EmbedField(false, "Message", log.getMessage()))
				.setDescription(error)
				.setColor(GlobalConst.discalColor.getRGB())
				.setTimestamp(Instant.now());
		if (log.getInfo() != null) {
			builder.addField(new WebhookEmbed.EmbedField(false, "Info", log.getInfo()));
		}

		exceptionClient.send(builder.build());
	}
}
