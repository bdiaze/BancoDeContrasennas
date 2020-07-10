package cl.theroot.passbank.dominio;

/**
 * Created by Benjamin on 11/10/2017.
 */

public class ParametroSeleccionable extends Parametro{
    private boolean seleccionado;

    /*
    public ParametroSeleccionable(String nombre, String valor, Integer posicion, boolean seleccionado) {
        super(nombre, valor, posicion);
        this.seleccionado = seleccionado;
    }
    */

    public ParametroSeleccionable(String nombre, String valor, Integer posicion, String descripcion, Integer tipo, Integer minimo, Integer maximo, boolean seleccionado) {
        super(nombre, valor, posicion, descripcion, tipo, minimo, maximo);
        this.seleccionado = seleccionado;
    }

    public boolean isSeleccionado() {
        return seleccionado;
    }

    public void setSeleccionado(boolean seleccionado) {
        this.seleccionado = seleccionado;
    }
}
