package com.factory.game;

import com.badlogic.gdx.math.Vector2;
import com.factory.game.utils.Utils;

//DEPRECATED
public abstract class ChainableBuilding extends LogisticBuilding {
    FACING directionFacing;
    public boolean endBelt;
    ChainableBuilding nBelt, eBelt, sBelt, wBelt;
    public ChainableBuilding(Vector2 v2, float gridWidth, float gridHeight, boolean isSensor) {
        super(v2, gridWidth, gridHeight, isSensor);
    }

    public abstract void pushAnyItem(ChainableBuilding destination);

    public abstract void iterate();

    public abstract void indexSurroundingBelts();

    public abstract void indexAndClearSurroundingBelts();

    public void makeSurroundingBeltsIndex() {
        ChainableBuilding tempNBelt = Utils.getIfChainable(getPosition().x, getPosition().y+1);
        ChainableBuilding tempEBelt = Utils.getIfChainable(getPosition().x+1, getPosition().y);
        ChainableBuilding tempSBelt = Utils.getIfChainable(getPosition().x, getPosition().y-1);
        ChainableBuilding tempWBelt = Utils.getIfChainable(getPosition().x-1, getPosition().y);

        if (tempNBelt != null)
            tempNBelt.indexAndClearSurroundingBelts();
        if (tempEBelt != null)
            tempEBelt.indexAndClearSurroundingBelts();
        if (tempSBelt != null)
            tempSBelt.indexAndClearSurroundingBelts();
        if (tempWBelt != null)
            tempWBelt.indexAndClearSurroundingBelts();
    }
}
