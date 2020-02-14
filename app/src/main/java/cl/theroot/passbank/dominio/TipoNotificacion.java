package cl.theroot.passbank.dominio;

public enum TipoNotificacion {
    GENERAL("BdC-general", "Información General"),
    IMPORTANTE("BdC-importante", "Importante");


    private final String id;
    private final String nombre;

    TipoNotificacion(String id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }

    public String getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }
}
