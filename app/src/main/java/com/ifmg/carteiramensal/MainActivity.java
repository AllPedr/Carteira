package com.ifmg.carteiramensal;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;

import ferramentas.EventosDB;
import modelo.Evento;

public class MainActivity extends AppCompatActivity {

    private TextView titulo;
    private TextView entrada;
    private TextView saida;
    private TextView saldo;
    private ImageButton entraBtn;
    private ImageButton saidaBtn;
    private Button anteriorBtn;
    private Button proxBtn;
    private Button novoBtn;
    private Calendar hoje;
    static Calendar dataApp;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // criando link entre os elementos Java x Xml
        titulo = (TextView) findViewById(R.id.tituloMain);
        entrada = (TextView) findViewById(R.id.entradaTxt);
        saida = (TextView) findViewById(R.id.saidaTxt);
        saldo = (TextView) findViewById(R.id.saldoTxt);

        entraBtn = (ImageButton) findViewById(R.id.entradaBtn);
        saidaBtn = (ImageButton) findViewById(R.id.saidaBtn);

        anteriorBtn = (Button) findViewById(R.id.anteriorBtn);
        proxBtn = (Button) findViewById(R.id.proximoBtn);
        novoBtn = (Button) findViewById(R.id.novoBtn);
        //responsavel por emplementar todos os eventos de botoes
        cadastroEventos();


        //recupera data e hora atual
        dataApp = Calendar.getInstance();
        hoje = Calendar.getInstance();

        mostraDataApp();
        atualizaValores();
        configuraPermissoes();

    }
    private void configuraPermissoes(){
        if(ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
            || ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},0);
        }
    }
    private void cadastroEventos(){
        anteriorBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                atualizaMes(-1);
            }
        });

        proxBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                atualizaMes(1);
            }
        });

        novoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //EventosDB db = new EventosDB(MainActivity.this);
                //db.insereEvento();


                //Toast.makeText(MainActivity.this, db.getDatabaseName(), Toast.LENGTH_LONG).show();
            }
        });

        entraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent trocaAct = new Intent(MainActivity.this, VisualizarEventos.class);

                trocaAct.putExtra("acao", 0);

                startActivityForResult(trocaAct, 0);
            }
        });

        saidaBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent trocaAct = new Intent(MainActivity.this, VisualizarEventos.class);

                trocaAct.putExtra("acao", 1);
                //pedindo para iniciar a activity passada como parametro
                startActivityForResult(trocaAct, 1);
            }
        });

    }


    private void  mostraDataApp(){
        String nomeMes[] = {"Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho", "Julho",
                "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro"};

        int mes = dataApp.get(Calendar.MONTH);
        int ano = dataApp.get(Calendar.YEAR);

        titulo.setText(nomeMes[mes] + "/" + ano);

    }
    private void atualizaMes(int ajuste){
        dataApp.add(Calendar.MONTH, ajuste);
        //proximo mes nao pode passar do mes atual
        if(ajuste > 0){
            if(dataApp.after(hoje)){
                dataApp.add(Calendar.MONTH,-1);
            }
        }else{
            //busca no banco de dados para avaliar se tem dados dos messes anteriores
        }


        mostraDataApp();
        atualizaValores();
    }

    private  void atualizaValores(){
        //buscando as entradas e as saidas nesse memo banco de dados
        EventosDB db = new EventosDB(MainActivity.this);

        ArrayList<Evento> saidasListas = db.buscaEvento(1, dataApp);
        ArrayList<Evento> entradasListas = db.buscaEvento(0, dataApp);

        double entradaTotal = 0.0;
        double saidaTotal = 0.0;

        for(int i = 0; i< entradasListas.size(); i++){
            entradaTotal += entradasListas.get(i).getValor();
        }
        for(int i = 0; i< saidasListas.size(); i++){
            saidaTotal += saidasListas.get(i).getValor();
        }
        double saldoTotal = entradaTotal - saidaTotal;

        entrada.setText(String.format("%2f", entradaTotal));
        saida.setText(String.format("%2f", saidaTotal));
        saldo.setText(String.format("%2f", saidaTotal));

    }
    protected void  onActivityResult(int codigoRequest, int codigoResultado, Intent data) {
        super.onActivityResult(codigoRequest, codigoResultado, data);

        atualizaValores();
    }
}