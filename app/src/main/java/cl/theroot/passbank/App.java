package cl.theroot.passbank;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

public class App extends Application {
    public static final String CHANNEL_ID_RESPALDAR = "BdC-respaldar";
    public static final String CHANNEL_ID_GENERAL = "BdC-general";

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannelRespaldo = new NotificationChannel(
                    CHANNEL_ID_RESPALDAR,
                    "Canal del Servicio de Respaldo",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationChannel serviceChannelGeneral = new NotificationChannel(
                    CHANNEL_ID_GENERAL,
                    "Canal de Notificaciones Generales",
                    NotificationManager.IMPORTANCE_DEFAULT
            );

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannelRespaldo);
            manager.createNotificationChannel(serviceChannelGeneral);
        }
    }
}
