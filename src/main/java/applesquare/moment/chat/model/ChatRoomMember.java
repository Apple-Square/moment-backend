package applesquare.moment.chat.model;

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
@Table(name = "chat_room_member",
        uniqueConstraints = @UniqueConstraint(columnNames = {"chat_room_id", "user_info_id"}))
public class ChatRoomMember {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id", nullable = false, updatable = false)
    private ChatRoom chatRoom;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_info_id", nullable = false, updatable = false)
    private UserInfo user;
    @Column(name = "last_read_message_id", nullable = true, updatable = true)
    private Long lastReadMessageId; // 가장 최근에 읽은 메시지의 ID
    @Builder.Default
    @Column(name = "notification_enabled", nullable = false, updatable = true)
    private boolean notificationEnabled = true;
}
