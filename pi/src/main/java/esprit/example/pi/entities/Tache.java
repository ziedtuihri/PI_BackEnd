package esprit.example.pi.entities;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Tache {


        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        Long idTache;
        @OneToMany(mappedBy = "tache", cascade = CascadeType.ALL)
        private List<Note> notes;

}
