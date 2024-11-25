package vn.fsaproject.carental.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.fsaproject.carental.dto.response.BookingResponse;
import vn.fsaproject.carental.entities.Booking;

import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    Page<Booking> findByUserId(Long userId, Pageable pageable);
    List<Booking> findByCarId(Long userId);
}
