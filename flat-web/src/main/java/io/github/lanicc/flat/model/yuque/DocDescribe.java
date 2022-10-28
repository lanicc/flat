package io.github.lanicc.flat.model.yuque;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * Created on 2022/10/27.
 *
 * @author lan
 */
@Data
public class DocDescribe {
    private Long id;
    private String slug;
    private String title;
    private String description;
    private float userId;
    private float bookId;
    private String format;

    @JSONField(name = "public")
    private boolean pub;
    private float status;
    private float viewStatus;
    private float readStatus;
    private float likesCount;
    private float readCount;
    private float commentsCount;
    private String contentUpdatedAt;
    private Date createdAt;
    private Date updatedAt;
    private String publishedAt;
    private Date firstPublishedAt;
    private Long draftVersion;
    private Long lastEditorId;
    private Long wordCount;
    private String _serializer;

    private EditUser lastEditor;

    @Data
    public static class EditUser {
        private Long id;
        private String type;
        private String login;
        private String name;
        private String avatar_url;
        private Long followersCount;
        private Long followingCount;
        private Date createdAt;
        private Date updatedAt;
        private String _serializer;
    }


    @Data
    public static class Result {
        private List<DocDescribe> data;
    }
}
