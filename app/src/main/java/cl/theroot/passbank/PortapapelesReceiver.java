package cl.theroot.passbank;

import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class PortapapelesReceiver extends BroadcastReceiver {
    private static final String TAG = "BdC-PortapapelesReceiver";
    public static final String LABEL_CLIPBOARD = "Hola";
    public static final int TIEMPO_DEFECTO = 30;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "onReceive(...) - Limpiando Portapapeles.");
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard.hasPrimaryClip()) {
            ClipData clip = clipboard.getPrimaryClip();
            if (clip != null && clip.getDescription().hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)) {
                clipboard.clearPrimaryClip();
            }
        }
    }
}
