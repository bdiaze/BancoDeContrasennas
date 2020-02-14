package cl.theroot.passbank.datos.nombres;

/**
 * Created by Benjamin on 07/10/2017.
 */

public enum ColCuenta {
    NOMBRE("NOMBRE"),
    DESCRIPCION("DESCRIPCION"),
    VALIDEZ("VALIDEZ"),
    VENCIMIENTO_INFORMADO("VENC_INFOR");

    private final String nombre;

    ColCuenta(String nombre) {
        this.nombre = nombre;
    }

    @Override
    public String toString() {
        return nombre;
    }
}
