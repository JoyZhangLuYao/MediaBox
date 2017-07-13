package com.zly.zly.mediabox.MyLibs;

import android.app.Application;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.anye.greendao.gen.CollectFileInfoDao;
import com.anye.greendao.gen.CollectMusicFileInfoDao;
import com.anye.greendao.gen.CollectPhotoFileInfoDao;
import com.anye.greendao.gen.DaoMaster;
import com.anye.greendao.gen.DaoSession;
import com.anye.greendao.gen.FileInfoDao;
import com.anye.greendao.gen.HMROpenHelper;
import com.anye.greendao.gen.MusicFileInfoDao;
import com.anye.greendao.gen.PhotoFileInfoDao;
import com.bdsdk.update.BaiDuAutoUpdatePopupwindow;
import com.orhanobut.logger.Logger;
import com.zly.zly.mediabox.bean.CollectFileInfo;
import com.zly.zly.mediabox.bean.CollectMusicFileInfo;
import com.zly.zly.mediabox.bean.CollectPhotoFileInfo;
import com.zly.zly.mediabox.bean.FileInfo;


import java.util.ArrayList;
import java.util.List;

/**
 * Created by ZhangLuyao on 2017/3/28.
 */

public class MyApplication extends Application {

    private SQLiteDatabase db;
    private DaoMaster mDaoMaster;
    private DaoSession mDaoSession;


    public IBluetooth iBluetooth;
    public int currentMain = 1;
    public int uAndTf = 1;
    public int loveMark = -1;
    boolean loveOK = false;

    public boolean searchMark;

    public int sendRootCollectListMark = 1;

    public boolean sendCollectListMark = true;

    //音乐、视频、图片列表
    List<CollectFileInfo> listV = new ArrayList<>();
    List<CollectMusicFileInfo> listM = new ArrayList<>();
    List<CollectPhotoFileInfo> listP = new ArrayList<>();

    public List<CollectFileInfo> getListV() {
        return listV;
    }

    public void setListV(List<CollectFileInfo> listV) {
        this.listV = listV;
    }

    public List<CollectMusicFileInfo> getListM() {
        return listM;
    }

    public void setListM(List<CollectMusicFileInfo> listM) {
        this.listM = listM;
    }

    public List<CollectPhotoFileInfo> getListP() {
        return listP;
    }

    public void setListP(List<CollectPhotoFileInfo> listP) {
        this.listP = listP;
    }

    public int getLoveMark() {
        return loveMark;
    }

    public void setLoveMark(int loveMark) {
        this.loveMark = loveMark;
    }

    public int getuAndTf() {
        return uAndTf;
    }

    public void setuAndTf(int uAndTf) {
        this.uAndTf = uAndTf;
    }

    public boolean isSearchMark() {
        return searchMark;
    }

    public void setSearchMark(boolean searchMark) {
        this.searchMark = searchMark;
    }

    public int getCurrentMain() {
        return currentMain;
    }

    public void setCurrentMain(int currentMain) {
        this.currentMain = currentMain;
    }

    public boolean isLoveOK() {
        return loveOK;
    }

    public void setLoveOK(boolean loveOK) {
        this.loveOK = loveOK;
    }

    public int getSendRootCollectListMark() {
        return sendRootCollectListMark;
    }

    public void setSendRootCollectListMark(int sendRootCollectListMark) {
        this.sendRootCollectListMark = sendRootCollectListMark;
    }

    public boolean isSendCollectListMark() {
        return sendCollectListMark;
    }

    public void setSendCollectListMark(boolean sendCollectListMark) {
        this.sendCollectListMark = sendCollectListMark;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Logger.init("ZhangLuyao");
        iBluetooth = IBluetooth.getInstance(this);
        setDatabase();

    }

    public Context getContxt() {
        return this;
    }

    public IBluetooth getIBluetooth() {
        return this.iBluetooth;
    }

    /**
     * 设置greenDao
     */
    private void setDatabase() {
        // 通过 DaoMaster 的内部类 DevOpenHelper，你可以得到一个便利的 SQLiteOpenHelper 对象。
        // 可能你已经注意到了，你并不需要去编写「CREATE TABLE」这样的 SQL 语句，因为 greenDAO 已经帮你做了。
        // 注意：默认的 DaoMaster.DevOpenHelper 会在数据库升级时，删除所有的表，意味着这将导致数据的丢失。
        // 所以，在正式的项目中，你还应该做一层封装，来实现数据库的安全升级。
//        DaoMaster.DevOpenHelper mHelpter = new DaoMaster.DevOpenHelper(this,"notes-db");
        HMROpenHelper mHelper = new HMROpenHelper(this, "notes-db", null);//为数据库升级封装过的使用方式
        db = mHelper.getWritableDatabase();
        // 注意：该数据库连接属于 DaoMaster，所以多个 Session 指的是相同的数据库连接。
        mDaoMaster = new DaoMaster(db);
        mDaoSession = mDaoMaster.newSession();
    }

    public DaoSession getDaoSession() {
        return mDaoSession;
    }

    public SQLiteDatabase getDb() {
        return db;
    }

    public FileInfoDao getFileInfoDao() {
        return getDaoSession().getFileInfoDao();
    }

    public MusicFileInfoDao getMusicFileInfoDao() {
        return getDaoSession().getMusicFileInfoDao();
    }

    public PhotoFileInfoDao getPhotoFileInfoDao() {
        return getDaoSession().getPhotoFileInfoDao();
    }

    public CollectPhotoFileInfoDao getCollectPhotoFileInfoDao() {
        return getDaoSession().getCollectPhotoFileInfoDao();
    }

    public CollectMusicFileInfoDao getCollectMusicFileInfoDao() {
        return getDaoSession().getCollectMusicFileInfoDao();
    }

    public CollectFileInfoDao getCollectFileInfoDao() {
        return getDaoSession().getCollectFileInfoDao();
    }
}
