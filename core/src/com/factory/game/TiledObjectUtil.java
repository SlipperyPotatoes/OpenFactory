package com.factory.game;

import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.objects.EllipseMapObject;
import com.badlogic.gdx.maps.objects.PolygonMapObject;
import com.badlogic.gdx.maps.objects.PolylineMapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.math.Ellipse;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Polyline;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.ChainShape;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;


public class TiledObjectUtil {
    public static void parseTiledObjectLayer(World world, MapObjects objects, float scale) {
        for(MapObject object : objects) {

            if (object instanceof RectangleMapObject)
            {
                RectangleMapObject rectangleObject = (RectangleMapObject) object;
                Rectangle mapRectangle = rectangleObject.getRectangle();

                BodyDef bodyDef = getBodyDef(mapRectangle.getX() + mapRectangle.getWidth() / (scale * 2), mapRectangle.getY() + mapRectangle.getHeight() / (scale * 2));
                Body body = world.createBody(bodyDef);

                PolygonShape polygonShape = new PolygonShape();
                polygonShape.setAsBox(mapRectangle.getWidth() / (scale * 2), mapRectangle.getHeight() / (scale * 2));

                body.createFixture(polygonShape, 0.0f);
                body.setUserData(new Entity());
                polygonShape.dispose();
            }
            else if (object instanceof EllipseMapObject)
            {
                EllipseMapObject circleObject = (EllipseMapObject) object;
                Ellipse mapEllipse = circleObject.getEllipse();

                BodyDef bodyDef = getBodyDef((mapEllipse.x + mapEllipse.width / 2) / scale, (mapEllipse.y + mapEllipse.height / 2) / scale);
                if (mapEllipse.width != mapEllipse.height)
                    throw new IllegalArgumentException("Only circles are allowed.");

                Body body = world.createBody(bodyDef);


                CircleShape circleShape = new CircleShape();
                circleShape.setRadius(mapEllipse.width / (scale * 2f));
                body.createFixture(circleShape, 0.0f);
                body.setUserData(new Entity());
                circleShape.dispose();
            }
            else if (object instanceof PolygonMapObject)
            {
                PolygonMapObject polygonMapObject = (PolygonMapObject) object;
                Polygon mapPolygon = polygonMapObject.getPolygon();

                float[] floatArray = new float[mapPolygon.getVertices().length + 2];
                Polygon polygon = new Polygon(floatArray);

                for (int i = 0; i < mapPolygon.getVertices().length; i++) {
                    if (i % 2 == 1)
                        mapPolygon.getVertices()[i] = mapPolygon.getVertices()[i] / scale;
                    else
                        mapPolygon.getVertices()[i] = mapPolygon.getVertices()[i] / scale;
                    polygon.getVertices()[i] = mapPolygon.getVertices()[i];
                }

                BodyDef bodyDef = getBodyDef(mapPolygon.getX() / scale, mapPolygon.getY() / scale);
                Body body = world.createBody(bodyDef);

                PolygonShape polygonShape = new PolygonShape();
                polygonShape.set(polygon.getVertices());
                body.createFixture(polygonShape, 0.0f);
                body.setUserData(new Entity());
                polygonShape.dispose();
            }
            else if (object instanceof PolylineMapObject) {
                PolylineMapObject polylineMapObject = (PolylineMapObject) object;
                Polyline mapPolyline = polylineMapObject.getPolyline();

                for (int i = 0; i < mapPolyline.getVertices().length; i++) {
                    mapPolyline.getVertices()[i] = mapPolyline.getVertices()[i] / mapPolyline.getScaleX();
                }

                BodyDef bodyDef = getBodyDef(mapPolyline.getX(), mapPolyline.getY());
                Body body = world.createBody(bodyDef);

                ChainShape chainShape = new ChainShape();
                chainShape.createChain(mapPolyline.getVertices());
                body.createFixture(chainShape, 0.0f);
                body.setUserData(new Entity());
                chainShape.dispose();
            }
        }

    }

    private static BodyDef getBodyDef(float x, float y) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;
        bodyDef.position.set(x, y);

        return bodyDef;
    }

}
