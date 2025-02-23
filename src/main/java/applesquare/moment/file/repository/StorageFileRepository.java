package applesquare.moment.file.repository;

import applesquare.moment.file.model.FileAccessGroupType;
import applesquare.moment.file.model.StorageFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface StorageFileRepository extends JpaRepository<StorageFile, Long> {
    List<StorageFile> findByFilename(String filename);
    void deleteByFilename(String filename);

    @Query("SELECT sf.filename " +
            "FROM StorageFile sf " +
            "WHERE sf.filename IN :filenames " +
                "AND sf.contentType LIKE CONCAT(:contentTypePrefix, '%')")
    List<String> findAllFilenameByFilenamesAndContentType(@Param("filenames") List<String> filenames,
                                                          @Param("contentTypePrefix") String contentTypePrefix);

    @Query("SELECT COALESCE(SUM(sf.fileSize), 0) " +
            "FROM StorageFile sf " +
            "WHERE sf.filename IN :filenames")
    long sumFileSizeByFilenames(@Param("filenames") List<String> filenames);

    void deleteByGroupTypeAndGroupId(FileAccessGroupType groupType, String groupId);
}