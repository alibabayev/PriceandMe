package price.and.me.Service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import price.and.me.R;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class MyServiceForBackground extends Service {

    public static Timer mTimer;
    public TimerTask timerTask = new TimerTask(){
        @Override
        public void run(){
            showNotification("Kampanyaları kaçırma", "Hesaplarını bağla, sana özel kampanya ve indirimlerden yararlan...");
        }
    };

    @Override
    public void onCreate() {
        Log.i("BackgroundManager", "onCreateCalled");
        super.onCreate();
        if(mTimer == null) {
            mTimer = new Timer();
            mTimer.schedule(timerTask, 30000L, 1000L * 60L * 60L * 24L);
        }
    }

    /*
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        long elapsedTime;
        long startTime = intent.getLongExtra("startTime", System.currentTimeMillis());
        //Log.i("checkMethodSERVICE", "start Time= "  + startTime);


        while(isSituationForNotification) {
            elapsedTime = System.currentTimeMillis() - startTime;
            ////Log.i("checkMethodSERVICE", "elapsedTime = "  + elapsedTime);
            if(ELAPSED_TIME_LIMIT_FOR_NOTIFICATION < elapsedTime) {
                showNotification("Kampanyaları kaçırma Kapattığında bile", "Hesaplarını bağla, sana özel kampanya ve indirimlerden haberdar ol");
                isSituationForNotification = false;
                //stopService(intent); //to stop running in the background
                stopSelf();
                break; //can be commented in the future
            }
        }

        //Log.i("checkMethodSERVICE", "Service STOPPED");
        return START_NOT_STICKY;
    }
    */

    @Override
    public IBinder onBind(Intent intent) {

        Log.i("BackgroundManager", "onBindCalled");
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("BackgroundManager", "onStartCommandCalled");
        if(mTimer != null){
            Log.i("BackgroundManager", "onStartCommandIFCalled");
            mTimer.cancel();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void showNotification(String title, String body){
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        String NOTIFICATION_CHANNEL_ID = "testChannel";

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "Notification",
                    NotificationManager.IMPORTANCE_DEFAULT);


            notificationChannel.setDescription("Price Channel");
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.BLUE);
            notificationChannel.enableLights(true);
            notificationManager.createNotificationChannel(notificationChannel);
        }

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);

        notificationBuilder.setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.andme2)
                .setContentTitle(title)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                .setContentText(body)
                .setContentInfo("Info");

        notificationManager.notify(new Random().nextInt(), notificationBuilder.build());
    }

    @Override
    public void onDestroy() {

        Log.i("BackgroundManager", "onDestroyCalled");
        super.onDestroy();
    }

    @Override
    public void onTrimMemory(int level) {

        Log.i("BackgroundManager", "onTrimMemoryCalled");
        super.onTrimMemory(level);
    }

    @Override
    public void onLowMemory() {

        Log.i("BackgroundManager", "onLowMemoryCalled");
        super.onLowMemory();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {

        Log.i("BackgroundManager", "onTaskRemovedCalled");
        super.onTaskRemoved(rootIntent);
    }
}
