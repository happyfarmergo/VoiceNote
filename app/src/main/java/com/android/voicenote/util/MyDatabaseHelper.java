package com.android.voicenote.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.android.voicenote.home.NoteContainer;

/**
 * Created by lvjinhua on 6/5/2016.
 */
public class MyDatabaseHelper extends SQLiteOpenHelper {

    final static int MAX_CAPTION_SIZE = 100;

    final String CREATE_TABEL_SQL = "create table notes(id integer primary key autoincrement, " +
            "create_time varchar(50), " +
            "modified_time varchar(50), " +
            "alarm_time varchar(50), " +
            "caption varchar(100), " +
            "content varchar(1048576))";//1MB

    final String INSERT_SQL = "insert into notes values(null, ? , ? , ? , ? , ?)";
    final String DELETE_SQL = "delete from notes where id = ?";
    final String QUERY_SQL = "select * from notes where id = ?";

    public void insertData(String create_time, String alarm_time, String caption, String content) {
        SQLiteDatabase db = this.getWritableDatabase();
        if (caption.length() > MAX_CAPTION_SIZE)
            caption = caption.substring(0, MAX_CAPTION_SIZE);
        db.execSQL(INSERT_SQL, new String[]{create_time, create_time, alarm_time, caption, content});
    }

    public void insertData(NoteContainer.NoteItem item) {
        insertData(item.create_time, item.alarm_time, item.caption, item.content);
    }

    public void deleteData(String id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL(DELETE_SQL, new String[]{id});
    }

    public NoteContainer.NoteItem queryData(String id){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(QUERY_SQL, new String[]{id});
        if(cursor.moveToNext()){
            NoteContainer.NoteItem item = new NoteContainer.NoteItem(
                    cursor.getInt(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getString(3),
                    cursor.getString(4),
                    cursor.getString(5)
            );
            return item;
        }
        return null;
    }

    public void updateData(NoteContainer.NoteItem item){
        updateData(Integer.toString(item.id), item.modified_time, item.alarm_time, item.caption, item.content);
    }

    public void updateData(String id, String modified_time, String alarm_time, String caption, String content) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("modified_time", modified_time);
        values.put("alarm_time", alarm_time);
        values.put("caption", caption);
        values.put("content", content);
        db.update("notes", values, "id=?", new String[]{id});
    }

    public MyDatabaseHelper(Context context, String name, int version) {
        super(context, name, null, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            db.execSQL(CREATE_TABEL_SQL);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
