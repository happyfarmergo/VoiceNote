package com.android.voicenote.home;


import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.android.voicenote.R;
import com.android.voicenote.SpeechNote;
import com.android.voicenote.util.MyDatabaseHelper;


/**
 * A simple {@link Fragment} subclass.
 */
public class HomePageFrag extends Fragment {

    private ListView lists;
    private MyCallback mCallbacks;
    private TextView blankText;
    private MyDatabaseHelper dbHelper;
    private SimpleAdapter adapter;
    private ArrayList<Map<String, String>> items;

    public interface MyCallback {
        void onItemSelected(String id);

        void onItemLongClicked(String id);
    }

    public HomePageFrag() {
        // Required empty public constructor
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (!(activity instanceof MyCallback)) {
            throw new IllegalStateException("MainActivity未实现回调接口");
        }
        mCallbacks = (MyCallback) activity;
        Log.v("???", mCallbacks.toString());
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbHelper = ((SpeechNote)getActivity().getApplication()).getDbHelper();
        items = new ArrayList<>();
        loadDatabase();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_home_page, container, false);
        lists = (ListView) root.findViewById(R.id.lists);
        blankText = (TextView) root.findViewById(R.id.blankText);

        Log.d("onCreateView", "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");

        lists.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mCallbacks.onItemSelected(items.get(position).get("id"));
            }
        });

        lists.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                mCallbacks.onItemLongClicked(items.get(position).get("id"));
                return true;
            }
        });

        setContent();
        return root;
    }

    public void setContent() {
        if (items.isEmpty()) {
            blankText.setVisibility(View.VISIBLE);
            return;
        }
        blankText.setVisibility(View.GONE);
        adapter = new SimpleAdapter(getActivity(),
                items,
                R.layout.simple_note_item,
                new String[]{"caption", "create_time", "content"},
                new int[]{R.id.list_header, R.id.list_time, R.id.list_content}

        );
        lists.setAdapter(adapter);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }

    private void loadDatabase() {
        Cursor cursor = dbHelper.getReadableDatabase().rawQuery("select * from notes", null);
        items.clear();
        while (cursor.moveToNext()) {
            Map<String, String> map = new HashMap<>();
            map.put("id", "" + cursor.getInt(0));
            map.put("create_time", cursor.getString(1));
            map.put("caption", cursor.getString(4));
            map.put("content", cursor.getString(5));
            items.add(map);
        }
    }

    public void updateUI() {
        loadDatabase();
        if (adapter != null) {
            adapter.notifyDataSetChanged();
            if (items.isEmpty()) {
                blankText.setVisibility(View.VISIBLE);
                adapter = null;
            }
        } else
            setContent();
    }

}
