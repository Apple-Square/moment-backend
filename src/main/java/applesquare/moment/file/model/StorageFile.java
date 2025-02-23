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
    @Enumerated(EnumType.STRING)
    @Column(name = "access_policy", nullable = false)
    private FileAccessPolicy accessPolicy; // 파일 접근 정책 (읽기 권한)
    @Column(name = "group_type", nullable = true)
    private FileAccessGroupType groupType;  // 파일이 속한 그룹 유형
    @Column(name = "group_id", nullable = true)
    private Long groupId; // 파일이 속한 그룹 ID

    /**
     * TO DO :
     * 사실 확장성 좋게 하려면 파일 별로 (동작, 권한 수준)을 저장한 별도의 테이블을 구성해야 한다.
     * (ex. 3번 파일 - read - public)
     * 하지만, 앞으로 write 혹은 그 이외의 동작에 권한 수준을 설정할 일이 없는 토이 프로젝트이기 때문에
     * 파일 접근 권한을 간단하게 구현했습니다.
     */
}
