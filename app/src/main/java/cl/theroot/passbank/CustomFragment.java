package cl.theroot.passbank;

import android.content.Context;

import androidx.fragment.app.Fragment;

public class CustomFragment extends Fragment {

    public ActividadPrincipal actividadPrincipal() {
        return (ActividadPrincipal) getActivity();
    }

    public Context getApplicationContext(){
        return actividadPrincipal().getApplicationContext();
    }
}
