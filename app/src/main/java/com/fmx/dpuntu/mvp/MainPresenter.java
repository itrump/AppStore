package com.fmx.dpuntu.mvp;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.fmx.dpuntu.api.Apimanager;
import com.fmx.dpuntu.api.AppListResponse;
import com.fmx.dpuntu.api.Response;
import com.fmx.dpuntu.ui.DialogActivity;
import com.fmx.dpuntu.utils.AppInfo;
import com.fmx.dpuntu.utils.AppRecyclerViewAdapter;
import com.fmx.dpuntu.utils.Loger;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

/**
 * Created on 2017/7/13.
 *
 * @author dpuntu
 */

public class MainPresenter implements MainContact.Presenter {
    private static final int SUCCESS = 0;
    private static final int FAIL = 1;
    private static final int DOWNLOAD = 2;

    private static final int REQUEST_DOWNLOAD = 100;

    private MainActivity view;
    private ArrayList<AppInfo> appInfos;
    private RecyclerView.LayoutManager mLayoutManager;

    public MainPresenter(MainContact.View view) {
        this.view = (MainActivity) view;
    }

    public void initView() {
        appInfos = getAppsList();
        if (appInfos.size() > 0) {
            mLayoutManager = new LinearLayoutManager(view);
            view.getRecyclerView().setLayoutManager(mLayoutManager);
            AppRecyclerViewAdapter mAppRecyclerViewAdapter = new AppRecyclerViewAdapter(view, appInfos);
            view.getRecyclerView().setAdapter(mAppRecyclerViewAdapter);
        }
    }

    private AppRecyclerViewAdapter.RecyclerViewClickListener mRecyclerViewClickListener = new AppRecyclerViewAdapter.RecyclerViewClickListener() {
        @Override
        public void onClick(View v, AppListResponse.DownloadAppInfo info) {
            mHandler.obtainMessage(DOWNLOAD, info).sendToTarget();
        }
    };

    public void fromNetToList() {
        view.getDownLoadAppsList();
    }

    @Override
    public ArrayList<AppInfo> getAppsList() {
        return view.getAppsList();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_DOWNLOAD) {
            switch (resultCode) {
                case RESULT_OK:

                    break;
                case RESULT_CANCELED:

                    break;
            }
        }
    }


    @Override
    public void getDownLoadAppsList() {
        Loger.i("--- getDownLoadAppsList ---");
        Apimanager.getApiManager()
                .getApiService()
                .appList("709BW_ZH_82", "866830020046093", "dj_w790", "1.6.4", "1", "20")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .filter(new Predicate<Response<AppListResponse>>() {
                    @Override
                    public boolean test(@NonNull Response<AppListResponse> response) throws Exception {
                        Loger.d("result = " + response.getResult());
                        if (response.getResult().equals("true")) {
                            return true;
                        }
                        return false;
                    }
                })
                .subscribe(new Observer<Response<AppListResponse>>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {

                    }

                    @Override
                    public void onNext(@NonNull Response<AppListResponse> response) {
                        List<AppListResponse.DownloadAppInfo> appinfos = response.getData().getRows();
                        mHandler.obtainMessage(SUCCESS, appinfos).sendToTarget();
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        Loger.i("--- onError ---");
                        mHandler.obtainMessage(FAIL, e).sendToTarget();
                    }

                    @Override
                    public void onComplete() {
                        Loger.i("--- onComplete ---");
                    }
                });
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case SUCCESS:
                    List<AppListResponse.DownloadAppInfo> appInfos = (List<AppListResponse.DownloadAppInfo>) msg.obj;
                    if (appInfos.size() > 0) {
                        mLayoutManager = new LinearLayoutManager(view);
                        view.getRecyclerView().setLayoutManager(mLayoutManager);
                        view.getRecyclerView().addItemDecoration(new RecyclerViewDecoration(view, RecyclerViewDecoration.VERTICAL_LIST));
                        AppRecyclerViewAdapter mAppRecyclerViewAdapter = new AppRecyclerViewAdapter(view, appInfos);
                        mAppRecyclerViewAdapter.serRecyclerViewClickListener(mRecyclerViewClickListener);
                        view.getRecyclerView().setAdapter(mAppRecyclerViewAdapter);
                    }
                    break;
                case FAIL:
                    Throwable e = (Throwable) msg.obj;
                    e.printStackTrace();
                    break;
                case DOWNLOAD:
                    AppListResponse.DownloadAppInfo info = (AppListResponse.DownloadAppInfo) msg.obj;
                    Intent intent = new Intent();
                    intent.setClass(view, DialogActivity.class);
                    intent.putExtra("appinfo", info);
                    view.startActivityForResult(intent, REQUEST_DOWNLOAD);
                    break;
            }
        }
    };

}
