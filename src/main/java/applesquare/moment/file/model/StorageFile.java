package applesquare.moment.file.model;

import applesquare.moment.common.model.BaseEntity;
import applesquare.moment.user.model.UserInfo;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "storage_file")
public class StorageFile extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, updatable = false)
    private String filename;
    @Column(nullable = false, updatable = false)
    private String originalFilename;
    @Column(nullable = false, updatable = false)
    private String contentType;
    @Column(nullable=false, updatable = false)
    private Long fileSize;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploader_id", nullable = false, updatable = false)
    private UserInfo uploader;
}
