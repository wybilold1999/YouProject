package com.youdo.karma.greendao;

import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.Property;
import org.greenrobot.greendao.internal.DaoConfig;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.database.DatabaseStatement;

import com.youdo.karma.entity.Conversation;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table "CONVERSATION".
*/
public class ConversationDao extends AbstractDao<Conversation, Long> {

    public static final String TABLENAME = "CONVERSATION";

    /**
     * Properties of entity Conversation.<br/>
     * Can be used for QueryBuilder and for referencing column names.
     */
    public static class Properties {
        public final static Property Id = new Property(0, Long.class, "id", true, "_id");
        public final static Property Talker = new Property(1, String.class, "talker", false, "TALKER");
        public final static Property TalkerName = new Property(2, String.class, "talkerName", false, "TALKER_NAME");
        public final static Property Content = new Property(3, String.class, "content", false, "CONTENT");
        public final static Property UnreadCount = new Property(4, int.class, "unreadCount", false, "UNREAD_COUNT");
        public final static Property CreateTime = new Property(5, long.class, "createTime", false, "CREATE_TIME");
        public final static Property Type = new Property(6, int.class, "type", false, "TYPE");
        public final static Property LocalPortrait = new Property(7, String.class, "localPortrait", false, "LOCAL_PORTRAIT");
        public final static Property FaceUrl = new Property(8, String.class, "faceUrl", false, "FACE_URL");
    }


    public ConversationDao(DaoConfig config) {
        super(config);
    }
    
    public ConversationDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(Database db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"CONVERSATION\" (" + //
                "\"_id\" INTEGER PRIMARY KEY AUTOINCREMENT ," + // 0: id
                "\"TALKER\" TEXT NOT NULL UNIQUE ," + // 1: talker
                "\"TALKER_NAME\" TEXT NOT NULL ," + // 2: talkerName
                "\"CONTENT\" TEXT NOT NULL ," + // 3: content
                "\"UNREAD_COUNT\" INTEGER NOT NULL ," + // 4: unreadCount
                "\"CREATE_TIME\" INTEGER NOT NULL ," + // 5: createTime
                "\"TYPE\" INTEGER NOT NULL ," + // 6: type
                "\"LOCAL_PORTRAIT\" TEXT," + // 7: localPortrait
                "\"FACE_URL\" TEXT);"); // 8: faceUrl
    }

    /** Drops the underlying database table. */
    public static void dropTable(Database db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"CONVERSATION\"";
        db.execSQL(sql);
    }

    @Override
    protected final void bindValues(DatabaseStatement stmt, Conversation entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
        stmt.bindString(2, entity.getTalker());
        stmt.bindString(3, entity.getTalkerName());
        stmt.bindString(4, entity.getContent());
        stmt.bindLong(5, entity.getUnreadCount());
        stmt.bindLong(6, entity.getCreateTime());
        stmt.bindLong(7, entity.getType());
 
        String localPortrait = entity.getLocalPortrait();
        if (localPortrait != null) {
            stmt.bindString(8, localPortrait);
        }
 
        String faceUrl = entity.getFaceUrl();
        if (faceUrl != null) {
            stmt.bindString(9, faceUrl);
        }
    }

    @Override
    protected final void bindValues(SQLiteStatement stmt, Conversation entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
        stmt.bindString(2, entity.getTalker());
        stmt.bindString(3, entity.getTalkerName());
        stmt.bindString(4, entity.getContent());
        stmt.bindLong(5, entity.getUnreadCount());
        stmt.bindLong(6, entity.getCreateTime());
        stmt.bindLong(7, entity.getType());
 
        String localPortrait = entity.getLocalPortrait();
        if (localPortrait != null) {
            stmt.bindString(8, localPortrait);
        }
 
        String faceUrl = entity.getFaceUrl();
        if (faceUrl != null) {
            stmt.bindString(9, faceUrl);
        }
    }

    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0);
    }    

    @Override
    public Conversation readEntity(Cursor cursor, int offset) {
        Conversation entity = new Conversation( //
            cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // id
            cursor.getString(offset + 1), // talker
            cursor.getString(offset + 2), // talkerName
            cursor.getString(offset + 3), // content
            cursor.getInt(offset + 4), // unreadCount
            cursor.getLong(offset + 5), // createTime
            cursor.getInt(offset + 6), // type
            cursor.isNull(offset + 7) ? null : cursor.getString(offset + 7), // localPortrait
            cursor.isNull(offset + 8) ? null : cursor.getString(offset + 8) // faceUrl
        );
        return entity;
    }
     
    @Override
    public void readEntity(Cursor cursor, Conversation entity, int offset) {
        entity.setId(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setTalker(cursor.getString(offset + 1));
        entity.setTalkerName(cursor.getString(offset + 2));
        entity.setContent(cursor.getString(offset + 3));
        entity.setUnreadCount(cursor.getInt(offset + 4));
        entity.setCreateTime(cursor.getLong(offset + 5));
        entity.setType(cursor.getInt(offset + 6));
        entity.setLocalPortrait(cursor.isNull(offset + 7) ? null : cursor.getString(offset + 7));
        entity.setFaceUrl(cursor.isNull(offset + 8) ? null : cursor.getString(offset + 8));
     }
    
    @Override
    protected final Long updateKeyAfterInsert(Conversation entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }
    
    @Override
    public Long getKey(Conversation entity) {
        if(entity != null) {
            return entity.getId();
        } else {
            return null;
        }
    }

    @Override
    public boolean hasKey(Conversation entity) {
        return entity.getId() != null;
    }

    @Override
    protected final boolean isEntityUpdateable() {
        return true;
    }
    
}
