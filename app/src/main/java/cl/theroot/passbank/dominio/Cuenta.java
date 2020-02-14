package cl.theroot.passbank.dominio;

public class Cuenta implements Comparable<Cuenta> {
    private static final String TAG = "BdC-Cuenta";
    private String nombre;
    private String descripcion;
    private Integer validez;
    private Integer vencInf;

    public Cuenta(String nombre, String descripcion, Integer validez) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.validez = validez;
        this.vencInf = null;
    }

    public Cuenta(String nombre, String descripcion, Integer validez, Integer vencInf) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.validez = validez;
        this.vencInf = vencInf;
    }

    @Override
    public int compareTo(Cuenta other) {
        return nombre.compareTo(other.getNombre());
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public Integer getValidez() {
        return validez;
    }

    public Integer getVencInf() {
        return vencInf;
    }

    public void setVencInf(Integer vencInf) {
        this.vencInf = vencInf;
    }
}
