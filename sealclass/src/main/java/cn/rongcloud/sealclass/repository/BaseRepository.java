package cn.rongcloud.sealclass.repository;

import android.content.Context;

import cn.rongcloud.sealclass.api.HttpClientManager;
import cn.rongcloud.sealclass.api.retrofit.RetrofitClient;

class BaseRepository {
    private RetrofitClient client;
    private Context context;
    private HttpClientManager httpManager;

    public BaseRepository(Context context) {
        this.httpManager = HttpClientManager.getInstance(context);
        client = httpManager.getClient();
        this.context = context;
    }

    public <T> T getService(Class<T> clazz) {
        return client.createService(clazz);
    }

    public HttpClientManager getHttpManager() {
        return httpManager;
    }

    public Context getContext() {
        return context;
    }
}
