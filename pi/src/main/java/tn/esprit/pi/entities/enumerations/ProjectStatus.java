package tn.esprit.pi.entities.enumerations;

public enum ProjectStatus {
    NOTSTARTED,
    PLANNED,      // Initial state, project not yet started
    IN_PROGRESS,  // Actively being worked on
    COMPLETED,    // All work is done and approved
    OVERDUE,      // Past its planned end date but not yet completed
    CANCELLED     // Project has been stopped
}