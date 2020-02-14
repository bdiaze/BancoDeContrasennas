package cl.theroot.passbank;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.view.Gravity;
import android.widget.TextView;
import android.widget.Toast;

public class CustomToast {
    public static void Build(Fragment fragment, int id) {
        Build(fragment, fragment.getResources().getString(id));
    }

    private static void Build(Fragment fragment, String string) {
        Build(fragment.getActivity(), string);
    }

    public static void Build(Activity activity, int id) {
        Build(activity.getApplicationContext(), activity.getResources().getString(id));
    }

    private static void Build(Activity activity, String string) {
        Build(activity.getApplicationContext(), string);
    }

    public static void Build(Context context, String mensaje) {
        Toast toast = Toast.makeText(context, mensaje, Toast.LENGTH_LONG);
        TextView textView = toast.getView().findViewById(android.R.id.message);
        if (textView != null) {
            textView.setGravity(Gravity.CENTER);
            textView.setTextSize(15f);
        }
        toast.setGravity(Gravity.CENTER_VERTICAL,0 ,0);
        toast.show();
    }
}
