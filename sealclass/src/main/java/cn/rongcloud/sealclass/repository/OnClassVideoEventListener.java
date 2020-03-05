package cn.rongcloud.sealclass.repository;

import java.util.List;

import cn.rongcloud.sealclass.model.StreamResource;

public interface OnClassVideoEventListener {

    void onInitVideoList(List<StreamResource> userIds);

    void onAddVideoUser(StreamResource userId);

    void onUserLeft(StreamResource info);

    void onUserOffline(StreamResource info);

    void onFirstFrameDraw(String userId, String tag);

    void onVideoEnabled(String userId, boolean enable);
}
