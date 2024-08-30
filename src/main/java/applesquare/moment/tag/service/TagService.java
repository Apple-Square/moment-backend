package applesquare.moment.tag.service;

import applesquare.moment.tag.model.Tag;

import java.util.Collection;


public interface TagService {
    int MIN_TAG_NAME_LENGTH=1;
    int MAX_TAG_NAME_LENGTH=100;

    Tag readTagByName(String tagName);
    void deleteUnreferencedTags(Collection<Tag> tags);
}
