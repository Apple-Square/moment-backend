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
@IdClass(CommentLikeKey.class)
@Table(name = "comment_like")
public class CommentLike extends BaseEntity {
    @Id
    @Column(name = "comment_id", nullable = false)
    private Long commentId;
    @Id
    @Column(name = "user_id", nullable = false)
    private String userId;
}
