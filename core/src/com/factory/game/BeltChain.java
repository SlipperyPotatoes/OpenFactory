package com.factory.game;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

import java.util.LinkedList;
import java.util.List;

public class BeltChain {
    private BeltItemList itemList;
    private LinkedList<ConveyorBelt> conveyorList;
    private ConveyorBelt nextBelt;
    private int length;
    private boolean deprecated;

    public BeltChain(ConveyorBelt belt) {
        itemList = new BeltItemList();
        conveyorList = new LinkedList<>();
        conveyorList.add(belt);
        length = 1;
        deprecated = false;
        FactoryGame.beltChains.add(this);
    }

    public BeltChain(BeltItemList itemList, LinkedList<ConveyorBelt> conveyorList) {
        this.itemList = itemList;
        this.conveyorList = conveyorList;
        this.length = conveyorList.size();
        deprecated = false;
        FactoryGame.beltChains.add(this);
    }

    public void addBeltToFront(ConveyorBelt belt) {
        if(belt.beltChain == null) {
            BeltItemList.extend(itemList, 1);
            itemList.resetMoveForward();
            belt.beltChain = this;
            conveyorList.addFirst(belt);
            belt.index = 0;
            length++;
            for(int i = 1; i < length; i++) {
                conveyorList.get(i).index = i;
            }
        } else if(belt.beltChain != this) {
            System.out.println("Belt added to the front of a chain shouldn't already have a beltChain");
        }
    }



    public void addBeltToBack(ConveyorBelt belt) {
        if(belt.beltChain == null) {
            belt.beltChain = this;
            conveyorList.addLast(belt);
            length++;
            belt.index = length - 1;
        } else if(belt.beltChain == this) { //if the belt forms a loop
            setNextBelt(conveyorList.get(conveyorList.size()-1));
        } else {
            combine(this, belt.beltChain);
        }
    }


    public void moveItemsForward(float distance) {
        itemList.moveItemsForward(distance);
    }

    public static void combine(BeltChain frontChain, BeltChain backChain) {
        BeltItemList frontList = frontChain.itemList;
        BeltItemList backList = backChain.itemList;
        BeltItemList.extend(backList, frontChain.getLength());
        backList.resetMoveForward();
        frontChain.itemList = BeltItemList.combine(frontList, backList);
        frontChain.conveyorList.addAll(backChain.conveyorList);
        for (int i = 0; i < frontChain.length + backChain.length - 1; i++) {
            ConveyorBelt belt = frontChain.conveyorList.get(i);
            belt.index = i;
            belt.beltChain = frontChain;
        }
        frontChain.length += backChain.length;

        backChain.setAsDeprecated();
    }

    //TODO: Add items that where on the belt being destroyed into the player's inventory or the ground
    //this function is used only if a belt is being destroyed and the og beltChain length is >=3
    public void splitChain(int indexOfSplit) {
        BeltChain chain1;
        BeltChain chain2;
        //gets indexes of the items
        int itemStartBeltIndex = itemList.binarySearch(indexOfSplit+1);
        int itemEndBeltIndex = itemList.binarySearch(indexOfSplit-0.5F);
        if (itemStartBeltIndex == itemEndBeltIndex) itemEndBeltIndex--;

        chain1 = new BeltChain(BeltItemList.split(itemList, 0, itemEndBeltIndex +1),
                new LinkedList<>(conveyorList.subList(0, indexOfSplit)));
        chain2 = new BeltChain(BeltItemList.split(itemList, itemStartBeltIndex, itemList.getItemIDs().size()),
                new LinkedList<>(conveyorList.subList(indexOfSplit+1, length)));

        for (int i = 0; i < chain1.length; i++) {
            ConveyorBelt belt = chain1.conveyorList.get(i);
            belt.beltChain = chain1;
            belt.index = i;
        }

        for (int i = 0; i < chain2.length; i++) {
            ConveyorBelt belt = chain2.conveyorList.get(i);
            belt.beltChain = chain2;
            belt.index = i;
        }

        BeltItemList.extend(chain2.itemList,-(indexOfSplit+1)); //decreases item's distances from the end by the number of belts removed
        setAsDeprecated();
    }

    public void sliceChain(int indexOfEndBelt, int indexOfStartBelt) {
        BeltChain chain1;
        BeltChain chain2;
        //gets indexes of the items
        int itemStartBeltIndex = itemList.binarySearch(indexOfEndBelt);
        int itemEndBeltIndex = itemStartBeltIndex-1;

        chain1 = new BeltChain(BeltItemList.split(itemList, 0, itemEndBeltIndex +1),
                new LinkedList<>(conveyorList.subList(0, indexOfEndBelt +1)));
        chain2 = new BeltChain(BeltItemList.split(itemList, itemStartBeltIndex, itemList.getItemIDs().size()),
                new LinkedList<>(conveyorList.subList(indexOfStartBelt, length)));

        for (int i = 0; i < chain1.length; i++) {
            ConveyorBelt belt = chain1.conveyorList.get(i);
            belt.beltChain = chain1;
            belt.index = i;
        }

        for (int i = 0; i < chain2.length; i++) {
            ConveyorBelt belt = chain2.conveyorList.get(i);
            belt.beltChain = chain2;
            belt.index = i;
        }

        BeltItemList.extend(chain2.itemList,-indexOfStartBelt); //decreases item's distances from the end by the number of belts removed

        setAsDeprecated();
    }



    public void addItem(short id, ConveyorBelt belt) {
        itemList.addItemToList(id, belt.index);
    }

    //TODO: FIX, this very broke!!
    public void drawItems(SpriteBatch batch) {
        for (int i = 0; i < itemList.getItemDistances().size(); i++) {
            float distance = itemList.getItemDistances().get(i);
            int beltIndex = (int) Math.floor(distance);
            ConveyorBelt belt = conveyorList.get(beltIndex);
            Vector2 itemPosition = belt.getPosition();
            if (belt.getConnectedTo() != null) {
                switch (belt.getConnectedTo()) {
                    case NORTH:
                        itemPosition.y += (distance - beltIndex);
                        break;
                    case EAST:
                        itemPosition.x += (distance - beltIndex);
                        break;
                    case SOUTH:
                        itemPosition.y -= (distance - beltIndex);
                        break;
                    case WEST:
                        itemPosition.x -= (distance - beltIndex);
                        break;
                }
            }
            batch.draw(FactoryGame.img, itemPosition.x-0.3F, itemPosition.y-0.3F, 0.6f, 0.6f);
        }
    }



    public int getLength() { return length; }

    public ConveyorBelt getBelt(int index) { return conveyorList.get(index); }

    public List<ConveyorBelt> getBelts() { return conveyorList; }

    public BeltItemList getItemList() { return itemList; }

    public boolean isDeprecated() { return deprecated; }

    //TODO: Add leftover items into player's inventory
    public void setAsDeprecated() { deprecated = true; }

    public ConveyorBelt getNextBelt() { return nextBelt; }

    public void setNextBelt(ConveyorBelt belt) { nextBelt = belt; }
}
