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
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = "nickname")
})
public class UserInfo extends BaseEntity {
    @Id
    @Column(length= UserInfoService.USER_ID_LENGTH, nullable = false, updatable = false)
    private String id;
    @Column(length= UserInfoService.MAX_NICKNAME_LENGTH, nullable = false, updatable = true)
    private String nickname;
    @Column(nullable=true, updatable=true)
    private LocalDate birth;
    @Enumerated(EnumType.STRING)
    @Column(nullable=true, updatable=true)
    private Gender gender;
    @Column(nullable=true, updatable = true)
    private String address;
    @OneToOne(cascade = CascadeType.ALL,
            orphanRemoval = true)
    @JoinColumn(nullable = true, updatable = true)
    private StorageFile profileImage;
}
