package applesquare.moment.post.service.impl;

import applesquare.moment.address.dto.AddressSearchResponseDTO;
import applesquare.moment.address.service.AddressService;
import applesquare.moment.comment.repository.CommentRepository;
import applesquare.moment.common.service.SecurityService;
import applesquare.moment.file.model.StorageFile;
import applesquare.moment.file.repository.StorageFileRepository;
import applesquare.moment.file.service.FileService;
import applesquare.moment.post.dto.PostCreateRequestDTO;
import applesquare.moment.post.dto.PostUpdateRequestDTO;
import applesquare.moment.post.model.Post;
import applesquare.moment.post.repository.PostRepository;
import applesquare.moment.post.service.PostService;
import applesquare.moment.tag.model.Tag;
import applesquare.moment.tag.service.TagService;
import applesquare.moment.user.model.UserInfo;
import applesquare.moment.user.repository.UserInfoRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Log4j2
@Service
@Transactional
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final UserInfoRepository userInfoRepository;
    private final StorageFileRepository storageFileRepository;
    private final SecurityService securityService;
    private final FileService fileService;
    private final TagService tagService;
    private final AddressService addressService;


    // 허용되는 MIME 타입 이미지 목록
    private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList(
            "image/jpeg",  // JPEG 이미지
            "image/png",   // PNG 이미지
            "image/webp"   // WEBP 이미지
    );

    // 허용되는 MIME 타입 비디오 목록
    private static final List<String> ALLOWED_VIDEO_TYPES = Arrays.asList(
            "video/mp4",   // MP4 비디오
            "video/webm"   // WEBM 비디오
    );


    /**
     * 게시글 생성
     * [필요 권한 : 로그인 상태]
     *
     * @param postCreateRequestDTO 게시글 생성 정보
     * @return 새로 생성된 게시글의 ID
     */
    @Override
    public Long create(PostCreateRequestDTO postCreateRequestDTO){
        // 권한 검사
        String userId= securityService.getUserId();

        // 입력 형식 검사
        List<MultipartFile> files=postCreateRequestDTO.getFiles();
        validatePostCreateFileInput(files);

        // 작성자 정보 가져오기
        UserInfo writer=userInfoRepository.findById(userId)
                .orElseThrow(()-> new EntityNotFoundException("존재하지 않는 사용자입니다. (id = "+userId+")"));

        // 태그 등록하기
        List<String> tagNames=postCreateRequestDTO.getTags();
        Set<Tag> tags=new LinkedHashSet<>();
        if(tagNames!=null){
            for(String tagName : tagNames){
                // 기존에 있던 태그면 가져오고, 없는 태그면 생성하기
                Tag tag=tagService.readByName(tagName);
                tags.add(tag);
            }
        }

        // 위치 정보 등록하기
        String address=postCreateRequestDTO.getAddress();
        String addressName=null;
        Double x=null;
        Double y=null;
        if(address!=null){
            AddressSearchResponseDTO addressSearchResponseDTO =addressService.searchAddress(address);
            if(addressSearchResponseDTO==null){
                throw new IllegalArgumentException("존재하지 않는 주소입니다. 정확한 주소를 입력해주세요.");
            }
            addressName= addressSearchResponseDTO.getAddressName();
            x=Double.parseDouble(addressSearchResponseDTO.getX());
            y=Double.parseDouble(addressSearchResponseDTO.getY());
        }

        // 첨부파일 처리
        Set<StorageFile> storageFiles=new LinkedHashSet<>();
        try{
            for(MultipartFile file : files){
                // 저장소에 파일 저장
                String url=fileService.upload(file);

                // StorageFile 엔티티 생성
                StorageFile storageFile=StorageFile.builder()
                        .filename(fileService.convertUrlToFilename(url))
                        .originalFilename(file.getOriginalFilename())
                        .contentType(file.getContentType())
                        .fileSize(file.getSize())
                        .uploader(writer)
                        .ord(storageFiles.size())
                        .build();

                storageFiles.add(storageFile);
            }

            // Post 엔티티 생성
            Post post=Post.builder()
                    .content(postCreateRequestDTO.getContent())
                    .viewCount(0)
                    .writer(writer)
                    .address(addressName)
                    .x(x)
                    .y(y)
                    .files(storageFiles)
                    .tags(tags)
                    .build();

            // DB 저장
            Post result=postRepository.save(post);

            // 리소스 ID 반환
            return result.getId();

        } catch (Exception exception){
            // 만약 게시글을 등록하던 도중 문제가 생긴다면, 저장소에 업로드한 파일을 삭제해야 한다.
            log.error(exception.getMessage());
            try{
                // 저장소에 업로드한 파일 삭제
                for(StorageFile storageFile : storageFiles){
                    fileService.delete(storageFile.getFilename());
                }
            } catch (IOException ioException){
                // 만약 삭제하는 것도 실패했다면, 로그를 남긴다.
                log.error(ioException.getMessage());
            }

            throw exception;
        }
    }

    /**
     * 특정 게시글 수정
     * [필요 권한 : 로그인 상태 & 게시글 소유자]
     *
     * @param postId 게시글 ID
     * @param postUpdateRequestDTO 게시글 변경 정보
     * @return 수정된 게시글의 ID
     */
    @Override
    public Long update(Long postId, PostUpdateRequestDTO postUpdateRequestDTO) {
        // DB에 저장된 이전 게시글 엔티티 가져오기
        Post oldPost=postRepository.findById(postId)
                .orElseThrow(()-> new EntityNotFoundException("존재하지 않는 게시글입니다. (id = "+postId+")"));
        Set<StorageFile> oldStorageFiles=oldPost.getFiles();
        Set<Tag> oldTags=oldPost.getTags();

        // 권한 검사
        String userId= securityService.getUserId();
        if(!isOwner(oldPost, userId)){
            throw new AccessDeniedException("게시글 작성자만 수정할 수 있습니다.");
        }

        // 입력 형식 검사
        List<String> urls=postUpdateRequestDTO.getUrls();
        List<String> urlFilenames=new LinkedList<>();
        if(urls!=null){
            // 입력으로 파일 URL 목록이 들어온 경우 (파일 URL 목록에 변화가 있는 경우)
            for(String url : urls){
                urlFilenames.add(fileService.convertUrlToFilename(url));
            }
        }
        else{
            // 만약 파일 URL 목록에 변화가 없다면 기존 StorageFile 그대로 가져오기
            for(StorageFile storageFile : oldStorageFiles){
                urlFilenames.add(storageFile.getFilename());
            }
        }

        // 파일 입력 형식 검사
        List<MultipartFile> files=postUpdateRequestDTO.getFiles();
        if(urls!=null || files!=null){
            validatePostUpdateFileInput(urlFilenames, files);
        }

        // 기존 StorageFile 중 유효한 것만 추리기
        Set<StorageFile> newStorageFiles=new LinkedHashSet<>();
        List<String> deletedFilenames=new LinkedList<>();  // 삭제된 기존 파일명
        if(urls!=null){
            // 입력으로 파일 URL 목록이 들어온 경우 (파일 URL 목록에 변화가 있는 경우)
            for(StorageFile storageFile : oldStorageFiles){
                if(urlFilenames.contains(storageFile.getFilename())){
                    newStorageFiles.add(storageFile.toBuilder()
                            .ord(newStorageFiles.size())
                            .build());
                }
                else{
                    deletedFilenames.add(storageFile.getFilename());
                }
            }
        }
        else{
            // 만약 파일 URL 목록에 변화가 없다면 기존 StorageFile 그대로 가져오기
            newStorageFiles.addAll(oldStorageFiles);
        }

        // 태그 추가
        List<String> tagNames=postUpdateRequestDTO.getTags();
        Set<Tag> newTags=new LinkedHashSet<>();
        if(tagNames!=null){
            // 태그에 변경사항이 있다면
            for(String tagName : tagNames){
                Tag tag=tagService.readByName(tagName);
                newTags.add(tag);
            }
        }
        else{
            // 태그에 변경사항이 없다면, 기존 데이터 그대로 사용
            newTags.addAll(oldTags);
        }

        // 유효하지 않은 태그 목록 추리기
        List<Tag> deletedTags=new LinkedList<>();
        if(tagNames!=null){
            for(Tag oldTag : oldTags){
                if(!tagNames.contains(oldTag.getName())){
                    deletedTags.add(oldTag);
                }
            }
        }

        // 위치 정보 등록하기
        String address=postUpdateRequestDTO.getAddress();
        String newAddress=oldPost.getAddress();
        Double newX=oldPost.getX();
        Double newY=oldPost.getY();
        if(address!=null){
            AddressSearchResponseDTO addressSearchResponseDTO =addressService.searchAddress(address);
            if(addressSearchResponseDTO ==null){
                throw new IllegalArgumentException("존재하지 않는 주소입니다. 정확한 주소를 입력해주세요.");
            }
            newAddress= addressSearchResponseDTO.getAddressName();
            newX=Double.parseDouble(addressSearchResponseDTO.getX());
            newY=Double.parseDouble(addressSearchResponseDTO.getY());
        }

        // 새로 등록한 파일의 StorageFile 엔티티 추가
        List<String> uploadFilenames=new LinkedList<>();
        try{
            if(files!=null){
                for(MultipartFile file : files){
                    // 저장소에 새로운 파일 저장
                    String url=fileService.upload(file);

                    // StorageFile 엔티티 생성
                    String uploadFilename=fileService.convertUrlToFilename(url);
                    StorageFile storageFile=StorageFile.builder()
                            .filename(uploadFilename)
                            .originalFilename(file.getOriginalFilename())
                            .contentType(file.getContentType())
                            .fileSize(file.getSize())
                            .uploader(oldPost.getWriter())
                            .ord(newStorageFiles.size())
                            .build();

                    uploadFilenames.add(uploadFilename);
                    newStorageFiles.add(storageFile);
                }
            }

            // 일부 필드를 변경한 새로운 Post 엔티티 생성
            String newContent=(postUpdateRequestDTO.getContent()!=null)? postUpdateRequestDTO.getContent() : oldPost.getContent();
            Post newPost=oldPost.toBuilder()
                    .content(newContent)
                    .address(newAddress)
                    .x(newX)
                    .y(newY)
                    .files(newStorageFiles)
                    .tags(newTags)
                    .build();

            // DB 저장
            postRepository.save(newPost);

        } catch(Exception exception){
            // 만약 게시글을 수정하던 도중 문제가 생긴다면, 저장소에 업로드한 파일을 삭제해야 한다.
            log.error(exception.getMessage());
            try{
                // 저장소에 새로 업로드한 파일 삭제
                for(String uploadFilename : uploadFilenames){
                    fileService.delete(uploadFilename);
                }
            } catch (IOException ioException){
                // 만약 삭제하는 것도 실패했다면, 로그를 남긴다.
                log.error(ioException.getMessage());
            }

            throw exception;
        }

        // 유효하지 않은 태그 삭제하기
        tagService.deleteUnreferencedTags(deletedTags);

        // 저장소에서 삭제된 기존 파일 제거
        try{
            for(String filename : deletedFilenames){
                fileService.delete(filename);
            }
        } catch (IOException ioException){
            // 만약 삭제에 실패했다면, 로그를 남긴다.
            log.error(ioException.getMessage());
        }

        // 리소스 ID 반환
        return postId;
    }

    /**
     * 특정 게시글 삭제
     * [필요 권한 : 로그인 상태 & 게시글 소유자]
     *
     * @param postId 게시글 ID
     */
    @Override
    public void delete(Long postId) throws IOException {
        // DB에 저장된 이전 게시글 엔티티 가져오기
        Post post=postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("이미 삭제된 게시글입니다. (id = "+postId+")"));

        // 권한 검사
        String userId= securityService.getUserId();
        if(!isOwner(post, userId)){
            throw new AccessDeniedException("게시글 작성자만 삭제할 수 있습니다.");
        }

        // 게시글 삭제
        postRepository.deleteById(postId);

        // 게시글 종속 엔티티 삭제 (태그)
        Set<Tag> tags=post.getTags();
        tagService.deleteUnreferencedTags(tags);

        // 게시글 종속 엔티티 삭제 (댓글)
        commentRepository.deleteByPostId(postId);

        // 첨부파일 삭제
        Set<StorageFile> storageFiles=post.getFiles();
        for(StorageFile storageFile : storageFiles){
            fileService.delete(storageFile.getFilename());
        }
    }


    /**
     * 특정 사용자가 특정 게시글을 소유하고 있는지 검사
     * @param post 게시글
     * @param userId 사용자 ID
     * @return 게시글 소유 여부
     */
    @Override
    public boolean isOwner(Post post, String userId){
        return post.getWriter().getId().equals(userId);
    }


    /**
     * 게시글 등록의 입력으로 들어온 파일이
     * 게시글 파일 등록 조건을 만족하는지 검사
     *
     * [파일 등록 조건]
     * - 파일은 반드시 하나 이상 등록해야 한다.
     * - 영상과 사진은 같이 등록할 수 없다.
     * - 사진 개수 제한 : 10개
     * - 영상 개수 제한 : 1개
     * - 파일 용량 제한 : 총합 100MB
     * - 사진 (JPEG, PNG, WEBP), 영상 (MP4, WEBM)만 등록 가능
     *
     * @param files 파일
     * @return 파일 등록 조건 만족 여부
     */
    private boolean validatePostCreateFileInput(List<MultipartFile> files){
        // 파일 목록이 비어있는지 검사
        if(files == null || files.isEmpty()){
            throw new IllegalArgumentException("반드시 하나 이상의 파일을 등록해야 합니다.");
        }

        // 파일 목록에서 이미지, 비디오를 분리
        List<MultipartFile> images=files.stream()
                .filter((file)-> FileService.isImage(file))
                .toList();
        List<MultipartFile> videos=files.stream()
                .filter((file)->FileService.isVideo(file))
                .toList();

        // 이미지, 비디오만 입력되었는지 검사
        int imageCount=images.size();
        int videoCount=videos.size();
        if(imageCount + videoCount != files.size()){
            throw new IllegalArgumentException("게시글에는 사진 혹은 영상 매체만 등록할 수 있습니다.");
        }

        // 이미지와 비디오가 섞여있지는 않은지 검사
        if(imageCount > 0 && videoCount > 0){
            throw new IllegalArgumentException("게시글에는 사진과 영상을 함께 등록할 수 없습니다.");
        }

        // 이미지가 입력된 경우
        if(imageCount > 0){
            // 이미지 개수 제한
            if(imageCount > MAX_IMAGE_COUNT){
                throw new IllegalArgumentException("게시글에는 최대 "+MAX_IMAGE_COUNT+"개의 사진을 등록할 수 있습니다.");
            }

            // 허용되지 않은 타입의 파일이 있는지 검사
            int totalFileSize=0;
            for(MultipartFile image : images){
                String contentType=image.getContentType();
                if(!ALLOWED_IMAGE_TYPES.contains(contentType)){
                    throw new IllegalArgumentException("지원하지 않는 형식의 파일입니다. (type ="+contentType+")");
                }

                // 파일 용량 계산
                totalFileSize += image.getSize();
            }

            // 파일 용량 검사
            if(totalFileSize>MAX_FILE_SIZE_BYTES){
                throw new IllegalArgumentException("게시글에 등록되는 파일 크기는 "+MAX_FILE_SIZE_MB+"MB를 초과할 수 없습니다.");
            }
        }

        // 비디오가 입력된 경우
        if(videoCount > 0){
            // 비디오 개수 제한
            if(videoCount > MAX_VIDEO_COUNT){
                throw new IllegalArgumentException("게시글에는 최대 "+MAX_VIDEO_COUNT+"개의 영상을 등록할 수 있습니다.");
            }

            // 허용되지 않은 타입의 파일이 있는지 검사
            int totalFileSize=0;
            for(MultipartFile video : videos){
                String contentType=video.getContentType();
                if(!ALLOWED_VIDEO_TYPES.contains(contentType)){
                    throw new IllegalArgumentException("지원하지 않는 형식의 파일입니다. (type ="+contentType+")");
                }

                // 파일 용량 계산
                totalFileSize += video.getSize();
            }

            // 파일 용량 검사
            if(totalFileSize>MAX_FILE_SIZE_BYTES){
                throw new IllegalArgumentException("게시글에 등록되는 파일 크기는 "+MAX_FILE_SIZE_MB+"MB를 초과할 수 없습니다.");
            }
        }

        // 모든 조건을 만족하면
        return true;
    }

    /**
     * 게시글 수정의 입력으로 들어온 파일이
     * 게시글 파일 등록 조건을 만족하는지 검사
     *
     * [파일 등록 조건]
     * - 파일은 반드시 하나 이상 등록해야 한다.
     * - 영상과 사진은 같이 등록할 수 없다.
     * - 사진 개수 제한 : 10개
     * - 영상 개수 제한 : 1개
     * - 파일 용량 제한 : 총합 100MB
     * - 사진 (JPEG, PNG, WEBP), 영상 (MP4, WEBM)만 등록 가능
     *
     * @param filenames 기존 게시글에 있던 파일명
     * @param files 수정하면서 추가된 파일
     * @return 파일 등록 조건 만족 여부
     */
    private boolean validatePostUpdateFileInput(List<String> filenames, List<MultipartFile> files){
        // URL 목록과 파일 목록이 비어있는지 검사
        int filenameCount=(filenames!=null)? filenames.size() : 0;
        int fileCount=(files!=null)? files.size() : 0;
        if(filenameCount + fileCount <= 0){
            throw new IllegalArgumentException("반드시 하나 이상의 파일을 등록해야 합니다.");
        }

        // URL 목록에서 이미지, 비디오를 분리
        List<String> imageNames=null;
        List<String> videoNames=null;

        if(filenames!=null){
            imageNames=storageFileRepository.findAllFilenameByFilenamesAndContentType(filenames, FileService.CONTENT_TYPE_IMAGE_PREFIX);
            videoNames=storageFileRepository.findAllFilenameByFilenamesAndContentType(filenames, FileService.CONTENT_TYPE_VIDEO_PREFIX);
        }


        // 파일 목록에서 이미지, 비디오를 분리
        List<MultipartFile> imageFiles=null;
        List<MultipartFile> videoFiles=null;

        if(files!=null){
            imageFiles=files.stream()
                    .filter((file)-> FileService.isImage(file))
                    .collect(Collectors.toList());
            videoFiles=files.stream()
                    .filter((file)->FileService.isVideo(file))
                    .collect(Collectors.toList());
        }


        // 이미지, 비디오 타입만 입력되었는지 검사
        int imageCount=0;
        if(imageNames!=null) imageCount+=imageNames.size();
        if(imageFiles!=null) imageCount+=imageFiles.size();

        int videoCount=0;
        if(videoNames!=null) videoCount+=videoNames.size();
        if(videoFiles!=null) videoCount+=videoFiles.size();

        if(imageCount + videoCount != filenameCount + fileCount){
            throw new IllegalArgumentException("게시글에는 사진 혹은 영상 매체만 등록할 수 있습니다.");
        }


        // 이미지와 비디오가 섞여있지는 않은지 검사
        if(imageCount > 0 && videoCount > 0){
            throw new IllegalArgumentException("게시글에는 사진과 영상을 함께 등록할 수 없습니다.");
        }

        // 이미지가 입력된 경우
        if(imageCount > 0){
            // 이미지 개수 제한
            if(imageCount > MAX_IMAGE_COUNT){
                throw new IllegalArgumentException("게시글에는 최대 "+MAX_IMAGE_COUNT+"개의 사진을 등록할 수 있습니다.");
            }

            // 허용되지 않은 타입의 파일이 있는지 검사
            long totalFileSize=(filenames!=null)? storageFileRepository.sumFileSizeByFilenames(filenames) : 0;
            if(imageFiles!=null){
                for(MultipartFile image : imageFiles){
                    String contentType=image.getContentType();
                    if(!ALLOWED_IMAGE_TYPES.contains(contentType)){
                        throw new IllegalArgumentException("지원하지 않는 형식의 파일입니다. (type ="+contentType+")");
                    }

                    // 파일 용량 계산
                    totalFileSize += image.getSize();
                }
            }

            // 파일 용량 검사
            if(totalFileSize>MAX_FILE_SIZE_BYTES){
                throw new IllegalArgumentException("게시글에 등록되는 파일 크기는 "+MAX_FILE_SIZE_MB+"MB를 초과할 수 없습니다.");
            }
        }

        // 비디오가 입력된 경우
        if(videoCount > 0){
            // 비디오 개수 제한
            if(videoCount > MAX_VIDEO_COUNT){
                throw new IllegalArgumentException("게시글에는 최대 "+MAX_VIDEO_COUNT+"개의 영상을 등록할 수 있습니다.");
            }

            // 허용되지 않은 타입의 파일이 있는지 검사
            long totalFileSize=(filenames!=null)? storageFileRepository.sumFileSizeByFilenames(filenames) : 0;
            if(videoFiles!=null){
                for(MultipartFile video : videoFiles){
                    String contentType=video.getContentType();
                    if(!ALLOWED_VIDEO_TYPES.contains(contentType)){
                        throw new IllegalArgumentException("지원하지 않는 형식의 파일입니다. (type ="+contentType+")");
                    }

                    // 파일 용량 계산
                    totalFileSize += video.getSize();
                }
            }

            // 파일 용량 검사
            if(totalFileSize>MAX_FILE_SIZE_BYTES){
                throw new IllegalArgumentException("게시글에 등록되는 파일 크기는 "+MAX_FILE_SIZE_MB+"MB를 초과할 수 없습니다.");
            }
        }

        // 모든 조건을 만족하면
        return true;
    }
}
