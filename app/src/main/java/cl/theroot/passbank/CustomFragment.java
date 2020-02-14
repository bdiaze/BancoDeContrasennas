package cl.theroot.passbank;

import android.app.Fragment;
import android.content.Context;

public class CustomFragment extends Fragment {

    public ActividadPrincipal actividadPrincipal() {
        return (ActividadPrincipal) getActivity();
    }

    public Context getApplicationContext(){
        return actividadPrincipal().getApplicationContext();
    }
}
