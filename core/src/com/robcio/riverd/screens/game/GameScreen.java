package com.robcio.riverd.screens.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.utils.TiledDrawable;
import com.robcio.riverd.Map;
import com.robcio.riverd.RiverDMain;
import com.robcio.riverd.TowerBuilder;
import com.robcio.riverd.tower.projectile.ProjectileContactListener;
import com.robcio.riverd.utils.BodyFactory;
import com.robcio.riverd.utils.Constants;
import com.robcio.riverd.utils.TextureManager;

public class GameScreen implements Screen {
	private final RiverDMain main;
	private final boolean DEBUG = false;
	private boolean paused = false;

	private Box2DDebugRenderer b2dr;
	private OrthogonalTiledMapRenderer tmr;

	private TiledDrawable background;

	private World world;
	private Map map;

	private ProjectileContactListener arrowListener;
	private TowerBuilder towerBuilder;

	private TowerMenu towerMenu;

	public GameScreen(final RiverDMain main) {
		this.main = main;
	}

	@Override
	public void show() {
		background = new TiledDrawable(TextureManager.createTexReg("bgtile"));
		world = BodyFactory.getClearWorld();

		map = new Map();

		b2dr = new Box2DDebugRenderer();
		tmr = new OrthogonalTiledMapRenderer(map.getTiledMap());
		tmr.setView(main.getCamera());

		arrowListener = new ProjectileContactListener();
		world.setContactListener(arrowListener);

		towerBuilder = new TowerBuilder(main.getCamera(), map.getTowerManager());

		towerMenu = new TowerMenu(towerBuilder, main);

		InputMultiplexer im = new InputMultiplexer();
		im.addProcessor(towerMenu.getStage());
		im.addProcessor(new GestureDetector(towerMenu));
		im.addProcessor(towerBuilder);
		Gdx.input.setInputProcessor(im);

	}

	public void update(float delta) {
		towerMenu.getStage().act(delta);
		if (!paused || Gdx.input.isKeyJustPressed(Input.Keys.V)) {
			world.step(1 / 60f, 6, 2);
			arrowListener.weldEverythingWaiting();
			towerBuilder.update();
			map.update(delta * Gdx.graphics.getFramesPerSecond() / 60);
		}
		inputUpdate(delta);
	}

	private void inputUpdate(float delta) {
		if (Gdx.input.isKeyJustPressed(Input.Keys.Z)) {
			map.clearBricks();
		}
		if (Gdx.input.isKeyJustPressed(Input.Keys.X)) {
			map.clearTowers();
		}
		if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
			Gdx.app.exit();
		}
		if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
			paused = !paused;
		}
	}

	public void render(float delta) {
		update(Gdx.graphics.getDeltaTime());
		main.getCamera().update();

		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		//main.getBatch().setProjectionMatrix(main.getCamera().combined);
		main.setBatchProjectionMatrixToCombined();

		main.getBatch().begin();
		main.getBatch().disableBlending();// Tak lepiej tlo robic
		background.draw(main.getBatch(), 0, 0, RiverDMain.WIDTH, RiverDMain.HEIGHT);
		main.getBatch().enableBlending();
		main.getBatch().end();

		tmr.render();

		main.getBatch().begin();
		map.renderBricksAndTowers(main.getBatch(), delta);

		towerBuilder.draw(main.getBatch());

		main.getFont().draw(main.getBatch(), "FPS: " + Gdx.graphics.getFramesPerSecond() + ", proj: "
				+ map.getTowerManager().getNumberOfProjectiles(), 2, 15);

		main.getBatch().end();

		towerMenu.getStage().draw();

		if (DEBUG) {
			b2dr.render(world, main.getCamera().combined.scl(Constants.PPM));
		}
	}

	@Override
	public void resize(int width, int height) {
		towerMenu.resize(width, height);
	}

	@Override
	public void pause() {

	}

	@Override
	public void resume() {

	}

	@Override
	public void hide() {

	}

	@Override
	public void dispose() {
		if (world != null)
			world.dispose();
		if (tmr != null)
			tmr.dispose();
		if (b2dr != null)
			b2dr.dispose();
		if (towerMenu != null)
			towerMenu.getStage().dispose();
	}

}
