/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package resources;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Task para realização do processo de Eleição de Líder.
 */
public class LeaderElectionTask extends TimerTask {

    Logger logger = Logger.getLogger(LeaderElectionTask.class.getName());

    private String IP_LIDER = "192.168.0.13"; /* IP do Servidor Lider */

    private boolean inElection = false;       /* Indica se SD está em processo de eleição (true) ou não (false) */

    /* Array do Endereço IP dos Servidores existentes no SD */
    private static final String[] HOSTS = {
        "192.168.0.16",
        "192.168.0.15",
        "192.168.0.10",
        "192.168.0.13"
    };

    /* Array do Endereço IP dos Clientes que usam o SD */
    private static final String[] CLIENTS = {
        "192.168.0.14"
    };

    /**
     * Método executado periodicamente pela task: verificação de falhas no líder
     * e, no caso de falhas, inicia a eleição.
     */
    @Override
    public void run() {

        // All-to-All Heartbeating
        this.allToAllHeartbeating();

        // Logs para teste
        if (!isLeader()) {
            logger.log(Level.INFO, "Verificando lider..." + IP_LIDER);
        } else {
            logger.log(Level.INFO, "Sou o lider...");
        }

        // se não houver nenhum pedido de eleição e servidor não for o líder...
        if (!inElection && !isLeader()) {

            // verifica líder e, se servidor líder não estiver funcionando, roda a eleição...
            if (!checkServer(this.IP_LIDER)) {
                logger.log(Level.INFO, "Lider falhou!! Iniciando eleicao...");

                // roda a eleição
                this.startElection();
            }
        }
    }

    /**
     * Método para all-to-all heartbeating.
     */
    private void allToAllHeartbeating() {

        for (String target : HOSTS) { // para cada um dos hosts do sistema distribuido...
            if (!target.equals(this.getIP())) {
                logger.log(Level.INFO, "Verificando " + target + " ...");
                checkServer(target); // verifica cada máquina que não for igual à própria máquina
            }
        }
    }

    /**
     * Método para iniciar a eleição.
     */
    public void startElection() {
        this.inElection = true; // inElection vai para true
        boolean okFound = this.sendElection();    // inicia eleição

        // se não receber nenhuma mensagem OK, é o novo líder e deve repassar a informação a todos os demais
        if (!okFound) {
            defineCoordinator();
        }
    }

    /**
     * Método para envio da mensagem "election" a todos os demais servidores.
     */
    private boolean sendElection() {
        Long snd = new Long(this.getIP().replace(".", ""));   // obtem numero do ip do sender (maquina)
        boolean okFound = false;                                    // para verificar se algum processo enviou "OK" de resposta

        for (String receiver : HOSTS) {
            Long rcv = new Long(receiver.replace(".", ""));       // obtem numero do ip do receiver

            // envia election a todos os processos com maior id (no caso, o id será o IP da máquina)
            if (rcv > snd) {
                // Para o estabelecimento da conexão com o servidor
                HttpURLConnection urlConnection = null;
                BufferedReader reader = null;

                // Para a resposta em JSON
                String serverResponse = null;

                try {
                    // Constroi a URL
                    URL url = new URL("http://" + receiver + ":8080/quiz/election");

                    // Cria a requisicao e abre a conexão
                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.setConnectTimeout(5000);
                    urlConnection.setReadTimeout(5000);
                    urlConnection.connect();

                    int statusCode = urlConnection.getResponseCode();

                    // Se codigo de resposta do servidor for 200 (sucesso na request)...    
                    if (statusCode == 200) {

                        // Processa a entrada
                        InputStream inputStream = urlConnection.getInputStream();
                        StringBuffer buffer = new StringBuffer();

                        if (inputStream != null) {
                            reader = new BufferedReader(new InputStreamReader(inputStream));
                            buffer.append(reader.readLine());

                            if (buffer.length() != 0) {
                                serverResponse = buffer.toString();

                                if (serverResponse.equals("OK")) { // se a resposta recebida for ok, fica aguardando
                                    logger.log(Level.INFO, serverResponse); // REMOVER;
                                    okFound = true;
                                }
                            }
                        }

                    } else { // caso indique problema na requisição, pode ser problema no server (ou na comunicação)
                        logger.log(Level.SEVERE, "Erro ao enviar requisicao a maquina de numero " + receiver);
                    }

                } catch (Exception e) { // caso alguma exception tenha ocorrido, problema no server...
                    logger.log(Level.SEVERE, "Erro ao enviar requisicao a maquina de numero " + receiver);
                }

            }
        }

        return okFound;
    }

    /**
     * Método para definir a máquina como coordinator.
     */
    private void defineCoordinator() {

        this.inElection = false;
        this.IP_LIDER = this.getIP();

        for (String receiver : HOSTS) {

            // envia coordinator a todos os demais processos
            if (!receiver.equals(this.IP_LIDER)) {

                // Para o estabelecimento da conexão com o servidor
                HttpURLConnection urlConnection = null;

                try {
                    // Constroi a URL
                    URL url = new URL("http://" + receiver + ":8080/quiz/coordinator");

                    // Cria a requisicao e abre a conexão
                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("POST");
                    urlConnection.setDoOutput(true);
                    urlConnection.setRequestProperty("Content-Type", "application/json");

                    String input = this.IP_LIDER;
                    OutputStream os = urlConnection.getOutputStream();
                    os.write(input.getBytes());
                    os.flush();

                    int statusCode = urlConnection.getResponseCode();

                    // Se codigo de resposta do servidor for 200 (sucesso na request)...    
                    if ((statusCode >= 200) && (statusCode < 300)) {
                        InputStream inputStream = urlConnection.getInputStream();
                    } else { // caso indique problema na requisição, pode ser problema no server (ou na comunicação)
                        //logger.log(Level.SEVERE, "Erro ao enviar coordinator ao "+receiver);
                    }
                    urlConnection.disconnect();

                } catch (Exception e) { // caso alguma exception tenha ocorrido, problema no server...
                    //logger.log(Level.SEVERE, "Erro ao enviar coordinator ao "+receiver);
                }

            }
        }

        for (String client : CLIENTS) {
            try {
                String data = URLEncoder.encode("ip", "UTF-8") + "=" + URLEncoder.encode(this.IP_LIDER, "UTF-8");

                Socket conClient;
                conClient = new Socket(client, 6789);
                String path = "/leader";

                BufferedWriter wr = new BufferedWriter(new OutputStreamWriter(conClient.getOutputStream(), "UTF8"));
                wr.write("POST " + path + " HTTP/1.0\r\n");
                wr.write("Content-Length: " + data.length() + "\r\n");
                wr.write("Content-Type: application/x-www-form-urlencoded\r\n");
                wr.write("\r\n");

                wr.write(data);
                wr.flush();

                BufferedReader rd = new BufferedReader(new InputStreamReader(conClient.getInputStream()));
                String line;
                while ((line = rd.readLine()) != null) {
                    System.out.println(line);
                }
                wr.close();
                rd.close();
            } catch (IOException ex) {
                logger.log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * Método para finalizar a eleição e definir o coordinator nos outros
     * servidores.
     *
     * @param crd : IP do coordenador (lider)
     */
    public void finishElection(String crd) {
        this.inElection = false;
        this.IP_LIDER = crd;
    }

    /**
     * Método para verificar se o servidor que se comunica está funcionando.
     *
     * @param server: IP do servidor a ser verificado
     */
    private boolean checkServer(String server) {
        // Para o estabelecimento da conexão com o servidor
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Para a resposta em JSON
        String serverResponse = null;

        try {
            // Constroi a URL
            URL url = new URL("http://" + server + ":8080/quiz/ping");

            // Cria a requisicao e abre a conexão
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setConnectTimeout(5000);
            urlConnection.setReadTimeout(5000);
            urlConnection.connect();

            int statusCode = urlConnection.getResponseCode();

            // Se codigo de resposta do servidor for 200 (sucesso na request)...    
            if (statusCode == 200) {

                // Processa a entrada
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();

                if (inputStream == null) {
                    return false; // indica problemas no server
                }

                reader = new BufferedReader(new InputStreamReader(inputStream));
                buffer.append(reader.readLine());

                if (buffer.length() == 0) {
                    return false; // indica problemas no server
                }

                serverResponse = buffer.toString();

                if (serverResponse.equals("ALIVE")) {
                    logger.log(Level.INFO, serverResponse); // REMOVER;
                    return true;        // servidor está OK
                } else {
                    logger.log(Level.SEVERE, "Falha detectada! Problemas de conexao ao servidor " + server);
                    return false;       // servidor com problemas
                }

            } else { // Caso indique problema, apresenta erro em log
                logger.log(Level.SEVERE, "Falha detectada! Problemas de conexao ao servidor " + server);
            }

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Falha detectada! Problemas de conexao ao servidor " + server);
        }

        return false; // se não tiver funcionando, retorna false
    }

    /**
     * Método que retorna o endereço IP da máquina.
     */
    private String getIP() {
        
        String ipAddr = "";

        try {
            ipAddr = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException ex) {
            Logger.getLogger(LeaderElectionTask.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
        
        return ipAddr;

//        Enumeration<NetworkInterface> ifaces;
//
//        try {
//            ifaces = NetworkInterface.getNetworkInterfaces();
//
//            while (ifaces.hasMoreElements()) {
//                NetworkInterface iface = ifaces.nextElement();
//                Enumeration<InetAddress> addresses = iface.getInetAddresses();
//
//                while (addresses.hasMoreElements()) {
//                    InetAddress addr = addresses.nextElement();
//                    if (addr instanceof Inet4Address && !addr.isLoopbackAddress()) {
//                        logger.log(Level.INFO, "Meu ip eh "+addr.toString().substring(1));
//                        return addr.toString().substring(1); // retorna o IP
//                    }
//                }
//            }
//        } catch (SocketException ex) {
//            Logger.getLogger(LeaderElectionTask.class.getName()).log(Level.SEVERE, null, ex);
//        }
        //return null;
    }

    /**
     * Método que retorna se a máquina em questão é a líder.
     *
     * @return boolean indicando true (líder) ou false (não é líder)
     */
    public boolean isLeader() {
        return getIP().equals(IP_LIDER);
    }

    /**
     * Método para verificar a possibilidade de alterar o status de eleição.
     */
    public boolean hasStartedElection() {
        return inElection;
    }

}
