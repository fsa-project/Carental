package vn.fsaproject.carental.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.fsaproject.carental.entities.Permission;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {

    boolean existsByModuleAndApiPathAndMethod(String method, String apiPath, String method1);
}
