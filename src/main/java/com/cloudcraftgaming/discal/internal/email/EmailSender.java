package com.cloudcraftgaming.discal.internal.email;

import com.cloudcraftgaming.discal.internal.file.ReadFile;
import org.simplejavamail.email.Email;
import org.simplejavamail.mailer.Mailer;
import org.simplejavamail.mailer.config.TransportStrategy;

import javax.mail.Message;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by Nova Fox on 2/15/17.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
public class EmailSender {
    private static EmailSender instance;

    private Mailer mailer;
    private EmailData data;

    private EmailSender() {}

    public static EmailSender getSender() {
        if (instance == null) {
            instance = new EmailSender();
        }
        return instance;
    }

    public void init(String emailDataFile) {
        data = ReadFile.readEmailLogin(emailDataFile);

        assert data != null;

        mailer = new Mailer("smtp.gmail.com", 465, data.getUsername(), data.getPassword(), TransportStrategy.SMTP_SSL);
    }

    public void sendExceptionEmail(Exception e, Class clazz) {
        Email email = new Email();

        String timeStamp = new SimpleDateFormat("yyyy/MM/dd-HH:mm:ss").format(Calendar.getInstance().getTime());
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

        email.setFromAddress("DisCal", data.getUsername());
        email.addRecipient("CloudCraft", "cloudcraftcontact@gmail.com", Message.RecipientType.TO);
        email.setSubject("[DNR] DisCal Error");
        email.setText("An error occurred on: " + timeStamp + com.cloudcraftgaming.discal.utils.Message.lineBreak + "In class: " + clazz.getName()  + com.cloudcraftgaming.discal.utils.Message.lineBreak + com.cloudcraftgaming.discal.utils.Message.lineBreak + error);

        mailer.sendMail(email);
    }

    public void sendDebugEmail(Class clazz, String step, String message) {
        Email email = new Email();

        String timeStamp = new SimpleDateFormat("yyyy/MM/dd-HH:mm:ss").format(Calendar.getInstance().getTime());

        email.setFromAddress("DisCal", data.getUsername());
        email.addRecipient("CloudCraft", "cloudcraftcontact@gmail.com", Message.RecipientType.TO);
        email.setSubject("[DNR] DisCal Debugger");
        email.setText("Debug message at: " + timeStamp + com.cloudcraftgaming.discal.utils.Message.lineBreak + "In class: " + clazz.getName() + com.cloudcraftgaming.discal.utils.Message.lineBreak + com.cloudcraftgaming.discal.utils.Message.lineBreak + "Debug Method Step: " + step + com.cloudcraftgaming.discal.utils.Message.lineBreak + com.cloudcraftgaming.discal.utils.Message.lineBreak + "Debug Message: " + message);

        mailer.sendMail(email);
    }
}