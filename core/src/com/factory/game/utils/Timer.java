package com.factory.game.utils;

import com.factory.game.FactoryGame;

//This util is used for running a section of code for the timerLength and then not running it again until the timer is reset
public class Timer {
    float timerLength, currentTime;

    public Timer(float timerLength) {
        this.timerLength = timerLength;
        this.currentTime = timerLength;
        FactoryGame.timerArrayList.add(this);
    }

    public void iterate() {
        if(!isActive()) return;
        currentTime += FactoryGame.delta;
    }

    public boolean isActive() {
        return currentTime <= timerLength;
    }

    public void reset() {
        currentTime = 0;
    }
}
