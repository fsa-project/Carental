package vn.fsaproject.carental.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.fsaproject.carental.entities.CarDocument;

@Repository
public interface CarDocumentRepository extends JpaRepository<CarDocument, Long> {
}
