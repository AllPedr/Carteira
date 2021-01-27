package com.ifmg.carteiramensal;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.text.SimpleDateFormat;
import java.time.Month;
import java.util.ArrayList;
import java.util.Calendar;

import modelo.Evento;
// classe que define o comportamento e informacoes de cada um dos itens da lista de eventos
public class itemListaEvento extends ArrayAdapter<Evento> {

    private Context contestoPai;
    private ArrayList<Evento> eventos;

    private static class ViewHorder{
        private TextView nomeTxt;
        private TextView valorTxt;
        private TextView dataTxt;
        private TextView repeteTxt;
        private TextView fotoTxt;
    }

    public itemListaEvento(Context contexto, ArrayList<Evento> dados){
        super(contexto, R.layout.item_lista_eventos, dados);

        this.contestoPai = contexto;
        this.eventos = dados;
    }

    @NonNull
    @Override
    public View getView(int indice, @Nullable View convertView, @NonNull ViewGroup parent) {
        //return super.getView(indice, convertView, parent);

        Evento eventoAtual = eventos.get(indice);
        ViewHorder novaView;

        final View resultado;

        //1- quando a lista esta sendo montada pela primeira vez
        if(convertView == null){
            novaView = new ViewHorder();

            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.item_lista_eventos, parent, false);

            //linkando com os componentes do xml
            novaView.dataTxt = (TextView) convertView.findViewById(R.id.dataItem);
            novaView.fotoTxt = (TextView) convertView.findViewById(R.id.fotoItem);
            novaView.nomeTxt = (TextView) convertView.findViewById(R.id.nomeItem);
            novaView.repeteTxt = (TextView) convertView.findViewById(R.id.repeteItem);
            novaView.valorTxt = (TextView) convertView.findViewById(R.id.valorItem);

            resultado = convertView;
            convertView.setTag(novaView);
        }else{
            //2- item modificado
            novaView = (ViewHorder) convertView.getTag();
            resultado = convertView;

        }
        //vamos settar os valores de cada campo
        novaView.nomeTxt.setText(eventoAtual.getNome());
        novaView.valorTxt.setText(eventoAtual.getValor()+"");
        novaView.fotoTxt.setText(eventoAtual.getCaminhoFoto() == null ? "Nao" : "Sim");
        SimpleDateFormat formataData = new SimpleDateFormat("dd/mm/yyyy");
        novaView.dataTxt.setText(formataData.format(eventoAtual.getOcorreu()));

        //Verificar se o evento repete
        Calendar data1 = Calendar.getInstance();
        data1.setTime(eventoAtual.getOcorreu());

        Calendar data2 = Calendar.getInstance();
        data2.setTime(eventoAtual.getValida());

        if(data1.get(Calendar.MONTH) != data2.get(Calendar.MONTH)){
            novaView.repeteTxt.setText("Sim");
        }else{
            novaView.repeteTxt.setText("Não");
        }
        return resultado;
    }
}
