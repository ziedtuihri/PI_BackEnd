package esprit.example.pi.repositories;

import esprit.example.pi.entities.Sprint;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SprintRepo extends JpaRepository <Sprint ,Long>
{
}
