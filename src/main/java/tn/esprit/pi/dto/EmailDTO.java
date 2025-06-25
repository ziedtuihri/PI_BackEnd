package tn.esprit.pi.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EmailDTO {
    @Email(message = "Email invalide")
    @NotBlank(message = "L'email est obligatoire")
    private String email;
}
