/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

/**
 *
 * @author daniel
 */
public class Jogador {

    int id;
    int pontos;

    public Jogador(int id) {
        this.id = id;
        this.pontos = 0;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPontos() {
        return pontos;
    }

    public void setPontos(int pontos) {
        this.pontos = pontos;
    }
    public void addPontos(int pontos) {
        this.pontos = this.pontos + pontos;
    }
    

}
