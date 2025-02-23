package applesquare.moment.chat.model;

import applesquare.moment.common.model.BaseEntity;
import applesquare.moment.file.model.StorageFile;
import applesquare.moment.post.model.Post;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.List;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "chat_message")
public class ChatMessage extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id", nullable = false, updatable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private ChatRoom chatRoom;
    @Column(name = "sender_id", nullable = false, updatable = false)
    private String senderId;
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private ChatMessageType type;
    @Column(name = "content", nullable = true, updatable = false)
    private String content;  // 텍스트 메시지
    @OneToMany(fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    @JoinTable(
            name="chat_message_files",
            joinColumns = @JoinColumn(name = "chat_message_id"),
            inverseJoinColumns = @JoinColumn(name = "file_id")
    )
    @BatchSize(size = 10)
    @OrderColumn(name = "file_order")
    private List<StorageFile> files;  // 이미지 혹은 비디오 파일 목록
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shared_post_id", nullable = true, updatable = false)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private Post sharedPost;    // 공유된 게시물
    @Column(name = "unread_count", nullable = false, updatable = true)
    private long unreadCount;
    @Builder.Default
    @Column(name = "is_deleted", nullable = false, updatable = true)
    private boolean isDeleted=false;
}
