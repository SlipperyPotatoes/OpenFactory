package com.factory.game;

import com.badlogic.gdx.math.Vector2;

abstract public class Conveyor extends LogisticBuilding implements Directional{
    protected FACING directionFacing;

    public Conveyor(Vector2 v2, float gridWidth, float gridHeight, FACING directionFacing) {
        super(v2, gridWidth, gridHeight, true);
        this.directionFacing = directionFacing;
    }

    @Override
    public FACING getDirectionFacing() { return directionFacing; }

    @Override
    public void setDirectionFacing(FACING newDirectionFacing) {
        directionFacing = newDirectionFacing;
    }
}
