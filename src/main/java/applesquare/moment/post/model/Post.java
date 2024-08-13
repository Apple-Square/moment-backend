package applesquare.moment.post.model;

import applesquare.moment.common.model.BaseEntity;
import applesquare.moment.post.service.PostService;
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
}
