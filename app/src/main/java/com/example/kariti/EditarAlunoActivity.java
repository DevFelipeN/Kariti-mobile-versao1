package com.example.kariti;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

public class EditarAlunoActivity extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener{
    ImageButton voltar;
    EditText nomeAluno, emailAluno;
    Button salvar;
    BancoDados bancoDados;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_aluno);

        nomeAluno = findViewById(R.id.editTextAlunoCad);
        emailAluno = findViewById(R.id.editTextEmailCad);
        voltar = findViewById(R.id.imgBtnVoltar);
        salvar = findViewById(R.id.buttonSalvarEdit);
        bancoDados = new BancoDados(this);

        String id_aluno = String.valueOf(getIntent().getExtras().getInt("id_aluno"));
        String aluno = bancoDados.pegaAluno(id_aluno);
        String email = bancoDados.pegaEmailAluno(id_aluno);
        nomeAluno.setText(aluno);
        emailAluno.setText(email);

        voltar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {Intent intent = new Intent(getApplicationContext(), VisualAlunoActivity.class);
                startActivity(intent);
            finish();}
        });

        salvar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String nomAtual = nomeAluno.getText().toString();
                String emailAtual = emailAluno.getText().toString();
                if (!nomAtual.equals(aluno) || !emailAtual.equals(email)) {
                    if(!email.isEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                        Integer id_aluno = getIntent().getExtras().getInt("id_aluno");
                        Boolean alteraDadoAluno = bancoDados.upadateDadosAluno(nomAtual, emailAtual, id_aluno);
                        if (alteraDadoAluno.equals(true))
                            Toast.makeText(EditarAlunoActivity.this, "Dados atualizados com sucesso!", Toast.LENGTH_SHORT).show();
                        finish();
                        Intent intent = new Intent(getApplicationContext(), VisualAlunoActivity.class);
                        startActivity(intent);
                    }else Toast.makeText(EditarAlunoActivity.this, "E-mail Inválido!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void popMenuAluno(View v){
        PopupMenu popupMenu = new PopupMenu(this, v);
        popupMenu.setOnMenuItemClickListener(this);
        popupMenu.inflate(R.menu.activity_menualuno);
        popupMenu.show();
    }
    @Override
    public boolean onMenuItemClick(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menuEditarAluno) {

            Toast.makeText(EditarAlunoActivity.this, "Editar Aluno selecionado: ", Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.menuExcluirAluno) {

            AlertDialog.Builder builder = new AlertDialog.Builder(EditarAlunoActivity.this);
            builder.setTitle("Atenção!")
                    .setMessage("Deseja Realmente Excluir Aluno?")
                    .setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Integer id_aluno = getIntent().getExtras().getInt("id_aluno");
                            Boolean deletAluno = bancoDados.deletarAluno(id_aluno);
                            if (deletAluno)
                                Toast.makeText(EditarAlunoActivity.this, "Aluno Excluido Com Sucesso", Toast.LENGTH_SHORT).show();
                            finish();
                            Intent intent = new Intent(getApplicationContext(), VisualAlunoActivity.class);
                            startActivity(intent);
                        }
                    })
                    .setNegativeButton("Não", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // cancelou
                        }
                    });
            AlertDialog alertDialog = builder.create();
            alertDialog.show();


            Toast.makeText(EditarAlunoActivity.this, "Excluir Aluno selecionado", Toast.LENGTH_SHORT).show();
            return true;
        } else {
            return false;
        }
    }
}


