package vn.fsaproject.carental.repository;

import vn.fsaproject.carental.entities.Booking;
import vn.fsaproject.carental.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookingDAO extends JpaRepository<Booking,Long> {
}
