package cl.theroot.passbank.datos.nombres;

/**
 * Created by Benjamin on 07/10/2017.
 */

public enum NombreParametro {
    SAL_HASH("HASH_SALT"),
    RESULTADO_HASH("HASH_RESULT"),
    SAL_ENCRIPTACION("ENCRIPTION_SALT"),
    CUENTA_GOOGLE("CUENTA_GOOGLE"),
    ULTIMO_MODO_GENERADOR("ULTIMO_MODO_GENERADOR"),
    PROXIMA_DESFRAGMENTACION("PROXIMA_DESFRAGMENTACION"),
    CANT_CARACTERES_GENERADOR("Generador: Cantidad de Caracteres"),
    CANT_PALABRAS_GENERADOR("Generador: Cantidad de Palabras"),
    COMPOSICION_GENERADOR("Generador: Posibles Caracteres"),
    SEPARADOR_GENERADOR("Generador: Separador de Palabras"),
    VALIDEZ_DEFECTO("Tiempo de Validez por Defecto"),
    NOMBRE_CATEGORIA_COMPLETA("Nombre de la Categoría General"),
    SEGUNDOS_PORTAPAPELES("Segundos en Portapapeles"),
    SEGUNDOS_CERRAR_SESION("Segundos para Cerrar Sesión");

    private final String nombre;

    NombreParametro(String nombre) {
        this.nombre = nombre;
    }

    @Override
    public String toString() {
        return nombre;
    }
}
