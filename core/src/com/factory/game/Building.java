package com.factory.game;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

public class Building extends Entity{


    public Building(Vector2 v2, float gridWidth, float gridHeight, boolean isSensor) {
        super(v2.x, v2.y, gridWidth, gridHeight, isSensor);

        FactoryGame.buildings.add(this);
    }

    public void update() {

    }

    public void draw(SpriteBatch batch) {

    }




}
