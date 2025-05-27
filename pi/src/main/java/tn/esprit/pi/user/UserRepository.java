package tn.esprit.pi.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByEmail(String email);

    Optional<User> findById(int id);

    @Query(value = "SELECT u.* FROM user_roles ur, user u where u.id=ur.users_id and ur.roles_id=3",nativeQuery = true)
    List<User> findUsersByRoles();

}
