package com.factory.game;

import static com.factory.game.FactoryGame.*;

import java.util.LinkedList;
import java.util.List;

public class BeltItemList {
    private LinkedList<Short> itemIdList;
    private LinkedList<Float> itemDistanceList;
    private LinkedList<Boolean> canMoveForward;

    public BeltItemList() {
        itemIdList = new LinkedList<>();
        itemDistanceList = new LinkedList<>();
        canMoveForward = new LinkedList<>();
    }

    public BeltItemList(BeltItemList conveyorItemList) {
        this.itemIdList = conveyorItemList.itemIdList;
        this.itemDistanceList = conveyorItemList.itemDistanceList;
        this.canMoveForward = conveyorItemList.canMoveForward;
    }

    public BeltItemList(LinkedList<Short> itemIdList, LinkedList<Float> itemDistanceList, LinkedList<Boolean> canMoveForward) {
        this.itemIdList = itemIdList;
        this.itemDistanceList = itemDistanceList;
        this.canMoveForward = canMoveForward;
    }

    public void moveItemsForward(float distance) {
        //moves the items temporarily and fixes any overlaps
        //when there are no longer any overlaps it can be assumed that all items are free to move
        //their required distance so the for loop is closed, i.e i becomes = listLength
        int listLength = itemDistanceList.size();
        for(int i = 0; i < listLength; i++) {
            if(!canMoveForward.get(i)) {
                continue;
            }
            itemDistanceList.set(i, itemDistanceList.get(i) - distance);
        }

        for(int i = 0; i < listLength; i++) {
            if(!canMoveForward.get(i)) {
                continue;
            }
            if(i == 0) {
                if(itemDistanceList.get(i) <= 0) {
                    itemDistanceList.set(i, 0f);
                    canMoveForward.set(i, false);
                }
            } else if(itemDistanceList.get(i) < itemDistanceList.get(i - 1) + itemSize) {
                itemDistanceList.set(i, itemDistanceList.get(i - 1) + itemSize);
                canMoveForward.set(i, false);
            } else {
                //if the item can move forward, all the items behind it can also move forward
                i = listLength;
            }
        }
    }

    public void addItemToList(short id, int distFromEnd) {
        //this finds the index of the element before the element that is greater than the distFromEnd
        int index = getNewItemIndex(distFromEnd);

        itemIdList.add(index, id);
        itemDistanceList.add(index, (float) distFromEnd);
        canMoveForward.add(index, true);
    }

    public int getNewItemIndex(int beltIndex) {
        //TODO: Add check for the case that an item is already merging from another branch
        if(itemDistanceList.size() == 0) {
            return 0;
        }

        int nextItemIndex = binarySearch(beltIndex - itemSize);

        return nextItemIndex;
    }

    public boolean canPlaceItemAt(int beltIndex) {
        if(itemDistanceList.size() == 0) {
            return true;
        }

        int nextItemIndex = binarySearch(beltIndex - itemSize);
        if(itemDistanceList.get(nextItemIndex) < beltIndex + itemSize) {
            return false;
        }

        return true;
    }

    public static BeltItemList extend(BeltItemList itemList, int extendLen) {
        for(int i = 0; i < itemList.itemDistanceList.size(); i++) {
            itemList.itemDistanceList.set(i, itemList.itemDistanceList.get(i) + extendLen);
        }
        return itemList;
    }

    public static BeltItemList combine(BeltItemList frontList, BeltItemList backList) {
        frontList.itemIdList.addAll(backList.itemIdList);
        frontList.itemDistanceList.addAll(backList.itemDistanceList);
        frontList.canMoveForward.addAll(backList.canMoveForward);
        return new BeltItemList(frontList);
    }

    public static BeltItemList split(BeltItemList conveyorItemList, int startIndex, int endIndex) {
        return new BeltItemList(new LinkedList<>(conveyorItemList.itemIdList.subList(startIndex, endIndex)),
                new LinkedList<>(conveyorItemList.itemDistanceList.subList(startIndex, endIndex)),
                new LinkedList<>(conveyorItemList.canMoveForward.subList(startIndex, endIndex)));
    }

    public int binarySearch(float target) {
        //this does a binary search, finding the index of the first element that is at a distance
        //greater then the target
        int low = 0, high = itemDistanceList.size();
        while(low != high) {
            int mid = (low + high) / 2;
            if(itemDistanceList.get(mid) <= target) {
                low = mid + 1;
            } else {
                high = mid;
            }
        }
        return high;
    }

    public void resetMoveForward() {
        for(int i = 0; i < canMoveForward.size(); i++) {
            canMoveForward.set(i, true);
        }
    }

    public List<Short> getItemIDs() {
        return itemIdList;
    }

    public List<Float> getItemDistances() {
        return itemDistanceList;
    }

}
