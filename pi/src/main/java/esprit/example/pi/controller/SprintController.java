package esprit.example.pi.controller;

import esprit.example.pi.entities.Sprint;
import esprit.example.pi.services.ISprintService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sprints")
public class SprintController {
    @Autowired
    private ISprintService sprintService;

    @PostMapping
    public Sprint createSprint(@RequestBody Sprint sprint) {
        return sprintService.saveSprint(sprint);
    }

    @GetMapping("/{id}")
    public Sprint getSprintById(@PathVariable Long id) {
        return sprintService.getSprintById(id);
    }
}