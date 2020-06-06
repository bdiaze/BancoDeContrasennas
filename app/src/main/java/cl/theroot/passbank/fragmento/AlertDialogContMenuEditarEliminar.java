package cl.theroot.passbank.fragmento;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

import butterknife.BindView;
import butterknife.ButterKnife;
import cl.theroot.passbank.R;

public class AlertDialogContMenuEditarEliminar extends AppCompatDialogFragment {
    private static final String TAG = "BdC-AlertContMenu";

    public static final int BOTON_EDITAR = 0;
    public static final int BOTON_ELIMINAR = 1;

    private iProcesarBoton listener;

    @BindView(R.id.TV_contEditar)
    TextView TV_contEditar;
    @BindView(R.id.TV_contEliminar)
    TextView TV_contEliminar;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.alert_dialog_cont_menu_editar_eliminar, null);
        ButterKnife.bind(this, view);

        TV_contEditar.setOnClickListener(v -> {
            this.dismiss();
            listener.procesarBoton(BOTON_EDITAR);
        });
        TV_contEliminar.setOnClickListener(v -> {
            this.dismiss();
            listener.procesarBoton(BOTON_ELIMINAR);
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view);
        return builder.create();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            if (getTargetFragment() != null) {
                listener = (iProcesarBoton) getTargetFragment();
            } else {
                listener = (iProcesarBoton) getActivity();
            }
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement iProcesarBoton.");
        }
    }

    public interface iProcesarBoton {
        void procesarBoton(int boton);
    }
}
