package com.internship.platform.Controllers;

import com.internship.platform.entities.Convention;
import com.internship.platform.services.ConventionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/conventions")
@CrossOrigin(origins = "http://localhost:4200")
public class ConventionController {

    @Autowired
    private ConventionService conventionService;

    @PostMapping
    public Convention createConvention(@RequestBody Convention convention) {
        return conventionService.createConvention(convention);
    }



    @DeleteMapping("/{id}")
    public void deleteConvention(@PathVariable Long id) {
        conventionService.deleteConvention(id);
    }

    @GetMapping("/{id}")
    public Convention getConvention(@PathVariable Long id) {
        return conventionService.getConvention(id);
    }

    @GetMapping
    public List<Convention> getAllConventions() {
        return conventionService.getAllConventions();
    }
}
