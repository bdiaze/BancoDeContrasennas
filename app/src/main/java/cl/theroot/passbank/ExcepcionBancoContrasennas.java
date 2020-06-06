package cl.theroot.passbank;

import cl.theroot.passbank.fragmento.AlertDialogSiNoOk;

public class ExcepcionBancoContrasennas extends Exception {
    private static final String TAG = "BdC-ExcepcionBancoContr";
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
        AlertDialogSiNoOk alertDialogSiNoOk = new AlertDialogSiNoOk();
        alertDialogSiNoOk.setTitulo(titulo);
        alertDialogSiNoOk.setMensaje(mensaje);
        alertDialogSiNoOk.setTargetFragment(fragment, 1);
        alertDialogSiNoOk.show(fragment.getFragmentManager(), TAG);
    }
}
