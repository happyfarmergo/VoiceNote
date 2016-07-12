package com.android.voicenote.voice;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;

import com.android.voicenote.R;
import com.android.voicenote.util.VoiceHelper;

public class VoiceActivity extends AppCompatActivity {

    private EditText demo;
    private VoiceHelper voiceHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        demo  =(EditText)findViewById(R.id.demo);
        voiceHelper = new VoiceHelper(VoiceActivity.this);
        voiceHelper.setShown(demo);
        voiceHelper.setUnderstand(true);

        final FloatingActionButton voiceInput = (FloatingActionButton) findViewById(R.id.voice_input);
        voiceInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                voiceHelper.startListening();
            }
        });
    }

}
