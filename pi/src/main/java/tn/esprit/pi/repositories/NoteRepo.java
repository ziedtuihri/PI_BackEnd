package tn.esprit.pi.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tn.esprit.pi.entities.Note;

import java.util.List;
import java.util.Optional;

public interface NoteRepo extends JpaRepository<Note,Long> {


    List<Note> findByEvaluationIdEvaluation(Long evaluationId);

    List<Note> findByEvaluation_Projet_IdProjetAndSprint_User_Id(Long projetId, Integer userId);

    List<Note> findBySprint_User_Id(Integer userId);

    List<Note> findByUser_Id(Integer userId);
    List<Note> findBySprint_Projet_IdProjetAndUser_Id(Long projetId, Integer userId);

    // NoteRepo.java
    //note deja affecter
    Optional<Note> findByUser_IdAndSprint_IdSprint(Integer userId, Long sprintId);

    //calcule de moyenne
     List<Note> findByEvaluation_Projet_IdProjetAndUser_Id(Long projetId, Integer userId);










}
