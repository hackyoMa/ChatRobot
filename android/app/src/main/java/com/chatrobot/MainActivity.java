package com.chatrobot;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.KeyEvent;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.Setting;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.cloud.ui.RecognizerDialogListener;

import static android.content.pm.PackageManager.PERMISSION_DENIED;

public class MainActivity extends Activity {

    public static final int REQUEST_SELECT_FILE = 100;
    private final static int FILECHOOSER_RESULTCODE = 2;

    static {
        System.loadLibrary("native-lib");
    }

    public ValueCallback<Uri[]> uploadMessage;
    private WebView webView;
    private Context context;
    private Handler mHandler = new Handler();
    private ValueCallback<Uri> mUploadMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;

        //申请语音权限
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PERMISSION_DENIED) {
            ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.RECORD_AUDIO}, 0);
        }

        //初始化语音识别
        SpeechUtility.createUtility(context, SpeechConstant.APPID + "=5922e1ee");
        Setting.setLocationEnable(false);
        //配置WebView
        webView = new WebView(context);
        webView.setWebViewClient(new webViewClient());
        webView.setWebChromeClient(new webChromeClient());
        webView.setHorizontalScrollBarEnabled(false);//关闭水平滚动条
        webView.setVerticalScrollBarEnabled(false);//关闭垂直滚动条
        webView.addJavascriptInterface(new javascriptInterface(), "ov");//启用JS调用Native支持
        WebSettings webSettings = webView.getSettings();
        webSettings.setUserAgentString(webSettings.getUserAgentString() + " ChatRobotApp");//修改ua使得web端正确判断
        webSettings.setSupportZoom(false);//禁止缩放
        webSettings.setJavaScriptEnabled(true);//支持JS
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);//支持通过JS打开新窗口
        webSettings.setAllowFileAccess(true);//设置可以访问文件
        webSettings.setLoadsImagesAutomatically(true);//支持自动加载图片
        webSettings.setDefaultTextEncodingName("UTF-8");//设置编码格式
        webSettings.setDomStorageEnabled(true);//开启DOMStorage功能
        webSettings.setAppCacheMaxSize(1024 * 1024 * 8);//设置缓存大小
        webSettings.setAppCachePath(getApplicationContext().getCacheDir().getAbsolutePath());//设置Application Caches缓存目录
        webSettings.setAppCacheEnabled(true);//开启Application Caches功能
        webSettings.setDatabaseEnabled(true);//开启database storage功能
        webView.loadUrl("https://www.spicybar.cn/");
    }

    /**
     * 解决返回键退出程序
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && webView.canGoBack()) {
            webView.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 开启语音识别
     */
    public void initSpeech(final Context context) {
        RecognizerDialog mDialog = new RecognizerDialog(context, null);
        mDialog.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
        mDialog.setParameter(SpeechConstant.ACCENT, "mandarin");
        mDialog.setListener(new RecognizerDialogListener() {

            @Override
            public void onResult(RecognizerResult recognizerResult, boolean isLast) {
                if (!isLast) {
                    readResult(recognizerResult.getResultString());
                }
            }

            @Override
            public void onError(SpeechError error) {
            }

        });
        mDialog.show();
    }

    /**
     * 读取识别结果并发送
     */
    public void readResult(final String result) {
        JSONObject resultObj = JSON.parseObject(result);
        JSONArray resultArr = resultObj.getJSONArray("ws");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < resultArr.size(); i++) {
            String resultStr = JSON.parseObject(JSON.parseObject(resultArr.get(i).toString()).getJSONArray("cw").get(0).toString()).getString("w");
            sb.append(resultStr);
        }
        webView.loadUrl("javascript:autoSendMessage('" + sb.toString() + "')");
    }

    /**
     * 获取选择的文件数据
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (requestCode == REQUEST_SELECT_FILE) {
                if (uploadMessage == null) {
                    return;
                }
                uploadMessage.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode, intent));
                uploadMessage = null;
            }
        } else if (requestCode == FILECHOOSER_RESULTCODE) {
            if (null == mUploadMessage) {
                return;
            }
            Uri result = intent == null || resultCode != MainActivity.RESULT_OK ? null : intent.getData();
            mUploadMessage.onReceiveValue(result);
            mUploadMessage = null;
        } else {
            Toast.makeText(getBaseContext(), "Failed to Upload Image", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * 与JS交互
     */
    private final class javascriptInterface {
        //开始语音识别
        @JavascriptInterface
        public void openVoice() {
            mHandler.post(new Runnable() {
                public void run() {
                    initSpeech(context);
                }
            });
        }
    }

    /**
     * 重写WebChromeClient
     */
    private class webChromeClient extends WebChromeClient {

        //配置加载进度
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            if (newProgress >= 100) {
                setContentView(webView);
            }
        }

        //选择图片文件
        protected void openFileChooser(ValueCallback<Uri> uploadMsg) {
            mUploadMessage = uploadMsg;
            Intent i = new Intent(Intent.ACTION_GET_CONTENT);
            i.addCategory(Intent.CATEGORY_OPENABLE);
            i.setType("image/*");
            startActivityForResult(Intent.createChooser(i, "File Chooser"), FILECHOOSER_RESULTCODE);
        }

        //3.0+选择图片文件
        protected void openFileChooser(ValueCallback uploadMsg, String acceptType) {
            mUploadMessage = uploadMsg;
            Intent i = new Intent(Intent.ACTION_GET_CONTENT);
            i.addCategory(Intent.CATEGORY_OPENABLE);
            i.setType("image/*");
            startActivityForResult(Intent.createChooser(i, "File Browser"), FILECHOOSER_RESULTCODE);
        }

        //4.1选择图片文件
        protected void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
            mUploadMessage = uploadMsg;
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/*");
            startActivityForResult(Intent.createChooser(intent, "File Browser"), FILECHOOSER_RESULTCODE);
        }

        //5.0+选择图片文件
        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        public boolean onShowFileChooser(WebView mWebView, ValueCallback<Uri[]> filePathCallback, WebChromeClient.FileChooserParams fileChooserParams) {
            if (uploadMessage != null) {
                uploadMessage.onReceiveValue(null);
                uploadMessage = null;
            }
            uploadMessage = filePathCallback;
            Intent intent = fileChooserParams.createIntent();
            try {
                startActivityForResult(intent, REQUEST_SELECT_FILE);
            } catch (ActivityNotFoundException e) {
                uploadMessage = null;
                Toast.makeText(getBaseContext(), "Cannot Open File Chooser", Toast.LENGTH_LONG).show();
                return false;
            }
            return true;
        }

    }

    /**
     * 重写WebViewClient
     */
    private class webViewClient extends WebViewClient {

        //防止打开外部页面
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }

        //配置错误页
        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            view.loadUrl("file:///android_asset/error.html");
        }

    }

}
