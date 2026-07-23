package com.taiyidu.taiyidu.gongsheng.pojo.entity;

import lombok.Data;
import java.util.List;

@Data
public class DouyinData {

    //聚合函数中抖音接口的实体类
    private String author;
    private Long uid;
    private Long followerCount;
    private Long totalFavorited;
    private String avatar;
    private String title;
    private String cover;
    private List<String> images;
    private String url;
    private Music music;

    /**
     * 是否图文图集
     * @return true=图文 false=视频
     */
    public boolean isImageGallery() {
        return images != null && !images.isEmpty();
    }

    /**
     * 是否视频
     * @return true=视频
     */
    public boolean isVideo() {
        return !isImageGallery();
    }

    /**
     * 获取图集图片列表
     * @return 图片集合，非图文返回空集合
     */
    public List<String> getImageList() {
        if (isImageGallery()) {
            return images;
        }
        return List.of();
    }

    /**
     * 获取图集提示文本
     * @return 非图文返回null
     */
    public String getImageTipText() {
        if (isImageGallery()) {
            return "共" + images.size() + "张图集";
        }
        return null;
    }
}



//抖音专属解析接口的实体类
//    // 作者信息
//    private String author;
//    private String uid;
//    private String avatar;
//    // 点赞数
//    private Long like;
//    // 发布时间戳
//    private Long time;
//    // 作品标题带话题
//    private String title;
//    // 封面图链接
//    private String cover;
//
//    // 关键：视频=字符串，图文=图片数组，用Object接收
//    private Object images;
//
//    // 资源链接：视频=视频直链，图文=提示文字
//    private String url;
//    // 视频时长ms，图文固定0
//    private Long duration;
//
//    // 音乐信息子对象
//    private DouyinMusic music;
//
//    // ====================== 工具方法，业务判断用 ======================
//    /** 是否图文图集 */
//    public boolean isImageGallery() {
//        if (images == null) return false;
//        return images instanceof List;
//    }
//
//    /** 是否短视频 */
//    public boolean isVideo() {
//        return !isImageGallery();
//    }
//
//    /** 获取图文图片列表，非图文返回空集合 */
//    @SuppressWarnings("unchecked")
//    public List<String> getImageList() {
//        if (!isImageGallery()) return List.of();
//        return (List<String>) images;
//    }
//
//    /** 获取短视频images提示文本，图文返回null */
//    public String getImageTipText() {
//        if (isImageGallery() || images == null) return null;
//        return images.toString();
//    }