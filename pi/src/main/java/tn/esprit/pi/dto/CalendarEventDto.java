package tn.esprit.pi.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CalendarEventDto {
    private String title;
    private LocalDate start;
    private LocalDate end;
}
