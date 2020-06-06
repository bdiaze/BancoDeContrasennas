package cl.theroot.passbank.fragmento;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

import butterknife.BindView;
import butterknife.ButterKnife;
import cl.theroot.passbank.R;

public class AlertDialogSiNoOk extends AppCompatDialogFragment {
    private static final String TAG = "BdC-AlertSiNo";

    public static final int TIPO_OK = 0;
    public static final int TIPO_SI_NO = 1;

    public static final int BOTON_OK = 0;
    public static final int BOTON_SI = 1;
    public static final int BOTON_NO = 2;

    private iProcesarBotonSiNoOk listener;

    @BindView(R.id.TV_tituloAlertDialog)
    TextView TV_tituloAlertDialog;
    @BindView(R.id.TV_mensajeAlertDialog)
    TextView TV_mensajeAlertDialog;
    @BindView(R.id.TR_contSiNoAlertDialog)
    TableRow TR_contSiNoAlertDialog;
    @BindView(R.id.TR_contOkAlertDialog)
    TableRow TR_contOkAlertDialog;
    @BindView(R.id.B_siAlertDialog)
    Button B_siAlertDialog;
    @BindView(R.id.B_noAlertDialog)
    Button B_noAlertDialog;
    @BindView(R.id.B_okAlertDialog)
    Button B_okAlertDialog;

    private static final String KEY_INT_TIP = "KEY_INT_TIP";
    private static final String KEY_STR_TIT = "KEY_STR_TIT";
    private static final String KEY_STR_MNJ = "KEY_STR_MNJ";
    private static final String KEY_BLN_QTR = "KEY_BLN_QTR";

    private int tipo = TIPO_OK;
    private String titulo;
    private String mensaje;
    private boolean quitarDialog = true;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            tipo = savedInstanceState.getInt(KEY_INT_TIP);
            titulo = savedInstanceState.getString(KEY_STR_TIT);
            mensaje = savedInstanceState.getString(KEY_STR_MNJ);
            quitarDialog = savedInstanceState.getBoolean(KEY_BLN_QTR);
        }

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.alert_dialog_si_no_ok, null);
        ButterKnife.bind(this, view);

        TV_tituloAlertDialog.setText(titulo);
        TV_mensajeAlertDialog.setText(mensaje);
        switch (tipo) {
            case TIPO_SI_NO:
                TR_contSiNoAlertDialog.setVisibility(View.VISIBLE);
                TR_contOkAlertDialog.setVisibility(View.GONE);
                break;
            case TIPO_OK:
                TR_contSiNoAlertDialog.setVisibility(View.GONE);
                TR_contOkAlertDialog.setVisibility(View.VISIBLE);
                break;
        }
        B_siAlertDialog.setOnClickListener(v -> {
            Log.i(TAG, "B_siAlertDialog.setOnClickListener(...)");
            if (quitarDialog) {
                this.dismiss();
            }
            listener.procesarBotonSiNoOk(BOTON_SI);
        });
        B_noAlertDialog.setOnClickListener(v-> {
            Log.i(TAG, "B_noAlertDialog.setOnClickListener(...)");
            if (quitarDialog) {
                this.dismiss();
            }
            listener.procesarBotonSiNoOk(BOTON_NO);
        });
        B_okAlertDialog.setOnClickListener(v -> {
            Log.i(TAG, "B_okAlertDialog.setOnClickListener(...)");
            if (quitarDialog) {
                this.dismiss();
            }
            listener.procesarBotonSiNoOk(BOTON_OK);
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
                listener = (iProcesarBotonSiNoOk) getTargetFragment();
            } else {
                listener = (iProcesarBotonSiNoOk) getActivity();
            }
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement iProcesarBotonSiNoOk.");
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putInt(KEY_INT_TIP, tipo);
        outState.putString(KEY_STR_TIT, titulo);
        outState.putString(KEY_STR_MNJ, mensaje);
        outState.putBoolean(KEY_BLN_QTR, quitarDialog);
        super.onSaveInstanceState(outState);
    }

    public void setTipo(int tipo) {
        this.tipo = tipo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public void setQuitarDialog(boolean quitarDialog) {
        this.quitarDialog = quitarDialog;
    }

    public interface iProcesarBotonSiNoOk {
        void procesarBotonSiNoOk(int boton);
    }
}
