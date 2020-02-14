package cl.theroot.passbank.dominio;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Benjamin on 11/10/2017.
 */

public class CuentaConFecha extends Cuenta {
    private static final String TAG = "BdC-CuentaConFecha";
    private String fechaContrasenna;

    public CuentaConFecha(String nombre, String descripcion, Integer validez, String fechaContrasenna) {
        super(nombre, descripcion, validez);
        this.fechaContrasenna = fechaContrasenna;
    }

    public boolean expiro() {
        if (getValidez() == 0) {
            return false;
        }

        Integer diasDiferencia = obtenerVejez();
        if (diasDiferencia != null && diasDiferencia > getValidez()) {
            return true;
        }
        return false;
    }

    public Integer obtenerVejez() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd");
        String strFechaActual = simpleDateFormat.format(calendar.getTime());
        try {
            Date dtFechaActual = simpleDateFormat.parse(strFechaActual);
            Date dtFechaContrasenna = simpleDateFormat.parse(fechaContrasenna);
            return (int) ((dtFechaActual.getTime() - dtFechaContrasenna.getTime()) / (1000 * 60 * 60 * 24));
        } catch(ParseException ex) {
            Log.e(TAG, "Error al tratar de parsear una fecha", ex);
        }
        return null;
    }

    public Integer tiempoVencido() {
        if (!expiro()) {
            return null;
        }
        Integer vejez = obtenerVejez();
        return vejez != null ? vejez - getValidez() : null;
    }
}
