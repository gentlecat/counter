package me.tsukanov.counter.ui;

import java.io.Serializable;

public class GameStatus implements Serializable {
    public String name;
    public int players;
    public int p1Health;
    public int p2health;


    public GameStatus(String name, int mode, int p1, int p2){
        this.name = name;
        this.players = mode;
        this.p1Health = p1;
        this.p2health = p2;

    }



}
