package com.internship.platform.repositories;
import com.internship.platform.entities.Encadrant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
@Repository
public interface EncadrantRepository extends JpaRepository<Encadrant, Long> {
    // Spring Data JPA will auto-implement this by parsing the method name
    List<Encadrant> findByEntrepriseId(Long entrepriseId);
}

