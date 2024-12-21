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
    @Column(name = "id")
    private Long id;
    @Column(name = "filename", nullable = false, updatable = false)
    private String filename;
    @Column(name = "original_filename", nullable = false, updatable = false)
    private String originalFilename;
    @Column(name = "content_type", nullable = false, updatable = false)
    private String contentType;
    @Column(name = "file_size", nullable=false, updatable = false)
    private Long fileSize;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploader_id", nullable = false, updatable = false)
    private UserInfo uploader;
}
