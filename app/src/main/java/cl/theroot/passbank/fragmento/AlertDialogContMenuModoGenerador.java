package cl.theroot.passbank.fragmento;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RadioButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDialogFragment;

import butterknife.BindView;
import butterknife.ButterKnife;
import cl.theroot.passbank.R;

public class AlertDialogContMenuModoGenerador extends AppCompatDialogFragment {
    private static final String TAG = "BdC-AlertDialogContMenuModo";

    public static final int RADIO_CARACTERES = 0;
    public static final int RADIO_PALABRAS = 1;

    private iProcesarSeleccion procesarSeleccion;

    @BindView(R.id.RB_caracteres)
    RadioButton RB_caracteres;
    @BindView(R.id.RB_palabras)
    RadioButton RB_palabras;

    private static final String KEY_INT_SEL = "KEY_INT_SEL";

    private Integer seleccion;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            seleccion = savedInstanceState.getInt(KEY_INT_SEL);
        }

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.alert_dialog_cont_menu_modo_generador, null);
        ButterKnife.bind(this, view);

        Log.i(TAG, String.format("onCreateDialog(...) - Seteando selección de radio buttons - seleccion: %d.", seleccion));
        if (seleccion == RADIO_CARACTERES) {
            RB_caracteres.setChecked(true);
        } else {
            RB_palabras.setChecked(true);
        }

        RB_caracteres.setOnClickListener(v -> {
            this.dismiss();
            procesarSeleccion.procesarSeleccion(RADIO_CARACTERES);
        });
        RB_palabras.setOnClickListener(v -> {
            this.dismiss();
            procesarSeleccion.procesarSeleccion(RADIO_PALABRAS);
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view);
        return builder.create();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        if (seleccion != null) outState.putInt(KEY_INT_SEL, seleccion);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            if (getTargetFragment() != null) {
                procesarSeleccion = (iProcesarSeleccion) getTargetFragment();
            } else {
                procesarSeleccion = (iProcesarSeleccion) getActivity();
            }
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement iProcesarSeleccion.");
        }
    }

    public void setSeleccion(int seleccion) {
        this.seleccion = seleccion;
    }

    public interface iProcesarSeleccion {
        void procesarSeleccion(int radioButton);
    }
}
