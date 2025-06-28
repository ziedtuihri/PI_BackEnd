package esprit.example.pi.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Commentaire {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idCommentaire;

    private String contenu;
    private LocalDateTime dateCommentaire;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "tache_id")
    private Tache tache;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "sprint_id")
    private Sprint sprint; // association avec Sprint
}
