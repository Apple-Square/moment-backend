package applesquare.moment.post.model;

import applesquare.moment.common.model.BaseEntity;
import applesquare.moment.file.model.StorageFile;
import applesquare.moment.post.service.PostManagementService;
import applesquare.moment.tag.model.Tag;
import applesquare.moment.user.model.UserInfo;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.BatchSize;

import java.util.List;

@Entity
@Getter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Post extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(length = PostManagementService.MAX_CONTENT_LENGTH, nullable = true, updatable = true)
    private String content;
    @Column(nullable = false, updatable = true)
    private long viewCount;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="writer_id", nullable = false, updatable = false)
    private UserInfo writer;
    @Column(nullable = true, updatable = true)
    private String address;
    @Column(nullable = true, updatable = true)
    private Double x;  // 경도
    @Column(nullable = true, updatable = true)
    private Double y;  // 위도
    @OneToMany(fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    @JoinTable(
            name="post_files",
            joinColumns = @JoinColumn(name = "post_id"),
            inverseJoinColumns = @JoinColumn(name = "file_id")
    )
    @BatchSize(size = 10)
    @OrderColumn(name = "file_order")
    private List<StorageFile> files;
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "post_tags",
            joinColumns = @JoinColumn(name = "post_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    @BatchSize(size = 10)
    @OrderColumn(name = "tag_order")
    private List<Tag> tags;
}
