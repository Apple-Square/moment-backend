package applesquare.moment.follow.repository;

import com.querydsl.core.Tuple;

import java.util.List;

public interface CustomFollowRepository {
    List<Tuple> searchFollowerByKeyword(String userId, String keyword, Long cursor, int pageSize);
    List<Tuple> searchFolloweeByKeyword(String userId, String keyword, Long cursor, int pageSize);
}
