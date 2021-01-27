package com.ifmg.carteiramensal;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;

import ferramentas.EventosDB;
import modelo.Evento;

public class CadastroEdicaoEvento extends AppCompatActivity {

    private DatePickerDialog calendarioUsuario;
    private TextView tituloTxt;
    private EditText nomeTxt;
    private EditText valorTxt;
    private TextView dataTxt;
    private CheckBox repeteBtn;
    private ImageView foto;
    private Button fotoBtn;
    private Button salvarBtn;
    private Button cancelarBtn;
    private Calendar calendarioTemp;
    private Spinner mesesRepeteSpi;


    //0 - cadastro de entrada; 1- cadastro de saída; 2- edição de entrada; 3- edição de saída
    private int acao = -1;
    private Evento eventoSelecionado;
    private String nomeFoto;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro_edicao_evento);

        tituloTxt = (TextView) findViewById(R.id.tituloCadastroTxt);
        nomeTxt = (EditText) findViewById(R.id.nomeCadastroTxt);
        valorTxt = (EditText) findViewById(R.id.valorCadastroTxt);
        dataTxt = (TextView) findViewById(R.id.dataCadastroTxt);
        repeteBtn = (CheckBox) findViewById(R.id.repeteBtn);
        foto = (ImageView) findViewById(R.id.fotoCadastro);
        fotoBtn = (Button) findViewById(R.id.fotoBtn);
        salvarBtn = (Button) findViewById(R.id.salvarCadastroBtn);
        cancelarBtn = (Button) findViewById(R.id.cancelarCadastroBtn);
        mesesRepeteSpi = (Spinner) findViewById(R.id.mesesSpiner);

        Intent intencao = getIntent();
        acao = intencao.getIntExtra("acao", -1);

        ajustaPorAcao();
        cadastraEventos();
        confSpinners();

    }
    private void confSpinners(){
        List<String> meses = new ArrayList<>();
        //permissão da repetição de apenas 24 meses de um evento
        for(int i = 1; i<= 24; i++){
            meses.add(i+"");
        }
        ArrayAdapter<String> listaAdapter = new ArrayAdapter<String>(this,
                R.layout.support_simple_spinner_dropdown_item, meses);
        mesesRepeteSpi.setAdapter(listaAdapter);
        mesesRepeteSpi.setEnabled(false);
    }

    private void cadastraEventos(){
        //Configuração do DatePicher
        calendarioTemp = Calendar.getInstance();
        if(acao >= 2){
            calendarioTemp.setTime(eventoSelecionado.getOcorreu());
        }

        calendarioUsuario = new DatePickerDialog(CadastroEdicaoEvento.this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int ano, int mes, int dia) {
                calendarioTemp.set(ano, mes, dia);
                dataTxt.setText(dia+"/"+(mes+1)+"/"+ano);
            }
        }, calendarioTemp.get(Calendar.YEAR), calendarioTemp.get(Calendar.MONTH), calendarioTemp.get(Calendar.DAY_OF_MONTH));

        dataTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calendarioUsuario.show();
            }
        });

        salvarBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(acao < 2) {
                    //cadastro de um novo evento
                    cadastrarNovoEvento();
                }else{
                    //update do evento
                    updateEvento();
                }
            }
        });
        //Tratando a repetição do evento

        repeteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(repeteBtn.isChecked()){
                    mesesRepeteSpi.setEnabled(true);
                }else{
                    mesesRepeteSpi.setEnabled(false);
                }
            }
        });

        cancelarBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(acao<2) {
                    //Termina a execucao de uma Activity e retorna a anterior
                    finish();
                }else{
                    //chamará o metodo delete do banco de dados
                }
            }
        });

        fotoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent cameraActivity = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                startActivityForResult(cameraActivity, 100);
            }
        });
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK){
            Bitmap imagemUser = (Bitmap) data.getExtras().get("data");
            foto.setImageBitmap(imagemUser);
            foto.setBackground(null);

            salvarImagem(imagemUser);
        }
    }
    private void salvarImagem(Bitmap img){
        Random gerador = new Random();
        Date instate = new Date();

        //definindo o nome da foto
        String nome = gerador.nextInt() + ""+instate.getTime() + ".png";
        nomeFoto = nome;

        File sd = Environment.getExternalStorageDirectory();
        File fotoArquivo = new File(sd, nome);

        //processo de gravacao em sistema de armazenamento no smartphone(dispositivo)
        try{
            FileOutputStream gravador = new FileOutputStream(fotoArquivo);
            img.compress(Bitmap.CompressFormat.PNG, 100, gravador);
            gravador.flush();
            gravador.close();

        }catch (Exception ex){
            System.err.println("erro ao armazenar a foto");
        }
    }
    //metodo chamado durante a edicao
    private void carregarImagem(){
        if(nomeFoto != null){
            File sd= Environment.getExternalStorageDirectory();
            File arquivoLeirura = new File(sd, nomeFoto);

            try{
                FileInputStream leitor = new FileInputStream(arquivoLeirura);
                Bitmap img = BitmapFactory.decodeStream(leitor);

                foto.setImageBitmap(img);
                foto.setBackground(null);

            }catch (Exception e){
                System.err.println("erro na leitura da foto");
            }
        }
    }


    // metodo que auxilia na reutilização da activity, modifica valores dos componentes reutilizaveis
    private void ajustaPorAcao(){
        // recuperacao da data de hj

        Calendar hoje = Calendar.getInstance();

        SimpleDateFormat formatador = new SimpleDateFormat("dd,MM,yyyy");
        dataTxt.setText(formatador.format(hoje.getTime()));


        switch (acao){
            case 0:{
                tituloTxt.setText("Cadast. Entrada");
            }break;
            case 1:{
                tituloTxt.setText("Cadast. Saída");
            }break;
            case 2:{
                //edicao de entradas
                tituloTxt.setText("Edição Entrada");
                ajusteEdicao();
            }break;
            case 3:{//edicao de saidas
                tituloTxt.setText("Edição Saída");
                ajusteEdicao();
            }break;
            default:{

            }

        }

    }

    private void ajusteEdicao(){
        cancelarBtn.setText("excluir");
        salvarBtn.setText("atualizar");

        int id = Integer.parseInt(getIntent().getStringExtra("id"));

        if(id != 0){
            EventosDB db = new EventosDB(CadastroEdicaoEvento.this);
            eventoSelecionado = db.buscaEvento(id);

            //carregar as inf dos campos recuperados do banco
            SimpleDateFormat formatar = new SimpleDateFormat("dd/MM/yyyy");
            nomeTxt.setText(eventoSelecionado.getNome());
            valorTxt.setText(eventoSelecionado.getValor()+"");
            dataTxt.setText(formatar.format(eventoSelecionado.getOcorreu()));

            nomeFoto = eventoSelecionado.getCaminhoFoto();
            carregarImagem();

            Calendar d1 = Calendar.getInstance();
            d1.setTime(eventoSelecionado.getValida());

            Calendar d2 = Calendar.getInstance();
            d2.setTime(eventoSelecionado.getOcorreu());

            repeteBtn.setChecked(d1.get(Calendar.MONTH) != d2.get(Calendar.MONTH)? true: false);

            if(repeteBtn.isChecked()){
                mesesRepeteSpi.setEnabled(true);

                //diferenca entre mes de cadastro e mes de validade
                mesesRepeteSpi.setSelection(d1.get(Calendar.MONTH) - d2.get(Calendar.MONTH) -1);

            }
        }
    }

    private void updateEvento(){
        eventoSelecionado.setNome(nomeTxt.getText().toString());
        eventoSelecionado.setValor(Double.parseDouble(valorTxt.getText().toString()));
        if(acao == 3){
            eventoSelecionado.setValor(eventoSelecionado.getValor() * -1);
        }

        eventoSelecionado.setOcorreu(calendarioTemp.getTime());
        //um novo calendario para calcular a data limite (repeticao)
        Calendar dataLimite = Calendar.getInstance();
        dataLimite.setTime(calendarioTemp.getTime());

        //verificando se esse evento ira repetir por alguns meses
        if(repeteBtn.isChecked()){
            //por enquanto estamos considerando apenas um mes
            String mesStr = (String)mesesRepeteSpi.getSelectedItem();
            dataLimite.add(Calendar.MONTH, Integer.parseInt(mesStr));
        }
        //setando para o ultimo dia do mes limite
        dataLimite.set(Calendar.DAY_OF_MONTH, dataLimite.getActualMaximum(Calendar.DAY_OF_MONTH));
        eventoSelecionado.setValida(dataLimite.getTime());
        eventoSelecionado.setCaminhoFoto(nomeFoto);

        EventosDB db = new EventosDB(CadastroEdicaoEvento.this);
        db.updateEvento(eventoSelecionado);
        finish();
    }
    private void cadastrarNovoEvento(){

        String nome = nomeTxt.getText().toString();
        Double valor = Double.parseDouble(valorTxt.getText().toString());

        if(acao == 1 || acao == 3){
            valor *= -1;
        }

        //SimpleDateFormat formatador = new SimpleDateFormat("dd/MM/yyyy");
        //String dataStr = dataTxt.getText().toString();

        //try {

            Date diaEvento = calendarioTemp.getTime();
            //um novo calendario para calcular a data limite)
            Calendar dataLimite = Calendar.getInstance();
            dataLimite.setTime(calendarioTemp.getTime());

            //verificando se esse evento ira repetir por alguns meses
            if(repeteBtn.isChecked()){
            //considerando apenas um mes
                String mesStr = (String)mesesRepeteSpi.getSelectedItem();
                dataLimite.add(Calendar.MONTH, Integer.parseInt(mesStr));
            }
            //settando para o ultimo dia do mes limite
            dataLimite.set(Calendar.DAY_OF_MONTH, dataLimite.getActualMaximum(Calendar.DAY_OF_MONTH));

            Evento novoEvento = new Evento(nome, valor, new Date(), dataLimite.getTime(), diaEvento, nomeFoto);

            //inserir esse evento no banco de dados
            EventosDB bd = new EventosDB(CadastroEdicaoEvento.this);
            bd.insereEvento(novoEvento);

            Toast.makeText(CadastroEdicaoEvento.this, "Cadastro feito com sucesso", Toast.LENGTH_LONG).show();

            finish();

        /*}catch (ParseException ex){
            System.err.println("erro no formato da data....");
        }*/

    }

}