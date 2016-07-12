package com.android.voicenote.util;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.LexiconListener;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.ui.RecognizerDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.LinkedHashMap;

import com.android.voicenote.R;
import com.iflytek.cloud.util.ContactManager;

/**
 * Created by lvjinhua on 6/7/2016.
 */
public class VoiceHelper {
    private RecognizerDialog mDialog;
    private SpeechRecognizer mIat;
    private HashMap<String, String> mResults = new LinkedHashMap<String, String>();
    private Context mContext;
    EditText editText;
    Toast toast;
    boolean isSpeaking;
    String result;
    String[] contacts;

    boolean understand;
    boolean call;
    boolean sms;
    Cursor cursor;

    public VoiceHelper(Context acitivity) {
        mContext = acitivity;
        isSpeaking = false;
        understand = false;
        toast = Toast.makeText(mContext, "", Toast.LENGTH_SHORT);
        mIat = SpeechRecognizer.createRecognizer(acitivity, mInitListener);
    }

    public void setShown(EditText text) {
        this.editText = text;
    }

    public void setUnderstand(boolean b) {
        understand = b;
        if (understand) {
            ContactManager mgr = ContactManager.createManager(mContext,
                    mContactListener);
            mgr.asyncQueryAllContactsName();
        }

    }

    private InitListener mInitListener = new InitListener() {

        @Override
        public void onInit(int code) {
            Log.d("no", "SpeechRecognizer init() code = " + code);
            if (code != ErrorCode.SUCCESS) {
                showTip("初始化失败，错误码：" + code);
            }
        }
    };
//
//    private RecognizerDialogListener mDialogListener = new RecognizerDialogListener() {
//        @Override
//        public void onResult(RecognizerResult recognizerResult, boolean b) {
//            printResult(recognizerResult);
//        }
//
//        @Override
//        public void onError(SpeechError speechError) {
//            showTip(speechError.getPlainDescription(true));
//        }
//    };

    private RecognizerListener mRecognizerListener = new RecognizerListener() {

        @Override
        public void onBeginOfSpeech() {
            // 此回调表示：sdk内部录音机已经准备好了，用户可以开始语音输入
            showTip("开始说话");
        }

        @Override
        public void onError(SpeechError error) {
            // Tips：
            // 错误码：10118(您没有说话)，可能是录音机权限被禁，需要提示用户打开应用的录音权限。
            // 如果使用本地功能（语记）需要提示用户开启语记的录音权限。
            showTip(error.getPlainDescription(true));
        }

        @Override
        public void onEndOfSpeech() {
            // 此回调表示：检测到了语音的尾端点，已经进入识别过程，不再接受语音输入
            showTip("结束说话");
            isSpeaking = false;
        }

        @Override
        public void onResult(RecognizerResult results, boolean isLast) {
            printResult(results);

            if (isLast) {

            }
        }

        @Override
        public void onVolumeChanged(int volume, byte[] data) {
            showTip("当前正在说话，音量大小：" + volume);
        }

        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {

        }
    };

    private LexiconListener mLexiconListener = new LexiconListener() {

        @Override
        public void onLexiconUpdated(String lexiconId, SpeechError error) {
            if (error != null) {
                showTip(error.toString());
            } else {
                showTip("上传联系人成功");
            }
        }
    };

    private ContactManager.ContactListener mContactListener = new ContactManager.ContactListener() {

        @Override
        public void onContactQueryFinish(final String contactInfos, boolean changeFlag) {
            // 注：实际应用中除第一次上传之外，之后应该通过changeFlag判断是否需要上传，否则会造成不必要的流量.
            // 每当联系人发生变化，该接口都将会被回调，可通过ContactManager.destroy()销毁对象，解除回调。
            // if(changeFlag) {
            // 指定引擎类型

            int ret = mIat.updateLexicon("contact", contactInfos, mLexiconListener);
            contacts = contactInfos.split("\\n");
            if (ret != ErrorCode.SUCCESS) {
                showTip("上传联系人失败：" + ret);
            }
        }
    };

    private void parseIntent() {
        call = sms = false;
        for (String key : mResults.keySet()) {
            if (mResults.get(key).contains("电话")) {
                call = true;
            }
            if (mResults.get(key).contains("短信")) {
                sms = true;
            }
        }
        String toCall = null;
        for (String people : contacts) {
            if (result.contains(people)) {
                toCall = people;
                break;
            }
        }
        if (toCall == null) return;

        if (call) {
            ContentResolver cr = mContext.getContentResolver();
            cursor = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
            while (cursor.moveToNext()) {
                int nameIndex = cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME);
                String name = cursor.getString(nameIndex);
                if (!name.equals(toCall)) continue;

                String contactID = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                Cursor phone = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=" + contactID, null, null);
                String number = null;
                if (phone.moveToNext()) {
                    number = phone.getString(phone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                }
                Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + number));
                mContext.startActivity(intent);
                cursor.close();
            }

        } else if (sms) {

        }
    }

    private void printResult(RecognizerResult results) {
        String text = JsonParser.parseIatResult(results.getResultString());
        String sn = null;
        // 读取json结果中的sn字段
        try {
            JSONObject resultJson = new JSONObject(results.getResultString());
            sn = resultJson.optString("sn");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (sn.equals("2")) {
            return;
        }

        mResults.put(sn, text);

        StringBuffer resultBuffer = new StringBuffer();
        for (String key : mResults.keySet()) {
            resultBuffer.append(mResults.get(key));
        }

        result = resultBuffer.toString();
        Log.d("Voice Text: ", resultBuffer.toString());

        editText.setText(editText.getText().toString() + result);
        editText.setSelection(editText.length());

        if (understand) {
            parseIntent();
        }
    }


    public boolean isSpeaking() {
        return isSpeaking;
    }

    public void startListening() {
        setParams();
        isSpeaking = true;
        mResults.clear();
        result = null;
        int ret = mIat.startListening(mRecognizerListener);
        if (ret != ErrorCode.SUCCESS) {
            showTip("听写失败,错误码：" + ret);
        } else {
            showTip(mContext.getString(R.string.text_begin));
        }
    }

    public void stopListening() {
        if (isSpeaking) {
            mIat.stopListening();
            isSpeaking = false;
        }

    }

    private void setParams() {
        mIat.setParameter(SpeechConstant.PARAMS, null);

        // 设置听写引擎
        mIat.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD);
        // 设置返回结果格式
        mIat.setParameter(SpeechConstant.RESULT_TYPE, "json");

        int lang = mContext.getSharedPreferences("user", mContext.MODE_PRIVATE).getInt("language", 0);
        if (lang == 0) {
            // 设置语言
            mIat.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
            // 设置语言区域
            mIat.setParameter(SpeechConstant.ACCENT, "mardarin");
        } else {
            mIat.setParameter(SpeechConstant.LANGUAGE, "en_us");
        }

        // 设置语音前端点:静音超时时间，即用户多长时间不说话则当做超时处理
        mIat.setParameter(SpeechConstant.VAD_BOS, "4000");

        // 设置语音后端点:后端点静音检测时间，即用户停止说话多长时间内即认为不再输入， 自动停止录音
        mIat.setParameter(SpeechConstant.VAD_EOS, "1000");

        // 设置标点符号,设置为"0"返回结果无标点,设置为"1"返回结果有标点
        mIat.setParameter(SpeechConstant.ASR_PTT, "1");

    }

    public void showTip(String s) {
        toast.setText(s);
        toast.show();
    }
}
