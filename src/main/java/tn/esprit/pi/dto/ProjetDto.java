package tn.esprit.pi.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor; // Added NoArgsConstructor for better DTO practice

@Data
@AllArgsConstructor
@NoArgsConstructor // It's good practice to have a no-argument constructor for DTOs
public class ProjetDto {
    private Long id;
    private String nom;
}