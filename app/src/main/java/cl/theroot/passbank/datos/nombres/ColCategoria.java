package cl.theroot.passbank.datos.nombres;

/**
 * Created by Benjamin on 07/10/2017.
 */

public enum ColCategoria {
    NOMBRE("NOMBRE"),
    POSICION("POSICION");

    private final String nombre;

    ColCategoria(String nombre) {
        this.nombre = nombre;
    }

    @Override
    public String toString() {
        return nombre;
    }
}
