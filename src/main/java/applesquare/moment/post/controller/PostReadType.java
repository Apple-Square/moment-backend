package applesquare.moment.post.controller;

public enum PostReadType {
    DETAIL,
    THUMBNAIL;

    public static PostReadType parsePostType(String type){
        try{
            return PostReadType.valueOf(type.toUpperCase());
        }catch (IllegalArgumentException e){
            throw new IllegalArgumentException("잘못된 조회 타입입니다. (type="+type+")");
        }
    }
}
