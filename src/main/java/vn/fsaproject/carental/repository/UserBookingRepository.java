package vn.fsaproject.carental.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.fsaproject.carental.entities.UserBooking;

@Repository
public interface UserBookingRepository extends JpaRepository<UserBooking, Long> {
}
