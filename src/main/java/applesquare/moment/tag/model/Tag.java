package applesquare.moment.tag.model;

import applesquare.moment.post.model.Post;
import applesquare.moment.tag.service.TagService;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Tag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(length = TagService.MAX_TAG_NAME_LENGTH, nullable = false, updatable = false)
    private String name;
    @ManyToMany(mappedBy = "tags", fetch = FetchType.LAZY)
    private List<Post> posts;
}