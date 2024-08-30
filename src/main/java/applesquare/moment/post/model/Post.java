package applesquare.moment.post.model;

import applesquare.moment.common.model.BaseEntity;
import applesquare.moment.file.model.StorageFile;
import applesquare.moment.post.service.PostService;
import applesquare.moment.tag.model.Tag;
import applesquare.moment.user.model.UserInfo;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Set;

@Entity
@Getter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Post extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(length = PostService.MAX_CONTENT_LENGTH, nullable = true, updatable = true)
    private String content;
    @Column(nullable = false, updatable = true)
    private int viewCount;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="writer_id", nullable = false, updatable = false)
    private UserInfo writer;
    @OneToMany(fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private Set<StorageFile> files;
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "post_tags",
            joinColumns = @JoinColumn(name = "post_id"),
            inverseJoinColumns = @JoinColumn(name = "tags_id")
    )
    private Set<Tag> tags;
}
