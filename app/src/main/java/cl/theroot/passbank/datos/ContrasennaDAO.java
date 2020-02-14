package cl.theroot.passbank.datos;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import cl.theroot.passbank.datos.nombres.ColContrasenna;
import cl.theroot.passbank.datos.nombres.NombreBD;
import cl.theroot.passbank.datos.nombres.Tabla;
import cl.theroot.passbank.dominio.Contrasenna;

/**
 * Created by Benjamin on 07/10/2017.
 */

public class ContrasennaDAO extends DAO{
    private static final String TAG = "BdC-ContrasennaDAO";

    public ContrasennaDAO(@NonNull Context context) {
        dbOpenHelper = DBOpenHelper.getInstance(context, NombreBD.BANCO_CONTRASENNAS);
        //dbOpenHelper = new DBOpenHelper(context, NombreBD.BANCO_CONTRASENNAS);
    }

    private ContrasennaDAO(@NonNull DBOpenHelper dbOpenHelper) {
        this.dbOpenHelper = dbOpenHelper;
    }

    public List<Contrasenna> seleccionarTodas(){
        SQLiteDatabase db = dbOpenHelper.getReadableDatabase();
        String select = "SELECT * FROM " + Tabla.CONTRASENNA + " ORDER BY " + ColContrasenna.ID;
        Cursor cursor = db.rawQuery(select, null);
        List<Contrasenna> resultado = new ArrayList<>();
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            do {
                Long id = cursor.getLong(cursor.getColumnIndex(ColContrasenna.ID.toString()));
                String nombreCuenta = cursor.getString(cursor.getColumnIndex(ColContrasenna.NOMBRE_CUENTA.toString()));
                String valor = cursor.getString(cursor.getColumnIndex(ColContrasenna.VALOR.toString()));
                String fecha = cursor.getString(cursor.getColumnIndex(ColContrasenna.FECHA.toString()));
                resultado.add(new Contrasenna(id, nombreCuenta, valor, fecha));
            } while(cursor.moveToNext());
        }
        cursor.close();
        //db.close();
        return resultado;
    }

    public List<Contrasenna> seleccionarPorCuenta(String nombreCuenta) {
        SQLiteDatabase db = dbOpenHelper.getReadableDatabase();
        String select = "SELECT * FROM " + Tabla.CONTRASENNA + " WHERE " + ColContrasenna.NOMBRE_CUENTA + " = ? ORDER BY " + ColContrasenna.ID + " DESC";
        String[] whereArgs = {nombreCuenta};
        Cursor cursor = db.rawQuery(select, whereArgs);
        List<Contrasenna> resultado = new ArrayList<>();
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            do {
                Long id = cursor.getLong(cursor.getColumnIndex(ColContrasenna.ID.toString()));
                String valor = cursor.getString(cursor.getColumnIndex(ColContrasenna.VALOR.toString()));
                String fecha = cursor.getString(cursor.getColumnIndex(ColContrasenna.FECHA.toString()));
                resultado.add(new Contrasenna(id, nombreCuenta, valor, fecha));
            } while(cursor.moveToNext());
        }
        cursor.close();
        //db.close();
        return resultado;
    }

    public Contrasenna seleccionarUltimaPorCuenta(@NonNull String nombreCuenta) {
        SQLiteDatabase db = dbOpenHelper.getReadableDatabase();
        String selMaxID = "SELECT MAX(" + ColContrasenna.ID + ") FROM " + Tabla.CONTRASENNA + " WHERE " + ColContrasenna.NOMBRE_CUENTA + " = ?";
        String select = "SELECT * FROM " + Tabla.CONTRASENNA + " WHERE " + ColContrasenna.ID + " IN (" + selMaxID + ")";
        String[] whereArgs = {nombreCuenta};
        Cursor cursor = db.rawQuery(select, whereArgs);
        Contrasenna resultado = null;
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            Long id = cursor.getLong(cursor.getColumnIndex(ColContrasenna.ID.toString()));
            String valor = cursor.getString(cursor.getColumnIndex(ColContrasenna.VALOR.toString()));
            String fecha = cursor.getString(cursor.getColumnIndex(ColContrasenna.FECHA.toString()));
            resultado = new Contrasenna(id, nombreCuenta, valor, fecha);
        }
        cursor.close();
        //db.close();
        return resultado;
    }

    public Contrasenna seleccionarUltima() {
        SQLiteDatabase db = dbOpenHelper.getReadableDatabase();
        String selMaxID = "SELECT MAX(" + ColContrasenna.ID + ") FROM " + Tabla.CONTRASENNA;
        String select = "SELECT * FROM " + Tabla.CONTRASENNA + " WHERE " + ColContrasenna.ID + " IN (" + selMaxID + ")";
        Cursor cursor = db.rawQuery(select, null);
        Contrasenna resultado = null;
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            Long id = cursor.getLong(cursor.getColumnIndex(ColContrasenna.ID.toString()));
            String nombreCuenta = cursor.getString(cursor.getColumnIndex(ColContrasenna.NOMBRE_CUENTA.toString()));
            String valor = cursor.getString(cursor.getColumnIndex(ColContrasenna.VALOR.toString()));
            String fecha = cursor.getString(cursor.getColumnIndex(ColContrasenna.FECHA.toString()));
            resultado = new Contrasenna(id, nombreCuenta, valor, fecha);
        }
        cursor.close();
        //db.close();
        return resultado;
    }

    public Contrasenna seleccionarUna(long id) {
        SQLiteDatabase db = dbOpenHelper.getReadableDatabase();
        String select = "SELECT * FROM " + Tabla.CONTRASENNA + " WHERE " + ColContrasenna.ID + " = ?";
        String[] whereArgs = {String.valueOf(id)};
        Cursor cursor = db.rawQuery(select, whereArgs);
        Contrasenna resultado = null;
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            String nombreCuenta = cursor.getString(cursor.getColumnIndex(ColContrasenna.NOMBRE_CUENTA.toString()));
            String valor = cursor.getString(cursor.getColumnIndex(ColContrasenna.VALOR.toString()));
            String fecha = cursor.getString(cursor.getColumnIndex(ColContrasenna.FECHA.toString()));
            resultado = new Contrasenna(id, nombreCuenta, valor, fecha);
        }
        cursor.close();
        //db.close();
        return resultado;
    }

    public long insertarUna(@NonNull Contrasenna contrasenna) {
        SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ColContrasenna.NOMBRE_CUENTA.toString(), contrasenna.getNombreCuenta());
        values.put(ColContrasenna.VALOR.toString(), contrasenna.getValor());
        values.put(ColContrasenna.FECHA.toString(), contrasenna.getFecha());
        return db.insert(Tabla.CONTRASENNA.toString(), null, values);
    }

    public int actualizarUna(@NonNull Contrasenna contrasenna) {
        SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ColContrasenna.VALOR.toString(), contrasenna.getValor());
        String where = ColContrasenna.ID + " = ?";
        String[] whereArgs = {String.valueOf(contrasenna.getId())};
        return db.update(Tabla.CONTRASENNA.toString(), values, where, whereArgs);
    }

    public int eliminarNoUltimasPorCuenta(@NonNull String nombreCuenta) {
        SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
        String selMaxID = "SELECT MAX(" + ColContrasenna.ID + ") FROM " + Tabla.CONTRASENNA + " WHERE " + ColContrasenna.NOMBRE_CUENTA + " = ?";
        String where = ColContrasenna.NOMBRE_CUENTA + " = ? AND " + ColContrasenna.ID + " NOT IN (" + selMaxID + ")";
        String[] whereArgs = {nombreCuenta, nombreCuenta};
        return db.delete(Tabla.CONTRASENNA.toString(), where, whereArgs);
    }

    public void imprimir() {
        SQLiteDatabase db = dbOpenHelper.getReadableDatabase();
        String select = "SELECT * FROM " + Tabla.CONTRASENNA;
        Cursor cursor = db.rawQuery(select, null);
        Log.i(TAG, "Imprimiendo los valores de la tabla " + Tabla.CONTRASENNA + "...");
        Log.i(TAG, ColContrasenna.ID + " - " + ColContrasenna.NOMBRE_CUENTA + " - " + ColContrasenna.VALOR + " - " + ColContrasenna.FECHA);
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            do {
                long id = cursor.getLong(cursor.getColumnIndex(ColContrasenna.ID.toString()));
                String nombreCuenta = cursor.getString(cursor.getColumnIndex(ColContrasenna.NOMBRE_CUENTA.toString()));
                String valor = cursor.getString(cursor.getColumnIndex(ColContrasenna.VALOR.toString()));
                String fecha = cursor.getString(cursor.getColumnIndex(ColContrasenna.FECHA.toString()));
                Log.i(TAG, id + " - " + nombreCuenta + " - " + valor + " - " + fecha);
            } while(cursor.moveToNext());
        }
        cursor.close();
        //db.close();
    }

    public static void cargarRespaldo(Context context, NombreBD origen, NombreBD destino) {
        DBOpenHelper dbOHOrigen = DBOpenHelper.getInstance(context, origen);
        DBOpenHelper dbOHDestino = DBOpenHelper.getInstance(context, destino);

        ContrasennaDAO daoOrigen = new ContrasennaDAO(dbOHOrigen);
        ContrasennaDAO daoDestino = new ContrasennaDAO(dbOHDestino);
        for (Contrasenna contrasenna : daoOrigen.seleccionarTodas()) {
            daoDestino.insertarUna(contrasenna);
        }
    }
}
