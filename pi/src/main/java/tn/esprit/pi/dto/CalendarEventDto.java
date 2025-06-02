package tn.esprit.pi.dto;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class CalendarEventDto {
    private Long id; // This is the field name
    private String title;
    private String start;
    private String end;
    private String color;
    private String category;
    private String description;
}