package com.android.voicenote;

import android.app.AlarmManager;
import android.app.Fragment;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.TimePicker;

import com.android.voicenote.home.AlarmActivity;
import com.android.voicenote.home.HomePageFrag;
import com.wangjie.androidbucket.utils.ABTextUtil;
import com.wangjie.rapidfloatingactionbutton.RapidFloatingActionButton;
import com.wangjie.rapidfloatingactionbutton.RapidFloatingActionHelper;
import com.wangjie.rapidfloatingactionbutton.RapidFloatingActionLayout;
import com.wangjie.rapidfloatingactionbutton.contentimpl.labellist.RFACLabelItem;
import com.wangjie.rapidfloatingactionbutton.contentimpl.labellist.RapidFloatingActionContentLabelList;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.android.voicenote.about.AboutFrag;
import com.android.voicenote.calender_view.CalenderViewFrag;
import com.android.voicenote.edit.NoteActivity;
import com.android.voicenote.service.MyService;
import com.android.voicenote.settings.SettingFrag;
import com.android.voicenote.util.MyDatabaseHelper;
import com.android.voicenote.util.TimeHolder;
import com.android.voicenote.voice.VoiceActivity;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        com.android.voicenote.home.HomePageFrag.MyCallback,
        SettingFrag.SettingCallBack,
        RapidFloatingActionContentLabelList.OnRapidFloatingActionContentLabelListListener {

    public static final int NEW_NOTE_ID = 0x100;

    private RapidFloatingActionLayout rfaLayout;
    private RapidFloatingActionButton rfaButton;
    private RapidFloatingActionHelper rfabHelper;

    private String mTitle;
    private TextView userName;
    private Class mFragmentClass;
    private Fragment fragment;
    private ArrayList<Class> mClassCreated;
    private ArrayList<Fragment> mFragments;

    private Calendar calendar;
    private TimeHolder createTime;
    private TimeHolder alarmTime;

    private EditText editClock;
    private AlertDialog clockDialog;

    private static Context mContext;
    private MyDatabaseHelper dbHelper;
    public SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mClassCreated = new ArrayList<>();
        mFragments = new ArrayList<>();
        mTitle = getResources().getString(R.string.app_name);
        createTime = new TimeHolder();
        alarmTime = new TimeHolder();
        calendar = Calendar.getInstance();
        clockDialog = null;
        mContext = this;

        preferences = getSharedPreferences("user", MODE_PRIVATE);
        dbHelper = getDbHelper();

        setRFAC();

        userName = (TextView) getLayoutInflater().inflate(R.layout.nav_header_main, null).findViewById(R.id.userName);
        userName.setText("hello, " + preferences.getString("name", "world"));

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        mFragmentClass = HomePageFrag.class;
        startTransFrag();

    }

    private void setRFAC() {
        rfaLayout = (RapidFloatingActionLayout) findViewById(R.id.activity_main_rfal);
        rfaButton = (RapidFloatingActionButton) findViewById(R.id.fab);
        RapidFloatingActionContentLabelList rfaContent = new RapidFloatingActionContentLabelList(this);
        rfaContent.setOnRapidFloatingActionContentLabelListListener(MainActivity.this);
        List<RFACLabelItem> items = new ArrayList<>();
        items.add(new RFACLabelItem<Integer>()
                .setLabel(getString(R.string.voice_speak))
                .setDrawable(getResources().getDrawable(android.R.drawable.ic_btn_speak_now))
                .setIconNormalColor(0xff1e9e1e)
                .setIconPressedColor(0xFF146E14)
                .setLabelColor(0xff1e9e1e)
                .setWrapper(0)
        );

        items.add(new RFACLabelItem<Integer>()
                .setLabel(getString(R.string.btn_clock))
                .setResId(R.drawable.ic_clock_32dp)
                .setIconNormalColor(0xff24a4e9)
                .setIconPressedColor(0xff199698)
                .setLabelColor(0xff24a4e9)
                .setWrapper(3)
        );

        items.add(new RFACLabelItem<Integer>()
                .setLabel(getString(R.string.btn_text))
                .setResId(R.drawable.ic_write_32dp)
                .setIconNormalColor(0xFF3C51F0)
                .setIconPressedColor(0xFF3C51F0)
                .setLabelColor(0xFF3C51F0)
                .setWrapper(2)
        );

        rfaContent
                .setItems(items)
                .setIconShadowRadius(ABTextUtil.dip2px(this, 5))
                .setIconShadowColor(0xff888888)
                .setIconShadowDy(ABTextUtil.dip2px(this, 5))
        ;

        rfabHelper = new RapidFloatingActionHelper(
                this,
                rfaLayout,
                rfaButton,
                rfaContent
        ).build();

    }


    @Override
    public void onRFACItemLabelClick(int i, RFACLabelItem rfacLabelItem) {
        Intent intent = new Intent(MainActivity.this, NoteActivity.class);
        Bundle bundle = new Bundle();
        switch (i) {
            case 0:
                bundle.putBoolean("speak_now", true);
                intent.putExtras(bundle);
                startActivityForResult(intent, NEW_NOTE_ID);
                break;
            case 1:
                startClock();
                break;
            case 2:
                startActivityForResult(intent, NEW_NOTE_ID);
                break;
        }
        rfabHelper.toggleContent();
    }

    @Override
    public void onRFACItemIconClick(int i, RFACLabelItem rfacLabelItem) {
        onRFACItemLabelClick(i, rfacLabelItem);
    }

    public void startClock() {
        LinearLayout layout = (LinearLayout) getLayoutInflater().inflate(R.layout.layout_set_clock, null);
        editClock = (EditText) layout.findViewById(R.id.edit_clock);
        editClock.addTextChangedListener(watcher);
        ImageButton imageButton = (ImageButton) layout.findViewById(R.id.btn_set_clock);
        imageButton.setOnClickListener(selectTimeListener);
        alarmTime.setTime(0, 0, 0, 0, 0, 0);
        clockDialog = new AlertDialog.Builder(this)
                .setView(layout)
                .setPositiveButton("SET", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //添加一条闹钟记录
                        addClock();
                        //设置闹钟
                        setClock(editClock.getText().toString());
                    }

                })
                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        clockDialog = null;
                    }
                })
                .create();
        clockDialog.show();
        clockDialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
    }

    private void setClock(String caption) {
        if (alarmTime.getSum() == 0 || TimeHolder.isTimeInvalid(createTime, alarmTime))
            return;
        calendar.set(alarmTime.getYear(), alarmTime.getMonth(), alarmTime.getDay(),
                alarmTime.getHour(), alarmTime.getMinute());
        Bundle bundle = new Bundle();
        bundle.putString("caption", caption);
        int id = preferences.getInt("remind", 1);
        switch (id) {
            case 0:
                bundle.putLong("when", calendar.getTimeInMillis());
                Intent serviceIntent = new Intent(MainActivity.this, MyService.class);
                serviceIntent.putExtras(bundle);
                startService(serviceIntent);
                break;
            case 1:
                Intent dIntent = new Intent(MainActivity.this, AlarmActivity.class);
                dIntent.putExtras(bundle);
                PendingIntent dpi = PendingIntent.getActivity(MainActivity.this, 0, dIntent, 0);
                AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), dpi);
                break;
        }
        Snackbar.make(rfaButton, "提醒设置成功~" + (id == 0 ? "notification" : "alarm"), Snackbar.LENGTH_SHORT)
                .setAction("Action", null).show();
    }

    private void addClock() {
        String caption = editClock.getText().toString();
        String content = editClock.getText().toString();
        String create_time = TimeHolder.getCurrentTime();
        String alarm_time = alarmTime.getDate() + " " + alarmTime.getTime();
        if (alarmTime.getSum() == 0 || TimeHolder.isTimeInvalid(createTime, alarmTime))
            alarm_time = null;

        dbHelper.insertData(create_time, alarm_time, caption, content);
        ((HomePageFrag) mFragments.get(0)).updateUI();
    }



    private View.OnClickListener selectTimeListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ScrollView layout = (ScrollView) getLayoutInflater().inflate(R.layout.layout_date_time, null);
            final DatePicker datePicker = (DatePicker) layout.findViewById(R.id.datePicker);
            final TimePicker timePicker = (TimePicker) layout.findViewById(R.id.timePicker);
            datePicker.init(calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH), null);
            timePicker.setIs24HourView(true);
            new AlertDialog.Builder(MainActivity.this)
                    .setView(layout)
                    .setPositiveButton("SET", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            alarmTime.setTime(datePicker.getYear(), datePicker.getMonth(),
                                    datePicker.getDayOfMonth(), timePicker.getCurrentHour(),
                                    timePicker.getCurrentMinute(), 0);
                        }
                    })
                    .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            alarmTime.setTime(0, 0, 0, 0, 0, 0);
                        }
                    }).create().show();

        }
    };


    private TextWatcher watcher = new TextWatcher() {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            if (s.length() == 0)
                clockDialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
            else
                clockDialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(true);
        }
    };

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.nav_home:
                mFragmentClass = HomePageFrag.class;
                mTitle = getResources().getString(R.string.app_name);
                break;
            case R.id.nav_calender:
                mFragmentClass = CalenderViewFrag.class;
                mTitle = getResources().getString(R.string.calender_view);
                break;
            case R.id.nav_helper:
                startActivity(new Intent(MainActivity.this, VoiceActivity.class));
                return true;
            case R.id.nav_settings:
                mFragmentClass = SettingFrag.class;
                mTitle = getResources().getString(R.string.settings);
                break;
            case R.id.nav_about:
                mFragmentClass = AboutFrag.class;
                mTitle = getResources().getString(R.string.about);
                break;
            default:
                mFragmentClass = HomePageFrag.class;
                mTitle = getResources().getString(R.string.app_name);
        }

        startTransFrag();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void startTransFrag() {
        try {
            if (!mClassCreated.contains(mFragmentClass)) {
                mClassCreated.add(mFragmentClass);
                fragment = (Fragment) mFragmentClass.newInstance();
                mFragments.add(fragment);
            } else {
                fragment = mFragments.get(mClassCreated.indexOf(mFragmentClass));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        getFragmentManager().beginTransaction().replace(R.id.to_replace, fragment).commit();
        getSupportActionBar().setTitle(mTitle);
    }

    @Override
    public void onItemSelected(String id) {

        Bundle bundle = new Bundle();
        bundle.putSerializable("id", id);
        Intent intent = new Intent(MainActivity.this, NoteActivity.class);
        intent.putExtras(bundle);
        startActivityForResult(intent, NEW_NOTE_ID);
    }

    @Override
    public void onItemLongClicked(final String id) {
        //展示删除界面
        new AlertDialog.Builder(this)
                .setTitle("删除")
                .setCancelable(true)
                .setMessage("确定要删除吗？")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //删除记录
                        getDbHelper().deleteData(id);

                        ((HomePageFrag) mFragments.get(0)).updateUI();
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //取消
                    }
                }).create().show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == NEW_NOTE_ID) {
            ((HomePageFrag) mFragments.get(0)).updateUI();
        }
    }

    public MyDatabaseHelper getDbHelper() {
        return ((SpeechNote) getApplication()).getDbHelper();
    }


    @Override
    public void onNameChanged(String newName) {
        userName.setText("hello, " + newName);
    }

    public static Context getContext() {
        return mContext;
    }
}
