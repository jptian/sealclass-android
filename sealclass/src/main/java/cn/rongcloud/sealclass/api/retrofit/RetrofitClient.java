package cn.rongcloud.sealclass.api.retrofit;

import android.content.Context;
import android.content.SharedPreferences;

import java.io.IOException;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

import cn.rongcloud.sealclass.api.ApiConstant;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static android.content.Context.MODE_PRIVATE;

public class RetrofitClient {
    private Context mContext;
    private Retrofit mRetrofit;

    public RetrofitClient(Context context, String baseUrl) {
        mContext = context;

        OkHttpClient.Builder okHttpBuilder = new OkHttpClient.Builder()
                .addInterceptor(new AddHeaderInterceptor(mContext))
                .addInterceptor(new ReceivedCookiesInterceptor(mContext))
                .connectTimeout(ApiConstant.CONNECT_TIME_OUT, TimeUnit.SECONDS)
                .readTimeout(ApiConstant.READ_TIME_OUT, TimeUnit.SECONDS)
                .writeTimeout(ApiConstant.WRITE_TIME_OUT, TimeUnit.SECONDS);

        mRetrofit = new Retrofit.Builder()
                .client(okHttpBuilder.build())
                .baseUrl(baseUrl) //设置网络请求的Url地址
                .addConverterFactory(GsonConverterFactory.create()) //设置数据解析器
                .build();
    }

    /**
     * 接受cookie拦截器
     */
    public class ReceivedCookiesInterceptor implements Interceptor {
        private Context mContext;

        public ReceivedCookiesInterceptor(Context context) {
            mContext = context;
        }

        @Override
        public Response intercept(Chain chain) throws IOException {
            Response originalResponse = chain.proceed(chain.request());

            if (!originalResponse.headers("Set-Cookie").isEmpty()) {
                HashSet<String> cookiesSet = new HashSet<>();

                for (String header : originalResponse.headers("Set-Cookie")) {
                    cookiesSet.add(header);
                }

                SharedPreferences.Editor config = mContext.getSharedPreferences(ApiConstant.SP_NAME_NET, MODE_PRIVATE)
                        .edit();
                config.putStringSet(ApiConstant.SP_KEY_NET_COOKIE_SET, cookiesSet);
                config.commit();
            }

            return originalResponse;
        }
    }

    /**
     * 添加header包含cookie拦截器
     */
    public class AddHeaderInterceptor implements Interceptor {
        private Context mContext;

        public AddHeaderInterceptor(Context context) {
            mContext = context;
        }

        @Override
        public Response intercept(Chain chain) throws IOException {
            Request.Builder builder = chain.request().newBuilder();
            SharedPreferences preferences = mContext.getSharedPreferences(ApiConstant.SP_NAME_NET,
                    Context.MODE_PRIVATE);

            //添加cookie
            HashSet<String> cookieSet = (HashSet<String>) preferences.getStringSet(ApiConstant.SP_KEY_NET_COOKIE_SET, null);
            if (cookieSet != null) {
                for (String cookie : cookieSet) {
                    builder.addHeader("Cookie", cookie);
                }
            }

            //添加用户登录认证
            String auth = preferences.getString(ApiConstant.SP_KEY_NET_HEADER_AUTH, null);
            if(auth != null) {
                builder.addHeader("Authorization", auth);
            }

            return chain.proceed(builder.build());
        }
    }

    public <T> T createService(Class<T> service) {
        return mRetrofit.create(service);
    }

    private static void allowAllSSL() {

    }
}
