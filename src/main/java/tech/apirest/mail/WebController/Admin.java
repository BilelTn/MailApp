package tech.apirest.mail.WebController;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import tech.apirest.mail.Entity.*;
import tech.apirest.mail.Repo.MailRepo;
import tech.apirest.mail.Repo.TransportRepo;
import tech.apirest.mail.Repo.UsersRepo;
import tech.apirest.mail.Repo.VirtualRepo;
import tech.apirest.mail.serviceMail.EmailController;
import tech.apirest.mail.serviceMail.ImapMail;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.persistence.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.transaction.Transactional;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@Transactional
public class Admin {
    private final TransportRepo transportRepo;
    private final VirtualRepo virtualRepo;
    private final UsersRepo usersRepo;
    private final MailRepo mailRepo;
    private EmailController emailController;
    private ImapMail imapMail;
    public Admin(TransportRepo transportRepo, VirtualRepo virtualRepo, UsersRepo usersRepo, MailRepo mailRepo, EmailController emailController, ImapMail imapMail) {
        this.transportRepo = transportRepo;
        this.virtualRepo = virtualRepo;
        this.usersRepo = usersRepo;
        this.mailRepo = mailRepo;
        this.emailController = emailController;
        this.imapMail = imapMail;
    }
    public Users findLogged() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            boolean isAdmin = false;

            String utilisateur = ((UserDetails) authentication.getPrincipal()).getUsername();


            Users appUser1 = usersRepo.findByUserid(utilisateur);

            return appUser1;

        } else return null;
    }
    @GetMapping(path = "/login")
    public String loginWeb(Model model) {
        LogInfo logInfo = new LogInfo("", "");
        model.addAttribute("log", logInfo);
        model.addAttribute("hello", "hello world");
        return "login";
    }

    @PostMapping(path = "/login")
    public String logMailUser(Authentication authentication) {
        System.out.println("Acceec a l url");


        return "login";
    }
    @GetMapping(path = "/CreateAccount")
    public  String CreateAccount(Model model,
                                 @RequestParam(value = "us",defaultValue = "")String us,
                                 @RequestParam(value = "name",defaultValue = "")String name,
                                 @RequestParam(value = "exist",defaultValue = "false")String exist){
        Users users=new Users();
        model.addAttribute("exist",exist);

        model.addAttribute("users",users);
        model.addAttribute("us",us);
        model.addAttribute("name",name);
        return "createAccount";
    }
    @PostMapping(value = "/createMail")
    public String createMail(Model model,@ModelAttribute(value = "user")Users users){
        if(users!=null) {

            Users users1 = usersRepo.findByUserid(users.getUserid() + "@apirest.tech");
            if (users1 != null) {

                return "redirect:/CreateAccount?us=" + users.getUserid() + "&name=" + users.getRealname() + "&&exist=" + true;
            }
            PasswordEncoder encoder=new BCryptPasswordEncoder();
            Users users2=new Users();

            users2.setUid(1000);
            users2.setGid(1000);
            users2.setPassword(encoder.encode(users.getPassword()));
            users2.setUserid(users.getUserid() + "@apirest.tech");
            users2.setRealname(users.getRealname());
            users2.setMail(users.getUserid() + "@apirest.tech");
            users2.setHome("apirest.tech/"+users.getUserid()+"/");
            users2.setTt(users.getPassword());
            //System.out.println(users2);
            usersRepo.save(users2);
            Transport transport=new Transport();
            transport.setTransport("virtual:");
            transport.setDomain(users.getUserid() + "@apirest.tech");
            transportRepo.save(transport);
            //System.out.println(transport);
            Virtual virtual=new Virtual();
            virtual.setAddress(users.getUserid() + "@apirest.tech");
            virtual.setUserid(users.getUserid() + "@apirest.tech");
            // System.out.println(virtual);
            virtualRepo.save(virtual);

        }
        return "redirect:/login";
    }

    @GetMapping(value = "/accueilMail")
    public String accueilMail(Model model, HttpSession session, Authentication authentication, HttpServletRequest request, HttpServletResponse response) throws MessagingException, IOException {
        String us=((UserDetails)  authentication.getPrincipal()).getUsername();
        System.out.println("Users found "+us);
        BCryptPasswordEncoder encoder=new BCryptPasswordEncoder();
//encoder.
        System.out.println("mdp trouvé  :"+findLogged().getPassword());
        List<MailEntity> mailEntityListImap=imapMail.readEmails(findLogged().getUserid(),findLogged().getTt());
        List<MailEntity> mailEntityList=mailRepo.findAllByMailUser(findLogged());

        System.out.println("Taille de la table spring : "+mailEntityList.size());
        System.out.println( " taille du tableau : " +mailEntityListImap.size());
        int size=mailEntityListImap.size();
        List<Message> messages1=new ArrayList<>();
//for(MailEntity mailhandler:mailhandlerList){
//    messages1.add(mailhandler.getMessages());
//}
        Set<String> existingDate=mailEntityList.stream().map(MailEntity::getDate).collect(Collectors.toSet());
        System.out.println("existing date avant ajout"+existingDate);
        model.addAttribute("size",size);
        for (MailEntity mail:mailEntityListImap){
            if (!existingDate.contains(mail.getDate())){
                mailRepo.save(mail);
                System.out.println("Date nouveau trouvé"+mail.getDate());
                existingDate.add(mail.getDate());
                System.out.println(existingDate);

            }
            System.out.println(mail);
        }

        model.addAttribute("messages",mailEntityList);


//        for (Mailhandler mailhandler:mailhandlerList) {
//            System.out.println("Entree dans for message : "+mailhandler.getMessages().getSentDate().toString());
//
//                if(!existingDate.contains(mailhandler.getMessages().getSentDate().toString())){
//                    System.out.println("Condition date if juste");
//                    MailEntity mailEntity=new MailEntity();
//                    mailEntity.setMailUser(findLogged());
//                    mailEntity.setDate(mailhandler.getMessages().getSentDate().toString());
//                    mailEntity.setSender(String.valueOf(mailhandler.getMessages().getFrom()[0]));
//                    mailEntity.setSubject(mailhandler.getMessages().getSubject());
//                    mailEntity.setBody(imapMail.getTextFromMessage(mailhandler.getMessages()));
//                    mailEntity.setIsRead(false);
//                    mailEntity.setJoinedName("");
//                    mailEntity.setPathJoined("");
//                    mailEntity.setType(EmailType.RECU);
//                    existingDate.add(mailhandler.getMessages().getSentDate().toString());
//                    System.out.println("existing date apres ajout"+existingDate);
//
//                    System.out.println("Corps du mailhandler : "+mailEntity.getBody());
//                   System.out.println("Enregistré avec sucess .. ");


        //}
//            else
//                {
//                    System.out.println("Date deja existant : "+mailhandler.getMessages().getSentDate().toString());
//                }
        System.out.println("Sortie de for message");

        //  }

        return "accueilMail";
    }
    @GetMapping(value = "/new")
    public String newMail(Model model){
        MailDetails mailDetails=new MailDetails();
        model.addAttribute("mailDetails",mailDetails);
        return "newMail";
    }
    @PostMapping(value = "/sendMail")
    public String sendMail(Model model,@ModelAttribute(value ="mailDetails" )MailDetails mailDetails){
        System.out.println(mailDetails);
        emailController.sendEmail(mailDetails.to,mailDetails.subject,mailDetails.message,"bilel@apirest.tech","123456");
        return "redirect:/new";
    }
    @GetMapping(value = "/inbox")
    public String inboxById(Model model, @RequestParam(value = "id" ,defaultValue = "")Integer id) throws MessagingException {
        List<MailEntity> message=imapMail.readEmails(findLogged().getUserid(), findLogged().getTt());
//        Message message1=message.get
        //        System.out.println(message1.getSubject());
//        for (Message message2:message){
//            System.out.println(message2.getMessageNumber());
//        }

        // model.addAttribute("message",message2);
        return "Inbox";
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    static class LogInfo {
        private String login;
        private String password;
    }
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    static class MailDetails{
        private String to;
        private String subject;
        private String message;
    }
}