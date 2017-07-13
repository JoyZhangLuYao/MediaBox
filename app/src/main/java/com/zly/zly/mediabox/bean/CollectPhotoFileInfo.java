package com.zly.zly.mediabox.bean;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Index;
import org.greenrobot.greendao.annotation.Keep;

import java.io.Serializable;

import org.greenrobot.greendao.annotation.Generated;

/**
 * Created ZhangLuyao Tim on 2017/3/31.
 */
@Entity(
        //设置在数据库中的表名,默认为对应的Entity类名
        nameInDb = "C_PHOTO_TABLE",

        //设置表为动态表，具有更新、查询、删除方法
//        active = true,

        //定义索引跨越的列
        indexes = {
                //为name列设置索引，降序
                @Index(value = "name DESC")
        })
public class CollectPhotoFileInfo implements Serializable {
    @Id(autoincrement = true)
    Long id;
    String name;
    Long fileNumber;
    int type;
    boolean clickMark;
    boolean love;
    byte[] buf;

    @Keep
    public CollectPhotoFileInfo() {
    }
    /*@Keep
    public CollectPhotoFileInfo(String name, Long fileNumber, int type, boolean clickMark, boolean love, byte[] buf) {
        this.name = name;
        this.fileNumber = fileNumber;
        this.type = type;
        this.clickMark = clickMark;
        this.love = love;
        this.buf=buf;
    }*/

    @Generated(hash = 1235366297)
    public CollectPhotoFileInfo(Long id, String name, Long fileNumber, int type, boolean clickMark, boolean love, byte[] buf) {
        this.id = id;
        this.name = name;
        this.fileNumber = fileNumber;
        this.type = type;
        this.clickMark = clickMark;
        this.love = love;
        this.buf = buf;
    }

    public byte[] getBuf() {
        return buf;
    }

    public void setBuf(byte[] buf) {
        this.buf = buf;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getFileNumber() {
        return fileNumber;
    }

    public void setFileNumber(Long fileNumber) {
        this.fileNumber = fileNumber;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public boolean getClickMark() {
        return clickMark;
    }

    public void setClickMark(boolean clickMark) {
        this.clickMark = clickMark;
    }

    public boolean getLove() {
        return love;
    }

    public void setLove(boolean love) {
        this.love = love;
    }

    @Override
    public String toString() {
        return "FileInfo{" +
                "name='" + name + '\'' +
                ", fileNumber=" + fileNumber +
                ", type=" + type +
                ", clickMark=" + clickMark +
                ", love=" + love +
                '}';
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
