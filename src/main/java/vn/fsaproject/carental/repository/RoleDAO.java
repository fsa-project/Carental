package vn.fsaproject.carental.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.fsaproject.carental.entities.Role;

import java.util.Optional;

public interface RoleDAO extends JpaRepository<Role,Long> {
    Optional<Role> findByName(String name);
}
