package vn.fsaproject.carental.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "car_documents")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CarDocument {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String documentName; // Tên tài liệu (Registration Paper, Insurance, v.v.)
    private String filePath;     // Đường dẫn tới file lưu trong hệ thống
    private String fileType;     // MIME type (application/pdf, image/jpeg, v.v.)

    @ManyToOne
    @JoinColumn(name = "car_id")
    private Car car; // Liên kết tới x
}
