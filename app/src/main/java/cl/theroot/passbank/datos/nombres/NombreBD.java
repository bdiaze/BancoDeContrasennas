package cl.theroot.passbank.datos.nombres;

/**
 * Created by Benjamin on 07/10/2017.
 */

public enum NombreBD {
    BANCO_CONTRASENNAS("banco_contrasennas"),
    BANCO_CONTRASENNAS_RESPALDO("banco_contrasennas_respaldo");

    private final String nombre;

    NombreBD(String nombre) {
        this.nombre = nombre;
    }

    @Override
    public String toString() {
        return nombre;
    }
}
