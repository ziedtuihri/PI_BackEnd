package tn.esprit.pi.role;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Integer> {


    Optional<Role> findByName(String role);

    // In RoleRepository.java
    @Query("SELECT r FROM Role r JOIN r.users u WHERE u.email = :email")
    List<Role> findRolesByUserEmail(@Param("email") String email);
}
