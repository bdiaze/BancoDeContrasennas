package cl.theroot.passbank.dominio;

public class Categoria implements Comparable<Categoria>{
    private String nombre;
    private Integer posicion;

    public Categoria(String nombre, Integer posicion) {
        this.nombre = nombre;
        this.posicion = posicion;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Categoria that = (Categoria) o;
        return nombre.equals(that.nombre);

    }

    @Override
    public int hashCode() {
        return nombre.hashCode();
    }

    @Override
    public int compareTo(Categoria other) {
        int buff = 0;
        if (posicion != null && other.getPosicion() != null) {
            buff = String.valueOf(posicion).compareTo(String.valueOf(other.getPosicion()));
        }

        if (buff == 0) {
            buff = nombre.compareTo(other.getNombre());
        }
        return buff;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Integer getPosicion() {
        return posicion;
    }

    public void setPosicion(Integer posicion) {
        this.posicion = posicion;
    }
}
