package cl.theroot.passbank.datos.nombres;

/**
 * Created by Benjamin on 07/10/2017.
 */

public enum ColCategoriaCuenta {
    NOMBRE_CATEGORIA("NOMBRE_CATEGORIA"),
    NOMBRE_CUENTA("NOMBRE_CUENTA"),
    POSICION("POSICION");

    private final String nombre;

    ColCategoriaCuenta(String nombre) {
        this.nombre = nombre;
    }

    @Override
    public String toString() {
        return nombre;
    }
}
