package cl.theroot.passbank;

import android.content.Context;

import cl.theroot.passbank.datos.CategoriaCuentaDAO;
import cl.theroot.passbank.datos.CategoriaDAO;
import cl.theroot.passbank.datos.ContrasennaDAO;
import cl.theroot.passbank.datos.CuentaDAO;
import cl.theroot.passbank.datos.DBOpenHelper;
import cl.theroot.passbank.datos.ParametroDAO;
import cl.theroot.passbank.datos.nombres.NombreBD;
import cl.theroot.passbank.datos.nombres.NombreParametro;
import cl.theroot.passbank.dominio.Contrasenna;
import cl.theroot.passbank.dominio.Parametro;

public class Desfragmentador {
    private static final String TAG = "BdC-Desfragmentador";
    public static final Long ID_PRIMERA_DESFRAGMENTACION = 5L;
    private static final Long DELTA_IDS = 5L;

    public static void intentarDesfragmentación(Context context) {
        //Log.i(TAG, "Iniciando desfragmentación...");
        ParametroDAO parametroDAO = new ParametroDAO(context);
        Parametro parProxDesf = parametroDAO.seleccionarUno(NombreParametro.PROXIMA_DESFRAGMENTACION.toString());
        if (parProxDesf != null) {
            //Log.i(TAG, "Proxima desfragmentación ID: " + parProxDesf.getValor());
            ContrasennaDAO contrasennaDAO = new ContrasennaDAO(context);
            Contrasenna ultimaContrasenna = contrasennaDAO.seleccionarUltima();
            if (ultimaContrasenna != null) {
                //Log.i(TAG, "Último ID: " + ultimaContrasenna.getId());
                if (ultimaContrasenna.getId() >= Long.parseLong(parProxDesf.getValor())) {
                    //Log.i(TAG, "Corresponde hacer desfragmentación... Comenzando...");
                    //Se crea, en el dispositivo, el archivo que almacenará la base de datos con los nuevos IDs.
                    DBOpenHelper.getInstance(context, NombreBD.BANCO_CONTRASENNAS_RESPALDO);

                    //Log.i(TAG, "Copiando datos al archivo temporal...");
                    //Copiar el archivo original, al respaldo.
                    CategoriaDAO.cargarRespaldo(context, NombreBD.BANCO_CONTRASENNAS, NombreBD.BANCO_CONTRASENNAS_RESPALDO);
                    CuentaDAO.cargarRespaldo(context, NombreBD.BANCO_CONTRASENNAS, NombreBD.BANCO_CONTRASENNAS_RESPALDO);
                    ParametroDAO.cargarRespaldo(context, NombreBD.BANCO_CONTRASENNAS, NombreBD.BANCO_CONTRASENNAS_RESPALDO);
                    CategoriaCuentaDAO.cargarRespaldo(context, NombreBD.BANCO_CONTRASENNAS, NombreBD.BANCO_CONTRASENNAS_RESPALDO);
                    ContrasennaDAO.cargarRespaldo(context, NombreBD.BANCO_CONTRASENNAS, NombreBD.BANCO_CONTRASENNAS_RESPALDO);

                    //Log.i(TAG, "Recreando archivo original...");
                    // Se cierran conexiones abiertas...
                    DBOpenHelper.getInstance(context, NombreBD.BANCO_CONTRASENNAS).cerrarConexiones();

                    //Se eliminar el archivo original
                    DBOpenHelper.deleteOriginal(context);
                    //Se crea un archivo original nuevo
                    DBOpenHelper.getInstance(context, NombreBD.BANCO_CONTRASENNAS);

                    //Log.i(TAG, "Copiando datos al archivo original...");
                    //Copiar el archivo de respaldo, al original.
                    CategoriaDAO.cargarRespaldo(context, NombreBD.BANCO_CONTRASENNAS_RESPALDO, NombreBD.BANCO_CONTRASENNAS);
                    CuentaDAO.cargarRespaldo(context, NombreBD.BANCO_CONTRASENNAS_RESPALDO, NombreBD.BANCO_CONTRASENNAS);
                    ParametroDAO.cargarRespaldo(context, NombreBD.BANCO_CONTRASENNAS_RESPALDO, NombreBD.BANCO_CONTRASENNAS);
                    CategoriaCuentaDAO.cargarRespaldo(context, NombreBD.BANCO_CONTRASENNAS_RESPALDO, NombreBD.BANCO_CONTRASENNAS);
                    ContrasennaDAO.cargarRespaldo(context, NombreBD.BANCO_CONTRASENNAS_RESPALDO, NombreBD.BANCO_CONTRASENNAS);

                    //Log.i(TAG, "Eliminando archivo temporal...");
                    // Se cierran conexiones abiertas...
                    DBOpenHelper.getInstance(context, NombreBD.BANCO_CONTRASENNAS_RESPALDO).cerrarConexiones();

                    //Se elimina el archivo de respaldo
                    DBOpenHelper.deleteBackup(context);

                    parametroDAO = new ParametroDAO(context);
                    contrasennaDAO = new ContrasennaDAO(context);

                    ultimaContrasenna = contrasennaDAO.seleccionarUltima();
                    if (ultimaContrasenna != null) {
                        Long proxDesf = ultimaContrasenna.getId() + DELTA_IDS;
                        parProxDesf.setValor(String.valueOf(proxDesf));
                        //Log.i(TAG, "Preparando próxima desfragmentación para ID: " + proxDesf);
                        parametroDAO.actualizarUna(parProxDesf);
                    }
                }
            }
        }
    }
}