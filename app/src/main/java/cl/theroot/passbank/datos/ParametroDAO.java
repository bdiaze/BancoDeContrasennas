package cl.theroot.passbank.datos;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import cl.theroot.passbank.datos.nombres.ColParametro;
import cl.theroot.passbank.datos.nombres.NombreBD;
import cl.theroot.passbank.datos.nombres.NombreParametro;
import cl.theroot.passbank.datos.nombres.Tabla;
import cl.theroot.passbank.dominio.Parametro;

public class ParametroDAO extends DAO{
    private static final String TAG = "BdC-ParametroDAO";

    public ParametroDAO(@NonNull Context context) {
        dbOpenHelper = DBOpenHelper.getInstance(context, NombreBD.BANCO_CONTRASENNAS);
        //dbOpenHelper = new DBOpenHelper(context, NombreBD.BANCO_CONTRASENNAS);
    }

    public ParametroDAO(@NonNull Context context, boolean usarRespaldo) {
        if (usarRespaldo) {
            dbOpenHelper = DBOpenHelper.getInstance(context, NombreBD.BANCO_CONTRASENNAS_RESPALDO);
            //dbOpenHelper = new DBOpenHelper(context, NombreBD.BANCO_CONTRASENNAS_RESPALDO);
        } else {
            dbOpenHelper = DBOpenHelper.getInstance(context, NombreBD.BANCO_CONTRASENNAS);
            //dbOpenHelper = new DBOpenHelper(context, NombreBD.BANCO_CONTRASENNAS);
        }
    }

    public ParametroDAO(@NonNull DBOpenHelper dbOpenHelper) {
        this.dbOpenHelper = dbOpenHelper;
    }

    private List<Parametro> seleccionarTodos() {
        SQLiteDatabase db = dbOpenHelper.getReadableDatabase();
        String select = "SELECT * FROM " + Tabla.PARAMETRO;
        Cursor cursor = db.rawQuery(select, null);
        List<Parametro> resultado = new ArrayList<>();
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            do {
                String nombre = cursor.getString(cursor.getColumnIndex(ColParametro.NOMBRE.toString()));
                String valor = cursor.getString(cursor.getColumnIndex(ColParametro.VALOR.toString()));
                Integer posicion = cursor.getInt(cursor.getColumnIndex(ColParametro.POSICION.toString()));
                resultado.add(new Parametro(nombre, valor, posicion));
            } while(cursor.moveToNext());
        }
        cursor.close();
        //db.close();
        return resultado;
    }

    public List<Parametro> seleccionarVisibles() {
        SQLiteDatabase db = dbOpenHelper.getReadableDatabase();
        String select = "SELECT * FROM " + Tabla.PARAMETRO + " WHERE " + ColParametro.POSICION + " IS NOT NULL ORDER BY " + ColParametro.POSICION;
        Cursor cursor = db.rawQuery(select, null);
        List<Parametro> resultado = new ArrayList<>();
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            do {
                String nombre = cursor.getString(cursor.getColumnIndex(ColParametro.NOMBRE.toString()));
                String valor = cursor.getString(cursor.getColumnIndex(ColParametro.VALOR.toString()));
                Integer posicion = cursor.getInt(cursor.getColumnIndex(ColParametro.POSICION.toString()));
                resultado.add(new Parametro(nombre, valor, posicion));
            } while(cursor.moveToNext());
        }
        cursor.close();
        //db.close();
        return resultado;
    }

    public Parametro seleccionarUno(NombreParametro nombreParametro) {
        return seleccionarUno(nombreParametro.toString());
    }

    public Parametro seleccionarUno(@NonNull String nombre) {
        SQLiteDatabase db = dbOpenHelper.getReadableDatabase();
        String select = "SELECT * FROM " + Tabla.PARAMETRO + " WHERE " + ColParametro.NOMBRE + " = ?";
        String[] whereArgs = {nombre};
        Cursor cursor = db.rawQuery(select, whereArgs);
        Parametro resultado = null;
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            String valor = cursor.getString(cursor.getColumnIndex(ColParametro.VALOR.toString()));
            Integer posicion = cursor.getInt(cursor.getColumnIndex(ColParametro.POSICION.toString()));
            resultado = new Parametro(nombre, valor, posicion);
        }
        cursor.close();
        //db.close();
        return resultado;
    }

    public int actualizarUna(@NonNull Parametro parametro) {
        SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ColParametro.VALOR.toString(), parametro.getValor());
        String where = ColParametro.NOMBRE + " = ?";
        String[] whereArgs= {parametro.getNombre()};
        return db.update(Tabla.PARAMETRO.toString(), values, where, whereArgs);
    }

    public void imprimir() {
        SQLiteDatabase db = dbOpenHelper.getReadableDatabase();
        String select = "SELECT * FROM " + Tabla.PARAMETRO;
        Cursor cursor = db.rawQuery(select, null);
        Log.i(TAG, "Imprimiendo los valores de la tabla " + Tabla.PARAMETRO + "...");
        Log.i(TAG, ColParametro.NOMBRE + " - " + ColParametro.VALOR + " - " + ColParametro.POSICION);
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            do {
                String nombre = cursor.getString(cursor.getColumnIndex(ColParametro.NOMBRE.toString()));
                String valor = cursor.getString(cursor.getColumnIndex(ColParametro.VALOR.toString()));
                int posicion = cursor.getInt(cursor.getColumnIndex(ColParametro.POSICION.toString()));
                Log.i(TAG, nombre + " - " + valor + " - " + posicion);
            } while(cursor.moveToNext());
        }
        cursor.close();
        //db.close();
    }

    public static void cargarRespaldo(Context context, NombreBD origen, NombreBD destino) {
        DBOpenHelper dbOHOrigen = DBOpenHelper.getInstance(context, origen);
        DBOpenHelper dbOHDestino = DBOpenHelper.getInstance(context, destino);

        ParametroDAO daoOrigen = new ParametroDAO(dbOHOrigen);
        ParametroDAO daoDestino = new ParametroDAO(dbOHDestino);
        for (Parametro parametro : daoOrigen.seleccionarTodos()) {
            daoDestino.actualizarUna(parametro);
        }
    }
}
