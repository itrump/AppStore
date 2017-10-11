package com.seuic.app.store.glide;

import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.widget.ImageView;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.seuic.app.store.AppStoreApplication;
import com.seuic.app.store.utils.AppStoreUtils;
import com.seuic.app.store.utils.HttpHeadUtils;
import com.seuic.app.store.utils.Loger;

import java.util.HashMap;

/**
 * Created on 2017/9/18.
 *
 * @author dpuntu
 */

public class GlideAppManager {

    /**
     * 清理缓存
     *
     * @return boolean 清理成功失败
     */
    public static boolean clearCache() {
        try {
            if (Looper.myLooper() == Looper.getMainLooper()) {
                new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(Void... params) {
                        GlideApp.get(AppStoreApplication.getApp()).clearDiskCache();
                        GlideApp.get(AppStoreApplication.getApp()).clearMemory();
                        return null;
                    }
                };
            } else {
                GlideApp.get(AppStoreApplication.getApp()).clearDiskCache();
                GlideApp.get(AppStoreApplication.getApp()).clearMemory();
            }
            return true;
        } catch (Exception exception) {
            Loger.e(android.util.Log.getStackTraceString(exception));
            return false;
        }
    }

    /**
     * 加载图片
     *
     * @param imageName
     *         图片名称
     * @param imageView
     *         控件
     * @param defaultId
     *         默认图
     * @param imageType
     *         加载图片类型
     */
    public static void loadImage(String imageName, ImageView imageView, int defaultId, String imageType) {
        String url = AppStoreUtils.APPSTORE_BASE_URL + "appstore/image?image_name=" + imageName + "&type=" + imageType;
        if (imageName == null || imageName.isEmpty()) {
            GlideApp.with(AppStoreApplication.getApp())
                    .load(url)
                    .listener(requestListener)
                    .centerCrop()
                    .skipMemoryCache(false)
                    .onlyRetrieveFromCache(false)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(defaultId)
                    .into(imageView);
        } else {
            HashMap<String, String> headMap = HttpHeadUtils.getHeadMap();
            GlideUrl glideUrl = new GlideUrl(url, new LazyHeaders.Builder()
                    .addHeader(HttpHeadUtils.HEAD_TIME, headMap.get(HttpHeadUtils.HEAD_TIME))
                    .addHeader(HttpHeadUtils.HEAD_PRS, headMap.get(HttpHeadUtils.HEAD_PRS))
                    .addHeader(HttpHeadUtils.HEAD_CHANNEL, headMap.get(HttpHeadUtils.HEAD_CHANNEL))
                    .addHeader(HttpHeadUtils.HEAD_RANDOM, headMap.get(HttpHeadUtils.HEAD_RANDOM))
                    .addHeader(HttpHeadUtils.HEAD_SIGN, headMap.get(HttpHeadUtils.HEAD_SIGN))
                    .addHeader(HttpHeadUtils.HEAD_SN, headMap.get(HttpHeadUtils.HEAD_SN))
                    .addHeader(HttpHeadUtils.HEAD_CONNECTION, headMap.get(HttpHeadUtils.HEAD_CONNECTION))
                    .build());
            GlideApp.with(AppStoreApplication.getApp())
                    .load(glideUrl)
                    .listener(requestListener)
                    .centerCrop()
                    .skipMemoryCache(false)
                    .onlyRetrieveFromCache(false)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(defaultId)
                    .into(imageView);
        }
    }

    /**
     * 错误监听
     */
    private static RequestListener<Drawable> requestListener = new RequestListener<Drawable>() {
        @Override
        public boolean onLoadFailed(@Nullable GlideException throwable, Object model, Target<Drawable> target, boolean isFirstResource) {
            Loger.w("-----   glide error exception start   -----");
            Loger.w("glide: " + android.util.Log.getStackTraceString(throwable));
            Loger.w("glide: " + model);
            Loger.w("glide: " + target.getRequest().isRunning());
            Loger.w("-----   glide error exception end   -----");
            return false;
        }

        @Override
        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
            return false;
        }
    };
}