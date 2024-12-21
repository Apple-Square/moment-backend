package applesquare.moment.like.model;

import applesquare.moment.common.model.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
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
public class CommentLike extends BaseEntity {
    @Id
    private Long commentId;
    @Id
    private String userId;
}
