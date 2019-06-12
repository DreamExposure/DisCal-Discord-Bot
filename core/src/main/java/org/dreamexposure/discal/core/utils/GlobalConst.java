package org.dreamexposure.discal.core.utils;

import discord4j.core.object.util.Snowflake;
import okhttp3.MediaType;

import java.awt.*;

public class GlobalConst {
	public static final String version = "3.0.0";

	public static String iconUrl;
	public static final String lineBreak = System.getProperty("line.separator");
	public static final Snowflake novaId = Snowflake.of(130510525770629121L);
	public static final Snowflake xaanitId = Snowflake.of(233611560545812480L);
	public static final Snowflake calId = Snowflake.of(142107863307780097L);
	public static final Snowflake dreamId = Snowflake.of(142107863307780097L);

	public static final Color discalColor = new Color(56, 138, 237);
	public static final String discalSite = "https://www.discalbot.com";
	public static final String supportInviteLink = "https://discord.gg/2TFqyuy";
	public static final String discalDashboardLink = "https://www.discalbot.com/dashboard";

	public static final long oneMinuteMs = 60000;
	public static final long oneHourMs = 3600000;
	public static final long oneDayMs = 86400000;

	public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
}