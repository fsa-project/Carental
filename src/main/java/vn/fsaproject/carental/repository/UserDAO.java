package vn.fsaproject.carental.repository;

import vn.fsaproject.carental.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserDAO extends JpaRepository<User,Long> {
    Optional<User> findByName(String name);
}
