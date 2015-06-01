package resources;

import datasource.QuizDS;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Date;
import javax.ejb.Stateless;

import javax.ws.rs.Path;
import javax.ws.rs.GET;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import model.Jogador;
import java.util.List;
import java.util.Timer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

/**
 * REST Web Service.
 */
@Stateless
@Path("/quiz")
public class QuizResource {
    
    Logger logger = Logger.getLogger(QuizResource.class.getName()); // PARA TESTE!!!

    private LeaderElectionTask leaderTask = new LeaderElectionTask(); /* task para eleição de líder */
    
    /**
     * A ideia do jogo é um quiz em que existem perguntas pré-definidas e à
     * medida que os jogadores respondem corretamente elas vão sendo bloqueadas
     * para outras respostas e o jogador que respondeu corretamente ganha os
     * pontos da questão Será assumido que os dois entrarão no jogo ao mesmo
     * tempo.
     */
    /**
     * Recurso para testar conectividade.
     */
    @GET
    @Path("/alive")
    @Produces("application/json")
    public String alive() {
        
        /**
         * Inicia a task que verifica periodicamente se líder apresenta problemas
         * Esta task irá rodar enquanto o servidor estiver funcionando no intervalo
         * delimitado pela variável intervalLeaderTask (em milissegundos).
         */
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(this.leaderTask, 0, ElectionProperties.VERIFY_INTERVAL); 
        
        return "yes";
    }
    
    /**
     * Metodo para verificar se o servidor está funcionando.
     */
    @GET
    @Path("/ping")
    @Produces("application/json")
    public String pingServer() {
        return "ALIVE";
    }
    
    /**
     * Recurso que retorna qual jogador (1 ou 2) o usuário assumiu ao entrar no
     * jogo Esse número será uma espécie de token que o identificará quando ele
     * responder as questões.
     */
    @GET
    @Path("/myplayer")
    @Produces("application/json")
    public int getPlayerNumber() throws MalformedURLException, IOException {
        //se nao for o lider processa a requisicao
        if (!this.leaderTask.isLeader()) {
            if (QuizDS.getJogador1() == null) {
                QuizDS.setJogador1(new Jogador(1));
                return QuizDS.getJogador1().getId();
            } else {
                QuizDS.setJogador2(new Jogador(1));
                return QuizDS.getJogador2().getId();
            }
        }else{//se nao for o lider envia a requisicao para os hosts
            //nessa parte ainda tem que tratar timeout no "while"
            /*int responseCode;
            Random gerador = new Random();
            int i;
            String url;
            URL obj;
            HttpURLConnection con;
            do {
                i = gerador.nextInt(NUM_HOSTS);
                url = "http://" + HOSTS[i] + "/quiz/myplayer";
                obj = new URL(url);
                con = (HttpURLConnection) obj.openConnection();
                con.setRequestMethod("GET");
                responseCode = con.getResponseCode();

            } while(responseCode != 200); //enquanto nao obtem sucesso fica enviando a requisicao
            BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line + "\n");
            }
            br.close();
            ObjectMapper mapper = new ObjectMapper();
            Integer num = mapper.readValue(sb.toString(), Integer.class);
            return num;*/
            return 0;
        }

    }

    /**
     * Recurso que zera o número de questões certas e erradas a cada início de quiz.
     */
    @GET
    @Path("/startquiz")
    @Produces("application/json")
    public String startQuiz() {
        QuizDS.resetQuestoesAcertadas();
        QuizDS.resetQuestoesErradas();
        return "ok";
    }

    /**
     * Recurso que verifica a resposta de uma questão.
     */
    @GET
    //@Path("/answer/{jogador}/{questao}/{letra}") // adicionar aos parametros: @PathParam("jogador") int jogador, 
    @Path("/answer/{questao}/{letra}")
    @Produces("application/json")
    public String answer(@PathParam("questao") int questao, @PathParam("letra") String alternativa) {
        
        //Se a questao ainda nao foi respondida
        if (!QuizDS.getQuestoesAcertadas().contains(questao) && !QuizDS.getQuestoesErradas().contains(questao)) {
            //se a questao foi corretamente respondida
            if (QuizDS.Gabarito.findQuestao(questao).getAlternativa().equalsIgnoreCase(alternativa)) {
                //QuizDS.findJogador(jogador).addPontos(PONTOS_QUESTAO_CORRETA); //jogador ganha os pontos da questao
                QuizDS.getQuestoesAcertadas().add(questao);//questao e inserida na lista de respondidas
                return "1";
            } else {
                QuizDS.getQuestoesErradas().add(questao);
                return "0";
            }
        } else {
            return "-1";
        }
    }

    /**
     * Recurso que verifica a pontuacao final do jogador.
     */
    @GET
    @Path("/myscore/{jogador}")
    @Produces("application/json")
    public int score(@PathParam("jogador") int jogador) {
        return QuizDS.findJogador(jogador).getPontos();
    }

    /**
     * Recurso que verifica questoes ja acertadas.
     */
    @GET
    @Path("/answered")
    @Produces("application/json")
    public List<Integer> aswered() {
        return QuizDS.getQuestoesAcertadas();
    }

    /**
     * Recurso que retorna arquivo com questoes certas e erradas.
     */
    @GET
    @Path("/file")
    @Produces("text/plain")
    public Response file() {
        try {
            Date d = new Date();
            File f = new File("resultados" + d.toString() + ".txt");
            f.createNewFile();
            //escreve no arquivo
            FileWriter fw = new FileWriter(f, true);

            BufferedWriter bw = new BufferedWriter(fw);

            bw.write("--- Desempenho ---");
            bw.newLine();

            if (!QuizDS.getQuestoesAcertadas().isEmpty()) {
                int lastCorretas = QuizDS.getQuestoesAcertadas().get(QuizDS.getQuestoesAcertadas().size() - 1);
                bw.write("Questoes Corretas: ");
                for (Integer i : QuizDS.getQuestoesAcertadas()) {
                    if (i == lastCorretas) {
                        bw.write(i + ".");
                    } else {
                        bw.write(i + ",");
                    }
                }
                bw.newLine();
            }

            if (!QuizDS.getQuestoesErradas().isEmpty()) {
                int lastErradas = QuizDS.getQuestoesErradas().get(QuizDS.getQuestoesErradas().size() - 1);

                bw.write("Questoes Incorretas: ");
                for (Integer i : QuizDS.getQuestoesErradas()) {
                    if (i == lastErradas) {
                        bw.write(i + ".");
                    } else {
                        bw.write(i + ",");
                    }
                }
            }

            bw.close();
            fw.close();

            ResponseBuilder response = Response.ok((Object) f);
            response.header("Content-Disposition", "attachment; filename=resultados" + d.toString() + ".txt");
            return response.build();

        } catch (Exception e) {
            return null;
        }
    }
    
    /***************************************************************
     * RECURSOS PARA A ELEIÇÃO DE LÍDER
     ************************************************************** */
    
    /**
     * Recurso para envio de mensagem Election.
     */
    @GET
    @Path("/election")
    @Produces("application/json")
    public String election() {
        if(!this.leaderTask.hasStartedElection()) { // se processo não houver iniciado a eleição, a inicia
            this.leaderTask.startElection();
        }
        return "OK";
    }
    
    /**
     * Recurso que envia a mensagem Coordinator.
     * @param crd : IP do coordenador
     */
    @POST
    @Path("/coordinator")
    @Consumes(MediaType.APPLICATION_JSON)
    public void coordinator(String crd) {
        this.leaderTask.finishElection(crd);
    }
    
}
