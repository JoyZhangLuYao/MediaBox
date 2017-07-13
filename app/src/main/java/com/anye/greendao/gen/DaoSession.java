package com.anye.greendao.gen;

import java.util.Map;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.AbstractDaoSession;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.identityscope.IdentityScopeType;
import org.greenrobot.greendao.internal.DaoConfig;

import com.zly.zly.mediabox.bean.CollectFileInfo;
import com.zly.zly.mediabox.bean.CollectMusicFileInfo;
import com.zly.zly.mediabox.bean.CollectPhotoFileInfo;
import com.zly.zly.mediabox.bean.FileInfo;
import com.zly.zly.mediabox.bean.MusicFileInfo;
import com.zly.zly.mediabox.bean.PhotoFileInfo;

import com.anye.greendao.gen.CollectFileInfoDao;
import com.anye.greendao.gen.CollectMusicFileInfoDao;
import com.anye.greendao.gen.CollectPhotoFileInfoDao;
import com.anye.greendao.gen.FileInfoDao;
import com.anye.greendao.gen.MusicFileInfoDao;
import com.anye.greendao.gen.PhotoFileInfoDao;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.

/**
 * {@inheritDoc}
 * 
 * @see org.greenrobot.greendao.AbstractDaoSession
 */
public class DaoSession extends AbstractDaoSession {

    private final DaoConfig collectFileInfoDaoConfig;
    private final DaoConfig collectMusicFileInfoDaoConfig;
    private final DaoConfig collectPhotoFileInfoDaoConfig;
    private final DaoConfig fileInfoDaoConfig;
    private final DaoConfig musicFileInfoDaoConfig;
    private final DaoConfig photoFileInfoDaoConfig;

    private final CollectFileInfoDao collectFileInfoDao;
    private final CollectMusicFileInfoDao collectMusicFileInfoDao;
    private final CollectPhotoFileInfoDao collectPhotoFileInfoDao;
    private final FileInfoDao fileInfoDao;
    private final MusicFileInfoDao musicFileInfoDao;
    private final PhotoFileInfoDao photoFileInfoDao;

    public DaoSession(Database db, IdentityScopeType type, Map<Class<? extends AbstractDao<?, ?>>, DaoConfig>
            daoConfigMap) {
        super(db);

        collectFileInfoDaoConfig = daoConfigMap.get(CollectFileInfoDao.class).clone();
        collectFileInfoDaoConfig.initIdentityScope(type);

        collectMusicFileInfoDaoConfig = daoConfigMap.get(CollectMusicFileInfoDao.class).clone();
        collectMusicFileInfoDaoConfig.initIdentityScope(type);

        collectPhotoFileInfoDaoConfig = daoConfigMap.get(CollectPhotoFileInfoDao.class).clone();
        collectPhotoFileInfoDaoConfig.initIdentityScope(type);

        fileInfoDaoConfig = daoConfigMap.get(FileInfoDao.class).clone();
        fileInfoDaoConfig.initIdentityScope(type);

        musicFileInfoDaoConfig = daoConfigMap.get(MusicFileInfoDao.class).clone();
        musicFileInfoDaoConfig.initIdentityScope(type);

        photoFileInfoDaoConfig = daoConfigMap.get(PhotoFileInfoDao.class).clone();
        photoFileInfoDaoConfig.initIdentityScope(type);

        collectFileInfoDao = new CollectFileInfoDao(collectFileInfoDaoConfig, this);
        collectMusicFileInfoDao = new CollectMusicFileInfoDao(collectMusicFileInfoDaoConfig, this);
        collectPhotoFileInfoDao = new CollectPhotoFileInfoDao(collectPhotoFileInfoDaoConfig, this);
        fileInfoDao = new FileInfoDao(fileInfoDaoConfig, this);
        musicFileInfoDao = new MusicFileInfoDao(musicFileInfoDaoConfig, this);
        photoFileInfoDao = new PhotoFileInfoDao(photoFileInfoDaoConfig, this);

        registerDao(CollectFileInfo.class, collectFileInfoDao);
        registerDao(CollectMusicFileInfo.class, collectMusicFileInfoDao);
        registerDao(CollectPhotoFileInfo.class, collectPhotoFileInfoDao);
        registerDao(FileInfo.class, fileInfoDao);
        registerDao(MusicFileInfo.class, musicFileInfoDao);
        registerDao(PhotoFileInfo.class, photoFileInfoDao);
    }
    
    public void clear() {
        collectFileInfoDaoConfig.getIdentityScope().clear();
        collectMusicFileInfoDaoConfig.getIdentityScope().clear();
        collectPhotoFileInfoDaoConfig.getIdentityScope().clear();
        fileInfoDaoConfig.getIdentityScope().clear();
        musicFileInfoDaoConfig.getIdentityScope().clear();
        photoFileInfoDaoConfig.getIdentityScope().clear();
    }

    public CollectFileInfoDao getCollectFileInfoDao() {
        return collectFileInfoDao;
    }

    public CollectMusicFileInfoDao getCollectMusicFileInfoDao() {
        return collectMusicFileInfoDao;
    }

    public CollectPhotoFileInfoDao getCollectPhotoFileInfoDao() {
        return collectPhotoFileInfoDao;
    }

    public FileInfoDao getFileInfoDao() {
        return fileInfoDao;
    }

    public MusicFileInfoDao getMusicFileInfoDao() {
        return musicFileInfoDao;
    }

    public PhotoFileInfoDao getPhotoFileInfoDao() {
        return photoFileInfoDao;
    }

}
