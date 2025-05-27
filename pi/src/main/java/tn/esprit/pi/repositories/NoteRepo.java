package tn.esprit.pi.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.pi.entities.Note;

import java.util.List;

public interface NoteRepo extends JpaRepository<Note,Long> {


    List<Note> findByEvaluationIdEvaluation(Long evaluationId);

    List<Note> findByEvaluation_Projet_IdProjetAndSprint_User_Id(Long projetId, Integer userId);

    List<Note> findBySprint_User_Id(Integer userId);

    List<Note> findByUser_Id(Integer userId);
    List<Note> findBySprint_Projet_IdProjetAndUser_Id(Long projetId, Integer userId);



}
