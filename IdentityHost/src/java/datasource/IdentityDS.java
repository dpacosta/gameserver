/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package datasource;

import javax.ejb.Singleton;

/**
 *
 * @author daniel
 */
/**
 * Base de Dados do Quiz.
 */
@Singleton
public class IdentityDS {

    // Variavel estática que conterá a instancia do método
    private static IdentityDS instance = new IdentityDS();
    private static String lider;

    public static synchronized String getLider() {
        return lider;
    }

    public static synchronized void setLider(String lider) {
        IdentityDS.lider = lider;
    }

    static {
        // Operações de inicialização da classe
    }

    // Método público estático de acesso único ao objeto!
    public static synchronized IdentityDS getInstance() {

        /*if(instance == null) 
         {
         instance = new IdentityDS();
         // O valor é retornado para quem está pedindo
 
         }*/
        return instance;
           // Retorna o a instância do objeto

    }

}
