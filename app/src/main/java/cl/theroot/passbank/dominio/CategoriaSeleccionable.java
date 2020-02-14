package cl.theroot.passbank.dominio;

/**
 * Created by Benjamin on 07/10/2017.
 */

public class CategoriaSeleccionable extends Categoria{
    private boolean seleccionado;

    public CategoriaSeleccionable(String nombre, Integer posicion, boolean seleccionado) {
        super(nombre, posicion);
        this.seleccionado = seleccionado;
    }

    public boolean isSeleccionado() {
        return seleccionado;
    }

    public void setSeleccionado(boolean seleccionado) {
        this.seleccionado = seleccionado;
    }
}
