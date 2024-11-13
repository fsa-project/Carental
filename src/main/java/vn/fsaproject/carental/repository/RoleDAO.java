package vn.fsaproject.carental.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.fsaproject.carental.entities.Role;

public interface RoleDAO extends JpaRepository<Role,Long> {
    Role findByName(String name);
}
