package online.padev.kariti;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class AnimacaoCorrecao extends AppCompatActivity {
    private TextView titulo, informativo;
    private static AnimacaoCorrecao instanciaEncerra;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_animacao_correcao);

        instanciaEncerra = this;

        ImageButton btnVoltar = findViewById(R.id.imgBtnVoltar);
        titulo = findViewById(R.id.toolbar_title);
        informativo = findViewById(R.id.textViewInformativo);

        informativo.setText(String.format("%s","Prova(s) enviada(s) para correção!\n\n" +
                "Em instantes sua prova será corrigida. Após a correção, o resultado " +
                "estará disponível na opção 'Visualizar Correção'\n\n" +
                "Por favor, aguarde..."));

        titulo.setText("Corrigindo");
        btnVoltar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
                finish();
            }
        });
    }
    protected void onDestroy() {
        super.onDestroy();
        instanciaEncerra = null;
    }

    public static void encerra() {
        if (instanciaEncerra != null) {
            instanciaEncerra.finish();
        }
    }
}