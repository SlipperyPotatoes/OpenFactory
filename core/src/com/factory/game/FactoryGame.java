package com.factory.game;

import static com.factory.game.utils.Utils.getTouchBuildCords;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.factory.game.utils.Clock;
import com.factory.game.utils.Timer;
import com.factory.game.utils.Utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

public class FactoryGame extends ApplicationAdapter {
	static boolean DEBUG;
	public static float delta;

	//game-cam
	public static OrthographicCamera gameCamera;
	private ExtendViewport gameViewport;
	private float zoom;
	//hud-cam
	private OrthographicCamera hudCamera;
	private Viewport hudViewport;

	SpriteBatch spriteBatch;
	SpriteBatch hudBatch;
	


	//box2d
	private Box2DDebugRenderer b2dRenderer;
	public static World world;
	private static LinkedList<Body> bodyDisposal;
	public static final float collisionBoxScale = 0.9F;

	//tiled map
	private TiledMap tiledMap;
	private OrthogonalTiledMapRenderer tiledMapRenderer;
	private float tiledMapScale;
	private Rectangle spawnPoint;

	//textures
	public static TextureRegion conveyorBelt;
	public static TextureRegion altConveyorBelt;
	Texture copperChunk;
	private Texture arrow;

	//player
	Player player;

	//lists
	public static ArrayList<Item> itemList;
	public static ArrayList<Building> buildings;
	public static LinkedList<BeltChain> beltChains;


	//factory stuffs
	public static ArrayList<Timer> timerArrayList;
	public static ArrayList<Clock> clockArrayList;
	private Clock conveyorClock;
	public final static float itemSize = 1F;


	//temp
	public static Texture img;
	private String hudString;
	public static BitmapFont font;
	private FreeTypeFontGenerator fontGenerator;
	private FreeTypeFontGenerator.FreeTypeFontParameter fontParameter;
	private ShapeRenderer shapeRenderer;


	@Override
	public void create () {
		DEBUG = true;

		setupCameras();
		setupRendering();

		setupBox2d();

		setupTiledMap();

		setupTextures();

		player = new Player(spawnPoint.x/tiledMapScale,spawnPoint.y/tiledMapScale,
				0.9F,2,5, img);

		setupItemList();

		setupFactoryLists();

		setupFactoryStuffs();
		
		Gdx.input.setInputProcessor(new InputAdapter() {

			//amountY: scroll up = -1, scroll down = +1
			@Override
			public boolean scrolled(float amountX, float amountY) {
				if (amountY > 0) {
					zoom = 1.1F;
				} else if (amountY < 0) {
					zoom = 0.9F;
				}
				return true;
			}
		});


		fontCrap();
	}

	@Override
	public void render () {
		ScreenUtils.clear(0F, 0.388235294F, 0.694117647F, 1);
		delta = Gdx.graphics.getDeltaTime();

		updateLogic();
		updateGraphics();

		shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
		drawGrid();
		shapeRenderer.end();

		spriteBatch.begin();
		drawSprites();
		spriteBatch.end();



		drawRenderers();

		disposeBodies();


		hudBatch.begin();
		player.data = "";
		hudBatch.draw(conveyorBelt, 1920/2.5F,1080/2.5F,
				0.5F, 0.5F, 100F,100F,
				1F, 1F, 270-player.buildFacing.getDegrees());
		drawHud();
		hudBatch.end();
	}


	//render loop code

	private void updateLogic() {
		world.step(1/60F,6,2);

		for (int i = 0; i < timerArrayList.size(); i++) {
			timerArrayList.get(i).iterate();
		}
		for (int i = 0; i < clockArrayList.size(); i++) {
			clockArrayList.get(i).iterate();
		}



		for (Iterator<Building> listIterator = buildings.iterator(); listIterator.hasNext();) {
			Building building = listIterator.next();
			if (building.isDead()) {
				listIterator.remove();
				continue;
			}
			building.update();
		}

		for (Iterator<BeltChain> listIterator = beltChains.iterator(); listIterator.hasNext();) {
			BeltChain beltChain = listIterator.next();
			if (beltChain.isDeprecated()) {
				listIterator.remove();
				continue;
			}
			beltChain.moveItemsForward(1/60f);
		}

		if (beltChains.size() != 0) {
			hudString = beltChains.getFirst().getItemList().getItemDistances().toString();
		}
		player.update();

		if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
			Gdx.app.exit();
		}
	}
	private void updateGraphics() {
		updateZoom();
		batchUpdate();
	}

	private void updateZoom() {
		if(zoom == 1)
			return;

		gameViewport.setWorldHeight(gameViewport.getWorldHeight()*zoom);
		gameViewport.setWorldWidth(gameViewport.getWorldWidth()*zoom);
		spriteBatch.setProjectionMatrix(gameCamera.combined);
		gameViewport.apply();
		zoom=1;
	}

	private void batchUpdate() {
		gameCamera.position.set(player.getPosition().x, player.getPosition().y, 0);

		gameCamera.update();
		hudCamera.update();
		spriteBatch.setProjectionMatrix(gameCamera.combined);
		hudBatch.setProjectionMatrix(hudCamera.combined);
		shapeRenderer.setProjectionMatrix(gameCamera.combined);

		tiledMapRenderer.setView(gameCamera);
	}

	private void drawGrid() {
		//Halves the actual size to be used for calculations
		float viewportHalfWidth = gameViewport.getWorldWidth()/2;
		float viewportHalfHeight = gameViewport.getWorldHeight()/2;
		Vector2 playerPosition = player.getBody().getPosition();

		Vector2 bottomLeft = getTouchBuildCords(new Vector2(playerPosition.x-viewportHalfWidth-1, playerPosition.y-viewportHalfHeight-1));
		Vector2 topRight = getTouchBuildCords(new Vector2(playerPosition.x+viewportHalfWidth+1, playerPosition.y+viewportHalfHeight+1));

		for (int i = 0; i < (topRight.x-bottomLeft.x)+1; i++) {
			shapeRenderer.line(bottomLeft.x+i,bottomLeft.y,bottomLeft.x+i,topRight.y, Color.GRAY, Color.GRAY);
		}

		for (int i = 0; i < (topRight.y-bottomLeft.y)+1; i++) {
			shapeRenderer.line(bottomLeft.x,bottomLeft.y+i,topRight.x,bottomLeft.y+i, Color.GRAY, Color.GRAY);
		}
	}

	private void drawSprites() {

		spriteBatch.draw(img,0,1,1F,1F);

		for (Building building : buildings) {
			building.draw(spriteBatch);
		}

		for (BeltChain chain : beltChains) {
			Vector2 vec1 = chain.getBelt(0).getPosition();
			spriteBatch.draw(arrow, vec1.x-0.5F, vec1.y+0.5F, 1F, 1F);
			Vector2 vec2 = chain.getBelt(chain.getLength()-1).getPosition();
			spriteBatch.draw(arrow, vec2.x-0.5F, vec2.y+0.5F, 1F, 1F);

			chain.drawItems(spriteBatch);
		}

		player.draw(spriteBatch);
	}

	private void drawHud() {
		if (hudString != null) {
			font.draw(hudBatch, hudString, -1920 / 2.5F, -1080 / 2.5F);
		}
	}

	private void drawRenderers() {
		tiledMapRenderer.render();
		b2dRenderer.render(world, gameCamera.combined);
	}


	//initialization code
	private void setupCameras() {
		gameCamera = new OrthographicCamera();
		gameViewport = new ExtendViewport(32,24, gameCamera);
		gameViewport.apply();
		zoom = 1;


		hudCamera = new OrthographicCamera();
		hudViewport = new ExtendViewport(1920, 1080, hudCamera);
	}

	private void setupRendering() {
		spriteBatch = new SpriteBatch();
		hudBatch = new SpriteBatch();
		shapeRenderer = new ShapeRenderer();
	}

	private void setupBox2d() {
		world = new World(new Vector2(0F,0F),false);
		b2dRenderer = new Box2DDebugRenderer();
		bodyDisposal = new LinkedList<>();
	}

	private void setupTiledMap() {
		tiledMap = new TmxMapLoader().load("testMap.tmx");

		if (!tiledMap.getProperties().get("tilewidth", Integer.class).equals(tiledMap.getProperties().get("tileheight", Integer.class))) {
			System.out.println("How tf did you manage to do this, tilewidth doesn't equal tileheight????????");
		}
		tiledMapScale = tiledMap.getProperties().get("tilewidth", Integer.class);
		TiledObjectUtil.parseTiledObjectLayer(world, tiledMap.getLayers().get("collision-layer").getObjects(), tiledMapScale);

		tiledMapRenderer = new OrthogonalTiledMapRenderer(tiledMap, 1f / tiledMapScale);

		MapObjects interactionLayerObjects = tiledMap.getLayers().get("interaction-layer").getObjects();
		RectangleMapObject mapSpawn = (RectangleMapObject) interactionLayerObjects.get("spawn");
		spawnPoint = new Rectangle(mapSpawn.getRectangle());


	}

	private void setupTextures() {
		img = new Texture("important.jpg");
		conveyorBelt = new TextureRegion(new Texture("conveyorBelt.png"));
		altConveyorBelt = new TextureRegion(new Texture("conveyorBeltAlt.png"));
		arrow = new Texture("arrow.png");
	}
	
	private void setupItemList() {
		itemList = new ArrayList<>();
		itemList.add(new Item((short) 0,new Texture("copperChunk.png")));
		itemList.add(new Item((short) 1,new Texture("obsidianChunk.png")));
	}

	private void setupFactoryLists() {
		buildings = new ArrayList<>();
		beltChains = new LinkedList<>();
	}

	private void setupFactoryStuffs() {
		timerArrayList = new ArrayList<>();
		clockArrayList = new ArrayList<>();
		conveyorClock = new Clock(0.25F);
	}

	private void fontCrap() {
		fontGenerator = new FreeTypeFontGenerator(Gdx.files.internal("HSR.ttf"));
		fontParameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
		fontParameter.size = 36;
		fontParameter.borderWidth = 1;
		fontParameter.shadowOffsetX = 1;
		fontParameter.shadowOffsetY = 1;
		font = fontGenerator.generateFont(fontParameter);
		fontGenerator.dispose();
	}

	@Override
	public void dispose () {
		spriteBatch.dispose();
		hudBatch.dispose();
		img.dispose();
		//copperChunk.dispose();
		conveyorBelt.getTexture().dispose();
		altConveyorBelt.getTexture().dispose();
		arrow.dispose();
		tiledMapRenderer.dispose();
		tiledMap.dispose();
		b2dRenderer.dispose();
		world.dispose();
		font.dispose();
		shapeRenderer.dispose();
		player.dispose();
	}

	@Override
	public void resize(int width, int height) {
		gameViewport.update(width, height);
		spriteBatch.setProjectionMatrix(gameCamera.combined);


		hudViewport.update(width, height);
		hudBatch.setProjectionMatrix(hudCamera.combined);
	}

	public static void addBodyToDisposal(Body body) {
		bodyDisposal.add(body);
	}

	private void disposeBodies() {
		for(Iterator<Body> bodyDisposalIterator = bodyDisposal.iterator(); bodyDisposalIterator.hasNext();) {
			Body body = bodyDisposalIterator.next();
			world.destroyBody(body);
			bodyDisposalIterator.remove();
		}
	}
}
