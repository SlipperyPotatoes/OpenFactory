package com.factory.game;

import com.badlogic.gdx.math.Vector2;

abstract public class LogisticBuilding extends Building implements ItemHandling{
    public LogisticBuilding(Vector2 v2, float gridWidth, float gridHeight, boolean isSensor) {
        super(v2, gridWidth, gridHeight, isSensor);
    }
}
