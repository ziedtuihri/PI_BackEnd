package esprit.example.pi.repositories;

import esprit.example.pi.entities.Sprint;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SprintRepo extends JpaRepository <Sprint ,Long>
{
    List<Sprint> findByNomContainingIgnoreCase(String nom);
}
