package com.mygdx.flappybird;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.Random;

public class Main extends ApplicationAdapter {

	private SpriteBatch batch;

	private Texture[] floppys;
	private Texture[] backgroundFires;
	private Texture background;
	private Texture startGameBackground;
	private Texture topPipe;
	private Texture bottomPipe;
	private Texture gameOver;

	private Circle floppyCircle;
	private Rectangle topPipeRectangle;
	private Rectangle bottomPipeRectangle;
	private Rectangle bottomScreenRectangle;
	private Rectangle topScreenRectangle;

	private float width;
	private float height;
	private float floppyImgDisplayed = 0;
	private float fireImgDisplayed = 0;
	private float gravity = 0;
	private float initialPositionY = 0;
	private float pipeInitialPositionX;
	private float pipeInitialPositionY;
	private float spaceBetweenPipes;
	private float birdHorizontalPosition = 0;
	private Random random;
	private int score = 0;
	private int highScore = 0;
	private boolean hasPipePassed;
	private int gameFase = 0;


	BitmapFont scoreText;
	BitmapFont retryText;
	BitmapFont highScoreText;
	BitmapFont startGameText;

	Sound flyingSound;
	Sound collisionSound;
	Sound scoreSound;
	Music gameSound;

	Preferences preferences;

	private OrthographicCamera camera;
	private Viewport viewport;
	private final float VIRTUAL_WIDTH = 720;
	private final float	VIRTUAL_HEIGHT = 1280;


	@Override
	public void create() {
		intializeTextures();
		initializeObjects();
	}

	@Override
	public void render() {

		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

		engine();
		scoreValidation();
		imgDisplayedChange();
		drawObjects();
		collisions();
	}

	private void collisions() {
		floppyCircle.set(50 + birdHorizontalPosition + floppys[0].getWidth() / 2, initialPositionY + floppys[0].getHeight() / 2, floppys[0].getWidth() / 2);
		topScreenRectangle.set(0, height, width, 3);
		bottomScreenRectangle.set(0, 0, width, 3);
		topPipeRectangle.set(pipeInitialPositionX, height / 2 + spaceBetweenPipes / 2 + pipeInitialPositionY, topPipe.getWidth(), topPipe.getHeight());
		bottomPipeRectangle.set(pipeInitialPositionX, height / 2 - bottomPipe.getHeight() - spaceBetweenPipes / 2 + pipeInitialPositionY,
								bottomPipe.getWidth(), bottomPipe.getHeight());

		if (Intersector.overlaps(floppyCircle, topPipeRectangle) || Intersector.overlaps(floppyCircle, bottomPipeRectangle) ||
			Intersector.overlaps(floppyCircle, topScreenRectangle) || Intersector.overlaps(floppyCircle, bottomScreenRectangle)) {
			if (gameFase == 1) {
				collisionSound.play();
				gameFase = 2;
			}
		}
	}

	private void scoreValidation() {
		if (pipeInitialPositionX < 40 - floppys[0].getWidth() && !hasPipePassed) {
			score++;
			hasPipePassed = true;
			scoreSound.play();
		}
	}

	private void imgDisplayedChange() {
		floppyImgDisplayed += Gdx.graphics.getDeltaTime() * 10;
		fireImgDisplayed += Gdx.graphics.getDeltaTime() * 10;

		//change bird img so it seems that que is flapping the wings
		if (floppyImgDisplayed > 3) {
			floppyImgDisplayed = 0;
		}

		//change the fire img so it seems that is burning
		if (fireImgDisplayed > 2) {
			fireImgDisplayed = 0;
		}
	}

	private void engine() {
		boolean hasTouchedTheScreen = Gdx.input.justTouched();

		switch (gameFase) {
			case 0:
				startGame(hasTouchedTheScreen);
				break;
			case 1:
				running(hasTouchedTheScreen);
				break;
			case 2:
				resetGame(hasTouchedTheScreen);
				break;
		}
	}

	private void startGame(boolean hasTouchedTheScreen) {
		if (hasTouchedTheScreen) {
			gravity = -15;
			gameFase = 1;
			flyingSound.play();
			gameSound.play();
		}
	}

	private void running(boolean hasTouchedTheScreen) {
		pipeInitialPositionX -= Gdx.graphics.getDeltaTime() * 200;

		if (pipeInitialPositionX < -bottomPipe.getWidth()) {
			pipeInitialPositionX = width;
			pipeInitialPositionY = random.nextInt(500) - 250;
			hasPipePassed = false;
		}

		if (initialPositionY > 0 || hasTouchedTheScreen) {
			initialPositionY -= gravity;
		}

		if (hasTouchedTheScreen) {
			gravity = -15;
			flyingSound.play();
		}

		if (!gameSound.isPlaying()) {
			gameSound.play();
		}

		gravity++;
	}

	private void resetGame(boolean hasTouchedTheScreen) {
		if (score > highScore) {
			highScore = score;
			preferences.putInteger("highScore", highScore);
			preferences.flush();
		}

		if (initialPositionY > 0 || hasTouchedTheScreen) {
			initialPositionY -= gravity;
		}

		gravity++;
		birdHorizontalPosition -= Gdx.graphics.getDeltaTime() * 500;

		if (hasTouchedTheScreen) {
			gameFase = 0;
			score = 0;
			gravity = 0;
			initialPositionY = height / 2;
			pipeInitialPositionX = width;
			birdHorizontalPosition = 0;
		}

		gameSound.stop();
	}

	private void drawObjects() {
		batch.setProjectionMatrix(camera.combined);

		batch.begin();

		if (gameFase == 0) {
			startGameText.draw(batch, "Touch to start the game", width / 2 - 230, height - 300);
			batch.draw(startGameBackground,0, 0, width, height);
		}

		if (gameFase == 1) {
			batch.draw(background, 0, 0, width, height);
			batch.draw(floppys[(int)floppyImgDisplayed], 50 + birdHorizontalPosition, initialPositionY);
			batch.draw(bottomPipe, pipeInitialPositionX, height / 2 - bottomPipe.getHeight() - spaceBetweenPipes / 2 + pipeInitialPositionY);
			batch.draw(topPipe, pipeInitialPositionX, height / 2 + spaceBetweenPipes / 2 + pipeInitialPositionY);
			batch.draw(backgroundFires[(int)fireImgDisplayed], 0, 0, width, height);
			scoreText.draw(batch, String.valueOf(score), width / 2, height - 110);
		}

		if (gameFase == 2) {
			batch.draw(background, 0, 0, width, height);
			batch.draw(gameOver, width / 2 - gameOver.getWidth() / 2 , height / 2);
			retryText.draw(batch, "Touch to try again", width / 2 - 140, height / 2 - gameOver.getHeight() / 2);
			highScoreText.draw(batch, "Highscore: " + highScore, width / 2 - 140, height / 2 - gameOver.getHeight());
		}

		batch.end();
	}

	private void intializeTextures () {
		floppys = new Texture[3];
		floppys[0] = new Texture("floppy1.png");
		floppys[1] = new Texture("floppy2.png");
		floppys[2] = new Texture("floppy3.png");

		backgroundFires = new Texture[2];
		backgroundFires[0] = new Texture("fire_bottom1.png");
		backgroundFires[1] = new Texture("fire_bottom2.png");

		background = new Texture("blue_sky_background.png");
		startGameBackground = new Texture("startGame.png");

		topPipe = new Texture("topTower.png");
		bottomPipe = new Texture("botTower.png");
		gameOver = new Texture("game_over.png");
	}

	private void initializeObjects () {
		batch = new SpriteBatch();
		random = new Random();

		width = VIRTUAL_WIDTH;
		height = VIRTUAL_HEIGHT;
		initialPositionY = height / 2;
		pipeInitialPositionX = width;
		spaceBetweenPipes = 250;

		scoreText = new BitmapFont();
		scoreText.setColor(Color.WHITE);
		scoreText.getData().setScale(5);

		retryText = new BitmapFont();
		retryText.setColor(Color.GREEN);
		retryText.getData().setScale(2);

		highScoreText = new BitmapFont();
		highScoreText.setColor(Color.RED);
		highScoreText.getData().setScale(2);

		startGameText = new BitmapFont();
		startGameText.setColor(Color.ORANGE);
		startGameText.getData().setScale(3);

		floppyCircle = new Circle();
		topPipeRectangle = new Rectangle();
		bottomPipeRectangle = new Rectangle();
		topScreenRectangle = new Rectangle();
		bottomScreenRectangle = new Rectangle();

		flyingSound = Gdx.audio.newSound(Gdx.files.internal("som_asa.wav"));
		collisionSound = Gdx.audio.newSound(Gdx.files.internal("som_batida.wav"));
		scoreSound = Gdx.audio.newSound(Gdx.files.internal("som_pontos.wav"));
		gameSound = Gdx.audio.newMusic(Gdx.files.internal("fisherEdited.mp3"));

		preferences = Gdx.app.getPreferences("floppyBird");
		highScore = preferences.getInteger("highScore", 0);

		camera = new OrthographicCamera();
		camera.position.set(VIRTUAL_WIDTH / 2 , VIRTUAL_HEIGHT / 2, 0);
		viewport = new StretchViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, camera);
	}

	@Override
	public void resize(int width, int height) {
		viewport.update(width, height);
	}
}
