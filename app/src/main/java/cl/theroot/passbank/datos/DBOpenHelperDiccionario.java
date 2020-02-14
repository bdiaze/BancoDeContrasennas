package cl.theroot.passbank.datos;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import cl.theroot.passbank.datos.nombres.NombreBD;

/**
 * Created by Benjamin on 12/10/2017.
 */

public class DBOpenHelperDiccionario extends SQLiteOpenHelper{
    private static final String TAG = "BdC-DBOpenHelperDicc";

    private static final int VERSION_BASE_DATOS = 1;
    private static final String NOMBRE_BASE_DATOS = "diccionario";

    static final String NOMBRE_TABLA = "PALABRA";
    static final String COLUMNA_ID = "ID";
    static final String COLUMNA_VALOR = "VALOR";

    private static DBOpenHelperDiccionario dbOpenHelperDiccionario = null;

    public static DBOpenHelperDiccionario getInstance(Context context) {
        crearBaseDatos(context);
        if (dbOpenHelperDiccionario == null) {
            dbOpenHelperDiccionario = new DBOpenHelperDiccionario(context);
        }
        return dbOpenHelperDiccionario;
    }

    public void cerrarConexiones() {
        if (dbOpenHelperDiccionario != null) {
            dbOpenHelperDiccionario.close();
        }
    }

    private DBOpenHelperDiccionario(Context context) {
        super(context, NOMBRE_BASE_DATOS, null, VERSION_BASE_DATOS);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(String.format("CREATE TABLE %s(%s INTEGER PRIMARY KEY AUTOINCREMENT, %s TEXT NOT NULL UNIQUE);",
                NOMBRE_TABLA,
                COLUMNA_ID,
                COLUMNA_VALOR));
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + NOMBRE_TABLA);
        onCreate(db);
    }

    private static void crearBaseDatos(Context context) {
        //Log.i(TAG, "Revisando si existe la base de datos del diccionario...");
        if (!context.getDatabasePath(NOMBRE_BASE_DATOS).exists()) {
            //Log.i(TAG, "Creando la base de datos del diccionario");
            String pathDestino = context.getDatabasePath(NombreBD.BANCO_CONTRASENNAS.toString()).getParentFile().getAbsolutePath();
            //Log.i(TAG, "Path destino: " + pathDestino);
            if (pathDestino != null) {
                try (InputStream in = context.getAssets().open(NOMBRE_BASE_DATOS); OutputStream out = new FileOutputStream(new File(pathDestino, NOMBRE_BASE_DATOS))) {
                    byte[] buffer = new byte[1024];
                    int read;
                    while ((read = in.read(buffer)) != -1) {
                        out.write(buffer, 0, read);
                    }
                } catch (IOException ex) {
                    Log.e(TAG, "Error al manipular el archivo: " + NOMBRE_BASE_DATOS, ex);
                }
                //Nada
                //Nada
            }
            //Log.i(TAG, "Finalizada la creación...");
        }
    }
}
