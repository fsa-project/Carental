package vn.fsaproject.carental.repository;

import vn.fsaproject.carental.entities.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FeedbackDAO extends JpaRepository<Feedback,Long> {
}
