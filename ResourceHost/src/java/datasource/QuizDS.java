package datasource;

import java.util.ArrayList;
import java.util.List;
import javax.ejb.Singleton;
import model.Jogador;

/** Base de Dados do Quiz.
 */
@Singleton
public class QuizDS {
    
    // Variavel estática que conterá a instancia do método
     private static QuizDS instance = new QuizDS();
     private static Jogador jogador1;
     private static Jogador jogador2;
     private static List<Integer> questoesAcertadas = new ArrayList<Integer>();
     private static List<Integer> questoesErradas = new ArrayList<Integer>();
     
    public static enum Gabarito {
    QUESTAO_1(1,"c"), QUESTAO_2(2,"a"), QUESTAO_3(4,"a"), QUESTAO_4(4,"b"), QUESTAO_5(5,"c"),
    QUESTAO_6(6,"d"),QUESTAO_7(7,"a"),QUESTAO_8(8,"b"),QUESTAO_9(9,"c"),QUESTAO_10(10,"d");
    
    int questao;
    String alternativa;
    
    Gabarito(int questao, String alternativa){
        this.questao = questao;
        this.alternativa = alternativa;
    }

    public int getQuestao() {
        return questao;
    }

    public String getAlternativa() {
        return alternativa;
    }
    
    public static Gabarito findQuestao(int questao) {
        switch (questao) {
            case 1:
                return Gabarito.QUESTAO_1;
            case 2:
                return Gabarito.QUESTAO_2;
            case 3:
                return Gabarito.QUESTAO_3;
            case 4:
                return Gabarito.QUESTAO_4;
            case 5:
                return Gabarito.QUESTAO_5;
            case 6:
                return Gabarito.QUESTAO_6;
            case 7:
                return Gabarito.QUESTAO_7;
            case 8:
                return Gabarito.QUESTAO_8;
            case 9:
                return Gabarito.QUESTAO_9;
            case 10:
                return Gabarito.QUESTAO_10;
            default:
                return null;
        }
    }

}

    public static synchronized Jogador findJogador(int numero) {
        switch (numero) {
            case 1:
                return getJogador1();
            case 2:
                return getJogador2();
            default:
                return null;
        }
    }
 
     static {
             // Operações de inicialização da classe
     }
     
 
     // Método público estático de acesso único ao objeto!
     public static synchronized QuizDS getInstance(){
 
           /*if(instance == null) 
           {
                instance = new QuizDS();
                // O valor é retornado para quem está pedindo
 
           }*/
           return instance;
           // Retorna o a instância do objeto
 
     }

    public static synchronized Jogador getJogador1() {
        return jogador1;
    }

    public static synchronized void setJogador1(Jogador jogador1) {
        QuizDS.jogador1 = jogador1;
    }

    public static synchronized Jogador getJogador2() {
        return jogador2;
    }

    public static synchronized void setJogador2(Jogador jogador2) {
        QuizDS.jogador2 = jogador2;
    }

    public static synchronized List<Integer> getQuestoesAcertadas() {
        return questoesAcertadas;
    }

    public static synchronized void setQuestoesAcertadas(List<Integer> questoesAcertadas) {
        QuizDS.questoesAcertadas = questoesAcertadas;
    }
    
    public static synchronized void resetQuestoesAcertadas() {
        QuizDS.questoesAcertadas = new ArrayList<Integer>();
    }
    
    public static synchronized void resetQuestoesErradas() {
        QuizDS.questoesErradas = new ArrayList<Integer>();
    }
    
    public static synchronized List<Integer> getQuestoesErradas() {
        return questoesErradas;
    }

    public static synchronized void setQuestoesErradas(List<Integer> questoesErradas) {
        QuizDS.questoesErradas = questoesErradas;
    }
     
}