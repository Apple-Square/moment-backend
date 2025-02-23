package applesquare.moment.chat.model;

import applesquare.moment.common.model.BaseEntity;
import applesquare.moment.user.model.UserInfo;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "chat_room")
public class ChatRoom extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "room_name", nullable = false, updatable = true)
    private String roomName;    // 채팅방 이름
    @Enumerated(EnumType.STRING)
    @Column(name = "room_type", nullable = false, updatable = false)
    private ChatRoomType roomType;  // 채팅방 유형 (1:1 채팅, 그룹 채팅)
    @BatchSize(size = 10)   // 한 번에 10명씩 로딩
    @Builder.Default
    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<ChatRoomMember> members = new HashSet<>();   // 채팅방 멤버 목록
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "last_message_id", nullable = true, updatable = true)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private ChatMessage lastMessage;    // 가장 최근에 주고받은 메시지


    public void addMember(UserInfo member){
        ChatRoomMember chatRoomMember=ChatRoomMember.builder()
                .chatRoom(this)
                .user(member)
                .build();
        members.add(chatRoomMember);
    }

    public void removeMember(String userId){
        members.removeIf(member -> member.getUser().getId().equals(userId));
    }
}
