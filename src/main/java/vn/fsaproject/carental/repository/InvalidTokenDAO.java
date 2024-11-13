package vn.fsaproject.carental.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.fsaproject.carental.entities.InvalidToken;

@Repository
public interface InvalidTokenDAO extends JpaRepository<InvalidToken, String> {
}
