package com.example.zapstation;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.IBinder;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

public class ServicioCargaCoche extends Service {
    private static final String CHANNEL_ID = "carga_coche_channel";
    private static final int NOTIFICACION_ID = 1;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Carga del coche",
                    NotificationManager.IMPORTANCE_HIGH
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Notification notification = crearNotificacion();

        startForeground(NOTIFICACION_ID, notification);

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private Notification crearNotificacion() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.bateria))
                .setWhen(System.currentTimeMillis())
                .setContentInfo("Información adicional")
                .setTicker("Cargando coche")
                .setContentTitle("Servicio de Carga del Coche")
                .setContentText("La carga del coche está en progreso.")
                .setSmallIcon(R.drawable.bateria)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(false)
                .setOngoing(true)
                .build();
    }
}
