package price.and.me;

// 3 imports for loading page
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ProgressBar;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Toast;

import price.and.me.R;

import price.and.me.Service.*;

public class MainActivity extends AppCompatActivity{
    private WebView webView;
    private ProgressBar spinner;
    private ImageView pandmeLogo;
    String ShowOrHideWebViewInitialUse = "show";
    private long startTime;
    //public static final int ELAPSED_TIME_LIMIT_FOR_NOTIFICATION = 20000;

    public static boolean isSituationForNotification;
    private long elapsedTime;
    public static Intent startServiceIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        getSupportActionBar().hide(); //hide the title bar
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        isSituationForNotification = false;
        elapsedTime = 0;
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        startTime = sharedPreferences.getLong("startTime", 0);

        if(startTime == 0){
            isSituationForNotification = true;
            startTime = System.currentTimeMillis();
            startServiceIntent = new Intent(this, MyServiceForBackground.class);
            //startServiceIntent.putExtra("startTime", startTime);
            Thread threadBackground = new Thread(new Runnable() {
                @Override
                public void run() {
                    startService(startServiceIntent);
                }
            });
            threadBackground.start();
            //showNotification("Price&Me'ye hoş geldin","Hesaplarını bağla, sana özel kampanya ve indirimlerden indirimlerden yararlan...");
            //Log.i("checkMethod000Thread", "CREATED");
        }

        if (!isConnected(MainActivity.this)) buildDialog(MainActivity.this).show();
        else {

            //Log.i("checkMethod000Create1", "startTime = "  + startTime + "; sharedPreferences " + sharedPreferences.getLong("startTime", System.currentTimeMillis()));

            Intent intent = getIntent();
            String action = intent.getAction();
            Uri data = intent.getData();

            setContentView(R.layout.activity_main);
            pandmeLogo = (ImageView) findViewById(R.id.pandmeLogo);
            webView = (WebView) findViewById(R.id.myWebView);

            spinner = (ProgressBar) findViewById(R.id.progressBar1);
            int colorCodeDark = Color.parseColor("#ffa500");
            spinner.setIndeterminateTintList(ColorStateList.valueOf(colorCodeDark));


            WebSettings myWebSettings = webView.getSettings();
            myWebSettings.setDomStorageEnabled(true);
            myWebSettings.setJavaScriptEnabled(true);
            webView.loadUrl("https://www.priceandme.com/");
            webView.setWebViewClient(new CustomWebViewClient());
            webView.getSettings();
            webView.setBackgroundColor(Color.WHITE);


            myWebSettings.setSaveFormData(false);
            CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.setAcceptCookie(true);
            cookieManager.setAcceptThirdPartyCookies(webView, true);

            myWebSettings.setAppCacheEnabled(true);
            cookieManager.setAcceptFileSchemeCookies(true);
        }
    }

    private class CustomWebViewClient extends WebViewClient {

        @Override
        public void onLoadResource(WebView view, String url) {
            //Log.i("checkMethod222", url);
            if("https://www.priceandme.com/api/user/getUserInfo".equals(url)){
                isSituationForNotification = false;
                if(MyServiceForBackground.mTimer != null)
                    MyServiceForBackground.mTimer.cancel();
                if(startServiceIntent != null)
                    stopService(startServiceIntent);
                //sendBroadcast(newIntentForBroadCast);
                //Log.i("timeStartGetUser", startTime + "===" + System.currentTimeMillis() + "==+" + elapsedTime);
            }
            if("https://www.priceandme.com/api/user/logout".equals(url)){
                startTime = System.currentTimeMillis();
                isSituationForNotification = true;
            }
            //elapsedTime = System.currentTimeMillis() - startTime;
            //Log.i("checkMethodElapsedLoad", "elapsedTime = "  + elapsedTime);
        }


        //Method to have deeplkink for apps or web app
        @Override
        public boolean shouldOverrideUrlLoading(WebView view,String url) {
            //Log.i("checkLink2", url);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            if (intent.resolveActivity(getPackageManager()) != null) {
                System.out.println("check111");

                try {
                    Thread.sleep(0);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                startActivity(intent);

                System.out.println("check115");
                return true;
            } else {
                System.out.println("check112");
                Toast.makeText(getApplicationContext(), "No apps to handle!!!", Toast.LENGTH_SHORT);
            }
            view.loadUrl(url);
            return true;
        }

        //To have loading page when app is opening after sleep(destroy)
        @Override
        public void onPageStarted(WebView webview, String url, Bitmap favicon) {
            //Log.i("checkLink3", url);
            // only make it invisible the FIRST time the app is run
            if (ShowOrHideWebViewInitialUse.equals("show")) {
                webview.setVisibility(webview.INVISIBLE);
            }
        }

        //To have normal homepage after loading page when app is opening after sleep(destroy)
        @Override
        public void onPageFinished(WebView view, String url) {
            //Log.i("checkLink4", url);
            ShowOrHideWebViewInitialUse = "hide";
            spinner.setVisibility(View.GONE);
            pandmeLogo.setVisibility(View.GONE);

            view.setVisibility(webView.VISIBLE);
            super.onPageFinished(view, url);
        }
    }

    //For checking internet connection in the beginning
    public boolean isConnected(Context context) {

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netinfo = cm.getActiveNetworkInfo();

        if (netinfo != null && netinfo.isConnectedOrConnecting()) {
            android.net.NetworkInfo wifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            android.net.NetworkInfo mobile = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

            if ((mobile != null && mobile.isConnectedOrConnecting()) || (wifi != null && wifi.isConnectedOrConnecting()))
                return true;
            else
                return false;
        } else
            return false;
    }

    //Alert Builder for no internet connection
    public AlertDialog.Builder buildDialog(Context c) {

        AlertDialog.Builder builder = new AlertDialog.Builder(c);
        builder.setTitle("İnternet Bağlantısı Yok");
        builder.setMessage("Uygulamayı kullanabilmeniz için internet bağlantınız olması gerekmektedir. Lütfen bağlantı ayarlanarızı kontrol ediniz.");
        builder.setIcon(R.drawable.andme2);
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {


            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });

        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                finish();
            }
        });

        return builder;
    }

    //To be able to go back with default android GoBack Button at the bottom
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Check if the key event was the Back button and if there's history
        if ((keyCode == KeyEvent.KEYCODE_BACK) && webView.canGoBack()) {
            webView.goBack();
            return true;
        }
        // If it wasn't the Back key or there's no web page history, bubble up to the default
        // system behavior (probably exit the activity)
        return super.onKeyDown(keyCode, event);
    }

    //To save startTime when isSituationForNotification is true
    @Override
    protected void onPause() {
        if(isSituationForNotification) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            //Log.i("checkMethod000Pause1", "startTime = " + startTime + "; sharedPreferences " + sharedPreferences.getLong("startTime", 0));

            SharedPreferences.Editor spEditor = sharedPreferences.edit();
            spEditor.putLong("startTime", startTime);
            spEditor.apply();

            //Log.i("checkMethod000Pause2", "startTime = " + startTime + "; sharedPreferences " + sharedPreferences.getLong("startTime", 0));
        }
        super.onPause();
    }

}
