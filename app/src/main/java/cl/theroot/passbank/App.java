package cl.theroot.passbank;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;

public class App extends Application {
    public static final String CHANNEL_ID_RESPALDAR = "BdC-respaldar";
    public static final String CHANNEL_ID_GENERAL = "BdC-general";

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    private void createNotificationChannel() {
        NotificationChannel serviceChannelRespaldo = new NotificationChannel(
                CHANNEL_ID_RESPALDAR,
                getString(R.string.canalServRespaldo),
                NotificationManager.IMPORTANCE_DEFAULT
        );
        NotificationChannel serviceChannelGeneral = new NotificationChannel(
                CHANNEL_ID_GENERAL,
                getString(R.string.canalGeneral),
                NotificationManager.IMPORTANCE_DEFAULT
        );

        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.createNotificationChannel(serviceChannelRespaldo);
        manager.createNotificationChannel(serviceChannelGeneral);
    }
}
