package com.anye.greendao.gen;

import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.Property;
import org.greenrobot.greendao.internal.DaoConfig;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.database.DatabaseStatement;

import com.zly.zly.mediabox.bean.FileInfo;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table "VIDEO_TABLE".
*/
public class FileInfoDao extends AbstractDao<FileInfo, Long> {

    public static final String TABLENAME = "VIDEO_TABLE";

    /**
     * Properties of entity FileInfo.<br/>
     * Can be used for QueryBuilder and for referencing column names.
    */
    public static class Properties {
        public final static Property Name = new Property(0, String.class, "name", false, "NAME");
        public final static Property FileNumber = new Property(1, Long.class, "fileNumber", true, "_id");
        public final static Property Type = new Property(2, int.class, "type", false, "TYPE");
        public final static Property ClickMark = new Property(3, boolean.class, "clickMark", false, "CLICK_MARK");
        public final static Property Love = new Property(4, boolean.class, "love", false, "LOVE");
        public final static Property Buf = new Property(5, byte[].class, "buf", false, "BUF");
    };


    public FileInfoDao(DaoConfig config) {
        super(config);
    }
    
    public FileInfoDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(Database db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"VIDEO_TABLE\" (" + //
                "\"NAME\" TEXT," + // 0: name
                "\"_id\" INTEGER PRIMARY KEY ," + // 1: fileNumber
                "\"TYPE\" INTEGER NOT NULL ," + // 2: type
                "\"CLICK_MARK\" INTEGER NOT NULL ," + // 3: clickMark
                "\"LOVE\" INTEGER NOT NULL ," + // 4: love
                "\"BUF\" BLOB);"); // 5: buf
        // Add Indexes
        db.execSQL("CREATE INDEX " + constraint + "IDX_VIDEO_TABLE_NAME_DESC ON VIDEO_TABLE" +
                " (\"NAME\" DESC);");
    }

    /** Drops the underlying database table. */
    public static void dropTable(Database db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"VIDEO_TABLE\"";
        db.execSQL(sql);
    }

    @Override
    protected final void bindValues(DatabaseStatement stmt, FileInfo entity) {
        stmt.clearBindings();
 
        String name = entity.getName();
        if (name != null) {
            stmt.bindString(1, name);
        }
 
        Long fileNumber = entity.getFileNumber();
        if (fileNumber != null) {
            stmt.bindLong(2, fileNumber);
        }
        stmt.bindLong(3, entity.getType());
        stmt.bindLong(4, entity.getClickMark() ? 1L: 0L);
        stmt.bindLong(5, entity.getLove() ? 1L: 0L);
 
        byte[] buf = entity.getBuf();
        if (buf != null) {
            stmt.bindBlob(6, buf);
        }
    }

    @Override
    protected final void bindValues(SQLiteStatement stmt, FileInfo entity) {
        stmt.clearBindings();
 
        String name = entity.getName();
        if (name != null) {
            stmt.bindString(1, name);
        }
 
        Long fileNumber = entity.getFileNumber();
        if (fileNumber != null) {
            stmt.bindLong(2, fileNumber);
        }
        stmt.bindLong(3, entity.getType());
        stmt.bindLong(4, entity.getClickMark() ? 1L: 0L);
        stmt.bindLong(5, entity.getLove() ? 1L: 0L);
 
        byte[] buf = entity.getBuf();
        if (buf != null) {
            stmt.bindBlob(6, buf);
        }
    }

    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 1) ? null : cursor.getLong(offset + 1);
    }    

    @Override
    public FileInfo readEntity(Cursor cursor, int offset) {
        FileInfo entity = new FileInfo( //
            cursor.isNull(offset + 0) ? null : cursor.getString(offset + 0), // name
            cursor.isNull(offset + 1) ? null : cursor.getLong(offset + 1), // fileNumber
            cursor.getInt(offset + 2), // type
            cursor.getShort(offset + 3) != 0, // clickMark
            cursor.getShort(offset + 4) != 0, // love
            cursor.isNull(offset + 5) ? null : cursor.getBlob(offset + 5) // buf
        );
        return entity;
    }
     
    @Override
    public void readEntity(Cursor cursor, FileInfo entity, int offset) {
        entity.setName(cursor.isNull(offset + 0) ? null : cursor.getString(offset + 0));
        entity.setFileNumber(cursor.isNull(offset + 1) ? null : cursor.getLong(offset + 1));
        entity.setType(cursor.getInt(offset + 2));
        entity.setClickMark(cursor.getShort(offset + 3) != 0);
        entity.setLove(cursor.getShort(offset + 4) != 0);
        entity.setBuf(cursor.isNull(offset + 5) ? null : cursor.getBlob(offset + 5));
     }
    
    @Override
    protected final Long updateKeyAfterInsert(FileInfo entity, long rowId) {
        entity.setFileNumber(rowId);
        return rowId;
    }
    
    @Override
    public Long getKey(FileInfo entity) {
        if(entity != null) {
            return entity.getFileNumber();
        } else {
            return null;
        }
    }

    @Override
    protected final boolean isEntityUpdateable() {
        return true;
    }
    
}
