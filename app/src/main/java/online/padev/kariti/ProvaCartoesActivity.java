package online.padev.kariti;

import static android.os.Environment.getExternalStoragePublicDirectory;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import online.padev.kariti.R;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ProvaCartoesActivity extends AppCompatActivity {
    ImageButton voltar;
    Button baixarCartoes;
    Integer id_turma, endereco, idTurmaSelect;
    String prova, turma, turmaSelecionada, idAluno;
    ArrayList<String> provalist, turmalist, alunolist;
    List<String[]> dados;
    ArrayList<Integer> listIdAlTurma, listIdsAlunos;
    BancoDados bancoDados;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prova_cartoes);

        voltar = findViewById(R.id.imgBtnVoltar);
        Spinner spinnerTurma = findViewById(R.id.spinnerTurma);
        Spinner spinnerProva = findViewById(R.id.spinnerProva);
        Spinner spinnerAluno = findViewById(R.id.spinnerAlunos);
        baixarCartoes = findViewById(R.id.baixarcatoes);

        bancoDados = new BancoDados(this);

        endereco = getIntent().getExtras().getInt("endereco");
        prova = getIntent().getExtras().getString("prova");

        turmalist = (ArrayList<String>) bancoDados.obterNomeTurmas();
        if(endereco.equals(02)){
            turmalist.add(0,"Selecione a turma");
            SpinnerAdapter adapterTurma = new SpinnerAdapter(this, turmalist);
            spinnerTurma.setAdapter(adapterTurma);

            spinnerTurma.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if(position!=0){
                        turmaSelecionada = spinnerTurma.getSelectedItem().toString();
                        idTurmaSelect = bancoDados.pegaIdTurma(turmaSelecionada);
                        provalist = (ArrayList<String>) bancoDados.obterNomeProvas(String.valueOf(idTurmaSelect));
                        //provalist.add(0, "Selecione a prova");
                        SpinnerAdapter adapterProva = new SpinnerAdapter(ProvaCartoesActivity.this, provalist);
                        spinnerProva.setAdapter(adapterProva);

                        alunolist = new ArrayList<>();
                        listIdAlTurma = (ArrayList<Integer>) bancoDados.listAlunosDturma(String.valueOf(idTurmaSelect));
                        alunolist.add(0, "Alunos");
                        int num = listIdAlTurma.size();
                        for(int x = 0; x < num; x++){
                            String id_aluno = String.valueOf(listIdAlTurma.get(x));
                            String alunoCadastrado = bancoDados.pegaNomeAluno(id_aluno);
                            if(alunoCadastrado != null){
                                alunolist.add(alunoCadastrado);
                            }

                        }
                        SpinnerAdapter adapterAluno = new SpinnerAdapter(ProvaCartoesActivity.this, alunolist);
                        spinnerAluno.setAdapter(adapterAluno);

                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });

        }else if(endereco.equals(01)){
            id_turma = getIntent().getExtras().getInt("id_turma");
            turma = bancoDados.pegaNomeTurma(String.valueOf(id_turma));
            turmalist.add(0, turma);
            SpinnerAdapter adapterTurma = new SpinnerAdapter(this, turmalist);
            spinnerTurma.setAdapter(adapterTurma);

            provalist = (ArrayList<String>) bancoDados.obterNomeProvas(String.valueOf(id_turma));
            provalist.add(0, prova);
            SpinnerAdapter adapterProva = new SpinnerAdapter(ProvaCartoesActivity.this, provalist);
            spinnerProva.setAdapter(adapterProva);

            alunolist = new ArrayList<>();
            listIdAlTurma = (ArrayList<Integer>) bancoDados.listAlunosDturma(String.valueOf(id_turma));
            alunolist.add(0, "Alunos");
            int num = listIdAlTurma.size();
            for(int x = 0; x < num; x++){
                String id_aluno = String.valueOf(listIdAlTurma.get(x));
                alunolist.add(bancoDados.pegaNomeAluno(id_aluno));
            }
            SpinnerAdapter adapterAluno = new SpinnerAdapter(ProvaCartoesActivity.this, alunolist);
            spinnerAluno.setAdapter(adapterAluno);

            spinnerTurma.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if(position!=0){
                        turmaSelecionada = spinnerTurma.getSelectedItem().toString();
                        idTurmaSelect = bancoDados.pegaIdTurma(turmaSelecionada);
                        provalist = (ArrayList<String>) bancoDados.obterNomeProvas(String.valueOf(idTurmaSelect));
                        provalist.add(0, "Selecione a prova");
                        SpinnerAdapter adapterProva = new SpinnerAdapter(ProvaCartoesActivity.this, provalist);
                        spinnerProva.setAdapter(adapterProva);

                        alunolist = new ArrayList<>();
                        listIdAlTurma = (ArrayList<Integer>) bancoDados.listAlunosDturma(String.valueOf(idTurmaSelect));//pegando Ids dos alunos
                        alunolist.add(0, "Alunos");
                        int num = listIdAlTurma.size();
                        for(int x = 0; x < num; x++){
                            String id_aluno = String.valueOf(listIdAlTurma.get(x));
                            alunolist.add(bancoDados.alunosGerarProva(id_aluno));
                        }
                        SpinnerAdapter adapterAluno = new SpinnerAdapter(ProvaCartoesActivity.this, alunolist);
                        spinnerAluno.setAdapter(adapterAluno);

                    }
                }
                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
        }
        baixarCartoes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isOnline()) {
                    if(spinnerProva.getSelectedItem() != null) {
                        String nomeProva = spinnerProva.getSelectedItem().toString();
                        String id_prova = String.valueOf(bancoDados.pegaIdProva(nomeProva));
                        String nomeTurma = spinnerTurma.getSelectedItem().toString();
                        String id_usuario = String.valueOf(BancoDados.USER_ID);
                        String prof = bancoDados.pegaUsuario(id_usuario);
                        String data = bancoDados.pegaData(id_prova);
                        String nota = String.valueOf(bancoDados.listNota(id_prova));
                        String questoes = String.valueOf(bancoDados.pegaqtdQuestoes(id_prova));
                        String alternativas = String.valueOf(bancoDados.pegaqtdAlternativas(id_prova));

                        dados = new ArrayList<>();

                        String idTurma = String.valueOf(bancoDados.pegaIdTurma(nomeTurma));
                        listIdsAlunos = (ArrayList<Integer>) bancoDados.listAlunosDturma(idTurma);
                        int qtdProvas = listIdsAlunos.size();
                        dados.add(new String[]{"ID_PROVA", "NOME_PROVA", "NOME_PROFESSOR", "NOME_TURMA", "DATA_PROVA", "NOTA_PROVA", "QTD_QUESTOES", "QTD_ALTERNATIVAS", "ID_ALUNO", "NOME_ALUNO"});
                        for (int x = 0; x < qtdProvas; x++) {
                            idAluno = String.valueOf(listIdsAlunos.get(x));
                            String aluno = bancoDados.alunosGerarProva(String.valueOf(listIdsAlunos.get(x)));
                            dados.add(new String[]{id_prova, nomeProva, prof, nomeTurma, data, nota, questoes, alternativas, idAluno, aluno});
                        }
                        try {
                            File filecsv = null;
                            String dateCart = new SimpleDateFormat(" HH_mm_ss").format(new Date());
                            String filePdf = nomeProva + dateCart + ".pdf";
                            //String estado = Environment.getExternalStorageState();
                            //if (estado.equals(Environment.MEDIA_MOUNTED)) {
                            //filecsv = new File(getExternalFilesDir(null), "/dadosProva.csv");
                            filecsv = new File(getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "dadosProva.csv");
                            GerarCsv.gerar(dados, filecsv);// Gerando e salvando arquivo.csv
                            //} else Toast.makeText(ProvaCartoesActivity.this, "Erro: Espaço de Armazenamento indisponível!", Toast.LENGTH_SHORT).show();
                            File fSaida = new File(getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), filePdf);
                            BaixarModeloCartao.solicitarCartoesResposta(filecsv, new FileOutputStream(fSaida), fSaida, filePdf, (DownloadManager) getSystemService(DOWNLOAD_SERVICE));

                            AlertDialog.Builder builder = new AlertDialog.Builder(ProvaCartoesActivity.this);
                            builder.setTitle("Por favor, Aguarde!")
                                    .setMessage("Download em execução. Você será notificado quando o arquivo estiver baixado.");
                            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    finish();
                                }
                            });
                            AlertDialog alertDialog = builder.create();
                            alertDialog.show();

                        } catch (Exception e) {
                            Log.e("Kariti", e.toString());
                            Toast.makeText(ProvaCartoesActivity.this, "Erro: " + e.toString(), Toast.LENGTH_SHORT).show();
                        }
                    }else Toast.makeText(ProvaCartoesActivity.this, "Selecione os dados", Toast.LENGTH_SHORT).show();
                }else Toast.makeText(ProvaCartoesActivity.this, "Sem conexão de rede!", Toast.LENGTH_SHORT).show();
            }
        });
        voltar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }
    private void downloadPDF(String pdfURF){
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(pdfURF));
        request.setTitle("PDF Download");
        request.setDescription("Downloading the PDF File");
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "pdf_file.pdf");
        DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        manager.enqueue(request);
    }
    private boolean isOnline() {
        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return manager.getActiveNetworkInfo() != null && manager.getActiveNetworkInfo().isConnectedOrConnecting();
    }
}