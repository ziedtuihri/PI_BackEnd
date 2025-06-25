package tn.esprit.pi.entities.enumerations;

public enum SprintStatus {
    PLANNED,        // The sprint is planned, not yet started
    IN_PROGRESS,    // The sprint is currently running
    INPROGRESS,

    COMPLETED,      // The sprint has finished its work (e.g., all tasks done, but might be pending review/retrospective)
    CLOSED,         // The sprint is officially closed, all associated activities are complete and it's archived
    OVERDUE,        // The sprint has passed its end date without being completed
    CANCELLED       // The sprint has been cancelled
}
