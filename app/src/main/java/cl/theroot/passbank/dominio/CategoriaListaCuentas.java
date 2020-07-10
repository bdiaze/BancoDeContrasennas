package cl.theroot.passbank.dominio;

import java.util.List;

/**
 * Created by Benjamin on 08/10/2017.
 */

public class CategoriaListaCuentas extends Categoria {
    private List<CuentaConFecha> cuentas;

    public CategoriaListaCuentas(String nombre, Integer posicion, List<CuentaConFecha> cuentas) {
        super(nombre, posicion);
        this.cuentas = cuentas;
    }

    public List<CuentaConFecha> getCuentas() {
        return cuentas;
    }

    /*
    public void setCuentas(List<CuentaConFecha> cuentas) {
        this.cuentas = cuentas;
    }
    */
}
