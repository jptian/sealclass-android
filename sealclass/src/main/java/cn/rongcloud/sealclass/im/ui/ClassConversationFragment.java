package cn.rongcloud.sealclass.im.ui;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import java.util.List;

import cn.rongcloud.sealclass.model.ClassMember;
import cn.rongcloud.sealclass.viewmodel.ClassViewModel;
import io.rong.imkit.fragment.ConversationFragment;
import io.rong.imkit.widget.adapter.MessageListAdapter;

/**
 * 自定义聊天列表
 * 使用了自定义的消息列表，来展示不同角色的头像
 */
public class ClassConversationFragment extends ConversationFragment {
    private ClassMessageListAdapter messageListAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        messageListAdapter = new ClassMessageListAdapter(inflater.getContext());
        /*
         * 以下会在 Android Studio 报出 getActivity() 无法找到错误，因为目前 IMKit 没有使用 Android X
         * 由于 Android Studio 自带了在编译时替换类的功能所以不会影响在编译和应用的使用
         */
        ClassViewModel classViewModel = ViewModelProviders.of(getActivity()).get(ClassViewModel.class);
        classViewModel.getMemberList().observe(this, new Observer<List<ClassMember>>() {
            @Override
            public void onChanged(List<ClassMember> classMembers) {
                messageListAdapter.setClassMemberInfoList(classMembers);
            }
        });
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public MessageListAdapter onResolveAdapter(Context context) {
        return messageListAdapter;
    }
}
