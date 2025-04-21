package com.internship.platform.services;

import com.internship.platform.entities.Convention;
import com.internship.platform.repositories.ConventionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ConventionService {

    @Autowired
    private ConventionRepository conventionRepository;

    public Convention createConvention(Convention convention) {
        return conventionRepository.save(convention);
    }



    public void deleteConvention(Long id) {
        conventionRepository.deleteById(id);
    }

    public Convention getConvention(Long id) {
        return conventionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Convention not found"));
    }

    public List<Convention> getAllConventions() {
        return conventionRepository.findAll();
    }
}
