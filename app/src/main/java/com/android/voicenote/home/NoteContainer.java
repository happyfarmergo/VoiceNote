package com.android.voicenote.home;

import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by lvjinhua on 5/31/2016.
 */
public class NoteContainer {

    public static class NoteItem{
        public int id;                 //每个记录的唯一id
        public String create_time;           //日期
        public String modified_time;
        public String alarm_time;
        public String caption;        //关键字
        public String content;        //记录内容

        public NoteItem(){
            create_time = modified_time = alarm_time = caption = content = "";
        }

        public NoteItem(int id, String create_time, String modified_time, String alarm_time, String caption, String content){
            this.id = id;
            this.create_time = create_time;
            this.modified_time = modified_time;
            this.alarm_time = alarm_time;
            this.caption = caption;
            this.content = content;
        }


    }

}
