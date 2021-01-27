package com.ifmg.carteiramensal;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import ferramentas.EventosDB;
import modelo.Evento;

public class VisualizarEventos extends AppCompatActivity {

    private TextView tituloTxt;
    private ListView listaEventos;
    private TextView totalTxt;
    private Button novoBtn;
    private Button cancelarBtn;

    private ArrayList<Evento> eventos;
    private itemListaEvento adapter;


    //0peracao = 0 indica entrada e operacao = 1 indica saida
    private int operacao = -1;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visualizar_eventos);

        tituloTxt = (TextView) findViewById(R.id.tituloTxt);
        listaEventos = (ListView) findViewById(R.id.listaEventos);
        totalTxt = (TextView) findViewById(R.id.valorTotalTxt);
        novoBtn = (Button) findViewById(R.id.novoBtn);
        cancelarBtn = (Button) findViewById(R.id.cancelarBtn);

        Intent intencao = getIntent();
        operacao = intencao.getIntExtra("acao", -1);
        //0 - entrada e 1 - saida

        ajustaOperacao();
        cadastrarEventos();
        cadastrarEventos();

        carregaEventosLista();

    }

    private void cadastrarEventos(){
        novoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(operacao != -1) {

                    Intent trocaAct = new Intent(VisualizarEventos.this, CadastroEdicaoEvento.class);
                    if(operacao == 0){
                        trocaAct.putExtra("acao",0);
                        startActivityForResult(trocaAct, 0);
                    }else{
                        trocaAct.putExtra("acao", 1);
                        startActivityForResult(trocaAct, 0);
                    }


                }

            }
        });
         cancelarBtn.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 finish();
             }
         });

    }


    private void ajustaOperacao(){
        //precisa realizar uma busca no banco a respeito dos eventos a serem apresentados na lista

        if(operacao == 0){
            tituloTxt.setText("Entradas");
        }else{
            if(operacao == 1){
                tituloTxt.setText("Saidas");
            }else{
                //erro na cofg. da intent 
                Toast.makeText(VisualizarEventos.this, "erro no parametro ação", Toast.LENGTH_LONG).show();
            }
        }

    }

    private void carregaEventosLista(){
        EventosDB db = new EventosDB(VisualizarEventos.this);
        eventos = db.buscaEvento(operacao, MainActivity.dataApp);

        adapter = new itemListaEvento(getApplicationContext(), eventos);
        listaEventos.setAdapter(adapter);

        listaEventos.setOnClickListener((new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listaEventos.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int indice, long id) {
                        Evento eventoSelecionado = eventos.get(indice);
                        Intent novoFluxo = new Intent(VisualizarEventos.this, CadastroEdicaoEvento.class);
                        if(operacao == 0){
                            novoFluxo.putExtra("acao", 2);

                        }else{
                            novoFluxo.putExtra("acao", 3);
                        }

                        novoFluxo.putExtra("acao", eventoSelecionado.getId()+ "");

                        startActivityForResult(novoFluxo, operacao);
                    }
                });
            }
        }));



        double total = 0.0;
        for(int i = 0; i < eventos.size(); i++ ){
            total += eventos.get(i).getValor();
        }
        totalTxt.setText(String.format("%2f", total));
    }

    protected void  onActivityResult(int codigoRequest, int codigoResultado, Intent data) {
        super.onActivityResult(codigoRequest, codigoResultado, data);

        carregaEventosLista();
    }


}