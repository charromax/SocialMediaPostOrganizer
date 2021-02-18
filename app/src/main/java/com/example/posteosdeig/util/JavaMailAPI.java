package com.example.posteosdeig.util;

import android.content.Context;
import android.os.AsyncTask;

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import static com.example.posteosdeig.ui.colecciones.ColeccionesFragmentKt.sMAIL;
import static com.example.posteosdeig.ui.colecciones.ColeccionesFragmentKt.sPWD;

public class JavaMailAPI extends AsyncTask<Void, Void, Void> {

    private Session session;
    private String subject, message, email;

    public JavaMailAPI(Context context, String subject, String message, String email) {
        this.subject = subject;
        this.message = message;
        this.email = email;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        session = Session.getDefaultInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(sMAIL, sPWD);
            }
        });
        MimeMessage mimeMessage = new MimeMessage(session);
        try {
            mimeMessage.setFrom(new InternetAddress(sMAIL));
            mimeMessage.addRecipients(Message.RecipientType.TO, String.valueOf(new InternetAddress(email)));
            mimeMessage.setSubject(subject);
            mimeMessage.setText(message);
            Transport.send(mimeMessage);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void sendMail(Context context, String subject, String message, String email) {
        JavaMailAPI api = new JavaMailAPI(
                context, subject, message, email
        );
        api.execute();
    }
}
