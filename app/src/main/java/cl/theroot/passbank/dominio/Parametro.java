package cl.theroot.passbank.dominio;

import androidx.annotation.NonNull;

import com.google.gson.Gson;

import cl.theroot.passbank.datos.nombres.NombreParametro;

public class Parametro implements Comparable<Parametro>{
    private String nombre;
    private String valor;
    private Integer posicion;
    private String descripcion = null;
    private Integer tipo = 0;
    private Integer minimo = null;
    private Integer maximo = null;

    public Parametro(NombreParametro nombreParametro, String valor, Integer posicion, String descripcion, Integer tipo, Integer minimo, Integer maximo) {
        this.nombre = nombreParametro.toString();
        this.valor = valor;
        this.posicion = posicion;
        this.descripcion = descripcion;
        this.tipo = tipo;
        this.minimo = minimo;
        this.maximo = maximo;
    }

    public Parametro(String nombre, String valor, Integer posicion, String descripcion, Integer tipo, Integer minimo, Integer maximo) {
        this.nombre = nombre;
        this.valor = valor;
        this.posicion = posicion;
        this.descripcion = descripcion;
        this.tipo = tipo;
        this.minimo = minimo;
        this.maximo = maximo;
    }

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

    @NonNull
    @Override
    public String toString() {
        Gson gson = new Gson();
        return gson.toJson(this, this.getClass());
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

    public String getDescripcion() {
        return descripcion;
    }

    /*
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
    */

    public Integer getTipo() {
        return tipo;
    }

    /*
    public void setTipo(Integer tipo) {
        this.tipo = tipo;
    }
    */

    public Integer getMinimo() {
        return minimo;
    }

    /*
    public void setMinimo(Integer minimo) {
        this.minimo = minimo;
    }
    */

    public Integer getMaximo() {
        return maximo;
    }

    /*
    public void setMaximo(Integer maximo) {
        this.maximo = maximo;
    }
    */
}
