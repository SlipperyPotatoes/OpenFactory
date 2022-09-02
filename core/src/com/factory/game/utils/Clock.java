package com.factory.game.utils;

import com.factory.game.FactoryGame;

//This util is used for running a section of code for once every set amount of time, on repeat
public class Clock {
    float timerLength, currentTime;

    public Clock(float timerLength) {
        this.timerLength = timerLength;
        this.currentTime = 0;
        FactoryGame.clockArrayList.add(this);
    }

    public void iterate() {
        if (currentTime > timerLength) return;
        currentTime += FactoryGame.delta;
    }

    public boolean canTick() {
        if(currentTime < timerLength) return false;
        reset();
        return true;
    }

    private void reset() {
        currentTime -= timerLength;
    }
}
