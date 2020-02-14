package cl.theroot.passbank.datos.nombres;

/**
 * Created by Benjamin on 07/10/2017.
 */

public enum ColContrasenna {
    ID("ID"),
    NOMBRE_CUENTA("NOMBRE_CUENTA"),
    VALOR("VALOR"),
    FECHA("FECHA");

    private final String nombre;

    ColContrasenna(String nombre) {
        this.nombre = nombre;
    }

    @Override
    public String toString() {
        return nombre;
    }
}
