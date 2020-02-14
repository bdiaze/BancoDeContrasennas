package cl.theroot.passbank.dominio;

public class CategoriaCuenta implements Comparable<CategoriaCuenta> {
    private String nombreCategoria;
    private String nombreCuenta;
    private Integer posicion;

    public CategoriaCuenta(String nombreCategoria, String nombreCuenta, Integer posicion) {
        this.nombreCategoria = nombreCategoria;
        this.nombreCuenta = nombreCuenta;
        this.posicion = posicion;
    }

    @Override
    public int compareTo(CategoriaCuenta other) {
        if (nombreCategoria != null && other.getNombreCategoria() != null && !nombreCategoria.equals(other.getNombreCategoria())) {
            return nombreCategoria.compareTo(other.getNombreCategoria());
        }

        int buff = 0;
        if (posicion != null && other.getPosicion() != null) {
            buff = String.valueOf(posicion).compareTo(String.valueOf(other.getPosicion()));
        }
        if (buff == 0) {
            buff = nombreCuenta.compareTo(other.getNombreCuenta());
        }
        return buff;
    }

    public String getNombreCategoria() {
        return nombreCategoria;
    }

    public String getNombreCuenta() {
        return nombreCuenta;
    }

    public void setNombreCuenta(String nombreCuenta) {
        this.nombreCuenta = nombreCuenta;
    }

    public Integer getPosicion() {
        return posicion;
    }

    public void setPosicion(Integer posicion) {
        this.posicion = posicion;
    }
}
