package org.ssg.Cambridge;
import org.newdawn.slick.AngelCodeFont;
import org.newdawn.slick.Color;
import org.newdawn.slick.Font;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.BasicGameState;
import org.newdawn.slick.state.StateBasedGame;

import paulscode.sound.SoundSystem;
import paulscode.sound.SoundSystemConfig;

public class MenuPauseState extends BasicGameState {
	
	private GlobalData data;
	private SoundSystem mySoundSystem;
	
	CambridgePlayerAnchor[] anchors;
	
	private int stateID;
	private boolean shouldRender;
	
	private boolean down, up, left, right, back, enter;
	private int inputDelay;
	private final int inputDelayConst = 200;
	
	private int selected;
	private float cursorY, cursorYTarget;
	
	Font font, font_white, font_small;
	String[] menuOptions;
	
	public MenuPauseState(int n, boolean renderon){
		stateID = n;
		shouldRender = renderon;
	}
	
	public void setShouldRender(boolean renderon){
		shouldRender = renderon;
	}
	
	@Override
	public void init(GameContainer gc, StateBasedGame sbg)
			throws SlickException {
		data = ((Cambridge) sbg).getData();
		mySoundSystem = data.mySoundSystem();
		
		up = false;
		down = false;
		left = false;
		right = false;
		enter = false;
		back = false;
		inputDelay = 0;
		
		cursorY = 0;
		cursorYTarget = 0;
		
		font = new AngelCodeFont(data.RESDIR + "8bitoperator.fnt", new Image(data.RESDIR + "8bitoperator_0.png"));
		font_white = new AngelCodeFont(data.RESDIR + "8bitoperator.fnt", new Image(data.RESDIR + "8bitoperator_0_white.png"));
		font_small = new AngelCodeFont(data.RESDIR + "8bitoperator_small.fnt", new Image(data.RESDIR + "8bitoperator_small_0.png"));
		
		menuOptions = new String[] {
				"Resume",
				"Change Character",
				"Exit",
		};
		
		anchors = data.playerAnchors();
		
		reset();
	}

	public void reset() {
		selected = 0;
//		cursorY = data.screenHeight()/3f-font_small.getLineHeight()/2f-5;
//		cursorYTarget = cursorY;
	}
	
	@Override
	public void render(GameContainer gc, StateBasedGame sbg, Graphics g)
			throws SlickException {
		if(!shouldRender)
			return;
	
	}

	@Override
	public void update(GameContainer gc, StateBasedGame sbg, int delta)
			throws SlickException {

	}
	
	@Override
	public void enter(GameContainer gc, StateBasedGame sbg){
		reset();
	}

	
	public float approachTarget(float val, float target, float inc){

		if(val<target){
			val+=inc;
			if(val>target)
				val=target;
		}
		if(val>target){
			val-=inc;
			if(val<target)
				val=target;
		}

		return val;
	}
	
	@Override
	public int getID() {
		return stateID;
	}

}
