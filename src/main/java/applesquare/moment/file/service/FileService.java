package applesquare.moment.file.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;


public interface FileService {
    String upload(MultipartFile file);
    Resource read(String filename) throws FileNotFoundException;
    void delete(String filename) throws IOException;

    String getContentType(Resource resource) throws IOException;
}
