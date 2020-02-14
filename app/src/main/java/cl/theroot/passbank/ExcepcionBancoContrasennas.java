package cl.theroot.passbank;

import android.app.AlertDialog;
import android.view.View;

import androidx.core.content.res.ResourcesCompat;

/**
 * Created by Benjamin on 05/10/2017.
 */

public class ExcepcionBancoContrasennas extends Exception{
    private String titulo;
    private String mensaje;

    public ExcepcionBancoContrasennas(String titulo, String mensaje) {
        super(mensaje);
        this.titulo = titulo;
        this.mensaje = mensaje;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void alertDialog(CustomFragment fragment) {
        AlertDialog alertDialog = new AlertDialog.Builder(fragment.getContext()).create();
        alertDialog.setTitle(titulo);
        alertDialog.setMessage(mensaje);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK", (dialog, which) -> dialog.dismiss());
        alertDialog.show();
        int titleDividerId = fragment.getResources().getIdentifier("titleDivider", "id", "android");
        View titleDivider = alertDialog.findViewById(titleDividerId);
        if (titleDivider != null) {
            titleDivider.setBackgroundColor(ResourcesCompat.getColor(fragment.getResources(), R.color.letraAtenuada, null));
        }
    }
}
