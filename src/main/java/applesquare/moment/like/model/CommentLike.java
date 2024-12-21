package applesquare.moment.like.model;

import applesquare.moment.common.model.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity(name = "CommentLike")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@IdClass(CommentLikeKey.class)
@Table(name = "comment_like")
public class CommentLike extends BaseEntity {
    @Id
    private Long commentId;
    @Id
    private String userId;
}
