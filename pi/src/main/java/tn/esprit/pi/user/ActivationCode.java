package tn.esprit.pi.user;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class ActivationCode {

    @Id
    @GeneratedValue
    private Integer id;

    @Column(unique = true)
    private String codeNumber;

    @Enumerated(EnumType.STRING)
    private CodeType typeCode;

    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private LocalDateTime validatedAt;

    @ManyToOne
    @JoinColumn(name = "userId", nullable = false)
    private User user;

}
