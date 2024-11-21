package vn.fsaproject.carental.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.fsaproject.carental.entities.Booking;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
}
