package cl.theroot.passbank.datos;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import cl.theroot.passbank.Desfragmentador;
import cl.theroot.passbank.datos.nombres.ColCategoria;
import cl.theroot.passbank.datos.nombres.ColCategoriaCuenta;
import cl.theroot.passbank.datos.nombres.ColContrasenna;
import cl.theroot.passbank.datos.nombres.ColCuenta;
import cl.theroot.passbank.datos.nombres.ColParametro;
import cl.theroot.passbank.datos.nombres.NombreBD;
import cl.theroot.passbank.datos.nombres.NombreParametro;
import cl.theroot.passbank.datos.nombres.Tabla;

public class DBOpenHelper extends SQLiteOpenHelper {
    private static final String TAG = "BdC-DBOpenHelper";

    private static final int VERSION_BASE_DATOS = 6;
    private static DBOpenHelper DBOPOriginal = null;
    private static DBOpenHelper DBOPRespaldo = null;

    public static DBOpenHelper getInstance(Context context, NombreBD nombreBD){
        if (nombreBD == NombreBD.BANCO_CONTRASENNAS_RESPALDO) {
            if (DBOPRespaldo == null) {
                DBOPRespaldo = new DBOpenHelper(context, nombreBD);
            }
            return DBOPRespaldo;
        }
        if (DBOPOriginal == null) {
            DBOPOriginal = new DBOpenHelper(context, nombreBD);
        }
        return DBOPOriginal;
    }

    public void cerrarConexiones() {
        if (DBOPOriginal != null) {
            DBOPOriginal.close();
        }

        if (DBOPRespaldo != null) {
            DBOPRespaldo.close();
        }
    }

    public static void deleteBackup(Context context) {
        if (context.deleteDatabase(NombreBD.BANCO_CONTRASENNAS_RESPALDO.toString())) {
            DBOPRespaldo = null;
        }
    }

    public static void deleteOriginal(Context context) {
        if (context.deleteDatabase(NombreBD.BANCO_CONTRASENNAS.toString())) {
            DBOPOriginal = null;
        }
    }

    private DBOpenHelper(Context context, NombreBD nombreBD) {
        super(context, nombreBD.toString(), null, VERSION_BASE_DATOS);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.i(TAG, "onCreate(...) - Se inicia la creación de tablas.");
        db.execSQL("CREATE TABLE " + Tabla.CATEGORIA + "(" +
                ColCategoria.NOMBRE + " TEXT NOT NULL, " +
                ColCategoria.POSICION + " INTEGER NOT NULL, " +
                "PRIMARY KEY(" + ColCategoria.NOMBRE + "));");

        db.execSQL("CREATE TABLE " + Tabla.CUENTA + "(" +
                ColCuenta.NOMBRE + " TEXT NOT NULL, " +
                ColCuenta.DESCRIPCION + " TEXT, " +
                ColCuenta.VALIDEZ + " INTEGER NOT NULL, " +
                "PRIMARY KEY(" + ColCuenta.NOMBRE + "));");

        db.execSQL("CREATE TABLE " + Tabla.CATEGORA_CUENTA + "(" +
                ColCategoriaCuenta.NOMBRE_CATEGORIA + " TEXT NOT NULL, " +
                ColCategoriaCuenta.NOMBRE_CUENTA + " TEXT NOT NULL, " +
                ColCategoriaCuenta.POSICION + " INTEGER NOT NULL, " +
                "PRIMARY KEY(" + ColCategoriaCuenta.NOMBRE_CATEGORIA + ", " + ColCategoriaCuenta.NOMBRE_CUENTA + "), " +
                "FOREIGN KEY(" + ColCategoriaCuenta.NOMBRE_CATEGORIA + ") REFERENCES " + Tabla.CATEGORIA + "(" + ColCategoria.NOMBRE + ") ON DELETE CASCADE ON UPDATE CASCADE, " +
                "FOREIGN KEY(" + ColCategoriaCuenta.NOMBRE_CUENTA + ") REFERENCES " + Tabla.CUENTA + "(" + ColCuenta.NOMBRE + ") ON DELETE CASCADE ON UPDATE CASCADE);");

        db.execSQL("CREATE TABLE " + Tabla.CONTRASENNA + "(" +
                ColContrasenna.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                ColContrasenna.NOMBRE_CUENTA + " TEXT NOT NULL, " +
                ColContrasenna.VALOR + " TEXT NOT NULL, " +
                ColContrasenna.FECHA + " TEXT NOT NULL, " +
                "FOREIGN KEY(" + ColContrasenna.NOMBRE_CUENTA + ") REFERENCES " + Tabla.CUENTA + "(" + ColCuenta.NOMBRE + ") ON DELETE CASCADE ON UPDATE CASCADE);");

        db.execSQL("CREATE TABLE " + Tabla.PARAMETRO + "(" +
                ColParametro.NOMBRE + " TEXT NOT NULL, " +
                ColParametro.VALOR + " TEXT NOT NULL, " +
                ColParametro.POSICION + " INTEGER, " +
                "PRIMARY KEY(" + ColParametro.NOMBRE + "));");

        insertarDatos(db);
        onUpgrade(db, 1, VERSION_BASE_DATOS);
    }

    private void insertarDatos(SQLiteDatabase db) {
        db.execSQL("INSERT INTO " + Tabla.PARAMETRO + " VALUES('" + NombreParametro.SAL_HASH + "', '', NULL);");
        db.execSQL("INSERT INTO " + Tabla.PARAMETRO + " VALUES('" + NombreParametro.RESULTADO_HASH + "', '', NULL);");
        db.execSQL("INSERT INTO " + Tabla.PARAMETRO + " VALUES('" + NombreParametro.SAL_ENCRIPTACION + "', '', NULL);");
        db.execSQL("INSERT INTO " + Tabla.PARAMETRO + " VALUES('" + NombreParametro.CUENTA_GOOGLE + "', '', NULL);");
        db.execSQL("INSERT INTO " + Tabla.PARAMETRO + " VALUES('" + NombreParametro.ULTIMO_MODO_GENERADOR + "', '0', NULL);");
        db.execSQL("INSERT INTO " + Tabla.PARAMETRO + " VALUES('" + NombreParametro.PROXIMA_DESFRAGMENTACION + "', '" + Desfragmentador.ID_PRIMERA_DESFRAGMENTACION + "', NULL);");
        db.execSQL("INSERT INTO " + Tabla.PARAMETRO + " VALUES('" + NombreParametro.CANT_PALABRAS_GENERADOR + "', '3', 10);");
        db.execSQL("INSERT INTO " + Tabla.PARAMETRO + " VALUES('" + NombreParametro.SEPARADOR_GENERADOR + "', '-', 20);");
        db.execSQL("INSERT INTO " + Tabla.PARAMETRO + " VALUES('" + NombreParametro.CANT_CARACTERES_GENERADOR + "', '10', 30);");
        db.execSQL("INSERT INTO " + Tabla.PARAMETRO + " VALUES('" + NombreParametro.COMPOSICION_GENERADOR + "', 'abcdefghijkmnopqrstuvwxyzABCDEFGHJKLMNPQRSTUVWXYZ123456789', 40);");
        db.execSQL("INSERT INTO " + Tabla.PARAMETRO + " VALUES('" + NombreParametro.VALIDEZ_DEFECTO + "', '180', 50);");
        db.execSQL("INSERT INTO " + Tabla.PARAMETRO + " VALUES('" + NombreParametro.NOMBRE_CATEGORIA_COMPLETA + "', 'Todas', 60);");
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.i(TAG, String.format("onUpgrade(...) - Se actualiza la base de datos. Versión %d -> %d", oldVersion, newVersion));
        while (oldVersion < newVersion) {
            oldVersion = actualizarUnaVersion(db, oldVersion);
        }
    }

    private int actualizarUnaVersion(SQLiteDatabase db, int versionActual) {
        switch (versionActual) {
            case 1:
                // Se actualiza la bd a la versión 2...
                db.execSQL("INSERT INTO " + Tabla.PARAMETRO + " VALUES('" + NombreParametro.SEGUNDOS_PORTAPAPELES + "', '20', 70);");
                break;
            case 2:
                // Se actualiza la bd a la versión 3...
                db.execSQL("ALTER TABLE " + Tabla.CUENTA + " ADD " + ColCuenta.VENCIMIENTO_INFORMADO + " INTEGER DEFAULT 0 NOT NULL;");
                break;
            case 3:
                db.execSQL("INSERT INTO " + Tabla.PARAMETRO + " VALUES('" + NombreParametro.SEGUNDOS_CERRAR_SESION + "', '60', 80);");
                break;
            case 4:
                // Se crean los campos DESCRIPCION, TIPO, MINIMO y MAXIMO para la tabla PARAMETRO...
                db.execSQL(String.format("ALTER TABLE %s ADD %s TEXT;", Tabla.PARAMETRO, ColParametro.DESCRIPCION));
                db.execSQL(String.format("ALTER TABLE %s ADD %s TEXT DEFAULT 0;", Tabla.PARAMETRO, ColParametro.TIPO));
                db.execSQL(String.format("ALTER TABLE %s ADD %s TEXT;", Tabla.PARAMETRO, ColParametro.MINIMO));
                db.execSQL(String.format("ALTER TABLE %s ADD %s TEXT;", Tabla.PARAMETRO, ColParametro.MAXIMO));
                break;
            case 5:
                // Se actualizan los valores de la columna TIPO de la tabla PARAMETRO...
                // TIPO 0 -> Caracteres alfanuméricos. Acepta letras, números y caracteres especiales.
                db.execSQL(String.format("UPDATE %s SET %s = 0 WHERE %s = '%s';", Tabla.PARAMETRO, ColParametro.TIPO, ColParametro.NOMBRE, NombreParametro.SEPARADOR_GENERADOR));
                db.execSQL(String.format("UPDATE %s SET %s = 0 WHERE %s = '%s';", Tabla.PARAMETRO, ColParametro.TIPO, ColParametro.NOMBRE, NombreParametro.COMPOSICION_GENERADOR));
                db.execSQL(String.format("UPDATE %s SET %s = 0 WHERE %s = '%s';", Tabla.PARAMETRO, ColParametro.TIPO, ColParametro.NOMBRE, NombreParametro.NOMBRE_CATEGORIA_COMPLETA));
                // TIPO 1 -> Caracteres númericos enteros positivos. Acepta solo números.
                db.execSQL(String.format("UPDATE %s SET %s = 1 WHERE %s = '%s';", Tabla.PARAMETRO, ColParametro.TIPO, ColParametro.NOMBRE, NombreParametro.CANT_PALABRAS_GENERADOR));
                db.execSQL(String.format("UPDATE %s SET %s = 1 WHERE %s = '%s';", Tabla.PARAMETRO, ColParametro.TIPO, ColParametro.NOMBRE, NombreParametro.CANT_CARACTERES_GENERADOR));
                db.execSQL(String.format("UPDATE %s SET %s = 1 WHERE %s = '%s';", Tabla.PARAMETRO, ColParametro.TIPO, ColParametro.NOMBRE, NombreParametro.VALIDEZ_DEFECTO));
                db.execSQL(String.format("UPDATE %s SET %s = 1 WHERE %s = '%s';", Tabla.PARAMETRO, ColParametro.TIPO, ColParametro.NOMBRE, NombreParametro.SEGUNDOS_PORTAPAPELES));
                db.execSQL(String.format("UPDATE %s SET %s = 1 WHERE %s = '%s';", Tabla.PARAMETRO, ColParametro.TIPO, ColParametro.NOMBRE, NombreParametro.SEGUNDOS_CERRAR_SESION));

                // Se actualizan los valores de la columna MINIMO de la tabla PARAMETRO...
                db.execSQL(String.format("UPDATE %s SET %s = 0 WHERE %s = '%s';", Tabla.PARAMETRO, ColParametro.MINIMO, ColParametro.NOMBRE, NombreParametro.SEPARADOR_GENERADOR));
                db.execSQL(String.format("UPDATE %s SET %s = 1 WHERE %s = '%s';", Tabla.PARAMETRO, ColParametro.MINIMO, ColParametro.NOMBRE, NombreParametro.COMPOSICION_GENERADOR));
                db.execSQL(String.format("UPDATE %s SET %s = 1 WHERE %s = '%s';", Tabla.PARAMETRO, ColParametro.MINIMO, ColParametro.NOMBRE, NombreParametro.NOMBRE_CATEGORIA_COMPLETA));
                db.execSQL(String.format("UPDATE %s SET %s = 1 WHERE %s = '%s';", Tabla.PARAMETRO, ColParametro.MINIMO, ColParametro.NOMBRE, NombreParametro.CANT_PALABRAS_GENERADOR));
                db.execSQL(String.format("UPDATE %s SET %s = 1 WHERE %s = '%s';", Tabla.PARAMETRO, ColParametro.MINIMO, ColParametro.NOMBRE, NombreParametro.CANT_CARACTERES_GENERADOR));
                db.execSQL(String.format("UPDATE %s SET %s = 0 WHERE %s = '%s';", Tabla.PARAMETRO, ColParametro.MINIMO, ColParametro.NOMBRE, NombreParametro.VALIDEZ_DEFECTO));
                db.execSQL(String.format("UPDATE %s SET %s = 0 WHERE %s = '%s';", Tabla.PARAMETRO, ColParametro.MINIMO, ColParametro.NOMBRE, NombreParametro.SEGUNDOS_PORTAPAPELES));
                db.execSQL(String.format("UPDATE %s SET %s = 0 WHERE %s = '%s';", Tabla.PARAMETRO, ColParametro.MINIMO, ColParametro.NOMBRE, NombreParametro.SEGUNDOS_CERRAR_SESION));
        }
        return versionActual + 1;
    }
}
