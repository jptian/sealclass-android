package cn.rongcloud.sealclass.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.Nullable;

public abstract class ExtendBaseActivity extends BaseActivity implements View.OnClickListener{

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(getLayoutResId());
        onInitView(savedInstanceState, getIntent());
        onInitViewModel();
    }

    @Override
    public void onClick(View v) {
        onClick(v, v.getId());
    }


    public  <T extends View> T findView(int id) {
        return findView(id, false);
    }

    public <T extends View> T findView(int id, boolean isClick) {
        View viewById = findViewById(id);
        if (isClick) {
            viewById.setOnClickListener(this);
        }
        return (T) viewById;
    }

    public <T extends View> T findView(View view, int id, boolean isClick) {
        View viewById = view.findViewById(id);
        if (isClick) {
            viewById.setOnClickListener(this);
        }
        return (T) viewById;
    }

    protected abstract int getLayoutResId();

    protected abstract void onInitView(Bundle savedInstanceState, Intent intent);

    protected void onInitViewModel() {

    }

    protected void onClick(View v, int id) {

    }

    protected void addFlags(){
        //| WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                        | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
    }
}
