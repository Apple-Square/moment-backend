package applesquare.moment.file;

import applesquare.moment.file.model.FileAccessGroupType;
import applesquare.moment.file.model.FileAccessPolicy;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class StorageFileReadResponseDTO {
    private Long id;
    private String filename;
    private String originalFilename;
    private String contentType;
    private Long fileSize;
    private String uploaderId;
    private FileAccessPolicy accessPolicy;
    private FileAccessGroupType groupType;
    private String groupId;
}
