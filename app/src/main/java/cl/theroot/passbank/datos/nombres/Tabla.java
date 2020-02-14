package cl.theroot.passbank.datos.nombres;

/**
 * Created by Benjamin on 07/10/2017.
 */

public enum Tabla {
    CATEGORIA("CATEGORIA"),
    CUENTA("CUENTA"),
    CATEGORA_CUENTA("CATEGORIA_CUENTA"),
    CONTRASENNA("CONTRASENNA"),
    PARAMETRO("PARAMETRO");

    private final String nombre;

    Tabla(String nombre) {
        this.nombre = nombre;
    }

    @Override
    public String toString() {
        return nombre;
    }
}
