package org.ssg.Cambridge;

import org.newdawn.slick.AngelCodeFont;
import org.newdawn.slick.Color;
import org.newdawn.slick.Font;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.Input;
import org.newdawn.slick.KeyListener;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.BasicGameState;
import org.newdawn.slick.state.StateBasedGame;

import paulscode.sound.SoundSystem;

public class GameOverState extends BasicGameState implements KeyListener {

	public boolean shouldRender;
	
	public int stateID;
	
	int[] scores;
	int numPlayers;
	String[] names = {"P1","P2","P3","P4"};
	
	GlobalData data;
	SoundSystem mySoundSystem;
	Font font, font_white, font_small;
	
	public GameOverState(int id, boolean renderOn){
		stateID = id;
		shouldRender = renderOn;
	}
	
	@Override
	public void init(GameContainer gc, StateBasedGame sbg) throws SlickException {
		
		data = ((Cambridge) sbg).getData();
		mySoundSystem = data.mySoundSystem();
		
		try {
			font = new AngelCodeFont(data.RESDIR + "8bitoperator.fnt", new Image(data.RESDIR + "8bitoperator_0.png"));
			font_white = new AngelCodeFont(data.RESDIR + "8bitoperator.fnt", new Image(data.RESDIR + "8bitoperator_0_white.png"));
			font_small = new AngelCodeFont(data.RESDIR + "8bitoperator_small.fnt", new Image(data.RESDIR + "8bitoperator_small_0.png"));
		} catch (SlickException e) {
			// TODO Auto-generated catch block
			System.out.println("Fonts not loaded properly. Uh oh. Spaghettio.");
			e.printStackTrace();
		}
		
		scores = new int[4];
		numPlayers = 2;
	}

	public void enter(GameContainer gc, StateBasedGame sbg, int[] scores) throws SlickException {
		mySoundSystem.pause("BGMGame");
		
		int first = 0;
		for (int i = scores.length - 1; i > 0; i -- ) {
  		first = 0;   //initialize to subscript of first element
			for(int j = 1; j <= i; j ++) {
      			if( scores[ j ] < scores[ first ] )
         				first = j;
  			}
  			int temp = scores[ first ];   //swap smallest found with element in position i.
  			scores[ first ] = scores[ i ];
  			scores[ i ] = temp; 
		
		String tempStr = names[first];
		names[first] = names[i];
		names[i] = tempStr;
		}
	}
	
	@Override
	public void render(GameContainer gc, StateBasedGame sbg, Graphics g) throws SlickException {
		g.setColor(Color.black);
		g.fillRect(0, 0, data.screenWidth(), data.screenHeight());
		g.setColor(Color.white);
		g.setFont(font_white);
		g.drawString("Final Score", data.screenWidth()/2-font.getWidth("FINAL SCORE")/2, 20);
		
		for(int i=0; i<numPlayers; i++){
			String str = names[i]+": "+scores[i];
			g.drawString(str, data.screenWidth()/2-font.getWidth(str)/2, 120+(font.getHeight("0")+5)*i);
			if(scores[i]==scores[0] && scores[0] != 0){
				g.drawString("WINNER!", data.screenWidth()/2-font.getWidth("WINNER!")-200, 120+(font.getHeight("0")+5)*i);
				g.drawString("WINNER!", data.screenWidth()/2+200, 120+(font.getHeight("0")+5)*i);
			}
		}
		
	}

	@Override
	public void update(GameContainer gc, StateBasedGame sbg, int delta) throws SlickException {
		
		Input input = gc.getInput();
		if(input.isKeyPressed(Input.KEY_ENTER)){
			setShouldRender(false);
			((MainMenuState)sbg.getState(data.MAINMENUSTATE)).setShouldRender(true);
			sbg.enterState(data.MAINMENUSTATE);
		}else if(input.isKeyPressed(Input.KEY_ESCAPE)){
			gc.exit();
			mySoundSystem.cleanup();
		}
	}

	public void setScores(int[] s){
		for(int i=0;i<scores.length;i++){
			if(i<s.length)
				scores[i]=s[i];
			else
				scores[i]=0;
		}
		numPlayers = s.length;
	}
	
	public void setShouldRender(boolean b) {
		shouldRender = b;
	}
	
	@Override
	public int getID() {
		return stateID;
	}



}