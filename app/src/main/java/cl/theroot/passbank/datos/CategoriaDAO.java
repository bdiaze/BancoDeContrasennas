package cl.theroot.passbank.datos;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import cl.theroot.passbank.datos.nombres.ColCategoria;
import cl.theroot.passbank.datos.nombres.NombreBD;
import cl.theroot.passbank.datos.nombres.Tabla;
import cl.theroot.passbank.dominio.Categoria;
import cl.theroot.passbank.dominio.CategoriaCuenta;

public class CategoriaDAO extends DAO {
    private static final String TAG = "BdC-CategoriaDAO";

    public CategoriaDAO(@NonNull Context context) {
        dbOpenHelper = DBOpenHelper.getInstance(context, NombreBD.BANCO_CONTRASENNAS);
    }

    public CategoriaDAO(@NonNull Context context, boolean usarRespaldo) {
        if (usarRespaldo) {
            dbOpenHelper = DBOpenHelper.getInstance(context, NombreBD.BANCO_CONTRASENNAS_RESPALDO);
            //dbOpenHelper = new DBOpenHelper(context, NombreBD.BANCO_CONTRASENNAS_RESPALDO);
        } else {
            dbOpenHelper = DBOpenHelper.getInstance(context, NombreBD.BANCO_CONTRASENNAS);
            //dbOpenHelper = new DBOpenHelper(context, NombreBD.BANCO_CONTRASENNAS);
        }
    }

    public CategoriaDAO(@NonNull DBOpenHelper dbOpenHelper) {
        this.dbOpenHelper = dbOpenHelper;
    }

    public List<Categoria> seleccionarTodas() {
        SQLiteDatabase db = dbOpenHelper.getReadableDatabase();
        String select = "SELECT * FROM " + Tabla.CATEGORIA + " ORDER BY " + ColCategoria.POSICION + ", " + ColCategoria.NOMBRE;
        Cursor cursor = db.rawQuery(select, null);
        List<Categoria> resultado = new ArrayList<>();
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            do {
                String nombre = cursor.getString(cursor.getColumnIndex(ColCategoria.NOMBRE.toString()));
                Integer posicion = cursor.getInt(cursor.getColumnIndex(ColCategoria.POSICION.toString()));
                resultado.add(new Categoria(nombre, posicion));
            } while(cursor.moveToNext());
        }
        cursor.close();
        //db.close();
        return resultado;
    }

    public Categoria seleccionarUna(@NonNull String nombre) {
        SQLiteDatabase db = dbOpenHelper.getReadableDatabase();
        String select = "SELECT * FROM " + Tabla.CATEGORIA + " WHERE " + ColCategoria.NOMBRE + " = ?";
        String[] whereArgs = {nombre};
        Cursor cursor = db.rawQuery(select, whereArgs);
        Categoria resultado = null;
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            Integer posicion = cursor.getInt(cursor.getColumnIndex(ColCategoria.POSICION.toString()));
            resultado = new Categoria(nombre, posicion);
        }
        cursor.close();
        //db.close();
        return resultado;
    }

    private int sigPosDisponible() {
        int salida = 1;
        String SB_MAXIMO = "MAXIMO";
        SQLiteDatabase db = dbOpenHelper.getReadableDatabase();
        String select = String.format("SELECT MAX(%s) AS %s FROM %s",
                ColCategoria.POSICION, SB_MAXIMO, Tabla.CATEGORIA);
        Cursor cursor = db.rawQuery(select, null);
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            salida = cursor.getInt(cursor.getColumnIndex(SB_MAXIMO)) + 1;
        }
        cursor.close();
        //db.close();
        return salida;
    }

    public long insertarUna(@NonNull Categoria categoria) {
        SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ColCategoria.NOMBRE.toString(), categoria.getNombre());
        values.put(ColCategoria.POSICION.toString(), sigPosDisponible());
        return db.insert(Tabla.CATEGORIA.toString(), null, values);
    }

    public int actualizarUna(@NonNull String nombreAnterior, @NonNull Categoria categoria) {
        SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ColCategoria.NOMBRE.toString(), categoria.getNombre());
        values.put(ColCategoria.POSICION.toString(), categoria.getPosicion());
        String where = ColCategoria.NOMBRE + " = ?";
        String[] whereArgs= {nombreAnterior};
        return db.update(Tabla.CATEGORIA.toString(), values, where, whereArgs);
    }

    public int eliminarUna(@NonNull String nombre) {
        SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
        int salida;
        CategoriaCuentaDAO categoriaCuentaDAO = new CategoriaCuentaDAO(dbOpenHelper);

        //Obtenemos lista de Categorías/AdapCuentas asociadas a la categoría, y se eliminan
        for (CategoriaCuenta categoriaCuenta : categoriaCuentaDAO.seleccionarPorCategoria(nombre)) {
            categoriaCuentaDAO.eliminarUna(categoriaCuenta.getNombreCategoria(), categoriaCuenta.getNombreCuenta());
        }

        //Se obtiene la posición de la categoría
        Categoria categoria = seleccionarUna(nombre);
        Integer pos = null;
        if (categoria != null) {
            pos = categoria.getPosicion();
        }

        //Se elimina la categoría
        String where = ColCategoria.NOMBRE + " = ?";
        String[] whereArgs = {nombre};
        salida = db.delete(Tabla.CATEGORIA.toString(), where, whereArgs);

        if (pos != null) {
            //Se modifican las posiciones del resto de categorías
            //Primero se obtiene una lista de las posiciones a modificar, cambiando los valores de posición
            List<Categoria> elementosModificar = new ArrayList<>();
            String select = String.format("SELECT * FROM %s WHERE %s > ?",
                    Tabla.CATEGORIA,
                    ColCategoria.POSICION);
            whereArgs = new String[]{String.valueOf(pos)};
            Cursor cursor = db.rawQuery(select, whereArgs);
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                do {
                    String nomCategoria = cursor.getString(cursor.getColumnIndex(ColCategoria.NOMBRE.toString()));
                    Integer posCategoria = cursor.getInt(cursor.getColumnIndex(ColCategoria.POSICION.toString())) - 1;
                    elementosModificar.add(new Categoria(nomCategoria, posCategoria));
                } while(cursor.moveToNext());
            }
            cursor.close();
            //Luego se actualizan las posiciones
            for (Categoria cat : elementosModificar) {
                actualizarUna(cat.getNombre(), cat);
            }
        }
        //db.close();
        return salida;
    }

    public void imprimir() {
        SQLiteDatabase db = dbOpenHelper.getReadableDatabase();
        String select = "SELECT * FROM " + Tabla.CATEGORIA;
        Cursor cursor = db.rawQuery(select, null);
        Log.i(TAG, "Imprimiendo los valores de la tabla " + Tabla.CATEGORIA + "...");
        Log.i(TAG, ColCategoria.NOMBRE + " - " + ColCategoria.POSICION);
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            do {
                String nombre = cursor.getString(cursor.getColumnIndex(ColCategoria.NOMBRE.toString()));
                int posicion = cursor.getInt(cursor.getColumnIndex(ColCategoria.POSICION.toString()));
                Log.i(TAG, nombre + " - " + posicion);
            } while(cursor.moveToNext());
        }
        cursor.close();
        //db.close();
    }

    public static void cargarRespaldo(Context context, NombreBD origen, NombreBD destino) {
        DBOpenHelper dbOHOrigen = DBOpenHelper.getInstance(context, origen);
        DBOpenHelper dbOHDestino = DBOpenHelper.getInstance(context, destino);
        //DBOpenHelper dbOHOrigen = new DBOpenHelper(context, origen);
        //DBOpenHelper dbOHDestino = new DBOpenHelper(context, destino);

        CategoriaDAO daoOrigen = new CategoriaDAO(dbOHOrigen);
        CategoriaDAO daoDestino = new CategoriaDAO(dbOHDestino);
        for (Categoria categoria : daoOrigen.seleccionarTodas()) {
            daoDestino.insertarUna(categoria);
        }
    }
}
