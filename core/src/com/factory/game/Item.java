package com.factory.game;

import com.badlogic.gdx.graphics.Texture;

public class Item {
    short id;
    Texture texture;

    public Item(short id, Texture texture) {
        this.id = id;
        this.texture = texture;
    }
}
