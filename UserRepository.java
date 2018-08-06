package ee.coop.core.repository;

import ee.coop.core.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {

    User findOneById(Long id);

    List<User> findAllByRole(String role);

    User findByUsername(String adminName);
}
