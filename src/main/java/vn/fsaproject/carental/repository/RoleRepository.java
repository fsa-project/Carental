package vn.fsaproject.carental.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.fsaproject.carental.entities.Role;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

}
