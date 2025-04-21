package com.internship.platform.repositories;

import com.internship.platform.entities.Offre;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OffreRepository extends JpaRepository<Offre, Long> {
    List<Offre> findByDisponibleTrue();
}
