package applesquare.moment.like.model;

import applesquare.moment.common.model.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@IdClass(PostLikeKey.class)
@Table(name = "post_like")
public class PostLike extends BaseEntity {
    @Id
    @Column(name = "post_id", nullable = false)
    private Long postId;
    @Id
    @Column(name = "user_id", nullable = false)
    private String userId;
}
