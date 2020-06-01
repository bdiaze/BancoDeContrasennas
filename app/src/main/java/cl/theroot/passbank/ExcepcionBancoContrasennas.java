package cl.theroot.passbank;

import androidx.appcompat.app.AlertDialog;

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
    }
}
