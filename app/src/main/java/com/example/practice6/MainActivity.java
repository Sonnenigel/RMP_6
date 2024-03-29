package com.example.practice6;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_NOTIFICATION = 1;
    private static final int PERMISSION_OVERLAY = 2;
    private NotificationChannel channel;
    private NotificationManager manager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        channel = new NotificationChannel("Notification", "Уведомленице", NotificationManager.IMPORTANCE_DEFAULT);
        manager = getSystemService(NotificationManager.class);
        manager.createNotificationChannel(channel);

        findViewById(R.id.buttonGetNotification).setOnClickListener(v -> {
            sendNotification();
            drawBanner();
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_OVERLAY && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            drawBanner();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        EditText editTextData = findViewById(R.id.editTextData);
        String notificationText = editTextData.getText().toString();
        if (requestCode == PERMISSION_OVERLAY && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Settings.canDrawOverlays(this)) {
            startForegroundService(new Intent(this, MyService.class).putExtra("info", notificationText));
        } else {
            Toast.makeText(this, "Меня запретили", Toast.LENGTH_SHORT).show();
        }
    }

    public void sendNotification(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.POST_NOTIFICATIONS}, PERMISSION_NOTIFICATION);
            return;
        }
        EditText editTextData = findViewById(R.id.editTextData);
        String notificationText = editTextData.getText().toString();
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channel.getId())
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(getString(R.string.notification_title))
                .setContentText(notificationText)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(1, builder.build());

        drawBanner();
    }

    public void drawBanner(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, PERMISSION_OVERLAY);
        } else {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.SYSTEM_ALERT_WINDOW) == PackageManager.PERMISSION_GRANTED) {
                EditText editTextData = findViewById(R.id.editTextData);
                String notificationText = editTextData.getText().toString();
                startForegroundService(new Intent(this, MyService.class).putExtra("info", notificationText));
            } else {
                ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.SYSTEM_ALERT_WINDOW}, PERMISSION_OVERLAY);
            }
        }
    }
}