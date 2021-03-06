package view;

import application.GameTools;
import application.MainGame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

import data.Entity;
import data.Forest;
import data.GameSettings;
import data.Map;
import data.TileType;
import data.Village;

public class Drawer {
	public final int TILE_SIZE = 32;
	private Map map;
	private Vector2 lowerLeftOfView;
	private SpriteBatch batch;
	private OrthographicCamera camera;
	private boolean drawMoveCenter;
	private Vector2 startTouchPosition;
	private Vector2 currTouchPosition;
	private float tileScreenWidth;
	private float tileScreenHeight;
	
	public Drawer(Map map) {
		this.map = map;
		lowerLeftOfView = Vector2.Zero;		
		
		camera = new OrthographicCamera(1, GameSettings.getAspectRatio());
        camera.update();
		batch = new SpriteBatch();
		batch.enableBlending();
	}	
	public void draw(float deltaTime) {
		setupDisplay( );		
		batch.begin();		
		drawMap(deltaTime);
		drawEntities(deltaTime);
		drawVillages(deltaTime);
		drawForests(deltaTime);
		drawUi(deltaTime);		
		batch.end();
	}
	public void drawMap(float deltaTime) {		
		tileScreenWidth = GameSettings.getScreenWidth( )/TILE_SIZE;
		tileScreenHeight = GameSettings.getScreenHeight( )/TILE_SIZE;
		
		int tileOffsetX = (int)(lowerLeftOfView.x/TILE_SIZE);
		int tileOffsetY = (int)(lowerLeftOfView.y/TILE_SIZE);
		
		Vector2 viewOffset = lowerLeftOfView.cpy();
		viewOffset.sub(new Vector2(tileOffsetX*TILE_SIZE,tileOffsetY*TILE_SIZE));
		
		for (int x = 0; x < tileScreenWidth + 1; x++) {
			for (int y = 0; y < tileScreenHeight + 1; y++) {
				Vector2 position = new Vector2(x+tileOffsetX,y+tileOffsetY);
				if(!map.contains(position))
					break;
				Sprite sprite = MainGame.getTextureRepo().getTile(map.getTileType(position)).getSprite();
				drawAtLocation(sprite, x * TILE_SIZE - (int)viewOffset.x, y * TILE_SIZE - (int)viewOffset.y);
			}
		}
	}
	public void drawEntities(float deltaTime) {
		for(Entity entity : MainGame.getEntityManager( ).getEntities( )) {			
			Vector2 monsterPos = entity.getPosition().cpy().mul(TILE_SIZE).sub(lowerLeftOfView);
			if(isOnScreen(monsterPos))
				drawAtLocation(entity.getSprite(), monsterPos.x, monsterPos.y);
		}
	}
	public void drawVillages(float deltaTime) {
		for(Village entity : MainGame.getMap().getVillages( )) {			
			Vector2 pos = entity.getPosition().cpy().mul(TILE_SIZE).sub(lowerLeftOfView);
			if(isOnScreen(pos))
				drawAtLocation(MainGame.getTextureRepo().getTile(TileType.Village).getSprite(), pos.x, pos.y);
		}
	}
	public void drawForests(float deltaTime) {
		for(Forest entity : MainGame.getMap().getForests( )) {			
			Vector2 pos = entity.getPosition().cpy().mul(TILE_SIZE).sub(lowerLeftOfView);
			if(isOnScreen(pos))
				drawAtLocation(MainGame.getTextureRepo().getTile(TileType.Forest).getSprite(), pos.x, pos.y);
		}
	}
	private boolean isOnScreen(Vector2 pos) {
		Vector2 posInPixels = pos.cpy().mul(TILE_SIZE);
		Vector2 clampedLoc = posInPixels;
		
		GameTools.clamp(clampedLoc, lowerLeftOfView, getUpperRightOfView( ));
		
		return clampedLoc.epsilonEquals(posInPixels, 2);
	}	
	public void drawUi(float deltaTime) {
		if(drawMoveCenter) {
			Sprite toDraw = MainGame.getTextureRepo().getUiElement(UiElement.MoveCenter).getSprite();
			drawAtLocation(toDraw, startTouchPosition);
		}
			
	}
	private void drawAtLocation(Sprite sprite, Vector2 position) {
		drawAtLocation(sprite, position.x, position.y);
	}
	private void drawAtLocation(Sprite sprite, float x, float y) {
		//This weirdness is related to how aspect ratio is handled
		sprite.setPosition(x/GameSettings.getScreenHeight( ),y/GameSettings.getScreenHeight( ));
		sprite.draw(batch);
	}
	private void setupDisplay( ) {
		GL10 gl = Gdx.graphics.getGL10();
		gl.glViewport(0, 0, (int)GameSettings.getScreenWidth( ), (int)GameSettings.getScreenHeight( ));		
		camera.position.set(0.5f, GameSettings.getScreenHeight() / GameSettings.getScreenWidth() / 2, 0);		
		camera.update();
		camera.apply(gl);
		
		Gdx.gl.glClearColor(1, 1, 1, 1);
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		
		batch.setProjectionMatrix(camera.combined);
	}
	
	public void moveView(Vector2 movementInTiles) {
		lowerLeftOfView.add(movementInTiles.cpy().mul(TILE_SIZE));
		
		float maxX = map.getWidth() * TILE_SIZE - (GameSettings.getScreenWidth( ));
		float maxY = map.getHeight() * TILE_SIZE - (GameSettings.getScreenHeight( ));
		
		GameTools.clamp(lowerLeftOfView, new Vector2(0, 0), new Vector2(maxX, maxY));
	}

	public void dispose() {
		batch.dispose();
	}

	public void setDrawMoveCenter(boolean draw) {
		drawMoveCenter = draw;
		
	}
	public void setStartTouch(Vector2 startTouchPoint) {
		startTouchPosition = startTouchPoint.cpy( );		
		startTouchPosition.y = GameSettings.getScreenHeight() - startTouchPosition.y;
		startTouchPosition.mul(GameSettings.getAspectRatio());
	}
	public void setCurrTouch(Vector2 startTouchPoint) {
		currTouchPosition = startTouchPoint.cpy( );		
		currTouchPosition.y = GameSettings.getScreenHeight() - startTouchPosition.y;
		currTouchPosition.mul(GameSettings.getAspectRatio());
	}
	
	private Vector2 getUpperRightOfView() {
		return lowerLeftOfView.cpy().add(new Vector2(GameSettings.getScreenWidth( ), GameSettings.getScreenHeight( )));	
	}
}
