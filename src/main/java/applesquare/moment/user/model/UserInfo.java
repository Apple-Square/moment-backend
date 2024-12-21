package applesquare.moment.user.model;

import applesquare.moment.common.model.BaseEntity;
import applesquare.moment.file.model.StorageFile;
import applesquare.moment.user.service.UserInfoService;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Getter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user_info",
        uniqueConstraints = {
        @UniqueConstraint(columnNames = "nickname")
})
public class UserInfo extends BaseEntity {
    @Id
    @Column(name = "id", length= UserInfoService.USER_ID_LENGTH, nullable = false, updatable = false)
    private String id;
    @Column(name = "nickname", length= UserInfoService.MAX_NICKNAME_LENGTH, nullable = false, updatable = true)
    private String nickname;
    @Column(name = "birth", nullable=true, updatable=true)
    private LocalDate birth;
    @Enumerated(EnumType.STRING)
    @Column(name = "gender", nullable=true, updatable=true)
    private Gender gender;
    @Column(name = "address", nullable=true, updatable = true)
    private String address;
    @Builder.Default
    @Column(name = "intro", nullable = false, updatable = true)
    private String intro="";
    @OneToOne(cascade = CascadeType.ALL,
            orphanRemoval = true)
    @JoinColumn(name = "profile_image_id", nullable = true, updatable = true)
    private StorageFile profileImage;
    @Column(name = "social")
    private boolean social;
}
