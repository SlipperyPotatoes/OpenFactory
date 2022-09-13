package com.factory.game.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.QueryCallback;
import com.factory.game.ChainableBuilding;
import com.factory.game.ConveyorBelt;
import com.factory.game.Entity;
import com.factory.game.FactoryGame;
import com.factory.game.LogisticBuilding;

public class Utils {
    public static Vector2 getTouchBuildCords() {
        return new Vector2((float) Math.ceil(getTransformedTouch().x)-1,(float) Math.ceil(getTransformedTouch().y)-1);
    }

    public static Vector2 getTouchBuildCords(Vector2 inputVector) {
        return new Vector2((float) Math.ceil(inputVector.x)-1,(float) Math.ceil(inputVector.y)-1);
    }

    public static Vector3 getTransformedTouch() {
        return FactoryGame.gameCamera.unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));
    }

    //(Independent Scale) returns a NEW scaled vector, not affecting the input
    public static Vector2 iScl(Vector2 inputVector, float scale){
        return new Vector2(inputVector.x * scale, inputVector.y * scale);
    }

    //(Independent Addition)
    public static Vector2 iAdd(Vector2 inputVector, float transform){
        return new Vector2(inputVector.x + transform, inputVector.y + transform);
    }

    //query overlap
    public static boolean queryRectOverlap(float centerX, float centerY, float dimensionX, float dimensionY) {
        final boolean[] query = {false};

        FactoryGame.world.QueryAABB(new QueryCallback() {
            @Override
            public boolean reportFixture(Fixture fixture) {
                if(fixture.getBody() == null) query[0] = false;
                else {
                    Entity entity = (Entity) fixture.getBody().getUserData();
                    query[0] = !entity.isDead();
                }
                return false;
            }
        },centerX-dimensionX,centerY-dimensionY,centerX+dimensionX,centerY+dimensionY);


        return query[0];
    }

    public static boolean queryCenterOverlap(float x, float y) {
        final boolean[] query = {false};

        FactoryGame.world.QueryAABB(new QueryCallback() {
            @Override
            public boolean reportFixture(Fixture fixture) {
                if(fixture.getBody() == null) query[0] = false;
                else {
                    Entity entity = (Entity) fixture.getBody().getUserData();
                    query[0] = !entity.isDead();
                }
                return false;
            }
        },x,y,x,y);

        return query[0];
    }

    public static boolean queryCenterTouchOverlap(float boxSideLength) {
        final boolean[] query = {false};
        Vector2 touchCords = getTouchBuildCords();

        FactoryGame.world.QueryAABB(new QueryCallback() {
            @Override
            public boolean reportFixture(Fixture fixture) {
                if(fixture.getBody() == null) query[0] = false;
                else {
                    Entity entity = (Entity) fixture.getBody().getUserData();
                    query[0] = !entity.isDead();
                }
                return false;
            }
        },touchCords.x,touchCords.y,touchCords.x+boxSideLength,touchCords.y+boxSideLength);


        return query[0];
    }

    public static boolean queryCenterTouchOverlap() {
        final boolean[] query = {false};
        Vector2 touchCords = getTouchBuildCords().add(0.5F,0.5F);

        FactoryGame.world.QueryAABB(new QueryCallback() {
            @Override
            public boolean reportFixture(Fixture fixture) {
                if(fixture.getBody() == null) query[0] = false;
                else {
                    Entity entity = (Entity) fixture.getBody().getUserData();
                    query[0] = !entity.isDead();
                }
                return false;
            }
        }, touchCords.x, touchCords.y, touchCords.x, touchCords.y);


        return query[0];
    }

    //get overlap
    public static Body getRectOverlap(float centerX, float centerY, float dimensionX, float dimensionY) {
        final Body[] overlappedBody = new Body[1];
        FactoryGame.world.QueryAABB(new QueryCallback() {
            @Override
            public boolean reportFixture(Fixture fixture) {
                if(fixture.getBody() == null) overlappedBody[0] = null;
                else {
                    Entity entity = (Entity) fixture.getBody().getUserData();
                    if (entity.isDead()) {
                        overlappedBody[0] = null;
                    } else {
                        overlappedBody[0] = fixture.getBody();
                    }
                }
                return false;
            }
        },centerX-dimensionX,centerY-dimensionY,centerX+dimensionX,centerY+dimensionY);


        return overlappedBody[0];
    }

    public static Body getCenterOverlap(float x, float y) {
        final Body[] overlappedBody = new Body[1];
        FactoryGame.world.QueryAABB(new QueryCallback() {
            @Override
            public boolean reportFixture(Fixture fixture) {
                if(fixture.getBody() == null) overlappedBody[0] = null;
                else {
                    Entity entity = (Entity) fixture.getBody().getUserData();
                    if (entity.isDead()) {
                        overlappedBody[0] = null;
                    } else {
                        overlappedBody[0] = fixture.getBody();
                    }
                }
                return false;
            }
        },x/*-0.01F*/,y/*-0.01F*/,x/*+0.01F*/,y/*+0.01F*/);

        return overlappedBody[0];
    }


    public static Entity getIfFrom(Class<?> assignedClass, float x, float y) {
        final Class<?> aClass = assignedClass; //anon methods cannot accept non-final params,
        //so a final copy of the assigned class must be made
        final Entity[] entity = new Entity[1];
        FactoryGame.world.QueryAABB(new QueryCallback() {
            @Override
            public boolean reportFixture(Fixture fixture) {
                entity[0] = ifAssignableFrom(aClass, fixture);
                return false;
            }
        },x,y,x,y);

        return entity[0];
    }


    //if the fixture at least inherits the class param, then it will return it
    private static Entity ifAssignableFrom(Class<?> assignedClass, Fixture fixture) {
        if(fixture.getBody() == null) return null;
        Entity entity = (Entity) fixture.getBody().getUserData();
        if(entity.isDead()) return null;
        if(!assignedClass.isAssignableFrom(entity.getClass())) return null;
        return entity; //what this method returns should still be cast to the assignedClass type
    }

    public static ChainableBuilding getIfChainable(float x, float y) {
        final Body[] overlappedBody = new Body[1];
        FactoryGame.world.QueryAABB(new QueryCallback() {
            @Override
            public boolean reportFixture(Fixture fixture) {
                if(fixture.getBody() == null) overlappedBody[0] = null;
                else {
                    Entity entity = (Entity) fixture.getBody().getUserData();
                    if (entity.isDead()) {
                        overlappedBody[0] = null;
                    } else {
                        overlappedBody[0] = fixture.getBody();
                    }
                }
                return false;
            }
        },x,y,x,y);
        if(overlappedBody[0] == null) return null;

        if(!ChainableBuilding.class.isAssignableFrom(overlappedBody[0].getUserData().getClass())) return null;


        return (ChainableBuilding) overlappedBody[0].getUserData();
    }
}
