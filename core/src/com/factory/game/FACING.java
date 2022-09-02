package com.factory.game;

import com.badlogic.gdx.Input;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


public enum FACING {
    NORTH(0), EAST(90), SOUTH(180), WEST(270);

    private final int degrees;

    private static final ArrayList<FACING> list = new ArrayList<>(Arrays.asList(FACING.values()));

    FACING(int degrees) {
        this.degrees = degrees;
    }

    public int getDegrees() {
        return this.degrees;
    }

    //clockwise rotation
    public FACING rotate(int rotation) {
        if (rotation % 90 != 0) throw new IllegalArgumentException("rotation needs to be a " +
                "multiple of 90, rotation: " + rotation);
        int newRotation = rotation + this.degrees;
        while(newRotation>270) {
            newRotation -= 360;
        }
        return valueOf(newRotation);
    }

    public static FACING valueOf(int degrees) {
        return list.get(degrees/90);
    }
}
