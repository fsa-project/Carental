package vn.fsaproject.carental.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.fsaproject.carental.entities.Feedback;

import java.util.List;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    List<Feedback> findByBooking_CarId(Long carId, Pageable pageable);
    List<Feedback> findByBooking_CarId(Long carId);
}
