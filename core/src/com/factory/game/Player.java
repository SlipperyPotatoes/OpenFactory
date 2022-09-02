package com.factory.game;

import static com.factory.game.utils.Utils.*;
import static com.factory.game.utils.Utils.queryCenterTouchOverlap;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.factory.game.utils.Utils;

import sun.rmi.runtime.Log;

public class Player extends Entity{
    //movement
    float movementSpeed;
    float horizontalV;
    float verticalV;
    //cursor
    FACING buildFacing;
    Vector2 touchBuildCords;
    boolean itemHeld;
    Vector2 itemDimensions;
    Texture greenBox;
    Texture redBox;
    String data;
    //player graphics
    Sprite sprite;

    public Player(float x, float y, float width, float height, float movementSpeed, Texture texture) {
        super(x, y, width, height, BodyDef.BodyType.DynamicBody, false);
        this.movementSpeed = movementSpeed;
        this.sprite = new Sprite(texture);
        sprite.setOrigin(width,height);
        sprite.setSize(width*2, height*2);

        buildFacing = FACING.NORTH;
        greenBox = new Texture("greenBox.png");
        redBox = new Texture("redBox.png");
        itemHeld = true;
        itemDimensions = new Vector2(1F, 1F);
        //itemDimensions.scl(0.9F);
        data = "";
    }

    public void update() {
        updateMovementLogic();
        updateCursorLogic();

        if(Gdx.input.isKeyJustPressed(Input.Keys.H)) {
            itemHeld = !itemHeld;
        }
    }

    private void updateMovementLogic() {
        horizontalV = 0;
        verticalV = 0;

        if(Gdx.input.isKeyPressed(Input.Keys.A)) {
            horizontalV -= 1;
        }
        if(Gdx.input.isKeyPressed(Input.Keys.D)) {
            horizontalV += 1;
        }
        if(Gdx.input.isKeyPressed(Input.Keys.S)) {
            verticalV -= 1;
        }
        if(Gdx.input.isKeyPressed(Input.Keys.W)) {
            verticalV += 1;
        }
        if(Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            getBody().applyTorque(10, true);
        }
        if(Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            getBody().applyTorque(-10, true);
        }

        getBody().setLinearVelocity(horizontalV*movementSpeed, verticalV*movementSpeed);
    }

    private void updateCursorLogic() {
        touchBuildCords = getTouchBuildCords();
        handlePlacementLogic(touchBuildCords);
        handleDestructionLogic(touchBuildCords);
        handleItemDroppingLogic(touchBuildCords);
    }

    private void handlePlacementLogic(Vector2 touchBuildCords) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.R) && Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
          switch (buildFacing) {
              case NORTH:
                  buildFacing = FACING.WEST;
                  break;
              case EAST:
                  buildFacing = FACING.NORTH;
                  break;
              case SOUTH:
                  buildFacing = FACING.EAST;
                  break;
              case WEST:
                  buildFacing = FACING.SOUTH;
                  break;
          }
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            switch (buildFacing) {
                case NORTH:
                    buildFacing = FACING.EAST;
                    break;
                case EAST:
                    buildFacing = FACING.SOUTH;
                    break;
                case SOUTH:
                    buildFacing = FACING.WEST;
                    break;
                case WEST:
                    buildFacing = FACING.NORTH;
                    break;
            }
        }

        if (!itemHeld || !Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
            return;
        }

        if (itemDimensions.x%2==0) {
            if (!queryRectOverlap(touchBuildCords.x, touchBuildCords.y,
                    itemDimensions.x*0.5F-0.25F,itemDimensions.y*0.5F-0.25F)) {
                new Building(touchBuildCords,itemDimensions.x,itemDimensions.y,false);
            }
        } else {
            if (!queryRectOverlap(touchBuildCords.x+0.5F, touchBuildCords.y+0.5F,
                    itemDimensions.x*0.5F-0.25F,itemDimensions.y*0.5F-0.25F)) {
                //temporary for testing
                if(itemDimensions.x == 1) {
                    if(Gdx.input.isKeyPressed(Input.Keys.SPACE)) {
                        new ConveyorBelt(touchBuildCords, buildFacing,true);
                    } else {
                        new ConveyorBelt(touchBuildCords, buildFacing,false);
                    }
                } else {
                    new Building(touchBuildCords,itemDimensions.x,itemDimensions.y,false);
                }
            }
            Body overlapBody = getCenterOverlap(touchBuildCords.x+0.5F,touchBuildCords.y+0.5F);
            if (overlapBody != null) {
                if (ConveyorBelt.class.isAssignableFrom(overlapBody.getUserData().getClass())) {
                    ConveyorBelt conveyorBelt = (ConveyorBelt) overlapBody.getUserData();
                    if (conveyorBelt.directionFacing != buildFacing) {
                        conveyorBelt.setDirectionFacing(buildFacing);
                    }
                }
            }
        }
    }

    private void handleDestructionLogic(Vector2 touchBuildCords) {
        if(!Gdx.input.isButtonPressed(Input.Buttons.RIGHT) || !Utils.queryCenterTouchOverlap()) return;

        Body overlappedBody = getCenterOverlap(touchBuildCords.x+0.5F,touchBuildCords.y+0.5F);

        //checks if the class of the body's userdata inherits the building class
        if (!Building.class.isAssignableFrom(overlappedBody.getUserData().getClass())) return;

        Building building = (Building) overlappedBody.getUserData();
        building.destroy();
    }

    private void handleItemDroppingLogic(Vector2 touchBuildCords) {
        if(!Gdx.input.isKeyJustPressed(Input.Keys.Z)) return;
        LogisticBuilding building = (LogisticBuilding) Utils.getIfFrom(LogisticBuilding.class,
                touchBuildCords.x+0.5f,touchBuildCords.y+0.5f);
        if(building == null) return;
        if(building.canAccept((short) 1)) {
            building.receiveItem((short) 1);
        }
    }

    public void draw(SpriteBatch batch) {
        sprite.setCenter(getPosition().x, getPosition().y);
        //sprite.setRotation((float)Math.toDegrees(getBody().getAngle()));
        //sprite.draw(batch);

        //Vector2 itemDimensions = iScl(itemDimensions, 1/FactoryGame.collisionBoxScale);
        if (!itemHeld) return;
        /*switch (buildFacing) {
            case left:
                batch.draw(conveyorBelt,touchBuildCords.x,touchBuildCords.y,0.5F,0.5F,1F,1F,1F,1F, 0);
                break;
            case down:
                batch.draw(conveyorBelt,touchBuildCords.x,touchBuildCords.y,0.5F,0.5F,1F,1F,1F,1F, 90);
                break;
            case right:
                batch.draw(conveyorBelt,touchBuildCords.x,touchBuildCords.y,0.5F,0.5F,1F,1F,1F,1F, 180);
                break;
            case up:
                batch.draw(conveyorBelt,touchBuildCords.x,touchBuildCords.y,0.5F,0.5F,1F,1F,1F,1F, 270);
                break;
        }*/

        if (itemDimensions.x%2==0) {
            if (queryRectOverlap(touchBuildCords.x, touchBuildCords.y,
                    itemDimensions.x*0.5F-0.25F,itemDimensions.y*0.5F-0.25F)) {
                batch.draw(redBox, touchBuildCords.x - itemDimensions.x / 2F, touchBuildCords.y - itemDimensions.y / 2F, itemDimensions.x, itemDimensions.y);
            } else {
                batch.draw(greenBox, touchBuildCords.x - itemDimensions.x / 2F, touchBuildCords.y - itemDimensions.y / 2F, itemDimensions.x, itemDimensions.y);
            }
        } else {
            if (queryRectOverlap(touchBuildCords.x+0.5F, touchBuildCords.y+0.5F,
                    itemDimensions.x*0.5F-0.25F,itemDimensions.y*0.5F-0.25F)) {
                batch.draw(redBox, touchBuildCords.x + 0.5F - itemDimensions.x / 2F, touchBuildCords.y + 0.5F - itemDimensions.y / 2F, itemDimensions.x, itemDimensions.y);
            } else {
                batch.draw(greenBox, touchBuildCords.x + 0.5F - itemDimensions.x / 2F, touchBuildCords.y + 0.5F - itemDimensions.y / 2F, itemDimensions.x, itemDimensions.y);
            }
        }

    }

    @Override
    public void dispose() {
        greenBox.dispose();
        redBox.dispose();
    }
}
