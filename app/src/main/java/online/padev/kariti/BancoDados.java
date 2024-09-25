package online.padev.kariti;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import androidx.annotation.NonNull;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
public class BancoDados extends SQLiteOpenHelper {
    public static final String DBNAME = "base_dados.db";
    public static Integer USER_ID;
    public static Integer ID_ESCOLA;
    public BancoDados(Context context) {
        super(context, DBNAME, null, 25);
    }
    @Override
    public void onCreate(SQLiteDatabase base_dados) {
        try {
            base_dados.execSQL("create Table usuario( id_usuario INTEGER primary Key AUTOINCREMENT, nomeUsuario TEXT not null, email TEXT UNIQUE not null, password varchar(256) not null)");
            base_dados.execSQL("create Table validacao_usuario( id_validacao INTEGER primary Key AUTOINCREMENT, id_usuario INT NOT NULL references usuario(id_usuario), codigo TEXT, data_expiracao TEXT)");
            base_dados.execSQL("create Table escola(id_escola INTEGER PRIMARY KEY AUTOINCREMENT, nomeEscola TEXT, bairro TEXT, id_usuario INT NOT NULL references usuario(id_usuario), status Integer not null check(status = 0 or status = 1))");
            base_dados.execSQL("create Table aluno(id_aluno Integer PRIMARY KEY AUTOINCREMENT, nomeAluno TEXT not null, email TEXT, status Integer not null check(status = 0 or status = 1), id_usuario INTEGER not null references usuario(id_usuario))");
            base_dados.execSQL("create Table turma(id_turma Integer PRIMARY KEY AUTOINCREMENT, id_escola INTEGER not null references escola(id_escola), nomeTurma TEXT not null, qtdAnonimos Integer not null)");
            base_dados.execSQL("create Table alunosTurma(id_turma Integer not null references turma(id_turma), id_aluno Integer not null references aluno(id_aluno), primary key (id_turma, id_aluno))");
            base_dados.execSQL("create Table prova(id_prova Integer PRIMARY KEY AUTOINCREMENT, nomeProva TEXT not null, dataProva TEXT not null, qtdQuestoes Integer not null, qtdAlternativas Interger not null, id_escola INTEGER, id_turma Integer not null references turma(id_turma))");
            base_dados.execSQL("create Table gabarito(id_gabarito Integer PRIMARY KEY AUTOINCREMENT, id_prova Integer not null references prova(id_prova), questao Integer not null, resposta Integer not null, nota Real not null)");
            base_dados.execSQL("create Table resultadoCorrecao(id_resultado Integer PRIMARY KEY AUTOINCREMENT, id_prova Integer not null references prova(id_prova), id_aluno Integer not null references aluno(id_aluno), questao Integer, respostaDada Integer)");
            base_dados.execSQL("create Table galeria(id INTEGER PRIMARY KEY AUTOINCREMENT, foto BLOB)");
        }catch(Exception e){
            Log.e("Error base_dados: ",e.getMessage());
        }
    }
    @Override
    public void onUpgrade(SQLiteDatabase base_dados, int oldVersion, int newVersion) {
        try {
            base_dados.execSQL("drop Table if exists usuario");
            base_dados.execSQL("drop Table if exists validacao_usuario");
            base_dados.execSQL("drop Table if exists escola");
            base_dados.execSQL("drop Table if exists aluno");
            base_dados.execSQL("drop Table if exists turma");
            base_dados.execSQL("drop Table if exists prova");
            base_dados.execSQL("drop Table if exists gabarito");
            base_dados.execSQL("drop Table if exists galeria");
            base_dados.execSQL("drop Table if exists alunosTurma");
            base_dados.execSQL("drop Table if exists resultadoCorrecao");
            onCreate(base_dados);
        }catch(Exception e){
            Log.e("Error base_dados: ",e.getMessage());
        }

    }

    /**
     * Este metodo cadastra novos usuários na tabela usuário.
     * @param nome nome do novo usuário que se deseja cadastrar
     * @param password senha do novo usuário que se deseja cadastrar
     * @param email email do novo usuário que se deseja cadastrar
     * @return retorna verdadeiro de cadastrado com sucesso falso, caso contrario
     */
    public boolean cadastrarUsuario(String nome, String password, String email) {
        SQLiteDatabase base_dados = null;
        try {
            base_dados = this.getWritableDatabase();
            ContentValues contentValues = new ContentValues();
            contentValues.put("nomeUsuario", nome);
            contentValues.put("password", to256(password));
            contentValues.put("email", email);
            long inserir = base_dados.insert("usuario", null, contentValues);
            return inserir != -1;
        } catch (Exception e) {
            Log.e("kariti", e.getMessage());
            return false;
        } finally {
            if (base_dados != null && base_dados.isOpen()) {
                base_dados.close(); // Fecha o banco de dados para liberar recursos
            }
        }
    }

    /**
     * Este método insere uma nova escola no banco
     * @param nomeEscola parametro esperado como nome da escola a ser cadastrada
     * @param bairro parametro esperado contendo o bairro da escola
     * @param status paramentro indicativo que a escola sera ativa
     * @return retorna true se a inserção for bem sucedida ou falso, caso contrario
     */
    public Boolean cadastrarEscola(String nomeEscola, String bairro, Integer status){
        SQLiteDatabase base_dados = null;
        try {
            base_dados = this.getWritableDatabase();
            ContentValues contentValues = new ContentValues();
            contentValues.put("nomeEscola", nomeEscola);
            contentValues.put("bairro", bairro);
            contentValues.put("status", status);
            contentValues.put("id_usuario", BancoDados.USER_ID);
            long inserir = base_dados.insert("escola", null, contentValues);
            return inserir != -1;
        } catch (Exception e){
            Log.e("kariti", e.getMessage());
            return false;
        }finally {
            if(base_dados != null && base_dados.isOpen()){
                base_dados.close();
            }
        }

    }
    public Boolean cadastrarTurma(String nomeTurma, Integer an){
        SQLiteDatabase base_dados = null;
        try {
            base_dados = this.getWritableDatabase();
            ContentValues contentValues = new ContentValues();
            contentValues.put("nomeTurma", nomeTurma);
            contentValues.put("qtdAnonimos", an);
            contentValues.put("id_escola", BancoDados.ID_ESCOLA);
            long inserir = base_dados.insert("turma", null, contentValues);
            return inserir != -1;
        } catch (Exception e){
            Log.e("kariti", e.getMessage());
            return false;
        } finally {
            if(base_dados != null && base_dados.isOpen()){
                base_dados.close();
            }
        }
    }
    public Boolean cadastrarAlunoNaTurma(Integer id_turma, Integer id_aluno){
        SQLiteDatabase base_dados = null;
        try {
            base_dados = this.getWritableDatabase();
            ContentValues contentValues = new ContentValues();
            contentValues.put("id_turma", id_turma);
            contentValues.put("id_aluno", id_aluno);
            long inserir = base_dados.insert("alunosTurma", null, contentValues);
            return inserir != -1;
        }catch (Exception e){
            Log.e("kariti", e.getMessage());
            return false;
        }finally {
            if (base_dados != null && base_dados.isOpen()){
                base_dados.close();
            }
        }
    }
    public Boolean cadastrarCorrecao(Integer id_prova, Integer id_aluno, Integer questao, Integer respostaDada){
        SQLiteDatabase base_dados = null;
        try {
            base_dados = this.getWritableDatabase();
            ContentValues contentValues = new ContentValues();
            contentValues.put("id_prova", id_prova);
            contentValues.put("id_aluno", id_aluno);
            contentValues.put("questao", questao);
            contentValues.put("respostaDada", respostaDada);
            long resultado = base_dados.insert("resultadoCorrecao", null, contentValues);
            if(resultado != -1){
                Log.e("kariti", "Resultado de correção cadastrado com sucesso");
                return true;
            }else{
                Log.e("kariti", "Erro ao tentar inserir resultado de correção no banco");
                return false;
            }
        }catch (Exception e){
            Log.e("kariti", "Erro ao tentar inserir resultado de correção no banco"+e.getMessage());
            return false;
        }finally {
            if (base_dados != null && base_dados.isOpen()) {
                base_dados.close();
            }
        }
    }
    public Integer cadastrarProva(String nomeProva, String dataProva, Integer qtdQuestoes, Integer qtdAlternativas, Integer id_turma){
        SQLiteDatabase base_dados = null;
        Integer id_prova = null;
        try {
            base_dados = this.getWritableDatabase();
            ContentValues contentValues = new ContentValues();
            contentValues.put("nomeProva", nomeProva);
            contentValues.put("dataProva", dataProva);
            contentValues.put("qtdQuestoes", qtdQuestoes);
            contentValues.put("qtdAlternativas", qtdAlternativas);
            contentValues.put("id_escola", BancoDados.ID_ESCOLA); // Excluir posteriormente
            contentValues.put("id_turma", id_turma);
            long inserir = base_dados.insert("prova", null, contentValues);
            id_prova = Math.toIntExact(inserir); // Alterar tipo de variavel de Id
        }catch (Exception e){
            Log.e("kariti", e.getMessage());
            return null;
        }finally {
            if (base_dados != null && base_dados.isOpen()) {
                base_dados.close();
            }
        }
        return id_prova;
    }
    public Boolean cadastrarGabarito(Integer id_prova, Integer questao, Integer resposta, Float nota){
        SQLiteDatabase base_dados = null;
        try {
            base_dados = this.getWritableDatabase();
            ContentValues contentValues = new ContentValues();
            contentValues.put("id_prova", id_prova);
            contentValues.put("questao", questao);
            contentValues.put("resposta", resposta);
            contentValues.put("nota", nota);
            long inserir = base_dados.insert("gabarito", null, contentValues);
            return inserir != -1;
        }catch (Exception e){
            Log.e("kariti", e.getMessage());
            return false;
        }finally {
            if (base_dados != null && base_dados.isOpen()){
                base_dados.close();
            }
        }
    }
    public Integer cadastrarAluno(String nomeAluno, String email, Integer status){
        SQLiteDatabase base_dados = null;
        try {
            base_dados = this.getWritableDatabase();
            ContentValues contentValues = new ContentValues();
            contentValues.put("nomeAluno", nomeAluno);
            contentValues.put("email", email);
            contentValues.put("status", status);
            contentValues.put("id_usuario", BancoDados.USER_ID);
            long inserir = base_dados.insert("aluno", null, contentValues);
            if(inserir != -1){
                return Math.toIntExact(inserir);
            }else{
                return -1;
            }
        }catch (Exception e){
            Log.e("kariti","Erro: aluno nao cadastrado!"+e.getMessage());
            return -1;
        }finally {
            if(base_dados != null && base_dados.isOpen()){
                base_dados.close();
            }
        }
    }
    /**
     * Este método deleta uma escola do banco
     * @param id_escola parametro contendo o id da prova que se deseja deletar
     * @return retorna o resultado da execução
     */
    public Boolean deletarEscola(Integer id_escola){
        SQLiteDatabase base_dados = null;
        SQLiteStatement stmt = null;
        try {
            base_dados = this.getWritableDatabase();
            String deleta = "DELETE FROM escola WHERE id_escola = ?";
            stmt = base_dados.compileStatement(deleta);
            stmt.bindLong(1, id_escola);
            stmt.executeUpdateDelete();
            return true;
        }catch (Exception e){
            Log.e("kariti","Erro ao tentar deletar escola!"+e.getMessage());
            return false;
        }finally {
            if(base_dados != null){
                base_dados.close();
            }
            if(stmt != null){
                stmt.close();
            }
        }
    }
    public Boolean deletarAluno(Integer id_aluno){
        SQLiteDatabase base_dados = null;
        SQLiteStatement stmt = null;
        try {
            base_dados = this.getWritableDatabase();
            String deleta = "DELETE FROM aluno WHERE id_aluno = ?";
            stmt = base_dados.compileStatement(deleta);
            stmt.bindLong(1, id_aluno);
            stmt.executeUpdateDelete();
            return true;
        }catch (Exception e){
           Log.e("kariti","Erro ao tentar deletar aluno"+e.getMessage());
           return false;
        }finally {
            if (base_dados != null && base_dados.isOpen()){
                base_dados.close();
            }
            if (stmt != null){
                stmt.close();
            }
        }
    }
    public Boolean deletarTurma(Integer id_turma){
        SQLiteDatabase base_dados = null;
        SQLiteStatement stmt = null;
        try {
            base_dados = this.getWritableDatabase();
            String deleta = "DELETE FROM turma WHERE id_turma = ?";
            stmt = base_dados.compileStatement(deleta);
            stmt.bindLong(1, id_turma);
            stmt.executeUpdateDelete();
            return true;
        }catch (Exception e){
            Log.e("kariti","Erro ao tentar deletar turma! "+e.getMessage());
            return false;
        }finally {
            if (base_dados != null && base_dados.isOpen()){
                base_dados.close();
            }
            if (stmt != null){
                stmt.close();
            }
        }
    }
    public Boolean deletarAlunoDeTurma(Integer id_turma){
        SQLiteDatabase base_dados = null;
        SQLiteStatement stmt = null;
        try {
            base_dados = this.getWritableDatabase();
            String deleta = "DELETE FROM alunosTurma WHERE id_turma = ?";
            stmt = base_dados.compileStatement(deleta);
            stmt.bindLong(1, id_turma);
            stmt.executeUpdateDelete();
            return true;
        }catch (Exception e){
            Log.e("kariti","Erro ao tentar deletar aluno da turma! "+e.getMessage());
            return false;
        }finally {
            if (base_dados != null && base_dados.isOpen()){
                base_dados.close();
            }
            if (stmt != null){
                stmt.close();
            }
        }
    }
    public Boolean deletarGabarito(Integer id_prova){
        SQLiteDatabase base_dados = null;
        SQLiteStatement stmt = null;
        try {
            base_dados = this.getWritableDatabase();
            String deleta = "DELETE FROM gabarito WHERE id_prova = ?";
            stmt = base_dados.compileStatement(deleta);
            stmt.bindLong(1, id_prova);
            stmt.executeUpdateDelete();
            return true;
        }catch (Exception e){
            Log.e("kariti","Erro ao tentar deletar gabarito! "+e.getMessage());
            return false;
        }finally {
            if (base_dados != null && base_dados.isOpen()){
                base_dados.close();
            }
            if (stmt != null){
                stmt.close();
            }
        }
    }
    public Boolean deletarProva(Integer id_prova){
        SQLiteDatabase base_dados = null;
        SQLiteStatement stmt = null;
        try {
            base_dados = this.getWritableDatabase();
            String deleta = "DELETE FROM prova WHERE id_prova = ?";
            stmt = base_dados.compileStatement(deleta);
            stmt.bindLong(1, id_prova);
            stmt.executeUpdateDelete();
            return true;
        }catch (Exception e){
            Log.e("kariti","Erro ao tentar deletar Prova! "+e.getMessage());
            return false;
        }finally {
            if (base_dados != null && base_dados.isOpen()){
                base_dados.close();
            }
            if (stmt != null){
                stmt.close();
            }
        }
    }
    public Boolean deletarCorrecao(Integer id_prova){
        SQLiteDatabase base_dados = null;
        SQLiteStatement stmt = null;
        try {
            base_dados = this.getWritableDatabase();
            String deleta = "DELETE FROM resultadoCorrecao WHERE id_prova = ?";
            stmt = base_dados.compileStatement(deleta);
            stmt.bindLong(1, id_prova);
            stmt.executeUpdateDelete();
            return true;
        }catch (Exception e){
            Log.e("kariti","Erro ao tentar deletar correção! "+e.getMessage());
            return false;
        }finally {
            if (base_dados != null && base_dados.isOpen()){
                base_dados.close();
            }
            if (stmt != null){
                stmt.close();
            }
        }
    }
    public Boolean deletarCorrecaoPorAluno(Integer id_prova, Integer id_aluno) {
        SQLiteDatabase base_dados = null;
        try {
            base_dados = this.getWritableDatabase();
            base_dados.delete("resultadoCorrecao", "id_prova = ? AND id_aluno = ?", new String[]{String.valueOf(id_prova), String.valueOf(id_aluno)});
            return true;
        } catch (Exception e){
            Log.e("kariti", "Erro ao tentar deletar correção do aluno");
            return false;
        } finally {
            if (base_dados != null && base_dados.isOpen()) {
                base_dados.close();
            }
        }
    }

    public Boolean deletarAnonimos(Integer id_turma){
        SQLiteDatabase base_dados = null;
        SQLiteStatement stmt = null;
        try {
            base_dados = this.getWritableDatabase();
            String deleta = "DELETE FROM aluno WHERE status = ? and id_aluno in (select id_aluno FROM alunosTurma WHERE id_turma = ?)";
            stmt = base_dados.compileStatement(deleta);
            stmt.bindLong(1, 0);
            stmt.bindLong(2, id_turma);
            stmt.executeUpdateDelete();
            return true;
        }catch (Exception e){
            Log.e("kariti","Erro ao tentar deletar aluno anonimo! "+e.getMessage());
            return false;
        }finally {
            if (base_dados != null && base_dados.isOpen()){
                base_dados.close();
            }
            if (stmt != null){
                stmt.close();
            }
        }
    }
    /**
     * Este método altera a senha do usuario no banco
     * @param password parâmetro esperado para substituir a senha antiga
     * @param id_usuario parâmetro esperado para determinar de qual usuário se deseja alterar a senha
     * @return retorna true em caso de sucesso e false caso contrário
     */
    public Boolean alterarSenha(String password, Integer id_usuario){
        SQLiteDatabase base_dados = null;
        SQLiteStatement stmt = null;
        try {
            base_dados = this.getWritableDatabase();
            String altera = "UPDATE usuario SET password = ? WHERE id_usuario = ?";
            stmt = base_dados.compileStatement(altera);
            stmt.bindString(1, to256(password));
            stmt.bindLong(2, id_usuario);
            stmt.executeUpdateDelete();
            return true;
        }catch (Exception e){
            Log.e("Kariti","Erro ao tentar alterar senha! "+e.getMessage());
            return false;
        }finally {
            if (base_dados != null && base_dados.isOpen()) {
                base_dados.close();
            }
            if (stmt != null) {
                stmt.close();
            }
        }
    }
    public Boolean alterarDadosTurma(String turma, Integer qtdAnonimos, Integer id_turma){
        SQLiteDatabase base_dados = null;
        SQLiteStatement stmt = null;
        try {
            base_dados = this.getWritableDatabase();
            String altera = "UPDATE turma SET nomeTurma=?, qtdAnonimos = ? WHERE id_turma=?";
            stmt = base_dados.compileStatement(altera);
            stmt.bindString(1, turma);
            stmt.bindLong(2, qtdAnonimos);
            stmt.bindLong(3, id_turma);
            stmt.executeUpdateDelete();
            return true;
        }catch (Exception e){
            Log.e("Kariti","Erro ao tentar alterar Turma! "+e.getMessage());
            return false;
        }finally {
            if (base_dados != null && base_dados.isOpen()) {
                base_dados.close();
            }
            if (stmt != null) {
                stmt.close();
            }
        }
    }
    public Boolean alterarDadosProva(Integer id_prova, String nomeProva, String dataProva, Integer id_turma, Integer questoes, Integer alternativas){
        SQLiteDatabase base_dados = null;
        SQLiteStatement stmt = null;
        try {
            base_dados = this.getWritableDatabase();
            String altera = "UPDATE prova SET nomeProva = ?, dataProva = ?, qtdQuestoes = ?, qtdAlternativas = ?, id_turma = ?  WHERE id_prova = ?";
            stmt = base_dados.compileStatement(altera);
            stmt.bindString(1, nomeProva);
            stmt.bindString(2, dataProva);
            stmt.bindLong(3, questoes);
            stmt.bindLong(4, alternativas);
            stmt.bindLong(5, id_turma);
            stmt.bindLong(6, id_prova);
            stmt.executeUpdateDelete();
            return true;
        }catch (Exception e){
            Log.e("Kariti","Erro ao tentar alterar dados da prova com id; "+id_prova);
            return false;
        }finally {
            if (base_dados != null && base_dados.isOpen()) {
                base_dados.close();
            }
            if (stmt != null) {
                stmt.close();
            }
        }
    }
    public Boolean AlterarDadosCorrecao(Integer id_prova, Integer id_aluno, Integer questao, Integer respostaDada){
        SQLiteDatabase base_dados = null;
        SQLiteStatement stmt = null;
        try {
            base_dados = this.getWritableDatabase();
            String altera = "UPDATE resultadoCorrecao SET respostaDada = ? WHERE id_prova = ? and id_aluno = ? and questao = ?";
            stmt = base_dados.compileStatement(altera);
            stmt.bindLong(1, respostaDada);
            stmt.bindLong(2, id_prova);
            stmt.bindLong(3, id_aluno);
            stmt.bindLong(4, questao);
            stmt.executeUpdateDelete();
            return true;
        }catch (Exception e){
            Log.e("Kariti", "Erro ao tentar alterar resposta da questao: "+questao);
            return false;
        }finally {
            if (base_dados != null && base_dados.isOpen()) {
                base_dados.close();
            }
            if(stmt != null){
                stmt.close();
            }
        }
    }
    public Boolean alterarDadosAluno(String nomeAluno, String email, Integer id_aluno){
        SQLiteDatabase base_dados = null;
        SQLiteStatement stmt = null;
        try {
            base_dados = this.getWritableDatabase();
            String altera = "UPDATE aluno SET nomeAluno=?, email=? WHERE id_aluno=?";
            stmt = base_dados.compileStatement(altera);
            stmt.bindString(1, nomeAluno);
            stmt.bindString(2, email);
            stmt.bindLong(3, id_aluno);
            stmt.executeUpdateDelete();
            return true;
        }catch (Exception e){
            Log.e("Kariti", "Erro ao tentar alterar dados do aluno com id: "+id_aluno);
            return false;
        }finally {
            if(base_dados != null){
                base_dados.close();
            }
            if(stmt != null){
                stmt.close();
            }
        }
    }

    /**
     * Este método altera o status da escola para ativa ou desativada.
     * @param id_escola parametro usado para determinar qual escola sera realizada a ação
     * @param status parametro de identificação que determina se a escola será ativada(1) ou desativada(0).
     * @return retorna true se execução bem sucedida e false caso contrário
     */
    public Boolean alterarStatusEscola(Integer id_escola, Integer status){
        SQLiteDatabase base_dados = null;
        SQLiteStatement stmt = null;
        try {
            base_dados = this.getWritableDatabase();
            String altera = "UPDATE escola SET status = ? WHERE id_escola = ?";
            stmt = base_dados.compileStatement(altera);
            stmt.bindLong(1, status);
            stmt.bindLong(2, id_escola);
            stmt.executeUpdateDelete();
            return true;
        }catch (Exception e){
            Log.e("Kariti", "Erro ao tentar alterar o status da escola com id: "+id_escola);
            return false;
        }finally {
            if(base_dados != null && base_dados.isOpen()){
                base_dados.close();
            }
            if(stmt != null){
                stmt.close();
            }
        }
    }
    /**
     * Este método verifica se determinado email está cadstrado no banco de dados
     * @param email parâmetro usado para verificar se existe o email no banco
     * @return retorna o id do usuario, caso exista o email e null caso contrário
     */
    public Integer verificaExisteEmail(String email) {
        SQLiteDatabase base_dados = null;
        Cursor cursor = null;
        Integer id_usuario = null;
        try {
            base_dados = this.getWritableDatabase();
            cursor = base_dados.rawQuery("SELECT id_usuario FROM usuario WHERE email = ?", new String[]{email});
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                id_usuario = cursor.getInt(0);
            }
        }catch (Exception e){
            Log.e("kariti","Erro ao tentar realizar consulta de e-mail! "+e.getMessage());
            return -1;
        }finally {
            if(base_dados != null && base_dados.isOpen()){
                base_dados.close();
            }
            if(cursor != null){
                cursor.close();
            }
        }
        return id_usuario;
    }
    public Boolean verificaExisteEmailAluno(String email) {
        SQLiteDatabase base_dados = null;
        Cursor cursor = null;
        try {
            base_dados = this.getReadableDatabase();
            cursor = base_dados.rawQuery("SELECT email FROM aluno WHERE email = ? AND id_usuario = ?", new String[]{email, BancoDados.USER_ID.toString()});
            return cursor != null && cursor.moveToFirst();
        }catch (Exception e){
            Log.e("kariti","Erro na vericação do email na tabela aluno: "+e.getMessage());
            return null;
        }finally {
            if(base_dados != null && base_dados.isOpen()){
                base_dados.close();
            }
            if (cursor != null){
                cursor.close();
            }
        }

    }
    public Boolean verificaExisteCorrecao(String id_prova){
        SQLiteDatabase base_dados = null;
        Cursor cursor = null;
        try {
            base_dados = this.getReadableDatabase();
            cursor = base_dados.rawQuery("SELECT id_prova FROM resultadoCorrecao WHERE id_prova = ?", new String[]{id_prova});
            return cursor != null && cursor.moveToFirst();
        }catch (Exception e){
            Log.e("kariti","Erro ao tentar verificar existencia de prova no banco! "+e.getMessage());
            return null;
        } finally {
            if(base_dados != null && base_dados.isOpen()){
                base_dados.close();
            }
            if(cursor != null){
                cursor.close();
            }
        }

    }
    public Boolean verificaExisteCorrecaoAluno(Integer id_prova, Integer id_aluno) {
        SQLiteDatabase base_dados = null;
        Cursor cursor = null;
        try{
            base_dados = this.getReadableDatabase();
            cursor = base_dados.rawQuery("SELECT id_prova FROM resultadoCorrecao WHERE id_prova = ? AND id_aluno = ?", new String[]{id_prova.toString(), id_aluno.toString()});
            return cursor != null && cursor.moveToFirst();
        }catch (Exception e){
            Log.e("kariti","Erro ao tentar consultar se a prova do aluno esta corrigida! "+e.getMessage());
            return null;
        }finally{
            if(base_dados != null && base_dados.isOpen()){
                base_dados.close();
            }
            if (cursor != null){
                cursor.close();
            }
        }
    }

    /**
     * Este método verifica se o email e senha informado pelo usuário são validos
     * @param email parameto usado para vericar se existe no banco
     * @param password parametro usado para analisa se pertence ao email informado
     * @return retorna o id do usuario caso os dados de autenticação sejam validos ou null caso contrário
     */
    public Integer verificaAutenticacao(String email, String password){
        SQLiteDatabase base_dados = null;
        Cursor cursor = null;
        Integer id_usuario = null;
        try {
            base_dados = this.getWritableDatabase();
            cursor = base_dados.rawQuery("SELECT id_usuario FROM usuario WHERE email = ? AND password = ?", new String[] {email, to256(password)});
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                id_usuario = cursor.getInt(0);
            }
        }catch (Exception e){
            Log.e("kariti","Erro de verificação de autenticação! "+e.getMessage());
            return -1;
        }finally {
            if(base_dados != null && base_dados.isOpen()){
                base_dados.close();
            }
            if(cursor != null){
                cursor.close();
            }
        }
        return id_usuario;
    }

    /**
     * Este método verifica se existe um determinada escola cadastrada no banco
     * @param nomeEscola parametro usado para saber qual escola esta sendo pesquisada
     * @return restorna true se a escola já estiver cadastrada ou false caso contrario
     */
    public Boolean verificaExisteEscola(String nomeEscola){
        SQLiteDatabase base_dados = null;
        Cursor cursor = null;
        try {
            base_dados = this.getReadableDatabase();
            cursor = base_dados.rawQuery("SELECT nomeEscola FROM escola WHERE nomeEscola = ? AND id_usuario = ?", new String[]{nomeEscola, BancoDados.USER_ID.toString()});
            return cursor != null && cursor.moveToFirst();
        } catch (Exception e){
            Log.e("kariti","Erro ao tentar verificar existencia de escola no banco! "+e.getMessage());
            return null;
        } finally {
            if(base_dados != null && base_dados.isOpen()){
                base_dados.close();
            }
            if(cursor != null){
                cursor.close();
            }
        }
    }

    public Boolean verificaExisteProvaPNome(String nomeProva, String id_turma) {
        SQLiteDatabase base_dados = null;
        Cursor cursor = null;
        try {
            base_dados = this.getWritableDatabase();
            cursor = base_dados.rawQuery("SELECT nomeProva FROM prova WHERE nomeProva = ? and id_turma = ?", new String[]{nomeProva, id_turma});
            return cursor != null && cursor.moveToFirst();
        }catch (Exception e){
            Log.e("kariti","Erro ao tentar verificar existencia de prova no banco! "+e.getMessage());
            return null;
        } finally {
            if(base_dados != null && base_dados.isOpen()){
                base_dados.close();
            }
            if(cursor != null){
                cursor.close();
            }
        }
    }

    public Boolean verificaExisteProvaPId(String id_prova){
        SQLiteDatabase base_dados = null;
        Cursor cursor = null;
        try {
            base_dados = this.getReadableDatabase();
            cursor = base_dados.rawQuery("SELECT id_prova FROM prova WHERE id_prova = ? AND id_escola = ?", new String[]{id_prova, String.valueOf(BancoDados.ID_ESCOLA)});
            return cursor != null && cursor.moveToFirst();
        }catch (Exception e){
            Log.e("kariti","Erro ao tentar verificar existencia de prova por id no banco! "+e.getMessage());
            return null;
        } finally {
            if(base_dados != null && base_dados.isOpen()){
                base_dados.close();
            }
            if(cursor != null){
                cursor.close();
            }
        }

    }
    public Boolean verificaExisteProvaCadastrada(){
        SQLiteDatabase base_dados = null;
        Cursor cursor = null;
        try {
            base_dados = this.getReadableDatabase();
            cursor = base_dados.rawQuery("SELECT id_prova FROM prova WHERE id_escola = ?", new String[]{BancoDados.ID_ESCOLA.toString()});
            return cursor != null && cursor.moveToFirst();
        }catch (Exception e){
            Log.e("kariti","Erro ao tentar verificar existencia de provas cadastradas no banco! "+e.getMessage());
            return null;
        } finally {
            if(base_dados != null && base_dados.isOpen()){
                base_dados.close();
            }
            if(cursor != null){
                cursor.close();
            }
        }
    }
    public Boolean verificaExisteProvaCorrigida(){
        SQLiteDatabase base_dados = null;
        Cursor cursor = null;
        try {
            base_dados = this.getReadableDatabase();
            cursor = base_dados.rawQuery("SELECT id_prova FROM prova WHERE id_escola = ? AND id_prova IN (SELECT id_prova FROM resultadoCorrecao)", new String[]{BancoDados.ID_ESCOLA.toString()});
            return cursor != null && cursor.moveToFirst();
        }catch (Exception e){
            Log.e("kariti","Erro ao tentar verificar existencia de provas corrigidas! "+e.getMessage());
            return null;
        } finally {
            if(base_dados != null && base_dados.isOpen()){
                base_dados.close();
            }
            if(cursor != null){
                cursor.close();
            }
        }
    }

    public Boolean verificaSituacaoCorrecao(Integer id_prova, Integer id_aluno, Integer estado){
        SQLiteDatabase base_dados = null;
        Cursor cursor = null;
        try {
            base_dados = this.getReadableDatabase();
            cursor = base_dados.rawQuery("SELECT questao FROM resultadoCorrecao WHERE id_prova = ? AND id_aluno = ? AND questao = ?", new String[]{id_prova.toString(), id_aluno.toString(), estado.toString()});
            return cursor != null && cursor.moveToFirst();
        }catch (Exception e){
            Log.e("kariti","Erro ao tentar verificar situaçao de correção por aluno! "+e.getMessage());
            return null;
        } finally {
            if(base_dados != null && base_dados.isOpen()){
                base_dados.close();
            }
            if(cursor != null){
                cursor.close();
            }
        }
    }

    public Boolean verificaExisteAlunoPNome(String nome){
        SQLiteDatabase base_dados = null;
        Cursor cursor = null;
        try {
            base_dados = this.getReadableDatabase();
            cursor = base_dados.rawQuery("SELECT nomeAluno FROM aluno WHERE nomeAluno = ? AND id_usuario = ?", new String[]{nome, BancoDados.USER_ID.toString()});
            return cursor != null && cursor.moveToFirst();
        }catch (Exception e){
            Log.e("kariti","Erro ao tentar verificar existencia de aluno! "+e.getMessage());
            return null;
        } finally {
            if(base_dados != null && base_dados.isOpen()){
                base_dados.close();
            }
            if(cursor != null){
                cursor.close();
            }
        }
    }

    public Boolean verificaExisteTurma(String turma){
        SQLiteDatabase base_dados = null;
        Cursor cursor = null;
        try {
            base_dados = this.getReadableDatabase();
            cursor = base_dados.rawQuery("SELECT nomeTurma FROM turma WHERE nomeTurma = ? and id_escola = ?", new String[]{turma, String.valueOf(BancoDados.ID_ESCOLA)});
            return cursor != null && cursor.moveToFirst();
        }catch (Exception e){
            Log.e("kariti","Erro ao tentar verificar existencia de turma! "+e.getMessage());
            return null;
        } finally {
            if(base_dados != null && base_dados.isOpen()){
                base_dados.close();
            }
            if(cursor != null){
                cursor.close();
            }
        }

    }

    public Boolean verificaExisteTurmaEmProva(Integer id_turma){
        SQLiteDatabase base_dados = null;
        Cursor cursor = null;
        try {
            base_dados = this.getReadableDatabase();
            cursor = base_dados.rawQuery("SELECT id_turma FROM prova WHERE id_turma = ? AND id_escola = ?", new String[]{String.valueOf(id_turma), String.valueOf(BancoDados.ID_ESCOLA)});
            return cursor != null && cursor.moveToFirst();
        }catch (Exception e){
            Log.e("kariti","Erro ao tentar verificar existencia de turma em prova! "+e.getMessage());
            return null;
        } finally {
            if(base_dados != null && base_dados.isOpen()){
                base_dados.close();
            }
            if(cursor != null){
                cursor.close();
            }
        }

    }
    public Boolean verificaExisteAlunoEmTurma(Integer id_aluno){
        SQLiteDatabase base_dados = null;
        Cursor cursor = null;
        try {
            base_dados = this.getReadableDatabase();
            cursor = base_dados.rawQuery("SELECT id_aluno FROM alunosTurma WHERE id_aluno = ?", new String[]{String.valueOf(id_aluno)});
            return cursor != null && cursor.moveToFirst();
        }catch (Exception e){
            Log.e("kariti","Erro ao tentar verificar existencia de aluno em turma! "+e.getMessage());
            return null;
        } finally {
            if(base_dados != null && base_dados.isOpen()){
                base_dados.close();
            }
            if(cursor != null){
                cursor.close();
            }
        }
    }
    public String pegarNomeUsuario(Integer id_usuario) {
        SQLiteDatabase base_dados = null;
        Cursor cursor = null;
        String nomeUsuario = null;
        try {
            base_dados = this.getReadableDatabase();
            cursor = base_dados.rawQuery("SELECT nomeUsuario FROM usuario WHERE id_usuario = ?", new String[]{id_usuario.toString()});
            if (cursor != null && cursor.moveToFirst()){
                nomeUsuario = cursor.getString(0);
            }
        }catch (Exception e){
            Log.e("kariti","Erro ao tentar pegar nome de usuario! "+e.getMessage());
            return null;
        } finally {
            if(base_dados != null && base_dados.isOpen()){
                base_dados.close();
            }
            if(cursor != null){
                cursor.close();
            }
        }
        return nomeUsuario;
    }
    public Integer pegarRespostaDadaQuestao(Integer id_prova, Integer id_aluno, Integer questao) {
        SQLiteDatabase base_dados = null;
        Cursor cursor = null;
        Integer respostaDada = null;
        try {
            base_dados = this.getWritableDatabase();
            cursor = base_dados.rawQuery("SELECT respostaDada FROM resultadoCorrecao WHERE id_prova = ? AND id_aluno = ? AND questao = ?", new String[]{id_prova.toString(), id_aluno.toString(), questao.toString()});
            if (cursor != null && cursor.moveToFirst()) {
                respostaDada = cursor.getInt(0);
            }
        } catch (Exception e){
            Log.e("kariti","Erro ao tentar pegar resposta dada da questao! "+e.getMessage());
            return null;
        } finally {
            if(base_dados != null && base_dados.isOpen()){
                base_dados.close();
            }
            if(cursor != null){
                cursor.close();
            }
        }
        return respostaDada;
    }

    public Integer pegarRespostaQuestaoGabarito(Integer id_prova, Integer questao) {
        SQLiteDatabase base_dados = null;
        Cursor cursor = null;
        Integer respostaGabarito = null;
        try {
            base_dados = this.getReadableDatabase();
            cursor = base_dados.rawQuery("Select resposta from gabarito where id_prova = ? and questao = ?", new String[]{id_prova.toString(), questao.toString()});
            if (cursor != null && cursor.moveToFirst()){
                respostaGabarito = cursor.getInt(0);
            }
        }catch (Exception e){
            Log.e("kariti","Erro ao tentar pegar resposta do gabarito por questao! "+e.getMessage());
            return null;
        } finally {
            if(base_dados != null && base_dados.isOpen()){
                base_dados.close();
            }
            if(cursor != null){
                cursor.close();
            }
        }
        return respostaGabarito;

    }
    public Float pegaNotaQuestao(Integer id_prova, Integer questao) {
        SQLiteDatabase base_dados = null;
        Cursor cursor = null;
        Float notaQuestao = null;
        try {
            base_dados = this.getWritableDatabase();
            cursor = base_dados.rawQuery("SELECT nota FROM gabarito WHERE id_prova = ? AND questao = ?", new String[]{id_prova.toString(), questao.toString()});
            if (cursor != null && cursor.moveToFirst()){
                notaQuestao = cursor.getFloat(0);
            }
        }catch (Exception e){
            Log.e("kariti","Erro ao tentar pegar nota da questao! "+e.getMessage());
            return null;
        } finally {
            if(base_dados != null && base_dados.isOpen()){
                base_dados.close();
            }
            if(cursor != null){
                cursor.close();
            }
        }
        return notaQuestao;
    }
    public String pegarNomeTurma(String id_turma) {
        SQLiteDatabase base_dados = null;
        Cursor cursor = null;
        String nomeTurma = null;
        try {
            base_dados = this.getWritableDatabase();
            cursor = base_dados.rawQuery("SELECT nomeTurma FROM turma WHERE id_turma = ? AND id_escola = ?", new String[]{id_turma, BancoDados.ID_ESCOLA.toString()});
            if (cursor != null && cursor.moveToFirst()){
                nomeTurma = cursor.getString(0);
            }
        }catch (Exception e){
            Log.e("kariti","Erro ao tentar pegar nome da turma! "+e.getMessage());
            return null;
        } finally {
            if(base_dados != null && base_dados.isOpen()){
                base_dados.close();
            }
            if(cursor != null){
                cursor.close();
            }
        }
        return nomeTurma;

    }
    public String pegaNomeAlunoPStatus(String id_aluno, Integer status) {
        SQLiteDatabase base_dados = null;
        Cursor cursor = null;
        String nomeAluno = null;
        try {
            base_dados = this.getReadableDatabase();
            cursor = base_dados.rawQuery("SELECT nomeAluno FROM aluno WHERE id_aluno = ? AND id_usuario = ? AND status = ?", new String[]{id_aluno, BancoDados.USER_ID.toString(), status.toString()});
            if (cursor != null && cursor.moveToFirst()){
                nomeAluno = cursor.getString(0);
            }
        }catch (Exception e){
            Log.e("kariti","Erro ao tentar pegar nome do aluno! "+e.getMessage());
            return null;
        } finally {
            if(base_dados != null && base_dados.isOpen()){
                base_dados.close();
            }
            if(cursor != null){
                cursor.close();
            }
        }
        return nomeAluno;

    }
    public String pegaNomeAluno(Integer id_aluno) {
        SQLiteDatabase base_dados = null;
        Cursor cursor = null;
        String nomeAluno = null;
        try {
            base_dados = this.getReadableDatabase();
            cursor = base_dados.rawQuery("SELECT nomeAluno FROM aluno WHERE id_aluno = ? AND id_usuario = ?", new String[]{id_aluno.toString(), BancoDados.USER_ID.toString()});
            if (cursor != null && cursor.moveToFirst()){
                nomeAluno = cursor.getString(0);
            }
        }catch (Exception e){
            Log.e("kariti","Erro ao tentar pegar nome do aluno! "+e.getMessage());
            return null;
        } finally {
            if(base_dados != null && base_dados.isOpen()){
                base_dados.close();
            }
            if(cursor != null){
                cursor.close();
            }
        }
        return nomeAluno;
    }
    
    public String pegaDataProva(String id_prova) {
        SQLiteDatabase base_dados = null;
        Cursor cursor = null;
        String dataProva = null;
        try {
            base_dados = this.getReadableDatabase();
            cursor = base_dados.rawQuery("SELECT dataProva FROM prova WHERE id_prova = ?", new String[]{id_prova});
            if (cursor != null && cursor.moveToFirst()){
                dataProva = cursor.getString(0);
            }
        }catch (Exception e){
            Log.e("kariti","Erro ao tentar pegar data da Prova! "+e.getMessage());
            return null;
        } finally {
            if(base_dados != null && base_dados.isOpen()){
                base_dados.close();
            }
            if(cursor != null){
                cursor.close();
            }
        }
        return dataProva;
    }
    public Integer pegarIdAluno(String nomeAluno) {
        SQLiteDatabase base_dados = null;
        Cursor cursor = null;
        Integer id_aluno = null;
        try {
            base_dados = this.getReadableDatabase();
            cursor = base_dados.rawQuery("SELECT id_aluno FROM aluno WHERE nomeAluno = ? AND id_usuario = ?", new String[]{nomeAluno, BancoDados.USER_ID.toString()});
            if (cursor != null && cursor.moveToFirst()) {
                id_aluno = cursor.getInt(0);
            }
        } catch (Exception e){
            Log.e("kariti","Erro ao tentar pegar id do aluno! "+e.getMessage());
            return null;
        } finally {
            if(base_dados != null && base_dados.isOpen()){
                base_dados.close();
            }
            if(cursor != null){
                cursor.close();
            }
        }
        return id_aluno;        
    }

    /**
     * Este método pega id de uma determinda escola
     * @param nomeEscola parâmetro usado para identificar o id de qual escola esta sendo solicitado.
     * @return retorna o id da escola do tipo inteiro
     */
    public Integer pegarIdEscola(String nomeEscola) {
        SQLiteDatabase base_dados = null;
        Cursor cursor = null;
        Integer id_escola = null;
        try {
            base_dados = this.getReadableDatabase();
            cursor = base_dados.rawQuery("SELECT id_escola FROM escola WHERE nomeEscola = ? AND id_usuario = ?", new String[]{nomeEscola, BancoDados.USER_ID.toString()});
            if (cursor != null && cursor.moveToFirst()) {
                id_escola = cursor.getInt(0);
            }
        }catch (Exception e){
            Log.e("kariti","Erro ao tentar pegar id da escola! "+e.getMessage());
            return null;
        } finally {
            if(base_dados != null && base_dados.isOpen()){
                base_dados.close();
            }
            if(cursor != null){
                cursor.close();
            }
        }
        return id_escola;
    }
    public Integer pegarIdProva(String nomeProva, Integer id_turma) {
        SQLiteDatabase base_dados = null;
        Cursor cursor = null;
        Integer id_prova = null;
        try {
            base_dados = this.getReadableDatabase();
            cursor = base_dados.rawQuery("SELECT id_prova FROM prova WHERE nomeProva = ? AND id_turma = ?", new String[]{nomeProva, id_turma.toString()});
            if (cursor != null && cursor.moveToFirst()){
                id_prova = cursor.getInt(0);
            }
        }catch (Exception e){
            Log.e("kariti","Erro ao tentar pegar id da escola! "+e.getMessage());
            return null;
        } finally {
            if(base_dados != null && base_dados.isOpen()){
                base_dados.close();
            }
            if(cursor != null){
                cursor.close();
            }
        }
        return id_prova;

    }
    public Integer pegarIdTurma(String nomeTurma) {
        SQLiteDatabase base_dados = null;
        Cursor cursor = null;
        Integer id_turma = null;
        try {
            base_dados = this.getReadableDatabase();
            cursor = base_dados.rawQuery("SELECT id_turma FROM turma WHERE nomeTurma = ? AND id_escola = ?", new String[]{nomeTurma, BancoDados.ID_ESCOLA.toString()});
            if (cursor != null && cursor.moveToFirst()) {
                id_turma = cursor.getInt(0);
            }
        }catch (Exception e){
            Log.e("kariti","Erro ao tentar pegar id da turma! "+e.getMessage());
            return null;
        } finally {
            if(base_dados != null && base_dados.isOpen()){
                base_dados.close();
            }
            if(cursor != null){
                cursor.close();
            }
        }
        return id_turma;
    }

    //  PAREI AQUIIII-----------------------------------------------------------------------------------------------
    public Integer pegaqtdQuestoes(String id_prova){
        SQLiteDatabase base_dados = this.getReadableDatabase();
        Cursor cursor = null;
        Integer qtdQuestoes = null;
        try {
            cursor = base_dados.rawQuery("SELECT qtdQuestoes FROM prova WHERE id_prova = ?", new String[]{id_prova});
            if (cursor.getCount() > 0){
                cursor.moveToFirst();
                qtdQuestoes = cursor.getInt(0);
            }
        }finally{
            if(cursor != null){
                cursor.close();
            }
        }
        return qtdQuestoes;
    }
    public Integer pegaqtdAlternativas(String id_prova) {
        SQLiteDatabase base_dados = this.getWritableDatabase();
        Cursor cursor = base_dados.rawQuery("Select qtdAlternativas from prova where id_prova = ?", new String[]{id_prova});
        if (cursor.getCount() > 0)
            cursor.moveToFirst();
        return cursor.getInt(0);
    }
    public String pegaEmailAluno(String id_aluno) {
        SQLiteDatabase base_dados = this.getWritableDatabase();
        Cursor cursor = base_dados.rawQuery("Select * from aluno where id_aluno = ? and status = ? and id_usuario = ?", new String[]{id_aluno, "1", BancoDados.USER_ID.toString()});
        if (cursor.getCount() > 0)
            cursor.moveToFirst();
        return cursor.getString(2);
    }
    public String pegaEscola(String escola) {
        SQLiteDatabase base_dados = this.getWritableDatabase();
        Cursor cursor = base_dados.rawQuery("Select * from escola where id_escola = ? and id_usuario = ?", new String[]{escola, BancoDados.USER_ID.toString()});
        if (cursor.getCount() > 0)
            cursor.moveToFirst();
        return cursor.getString(1);
    }
    public String pegaUsuario(String id_usuario) {
        SQLiteDatabase base_dados = this.getWritableDatabase();
        Cursor cursor = base_dados.rawQuery("Select * from usuario where id_usuario = ?", new String[]{id_usuario});
        if (cursor.getCount() > 0)
            cursor.moveToFirst();
        return cursor.getString(1);
    }

    public void listTeste(){
        Cursor cursor = null;
        try {
            SQLiteDatabase database = this.getWritableDatabase();
            cursor = database.rawQuery("SELECT id_prova FROM prova WHERE id_escola = ?", new String[]{BancoDados.ID_ESCOLA.toString()});
            long id_prova = cursor.getInt(0);
        }finally {
            if(cursor != null){
                cursor.close();
            }
        }
    }
    @NonNull
    private static String bytesToHex(@NonNull byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (int i = 0; i < hash.length; i++) {
            String hex = Integer.toHexString(0xff & hash[i]);
            if(hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
    public String to256(String text){
        try {
            final MessageDigest digest = MessageDigest.getInstance("SHA-256");
            final byte[] hashbytes = digest.digest(
                    text.getBytes(StandardCharsets.UTF_8));
            String sha3Hex = bytesToHex(hashbytes);
            return sha3Hex;
        }catch(Exception e){
            return "ERROR";
        }
    }
    public List<String> obterNomesAlunos() {
        List<String> nomesAlunos = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT nomeAluno FROM aluno where status = ? and id_usuario = ? ORDER BY nomeAluno ASC", new String[]{"1", BancoDados.USER_ID.toString()});
        if (cursor != null && cursor.moveToFirst()) {
            do {
                String nomeAluno = cursor.getString(0);
                nomesAlunos.add(nomeAluno);
            } while (cursor.moveToNext());
            cursor.close();
        }
        db.close();
        return nomesAlunos;
    }
    public List<String> listTodosAlunosDaTurma(String id_turma) {
        ArrayList<String>  alunos = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT nomeAluno FROM aluno where id_usuario = ? and id_aluno in (select DISTINCT id_aluno FROM alunosTurma WHERE id_turma = ?) ORDER BY nomeAluno ASC", new String[]{BancoDados.USER_ID.toString(), id_turma});
        if (cursor != null && cursor.moveToFirst()) {
            do {
                String aluno = cursor.getString(0);
                alunos.add(aluno);
            } while (cursor.moveToNext());
            cursor.close();
        }
        db.close();
        return alunos;
    }
    public List<String> listAlunosDaTurmaSemAnonimos(String id_turma) {
        ArrayList<String>  alunos = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT nomeAluno FROM aluno where id_usuario = ? and status = ? and id_aluno in (select DISTINCT id_aluno FROM alunosTurma WHERE id_turma = ?) ORDER BY nomeAluno ASC", new String[]{BancoDados.USER_ID.toString(), "1", id_turma});
        if (cursor != null && cursor.moveToFirst()) {
            do {
                String aluno = cursor.getString(0);
                alunos.add(aluno);
            } while (cursor.moveToNext());
            cursor.close();
        }
        db.close();
        return alunos;
    }
    public List<String> obterNomeTurmas() {
        List<String>  nomesTurma = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT nomeTurma FROM turma where id_escola = ?", new String[]{String.valueOf(BancoDados.ID_ESCOLA)});
        if (cursor != null && cursor.moveToFirst()) {
            do {
                // O índice 0 corresponde à coluna 'nome' no exemplo
                String nomeTurma = cursor.getString(0);
                nomesTurma.add(nomeTurma);
            } while (cursor.moveToNext());
            cursor.close();
        }
        db.close();
        return nomesTurma;
    }
    public List<Integer> listProvasPorTurma(String id_turma) {
        List<Integer>  ids_provas = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT id_prova FROM prova WHERE id_turma = ? and id_escola = ?", new String[]{id_turma, BancoDados.ID_ESCOLA.toString()});
        if (cursor != null && cursor.moveToFirst()) {
            do {
                Integer id_prova = cursor.getInt(0);
                ids_provas.add(id_prova);
            } while (cursor.moveToNext());
            cursor.close();
        }
        db.close();
        return ids_provas;
    }
    public List<String> listTurmasPorProva() {
        List<String>  nomesTurma = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT nomeTurma FROM turma where id_escola = ? and id_turma in (select DISTINCT id_turma FROM prova) ", new String[]{String.valueOf(BancoDados.ID_ESCOLA)});
        if (cursor != null && cursor.moveToFirst()) {
            do {
                // O índice 0 corresponde à coluna 'nome' no exemplo
                String nomeTurma = cursor.getString(0);
                nomesTurma.add(nomeTurma);
            } while (cursor.moveToNext());
            cursor.close();
        }
        db.close();
        return nomesTurma;
    }
    public List<String> obterNomeProvas(String id_turma) {
        List<String>  nomesProvas = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT nomeProva FROM prova where id_turma = ?", new String[]{id_turma});
        if (cursor != null && cursor.moveToFirst()) {
            do {
                String nomeProva = cursor.getString(0);
                nomesProvas.add(nomeProva);
            } while (cursor.moveToNext());
            cursor.close();
        }
        db.close();
        return nomesProvas;
    }

    public List<String> listProvasNCorrigidas(Integer id_turma) {
        List<String>  nomesProvas = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT nomeProva, id_prova FROM prova p WHERE id_turma = ? and NOT EXISTS (SELECT id_prova FROM resultadoCorrecao r WHERE r.id_prova = p.id_prova)", new String[]{id_turma.toString()});
        if (cursor != null && cursor.moveToFirst()) {
            do {
                String nomeProva = cursor.getString(0);
                nomesProvas.add(nomeProva);
            } while (cursor.moveToNext());
            cursor.close();
        }
        db.close();
        return nomesProvas;
    }

    public List<Integer> listAlunosDturma(String id_turma) {
        List<Integer>  ids = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT id_aluno FROM alunosTurma where id_turma = ?", new String[]{id_turma});
        if (cursor != null && cursor.moveToFirst()) {
            do {
                Integer id = cursor.getInt(0);
                ids.add(id);
            } while (cursor.moveToNext());
            cursor.close();
        }
        db.close();
        return ids;
    }
    public List<Integer> qtdAlunosAnonimatos(String id_turma) {
        List<Integer>  ids_anonimos = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT DISTINCT id_aluno FROM aluno where id_aluno in (select DISTINCT id_aluno FROM alunosTurma WHERE id_turma = ?) and status = ?", new String[]{id_turma, "0"});
        if (cursor != null && cursor.moveToFirst()) {
            do {
                Integer id = cursor.getInt(0);
                ids_anonimos.add(id);
            } while (cursor.moveToNext());
            cursor.close();
        }
        db.close();
        return ids_anonimos;
    }
    public Float listNota(String id_prova) {
        float notaTot = 0;
        //ArrayList<Integer>  notas = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT nota FROM gabarito where id_prova = ?", new String[]{id_prova});
        if (cursor != null && cursor.moveToFirst()) {
            do {
                // O índice 0 corresponde à coluna 'nome' no exemplo
                float nota = cursor.getFloat(0);
                Log.e("kariti","Nota = "+nota);
                notaTot = notaTot + nota;
                //notas.add(nota);
            } while (cursor.moveToNext());
            cursor.close();
        }
        db.close();
        return notaTot;
    }

    /**
     * Este método obtém as notas de cada questão de uma prova.
     * @param id_prova codigo da prova que se deseja saber as notas das questões.
     * @return lista com um item de texto para cada questão correspondendo a nota.
     * */
    public List<String> listNotaPorQuetao(Integer id_prova) {
        ArrayList<String>  notas = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT nota FROM gabarito where id_prova = ? ORDER BY questao", new String[]{id_prova.toString()});
        if (cursor != null && cursor.moveToFirst()) {
            do {
                // O índice 0 corresponde à coluna 'nome' no exemplo
                String nota = cursor.getString(0);
                notas.add(nota);
            } while (cursor.moveToNext());
            cursor.close();
        }
        db.close();
        return notas;
    }
    public List<Integer> listAlunoPorProvaCorrigida(Integer id_prova){
        ArrayList<Integer> ids_alunos = new ArrayList<>();
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = this.getReadableDatabase();
            cursor = db.rawQuery("SELECT id_aluno FROM aluno WHERE id_aluno in (SELECT distinct id_aluno FROM resultadoCorrecao where id_prova = ?) ORDER BY nomeAluno", new String[]{id_prova.toString()});
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Integer id_aluno = cursor.getInt(0);
                    ids_alunos.add(id_aluno);
                } while (cursor.moveToNext());
            }
        }finally {
            if (cursor != null){
                cursor.close();
            }
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
        return ids_alunos;
    }
    public List<Integer> listQuestoes(Integer id_prova, Integer id_aluno) {
        ArrayList<Integer>  questoes = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM resultadoCorrecao where id_prova = ? and id_aluno = ?", new String[]{id_prova.toString(), id_aluno.toString()});
        if (cursor != null && cursor.moveToFirst()) {
            do {
                Integer questao = cursor.getInt(3);
                questoes.add(questao);
            } while (cursor.moveToNext());
            cursor.close();
        }
        db.close();
        return questoes;
    }

    /**
     * Este método lista todas as escolas do banco de dados pertecentes a um determinado usuário
     * @param status parametro que determina se as escolas listadas serão as ativas ou as desativadas
     * @return retorna uma lista de string contendo todas as escolas pertencentes ao usuario
     * logado caso não tenha, retorna uma lista vazia.
     */
    public List<String> listEscolas(Integer status) {
        ArrayList<String> escolas = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT nomeEscola FROM escola WHERE id_usuario = ? AND status = ?  ORDER BY nomeEscola ASC", new String[]{BancoDados.USER_ID.toString(), status.toString()});
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String escola = cursor.getString(0);
                    escolas.add(escola);
                } while (cursor.moveToNext());
            }
        }finally {
            if(cursor != null){
                cursor.close();
            }
            db.close();
        }
        return escolas;
    }
    public String mostraGabarito(Integer id_prova) {
        String gabarito = "";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT resposta FROM gabarito WHERE id_prova = ? ORDER BY questao ASC", new String[]{id_prova.toString()});
        if (cursor != null && cursor.moveToFirst()) {
            do {
                String resposta = cursor.getString(0);
                char r = (char) (Integer.parseInt(resposta)-1+'A');
                gabarito += r + "\n";
            } while (cursor.moveToNext());
            cursor.close();
        }
        db.close();
        return gabarito;
    }
    public String mostraGabaritoInt(Integer id_prova) {
        String gabarito = "";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT resposta FROM gabarito WHERE id_prova = ? ORDER BY questao ASC", new String[]{id_prova.toString()});
        if (cursor != null && cursor.moveToFirst()) {
            do {
                String resposta = cursor.getString(0);
                gabarito += resposta + "\n";
            } while (cursor.moveToNext());
            cursor.close();
        }
        db.close();
        return gabarito;
    }
    public List<String> carregaGabarito(Integer id_prova) {
        ArrayList<String> respostasEsperadas = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT resposta FROM gabarito WHERE id_prova = ? ORDER BY questao ASC", new String[]{id_prova.toString()});
        if (cursor != null && cursor.moveToFirst()) {
            do {
                String resposta = cursor.getString(0);
                char r = (char) (Integer.parseInt(resposta)-1+'A');
                respostasEsperadas.add(String.valueOf(r));
            } while (cursor.moveToNext());
            cursor.close();
        }
        db.close();
        return respostasEsperadas;
    }

    //Restorna os dados do Gabarito
    public String detalhePorAluno(Integer id_prova, Integer id_aluno) {
        String detalhes = "";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT respostaDada FROM resultadoCorrecao WHERE id_prova = ? and id_aluno = ? ORDER BY questao ASC", new String[]{id_prova.toString(), id_aluno.toString()});
        if (cursor != null && cursor.moveToFirst()) {
            do {
                String resposta = cursor.getString(0);
                char r = (char) (Integer.parseInt(resposta)-1+'A');
                detalhes += r + "\n";
            } while (cursor.moveToNext());
            cursor.close();
        }
        db.close();
        return detalhes;
    }
    public List<String> respostasDadas(Integer id_prova, Integer id_aluno) {
        ArrayList<String> respostasDadas = new ArrayList<>();
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = this.getReadableDatabase();
            cursor = db.rawQuery("SELECT respostaDada FROM resultadoCorrecao WHERE id_prova = ? and id_aluno = ? ORDER BY questao ASC", new String[]{id_prova.toString(), id_aluno.toString()});
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String aux = "";
                    String resposta = cursor.getString(0);
                    if(!resposta.equals("-1")){
                        Log.e("kariti", resposta);
                        if (resposta.equals("0")) {
                            aux = "-";
                        } else {
                            aux = String.valueOf((char) (Integer.parseInt(String.valueOf(resposta.charAt(0))) - 1 + 'A'));
                        }
                        for (int i = 1; i < resposta.length(); i++) {
                            if (!String.valueOf(resposta.charAt(i)).equals("0")) {
                                aux += "+" + String.valueOf((char) (Integer.parseInt(String.valueOf(resposta.charAt(i))) - 1 + 'A'));
                            }
                        }
                        respostasDadas.add(aux);
                    }
                } while (cursor.moveToNext());
            }
        }finally {
            if (cursor != null){
                cursor.close();
            }
            if (db != null && db.isOpen()){
                db.close();
            }
        }
        return respostasDadas;
    }
    public String listRespostasAluno(String id_prova, String id_aluno) {
        String respostasDadas = "";
        String ultimaQuestao = "";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT respostaDada, questao FROM resultadoCorrecao where id_prova = ? and id_aluno = ? ORDER BY questao ASC", new String[]{id_prova, id_aluno});
        if (cursor != null && cursor.moveToFirst()) {
            do {
                String resposta = cursor.getString(0);
                String questao = cursor.getString(1);
                if(!respostasDadas.isEmpty() && !questao.equals(ultimaQuestao)){
                    respostasDadas += ",";
                }
                respostasDadas += resposta;
                ultimaQuestao = questao;
            } while (cursor.moveToNext());
            cursor.close();
        }
        db.close();
        return respostasDadas;
    }
    public String listRespostasGabarito(String id_prova) {
        String respostasGabarito = "";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT resposta FROM gabarito WHERE id_prova = ? ORDER BY questao ASC", new String[]{id_prova});
        if (cursor != null && cursor.moveToFirst()) {
            do {
                String resposta = cursor.getString(0);
                respostasGabarito += resposta;
            } while (cursor.moveToNext());
            cursor.close();
        }
        db.close();
        return respostasGabarito;
    }
    public String listNotaQuestao(String id_prova) {
        String notasQuestoes = "";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT nota FROM gabarito WHERE id_prova = ? ORDER BY questao ASC", new String[]{id_prova});
        if (cursor != null && cursor.moveToFirst()) {
            do {
                String nota = cursor.getString(0);
                notasQuestoes += nota;
            } while (cursor.moveToNext());
            cursor.close();
        }
        db.close();
        return notasQuestoes;
    }
}