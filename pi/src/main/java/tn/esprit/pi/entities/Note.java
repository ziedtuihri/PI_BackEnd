package tn.esprit.pi.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import tn.esprit.pi.user.User;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Note {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idNote;
    private double valeur;

    /*
    @ManyToOne
    @JoinColumn(name = "evaluation_id")
    private Evaluation evaluation;  */

    @ManyToOne
    @JoinColumn(name = "evaluation_id")
    @JsonIgnore
    private Evaluation evaluation;


    @ManyToOne
    @JoinColumn(name = "sprint_id")
    private Sprint sprint;

    // relation entre note et user
    @ManyToOne
    @JoinColumn(name = "user_id") // Colonne dans la table Note pointant vers l'ID de User
    private User user;





}