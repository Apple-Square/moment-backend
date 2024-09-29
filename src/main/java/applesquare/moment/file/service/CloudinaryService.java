package applesquare.moment.file.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface CloudinaryService {
    byte[] convertVideoToThumbnail(MultipartFile video, String thumbFilename, int width, int videoLength) throws IOException;
}
