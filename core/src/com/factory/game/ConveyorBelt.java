package com.factory.game;

import static com.factory.game.FactoryGame.*;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.factory.game.utils.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ConveyorBelt extends Conveyor {
    BeltChain beltChain;
    int index;
    private boolean alt;

    private FACING connectedTo;

    public ConveyorBelt(Vector2 v2, FACING directionFacing, boolean alt) {
        super(v2, 1, 1, directionFacing);
        this.beltChain = null;
        this.alt = alt;
        this.connectedTo = null;

        indexSurroundingBelts();
    }

    @Override
    public void update() {

    }

    public void indexSurroundingBelts() {
        List<ConveyorBelt> cardinalBelts = new ArrayList<>();
        cardinalBelts.add((ConveyorBelt) Utils.getIfFrom(ConveyorBelt.class, getPosition().x, getPosition().y+1));
        cardinalBelts.add((ConveyorBelt) Utils.getIfFrom(ConveyorBelt.class, getPosition().x+1, getPosition().y));
        cardinalBelts.add((ConveyorBelt) Utils.getIfFrom(ConveyorBelt.class, getPosition().x, getPosition().y-1));
        cardinalBelts.add((ConveyorBelt) Utils.getIfFrom(ConveyorBelt.class, getPosition().x-1, getPosition().y));

        //rotates the list so that its order is front, right, back, left
        Collections.rotate(cardinalBelts,-directionFacing.getDegrees()/90);

        ConveyorBelt frontBelt = cardinalBelts.get(0);
        ConveyorBelt rightBelt = cardinalBelts.get(1);
        ConveyorBelt backBelt = cardinalBelts.get(2);
        ConveyorBelt leftBelt = cardinalBelts.get(3);

        if(frontBelt!=null) {
            if(frontBelt.directionFacing == directionFacing.rotate(180)) {
                frontBelt = null;
            }
        }
        if(rightBelt!=null) {
            if(rightBelt.directionFacing != directionFacing.rotate(270)) {
                rightBelt = null;
            }
        }
        if(backBelt!=null) {
            if(backBelt.directionFacing != directionFacing) {
                backBelt = null;
            } else {
                connectedTo = directionFacing.rotate(180);
            }
        }
        if(leftBelt!=null) {
            if(leftBelt.directionFacing != directionFacing.rotate(90)) {
                leftBelt = null;
            }
        }

        if(backBelt!=null) {
            backBelt.beltChain.addBeltToFront(this);
        } else if (leftBelt!=null && rightBelt==null) {
            leftBelt.beltChain.addBeltToFront(this);
            connectedTo = directionFacing.rotate(270);
        } else if (rightBelt!=null && leftBelt==null) {
            rightBelt.beltChain.addBeltToFront(this);
            connectedTo = directionFacing.rotate(90);
        }

        if(frontBelt!=null) {
            if (frontBelt.directionFacing == this.directionFacing) {
                frontBelt.beltChain.addBeltToBack(this);
            } else if (frontBelt.beltChain.getLength() - 1 == frontBelt.index) { //if frontBelt is the end belt
                frontBelt.beltChain.addBeltToBack(this);
            }
        }

        if(beltChain==null) {
            beltChain = new BeltChain(this);
        }
    }

    //TODO: Implement all item handling methods
    @Override
    public boolean canAccept(short itemId) {
        return beltChain.getItemList().canPlaceItemAt(index);
    }

    @Override
    public boolean hasItems() {
        return false;
    }

    @Override
    public void pushItem(short itemId, LogisticBuilding destination) {

    }

    @Override
    public void receiveItem(short itemId) {
        beltChain.addItem(itemId,this);
    }

    @Override
    public void draw(SpriteBatch batch) {
        float rotation = 0;
        switch (directionFacing) {
            case WEST:
                rotation = 0;
                break;
            case SOUTH:
                rotation = 90;
                break;
            case EAST:
                rotation = 180;
                break;
            case NORTH:
                rotation = 270;
                break;
        }
        if(alt) {
            batch.draw(altConveyorBelt,getPosition().x-0.5F,getPosition().y-0.5F,0.5F,0.5F,1F,1F,1F,1F,rotation);
        } else {
            batch.draw(conveyorBelt, getPosition().x - 0.5F, getPosition().y - 0.5F, 0.5F, 0.5F, 1F, 1F, 1F, 1F, rotation);
        }
    }

    @Override
    public void destroy() {
        super.destroy();
        if(beltChain.getLength() >= 3 && index != 0 && index != beltChain.getLength()-1) { //if not on the ends of a >=3 len belt
            beltChain.splitChain(index);
        } else if (beltChain.getLength() == 1) {
            beltChain.setAsDeprecated();
            //TODO: Add items leftover into player's inventory
        } else { //if on the ends of a belt w a len of >1
            if(index==0) {
                beltChain.sliceChain(0, 1);
            } else {
                beltChain.sliceChain(index-1, index);
            }
            beltChain.setAsDeprecated();
            //TODO: Add items leftover into player's inventory
        }
    }

    @Override
    public void setDirectionFacing(FACING newDirectionFacing) {
        super.setDirectionFacing(newDirectionFacing);
        if(index != 0) {
            beltChain.sliceChain(index-1,index);
        }
        indexSurroundingBelts();
    }

    @Override
    public FACING getDirectionFacing() { return directionFacing; }

    public FACING getConnectedTo() { return connectedTo; }
}
