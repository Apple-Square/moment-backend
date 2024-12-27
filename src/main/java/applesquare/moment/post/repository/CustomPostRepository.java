package applesquare.moment.post.repository;

import java.util.List;

public interface CustomPostRepository {
    List<Long> searchPostIdsByKeyword(String keyword, Long cursor, int size);
    List<Long> searchMomentIdsByKeyword(String keyword, Long cursor, int size);
}
