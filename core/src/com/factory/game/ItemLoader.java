package com.factory.game;

import com.badlogic.gdx.math.Vector2;

public class ItemLoader extends LogisticBuilding implements Directional {
    FACING directionFacing;

    public ItemLoader(Vector2 v2, FACING directionFacing) {
        super(v2, 1, 1, true);
        this.directionFacing = directionFacing;
    }

    @Override
    public void pushItem(short itemId, LogisticBuilding destination) {

    }

    @Override
    public void setDirectionFacing(FACING newDirectionFacing) {

    }

    @Override
    public FACING getDirectionFacing() { return directionFacing; }

    @Override
    public boolean hasItems() {
        return true;
    }

    @Override
    public void receiveItem(short itemId) {
        throw new RuntimeException("Loaders cannot accept items, itemId: " + itemId);
    }

    @Override
    public boolean canAccept(short itemId) {
        return false;
    }
}
