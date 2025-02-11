package applesquare.moment.notification.model;

import applesquare.moment.common.model.BaseEntity;
import applesquare.moment.user.model.UserInfo;
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
@Table(name="notification")
public class Notification extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private  Long id;
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, updatable = false)
    private NotificationType type;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false, updatable = false)
    private UserInfo sender;
    @Column(name = "title", nullable = true, updatable = true)
    private String title;
    @Column(name = "content", nullable = true, updatable = true)
    private String content;
    @Column(name = "reference_id", nullable = true, updatable = true)
    private String referenceId;
    @OneToMany(mappedBy = "notification",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private List<UserNotification> userNotifications;


    // referenceId를 Long 타입으로 변환
    public Long getReferenceIdAsLong() {
        try {
            return referenceId != null && !referenceId.isBlank()? Long.valueOf(referenceId) : null;
        } catch (NumberFormatException e) {
            e.printStackTrace();
            throw e;
        }
    }
}