package tn.esprit.pi.user;

import org.springframework.data.jpa.repository.JpaRepository;

import javax.swing.text.html.Option;
import java.util.Optional;

public interface TokenRepository extends JpaRepository<ActivationCode, Integer> {
    Optional<ActivationCode> findByCodeNumber(String codeNumber);
}
