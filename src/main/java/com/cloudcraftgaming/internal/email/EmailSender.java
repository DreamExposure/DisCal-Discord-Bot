package com.cloudcraftgaming.internal.email;

import com.cloudcraftgaming.internal.file.ReadFile;
import org.simplejavamail.email.Email;
import org.simplejavamail.mailer.Mailer;
import org.simplejavamail.mailer.config.TransportStrategy;

import javax.mail.Message;
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
    EmailData data;

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

    public void sendExceptionEmail(Exception e) {
        Email email = new Email();

        String timeStamp = new SimpleDateFormat("yyyy/MM/dd_HH:mm:ss").format(Calendar.getInstance().getTime());

        email.setFromAddress("DisCal", data.getUsername());
        email.addRecipient("CloudCraft", "cloudcraftcontact@gmail.com", Message.RecipientType.TO);
        email.setSubject("[DNR] DisCal Error");
        email.setText("An error occurred on: " + timeStamp + com.cloudcraftgaming.utils.Message.lineBreak + e.toString());

        mailer.sendMail(email);
    }
}