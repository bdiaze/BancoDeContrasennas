package cl.theroot.passbank.datos.nombres;

/**
 * Created by Benjamin on 07/10/2017.
 */

public enum ColParametro {
    NOMBRE("NOMBRE"),
    VALOR("VALOR"),
    POSICION("POSICION"),
    DESCRIPCION("DESCRIPCION"),
    TIPO("TIPO"),
    MINIMO("MINIMO"),
    MAXIMO("MAXIMO");

    private final String nombre;

    ColParametro(String nombre) {
        this.nombre = nombre;
    }

    @Override
    public String toString() {
        return nombre;
    }
}
