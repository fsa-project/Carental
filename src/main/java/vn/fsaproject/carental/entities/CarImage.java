package vn.fsaproject.carental.entities;

import jakarta.persistence.*;

@Entity
public class CarImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String filePath; // Đường dẫn lưu trữ file ảnh trên server

    @ManyToOne
    @JoinColumn(name = "car_id", nullable = false) // car_id là khóa ngoại
    private Car car;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public Car getCar() {
        return car;
    }

    public void setCar(Car car) {
        this.car = car;
    }
}
