package tech.apirest.mail.serviceMail;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import tech.apirest.mail.Entity.EmailType;
import tech.apirest.mail.Entity.MailEntity;
import tech.apirest.mail.Entity.Mailhandler;
import tech.apirest.mail.Entity.Users;
import tech.apirest.mail.Repo.UsersRepo;

import javax.mail.*;
import javax.mail.internet.MimeMultipart;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

@Service
public class ImapMail {
        public List<MailEntity> readEmails(String user, String password) {
            Mailhandler mailhandler=new Mailhandler();
            List<Mailhandler> mailhandlerList=new ArrayList<>();
        String host="mail.apirest.tech";
        Message[] messages = new Message[0];
        Properties properties = new Properties();
        properties.put("mail.store.protocol", "imap");
        properties.put("mail.imap.host", host);
        properties.put("mail.imap.port", "993");
        properties.put("mail.imap.ssl.enable", "true");
        properties.put("mail.imap.auth", "true"); // Force l'authentification
        properties.put("mail.imap.ssl.trust", "mail.apirest.tech"); // Confiance au certificat SSL

        Session session = Session.getInstance(properties);
            List<MailEntity> mailEntityList=new ArrayList<>();
        try (Store store = session.getStore("imap")) {
            // Connexion au serveur IMAP
            store.connect(user, password);

            // Accéder au dossier INBOX
            Folder inbox = store.getFolder("INBOX");
            if (!inbox.isOpen()) {
                inbox.open(Folder.READ_WRITE);
            }

            // Récupérer les messages
            messages = inbox.getMessages();
            System.out.println("Nombre de messages : " + messages.length);

            for (Message message : messages) {
                try {
                    System.out.println("Id unique du message : "+message.getInputStream());
                    MailEntity mailEntity=new MailEntity();
                    mailEntity.setMailUser(findLogged());
                    mailEntity.setDate(message.getSentDate().toString());
                    mailEntity.setSender(message.getFrom()[0].toString());
                    mailEntity.setSubject(message.getSubject());
                    mailEntity.setBody(getTextFromMessage(message));
                    mailEntity.setIsRead(false);
                    mailEntity.setJoinedName("");
                    mailEntity.setPathJoined("");
                    mailEntity.setType(EmailType.RECU);
                    mailEntityList.add(mailEntity);






                } catch (MessagingException e) {
                    System.err.println("Erreur lors de l'affichage d'un message : " + e.getMessage());
                    e.printStackTrace();
                }
            }

            for (Mailhandler mailhandler1:mailhandlerList){
                System.out.println("Apres boucle form imap : "+mailhandler1.getBody().toString());
            }

            // Fermer le dossier
            inbox.close(false);
        } catch (MessagingException e) {
            System.err.println("Erreur de connexion ou d'accès au serveur IMAP : " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Erreur générale : " + e.getMessage());
            e.printStackTrace();
        }
        return mailEntityList;
    }

    public String getTextFromMessage(Message message) throws MessagingException, IOException {
        if (message.isMimeType("text/plain")) {
            return message.getContent().toString();
        } else if (message.isMimeType("multipart/*")) {
            MimeMultipart mimeMultipart = (MimeMultipart) message.getContent();
            return getTextFromMimeMultipart(mimeMultipart);


        }
        return "";
    }
    private String getTextFromMimeMultipart(MimeMultipart mimeMultipart) throws MessagingException, IOException {
        StringBuilder result = new StringBuilder();
        int count = mimeMultipart.getCount();
        for (int i = 0; i < count; i++) {
            BodyPart bodyPart = mimeMultipart.getBodyPart(i);

            // Vérifier si la partie est du texte brut
            if (bodyPart.isMimeType("text/plain")) {
                result.append(bodyPart.getContent());

                // Vérifier si la partie est du HTML
            } else if (bodyPart.isMimeType("text/html")) {
                result.append(bodyPart.getContent());

                // Vérifier si la partie est une pièce jointe (fichier)
            } else if (bodyPart.getDisposition() != null && bodyPart.getDisposition().equals(Part.ATTACHMENT)) {
                // Extraire les informations de la pièce jointe
                String fileName = bodyPart.getFileName();
                String contentType = bodyPart.getContentType();
                System.out.println("Pièce jointe trouvée : " + fileName + " (" + contentType + ")");

                // Vous pouvez maintenant décider de stocker cette pièce jointe ou de la traiter.
                // Par exemple, sauvegarder le fichier sur le serveur.

                // Pour récupérer le contenu du fichier (comme un flux binaire), vous pouvez faire :
                InputStream inputStream = bodyPart.getInputStream();
               // saveAttachment(inputStream, fileName);
            } else if (bodyPart.getContent() instanceof MimeMultipart) {
                // Gérer les parties multipart imbriquées
                result.append(getTextFromMimeMultipart((MimeMultipart) bodyPart.getContent()));
            }
        }
        return result.toString();
    }

//    private void saveAttachment(InputStream inputStream, String fileName) throws IOException {
//        // Par exemple, vous pouvez sauvegarder l'attachement dans un répertoire spécifique
//        Path path = Paths.get("/path/to/save/attachments", fileName);
//        Files.copy(inputStream, path, StandardCopyOption.REPLACE_EXISTING);
//        System.out.println("Pièce jointe enregistrée sous : " + path.toString());
//    }
    @Autowired
    UsersRepo usersRepo;
public Users findLogged() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication != null) {
        boolean isAdmin = false;

        String utilisateur = ((UserDetails) authentication.getPrincipal()).getUsername();


        Users appUser1 = usersRepo.findByUserid(utilisateur);

        return appUser1;

    } else return null;
}

}
