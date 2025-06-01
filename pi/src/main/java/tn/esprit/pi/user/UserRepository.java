package tn.esprit.pi.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByEmail(String email);

    Optional<User> findById(int id);


    // In UserRepository.java
    @Modifying
    @Query(value = "DELETE FROM user_roles WHERE users_id = :userId", nativeQuery = true)
    void removeAllRolesFromUser(@Param("userId") Integer userId);

    @Modifying
    @Query(value = "INSERT INTO user_roles(users_id, roles_id) VALUES (:userId, :roleId)", nativeQuery = true)
    void addRoleToUser(@Param("userId") Integer userId, @Param("roleId") Integer roleId);

}
