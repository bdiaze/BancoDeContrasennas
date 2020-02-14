package cl.theroot.passbank.dominio;

import cl.theroot.passbank.datos.nombres.NombreParametro;

public class Parametro implements Comparable<Parametro>{
    private String nombre;
    private String valor;
    private Integer posicion;

    public Parametro(NombreParametro nombreParametro, String valor, Integer posicion) {
        this.nombre = nombreParametro.toString();
        this.valor = valor;
        this.posicion = posicion;
    }

    public Parametro(String nombre, String valor, Integer posicion) {
        this.nombre = nombre;
        this.valor = valor;
        this.posicion = posicion;
    }

    @Override
    public int compareTo(Parametro other) {
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

    public String getValor() {
        return valor;
    }

    public void setValor(String valor) {
        this.valor = valor;
    }

    public Integer getPosicion() {
        return posicion;
    }

    public void setPosicion(Integer posicion) {
        this.posicion = posicion;
    }
}
