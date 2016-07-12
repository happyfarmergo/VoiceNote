package com.android.voicenote.edit;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.TimePicker;

import java.util.Calendar;

import com.android.voicenote.R;
import com.android.voicenote.SpeechNote;
import com.android.voicenote.home.AlarmActivity;
import com.android.voicenote.home.NoteContainer;
import com.android.voicenote.service.MyService;
import com.android.voicenote.util.MyDatabaseHelper;
import com.android.voicenote.util.TimeHolder;
import com.android.voicenote.util.VoiceHelper;

public class NoteActivity extends AppCompatActivity {

    boolean isNew;
    boolean isReturn;
    boolean deleteAlarm;

    View myToolbar;
    FloatingActionButton fab;
    ImageButton img_btn_home, img_btn_clock, img_btn_detail, img_btn_share;

    EditText editTitle, editContent;
    MyDatabaseHelper dbHelper;
    NoteContainer.NoteItem item;
    VoiceHelper voiceHelper;
    Calendar calendar;
    TimeHolder alarmTime;

    SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);

        isReturn = false;
        deleteAlarm = false;
        editTitle = (EditText) findViewById(R.id.edit_title);
        editContent = (EditText) findViewById(R.id.edit_content);
        myToolbar = getLayoutInflater().inflate(R.layout.layout_my_toolbar, null);
        fab = (FloatingActionButton) findViewById(R.id.fab_voice);
        calendar = Calendar.getInstance();
        alarmTime = new TimeHolder();
        img_btn_home = (ImageButton) findViewById(R.id.img_btn_home);
        img_btn_clock = (ImageButton) findViewById(R.id.img_btn_clock);
        img_btn_detail = (ImageButton) findViewById(R.id.img_btn_detail);
        img_btn_share = (ImageButton) findViewById(R.id.img_btn_share);
        preferences = getSharedPreferences("user", MODE_PRIVATE);

        Bundle bundle = getIntent().getExtras();
        dbHelper = ((SpeechNote) getApplication()).getDbHelper();
        if (bundle != null && !bundle.getBoolean("speak_now", false)) {
            isNew = false;
            item = dbHelper.queryData(bundle.getString("id"));
            editTitle.setText(item.caption);
            editContent.setText(item.content);
            if (item.alarm_time != null) {
                img_btn_clock.setImageAlpha(255);
            }
        } else {
            isNew = true;
            item = new NoteContainer.NoteItem();
            item.create_time = TimeHolder.getCurrentTime();
            img_btn_clock.setImageAlpha(100);
        }

        voiceHelper = new VoiceHelper(this);
        voiceHelper.setShown(editContent);
        editContent.requestFocus();

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!voiceHelper.isSpeaking()) {
                    voiceHelper.startListening();
                } else {
                    voiceHelper.stopListening();
                }
            }
        });

        editContent.setOnClickListener(focusListener);
        editTitle.setOnClickListener(focusListener);

        img_btn_home.setOnClickListener(homeListener);
        img_btn_clock.setOnClickListener(clockListener);
        img_btn_share.setOnClickListener(shareListener);
        img_btn_detail.setOnClickListener(detailListener);

        if (isNew && bundle != null && bundle.getBoolean("speak_now", false)) {
            voiceHelper.startListening();
        }
    }

    private View.OnClickListener focusListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.edit_title) {
                Log.d("listen:", "title");
                editTitle.requestFocus();
                voiceHelper.setShown(editTitle);
            } else if (v.getId() == R.id.edit_content) {
                Log.d("listen:", "content");
                editContent.requestFocus();
                voiceHelper.setShown(editContent);
            }
            img_btn_home.setImageDrawable(getResources().getDrawable(R.drawable.ic_ok_24dp));
            isReturn = false;
        }
    };

    private View.OnClickListener homeListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (!isReturn) {
                InsertOrUpdateItem();
                isReturn = true;
                setClock(item.alarm_time);
            } else {
                finish();
            }
        }
    };

    private View.OnClickListener shareListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, "来看看今天我记的笔记吧~");
            startActivity(Intent.createChooser(shareIntent, "Share link using"));
        }
    };

    private View.OnClickListener clockListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            PopupMenu popMenu = new PopupMenu(NoteActivity.this, img_btn_clock);
            popMenu.getMenuInflater().inflate(R.menu.popup_menu, popMenu.getMenu());
            popMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    int id = item.getItemId();
                    if (id == R.id.popup_set_clock) {
                        ScrollView layout = (ScrollView) getLayoutInflater().inflate(R.layout.layout_date_time, null);
                        final DatePicker datePicker = (DatePicker) layout.findViewById(R.id.datePicker);
                        final TimePicker timePicker = (TimePicker) layout.findViewById(R.id.timePicker);
                        datePicker.init(calendar.get(Calendar.YEAR),
                                calendar.get(Calendar.MONTH),
                                calendar.get(Calendar.DAY_OF_MONTH), null);
                        timePicker.setIs24HourView(true);
                        new AlertDialog.Builder(NoteActivity.this)
                                .setView(layout)
                                .setPositiveButton("SET", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        alarmTime.setTime(datePicker.getYear(), datePicker.getMonth(),
                                                datePicker.getDayOfMonth(), timePicker.getCurrentHour(),
                                                timePicker.getCurrentMinute(), 0);
                                        deleteAlarm = false;
                                        img_btn_clock.setImageAlpha(255);
                                    }
                                })
                                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        alarmTime.setTime(0, 0, 0, 0, 0, 0);
                                    }
                                }).create().show();

                    } else if (id == R.id.popup_cancel) {
                        deleteAlarm = true;
                        img_btn_clock.setImageAlpha(100);
                    }
                    return true;
                }
            });
            popMenu.show();
        }
    };

    private View.OnClickListener detailListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.layou_popup, null);
            TextView tvCreate = (TextView) view.findViewById(R.id.tvCreate);
            TextView tvUpdate = (TextView) view.findViewById(R.id.tvUpdate);
            TextView tvAlarm = (TextView) view.findViewById(R.id.tvAlarm);

            PopupWindow popupWindow = new PopupWindow(view,
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.WRAP_CONTENT);
            popupWindow.setFocusable(true);

            ColorDrawable colorDrawable = new ColorDrawable(0x66000000);
            popupWindow.setBackgroundDrawable(colorDrawable);

            popupWindow.setAnimationStyle(R.style.popWindowAnimStyle);
            popupWindow.showAtLocation(fab, Gravity.BOTTOM, 0, 0);

            tvCreate.setText(item.create_time);
            tvUpdate.setText(item.modified_time);
            tvAlarm.setText(item.alarm_time == null ? "none" : item.alarm_time);
        }
    };

    private void InsertOrUpdateItem() {
        item.modified_time = TimeHolder.getCurrentTime();
        item.content = editContent.getText().toString();
        item.caption = editTitle.getText().toString();

        if (alarmTime.getSum() != 0)
            item.alarm_time = alarmTime.getDate() + " " + alarmTime.getTime();
        if (deleteAlarm)
            item.alarm_time = null;

        if (item.content == null && item.caption == null ||
                item.content.isEmpty() && item.caption.isEmpty())
            return;

        if (item.caption == null || item.caption.equals(""))
            item.caption = item.content.substring(0,
                    20 > item.content.length() ? item.content.length() : 20);

        if (isNew)
            dbHelper.insertData(item);
        else
            dbHelper.updateData(item);

        img_btn_home.setImageDrawable(getResources().getDrawable(R.drawable.ic_return_24dp));
    }

    private void setClock(String caption) {
        if (caption == null || alarmTime.getSum() == 0 || TimeHolder.isTimeInvalid(
                TimeHolder.parseTime(item.modified_time), alarmTime))
            return;
        calendar.set(alarmTime.getYear(), alarmTime.getMonth(), alarmTime.getDay(),
                alarmTime.getHour(), alarmTime.getMinute());
        Bundle bundle = new Bundle();
        bundle.putString("caption", caption);
        int id = preferences.getInt("remind", 1);
        switch (id) {
            case 0:
                bundle.putLong("when", calendar.getTimeInMillis());
                Intent serviceIntent = new Intent(NoteActivity.this, MyService.class);
                serviceIntent.putExtras(bundle);
                startService(serviceIntent);
                break;
            case 1:
                Intent dIntent = new Intent(NoteActivity.this, AlarmActivity.class);
                dIntent.putExtras(bundle);
                PendingIntent dpi = PendingIntent.getActivity(NoteActivity.this, 0, dIntent, 0);
                AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), dpi);
                break;
        }
        Snackbar.make(fab, "提醒设置成功~" + (id == 0 ? "notification" : "alarm"), Snackbar.LENGTH_SHORT)
                .setAction("Action", null).show();
    }

}

