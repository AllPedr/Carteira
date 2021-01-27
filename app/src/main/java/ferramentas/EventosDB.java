package ferramentas;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import modelo.Evento;

public class EventosDB extends SQLiteOpenHelper {
    private Context contexto;

    public EventosDB(Context cont){
        super(cont, "evento", null, 1);
        contexto = cont;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String criaTabela ="CREATE TABLE IF NOT EXISTS evento(id INTEGER PRIMARY KEY AUTOINCREMENT, NOME TEXT,"+
        "valor REAL, imagem TEXT, dataocorreu DATE, datacadastro DATE, datavalida DATE)";

        db.execSQL(criaTabela);
    }

    public void insereEvento(Evento novoEvento){

        try(SQLiteDatabase db = this.getWritableDatabase()){

            ContentValues valores = new ContentValues();

            valores.put("nome", novoEvento.getNome());
            valores.put("valor", novoEvento.getValor());
            valores.put("imagem", novoEvento.getCaminhoFoto());
            valores.put("dataocorreu", novoEvento.getOcorreu().getTime());
            valores.put("datacadastro", new Date().getTime());
            valores.put("dataValida", novoEvento.getValida().getTime());


            db.insert("evento", null, valores);

        }catch (SQLException ex){
            ex.printStackTrace();
        }

    }
    public void updateEvento(Evento eventoAtualizado){
        try(SQLiteDatabase db = this.getWritableDatabase()){
            ContentValues valores = new ContentValues();
            valores.put("nome", eventoAtualizado.getNome());
            valores.put("valor", eventoAtualizado.getValor());
            valores.put("imagem", eventoAtualizado.getCaminhoFoto());
            valores.put("dataocorreu", eventoAtualizado.getOcorreu().getTime());
            valores.put("datavalida", eventoAtualizado.getValida().getTime());

            db.update("evento", valores, "id = ?", new String[]{eventoAtualizado.getId()+ ""});


        }catch (SQLiteException ex){
            System.err.println("erro no atualização do evento");
            ex.printStackTrace();
        }

    }

    public Evento buscaEvento(int idEvento){
        String sql = "SELECT*FROM evento WHERE id = "+idEvento;

        Evento resultado = null;

        try(SQLiteDatabase db = this.getWritableDatabase()){
            //executa a sql
            Cursor tupla = db.rawQuery(sql, null);

            //extrai as informacoes do evento
            if(tupla.moveToFirst()){

                String nome = tupla.getString(1);
                double valor = tupla.getDouble(2);
                if(valor < 0 ){
                    valor *=-1;
                }
                String urlfoto = tupla.getString(3);
                Date dataocoreu = new Date(tupla.getLong(4));
                Date datacadastro = new Date(tupla.getLong(5));
                Date datavalida = new Date(tupla.getLong(6));
                resultado = new Evento(idEvento, nome, valor, datacadastro, datavalida, dataocoreu, urlfoto);
            }

        }catch (SQLiteException ex){
            System.err.println("erro da consulta SQL da busca de evento por id");
            ex.printStackTrace();
        }
        return resultado;
    }
    public ArrayList<Evento> buscaEvento(int op, Calendar data){

        ArrayList<Evento> resultado = new ArrayList<>();
        Calendar dia1 = Calendar.getInstance();
        dia1.setTime(dia1.getTime());
        dia1.set(Calendar.DAY_OF_MONTH,1);
        dia1.set(Calendar.HOUR, -12);
        dia1.set(Calendar.MINUTE, 0);
        dia1.set(Calendar.SECOND, 0);

        //ultimo dia do mes
        Calendar dia2 = Calendar.getInstance();
        dia2.setTime(dia2.getTime());
        dia2.set(Calendar.DAY_OF_MONTH, dia2.getActualMaximum(Calendar.DAY_OF_MONTH));
        dia2.set(Calendar.HOUR, 11);
        dia2.set(Calendar.MINUTE, 59);
        dia2.set(Calendar.SECOND, 59);

        String sql = "SELECT * FROM evento WHERE ((datavalida <=" + dia2.getTime().getTime() +
                " AND dataovalida >=" + dia1.getTime().getTime()+") OR (dataocoreu <= "+ dia2.getTime().getTime()+
                "AND datavalida>="+dia1.getTime().getTime()+"))";

        sql+="AND valor ";

        if(op == 0){
            //Entradas
            sql+= ">= 0";
        }else{
            //Saida(indicado por um valor negativo)
            sql+="-< 0";
        }

        try(SQLiteDatabase db = this.getWritableDatabase()){
            Cursor tuplas = db.rawQuery(sql, null);
            //efetuar a leitura da tuplas
            if(tuplas.moveToFirst()){
                do{
                    int id = tuplas.getInt(0);
                    String nome = tuplas.getString(1);
                    double valor = tuplas.getDouble(2);
                    if(valor < 0 ){
                        valor *=-1;
                    }
                    String urlfoto = tuplas.getString(3);
                    Date dataocoreu = new Date(tuplas.getLong(4));
                    Date datacadastro = new Date(tuplas.getLong(5));
                    Date datavalida = new Date(tuplas.getLong(6));


                    Evento temporario = new Evento((long)id, nome, valor, datacadastro, datavalida, dataocoreu, urlfoto);
                    resultado.add(temporario);

                }while(tuplas.moveToNext());
            }

        }catch (SQLiteException ex){

        }

        return resultado;
    }



    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    //ficara parado ate a ativacao da activity de update
    }
}
