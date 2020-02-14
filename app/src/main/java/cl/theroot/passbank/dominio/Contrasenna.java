package cl.theroot.passbank.dominio;


public class Contrasenna implements  Comparable<Contrasenna>{
    private Long id;
    private String nombreCuenta;
    private String valor;
    private String fecha;

    public Contrasenna(Long id, String nombreCuenta, String valor, String fecha) {
        this.id = id;
        this.nombreCuenta = nombreCuenta;
        this.valor = valor;
        this.fecha = fecha;
    }

    @Override
    public int compareTo(Contrasenna other) {
        return String.valueOf(id).compareTo(String.valueOf(other.getId()));
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombreCuenta() {
        return nombreCuenta;
    }

    public void setNombreCuenta(String nombreCuenta) {
        this.nombreCuenta = nombreCuenta;
    }

    public String getValor() {
        return valor;
    }

    public void setValor(String valor) {
        this.valor = valor;
    }

    public String getFecha() {
        return fecha;
    }
}
