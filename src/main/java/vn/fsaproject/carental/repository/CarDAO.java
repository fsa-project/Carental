package vn.fsaproject.carental.repository;

import vn.fsaproject.carental.entities.Car;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CarDAO extends JpaRepository<Car,Long> {
}
