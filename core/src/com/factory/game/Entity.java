package com.factory.game;

import static com.factory.game.FactoryGame.*;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;

public class Entity {
    private Body body;
    private float width;
    private float height;
    private boolean deprecated;

    public Entity(float x, float y, float width, float height, BodyDef.BodyType type, boolean isSensor) {
        BodyDef bodyDef = new BodyDef();
        FixtureDef fixtureDef = new FixtureDef();
        this.width = width;
        this.height = height;

        bodyDef.type = type;
        fixtureDef.isSensor = isSensor;

        bodyDef.position.set(x,y);

        body = world.createBody(bodyDef);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(width, height);
        fixtureDef.shape = shape;

        fixtureDef.density = 1.0F;
        fixtureDef.friction = 0;
        //fixtureDef.restitution = 1;

        body.createFixture(fixtureDef);
        shape.dispose();

        deprecated = false;

        //stores a reference of the entire object, including parts from children, in the userdata
        //because this is a pointer, any modifications made to the userdata affect the object its storing
        body.setUserData(this);
    }

    public Entity(float x, float y, float gridWidth, float gridHeight, boolean isSensor) {
        BodyDef bodyDef = new BodyDef();
        FixtureDef fixtureDef = new FixtureDef();

        this.width = gridWidth;
        this.height = gridHeight;

        bodyDef.type = BodyDef.BodyType.StaticBody;
        fixtureDef.isSensor = isSensor;

        if(width == height && width % 2 == 1) {
            bodyDef.position.set(x+0.5F,y+0.5F);
        } else if (width == height) {
            bodyDef.position.set(x,y);
        }

        body = world.createBody(bodyDef);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(width/2F, height/2F);
        fixtureDef.shape = shape;

        fixtureDef.density = 1.0F;
        fixtureDef.friction = 0;

        body.createFixture(fixtureDef);
        shape.dispose();

        deprecated = false;

        //stores a reference of the entire object, including parts from children, in the userdata
        //because this is a pointer, any modifications made to the userdata affect the object its storing
        body.setUserData(this);
    }

    //just a small fix so that the game doesn't crap itself when trying to find what class the userdata of objects made without the entity class
    public Entity() { deprecated = false; }

    public Body getBody() { return body; }

    public Vector2 getPosition() { return body.getPosition(); }

    public boolean isDead() {
        return deprecated;
    }

    public void destroy() {
        if(isDead()) return;
        deprecated = true;

        FactoryGame.addBodyToDisposal(body);
    }

    public void dispose() {}
}
