package com.cloudcraftgaming.discal.utils;

import com.cloudcraftgaming.discal.Main;
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

/**
 * Created by Nova Fox on 4/13/2017.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
public class ExceptionHandler {
    public static void sendException(@Nullable IUser author, @Nullable String message, Exception e, Class clazz) {
        IUser bot = Main.getSelfUser();
        EmbedBuilder em = new EmbedBuilder();
        em.withAuthorIcon(bot.getAvatarURL());
        if (author != null) {
            em.withAuthorName(author.getName());
            em.withThumbnail(author.getAvatarURL());
        }
        em.withColor(239, 15, 0);
        em.withFooterText(clazz.getName());

        String timeStamp = new SimpleDateFormat("yyyy/MM/dd-HH:mm:ss").format(Calendar.getInstance().getTime());
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        String error = sw.toString(); // stack trace as a string
        if (error.length() > 1500) {
            error = error.substring(0, 1500);
        }
        pw.close();

        try {
            sw.close();
        } catch (IOException e1) {
            //Can ignore silently...
        }

        em.appendField("Time", timeStamp, true);
        if (e.getMessage() != null) {
            em.appendField("Exception", e.getMessage(), true);
        }
        if (message != null) {
            em.appendField("Message", message, true);
        }

        //Get DisCal guild and channel..
        IGuild guild = Main.client.getGuildByID("266063520112574464");
        IChannel channel = guild.getChannelByID("302249332244217856");

        Message.sendMessage(em.build(), "```" + error + "```", channel, Main.client);
    }
}