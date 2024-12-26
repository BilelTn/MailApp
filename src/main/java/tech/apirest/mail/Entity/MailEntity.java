package tech.apirest.mail.Entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table
public class MailEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String sender;
    private String subject;
    private String date;
    @Lob
    private String body;
    private Boolean isRead=false;
    private String joinedName;
    private String pathJoined;
    @Enumerated(EnumType.STRING)
    private EmailType type;
    @ManyToOne
    @JoinColumn(name = "user_id")
    private Users mailUser;
}
