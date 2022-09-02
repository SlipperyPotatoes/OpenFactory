package com.factory.game;

public interface ItemHandling {
    boolean canAccept(short itemId);
    boolean hasItems();
    void pushItem(short itemId, LogisticBuilding destination);
    void receiveItem(short itemId);
}
