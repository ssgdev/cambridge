package org.ssg.Cambridge;

import java.util.ArrayList;
import java.io.*;

import net.java.games.input.Controller;

import org.ini4j.*;
import org.newdawn.slick.Color;
import org.newdawn.slick.Font;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.Input;
import org.newdawn.slick.KeyListener;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.Sound;
import org.newdawn.slick.SpriteSheet;
import org.newdawn.slick.AngelCodeFont;
import org.newdawn.slick.geom.Shape;
import org.newdawn.slick.state.BasicGameState;
import org.newdawn.slick.state.StateBasedGame;

import paulscode.sound.SoundSystem;
import paulscode.sound.SoundSystemConfig;

public class GameplayState extends BasicGameState implements KeyListener{

	final String RESDIR = "res/";
	
	final String TENNIS = "TENNIS";
	final String SQUASH = "SQUASH";
	final String SOCCER = "SOCCER";
	final String HOCKEY = "HOCKEY";
	
	public int stateID = 1;
	
	int gameType;
	int NUMGAMES;
	String NAME;
	float gameModeAlpha;
	
	public float SCREENWIDTH;
	public float SCREENHEIGHT;
	public int ACTIONCAM;
	public float maxZoom;
	
	public int FIELDWIDTH;// = 1600;
	public int FIELDHEIGHT;// = 900;
	public int FULLCOURT;// = 1;//1 is full court, 0 is halfs
	public int GOALSIZE;
	public int GOALTYPE;
	public float KICKRANGE;// = 50f;
	public float BALLSIZE = 20f;
	public int postWidth;//Is actually the size of goalie box
	public Goal[] goals;
	
	
	private int minX, minY;//The top left bounds of active objects
	private int maxX, maxY;// bottom right bounds
	float tempX, tempY;//Width and height of camera 'box'
	private int viewX, viewY;//Top left corner of the camera
	private float scaleFactor;
	private int boundingWidth = 100;
	
	AngelCodeFont font, font_white, font_small;
	Image triangle;
	Image goalScroll1, goalScroll2, goalScroll1v, goalScroll2v, goalScroll;
	int scrollX;//For the "GOAL" scroll
	int scrollY;
	int scrollXDir, scrollYDir;
	float resetVelocity[], targetX, targetY;
	
	Ball ball;//temporary, just for testing purposes
	Player p1;
	Player p2;
	Player[] players;
	
	float[] kickFloat;//Unit vector to set ball velocity after kicking
	float[] spinFloat;//vector used to store orthogonal projection of player's v on kickFloat
	float[] tempTrailArr;//Used in trail drawing, size 4
	
	int centreX;
	int centreY;
	
	int scores[];//Scores for p1 and p2
	boolean scored;//Did a goal just get scored
	
	SoundSystem mySoundSystem;
	Controller c1, c2;
	boolean c1Exist, c2Exist;
	
	public GameplayState(int i, boolean renderoff, int gt){
		stateID = i;
		gameType = gt;
	}
	
	public void getControllers(Controller c1, Controller c2) {
		this.c1 = c1;
		this.c2 = c2;
		c1Exist = (c1 != null);
		c2Exist = (c2 != null);
	}
	
	@Override
	public void init(GameContainer gc, StateBasedGame sbg) throws SlickException {
		
		initFields(gc);

		font = new AngelCodeFont(RESDIR + "8bitoperator.fnt", new Image(RESDIR + "8bitoperator_0.png"));
		font_white = new AngelCodeFont(RESDIR + "8bitoperator.fnt", new Image(RESDIR + "8bitoperator_0_white.png"));
		font_small = new AngelCodeFont(RESDIR + "8bitoperator_small.fnt", new Image(RESDIR + "8bitoperator_small_0.png"));
		triangle = new Image(RESDIR + "triangle.png");
		goalScroll1 = new Image(RESDIR + "goal.png");
		goalScroll2 = new Image(RESDIR + "goal_own.png");
		goalScroll1v = new Image(RESDIR + "goal_v.png");
		goalScroll2v = new Image(RESDIR + "goal_own_v.png");
		goalScroll = goalScroll1;
	}

	public void initFields(GameContainer gc){
		
		Ini ini;
		
		float[] playerConsts = new float[8];
		float[] ballConsts = new float[3];
		
		try {
			ini = new Ini(new File(RESDIR + "config.cfg"));
			
			NUMGAMES = ini.get("CONF","NUMGAMES", int.class);
			SCREENWIDTH = ini.get("CONF", "SCREENWIDTH", float.class);
			SCREENHEIGHT = ini.get("CONF", "SCREENHEIGHT", float.class);
			ACTIONCAM = ini.get("CONF", "ACTIONCAM", int.class);
			
			Ini.Section section = ini.get(""+gameType);
			
			NAME = section.get("NAME");
			FIELDWIDTH = section.get("FIELDWIDTH", int.class);
			FIELDHEIGHT = section.get("FIELDHEIGHT", int.class);
			GOALSIZE = section.get("GOALSIZE", int.class);
			postWidth = GOALSIZE+150;
			if(postWidth>FIELDHEIGHT)
				postWidth = FIELDHEIGHT;
			GOALTYPE = section.get("GOALTYPE", int.class);
			FULLCOURT = section.get("FULLCOURT", int.class);
			KICKRANGE = section.get("KICKRANGE", float.class);
			
			playerConsts[0] = section.get("VELMAG", float.class);
			playerConsts[1] = section.get("POWERVELMAG", float.class);
			playerConsts[2] = section.get("KICKCOOLDOWN", float.class);
			playerConsts[3] = section.get("MAXPOWER", float.class);
			playerConsts[4] = section.get("POWERCOOLDOWN", float.class);
			playerConsts[5] = section.get("NORMALKICK", float.class);
			playerConsts[6] = section.get("POWERKICK", float.class);
			playerConsts[7] = KICKRANGE;
			
			ballConsts[0] = section.get("ACCSCALE", float.class);
			ballConsts[1] = section.get("BOUNCEDAMP", float.class);
			ballConsts[2] = section.get("FLOORFRICTION", float.class);
			
		} catch (InvalidFileFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		minX = FIELDWIDTH/2-(int)SCREENWIDTH/2;
		minY = FIELDHEIGHT/2-(int)SCREENHEIGHT/2;
		maxX = FIELDWIDTH/2+(int)SCREENWIDTH/2;
		maxY = FIELDHEIGHT/2+(int)SCREENHEIGHT/2;
		viewX = minX;
		viewY = minX;
		scaleFactor = 1;
		if(maxZoom == 0){
			if(GOALTYPE == 0){
				maxZoom = 2;
			}else{
				maxZoom = 1;
			}
		}
		
		int randomNum = (int)(Math.random()*2);

		initGoals(randomNum);

		//ball = new Player(new int[]{FIELDWIDTH,FIELDHEIGHT},new int[]{Input.KEY_I, Input.KEY_K, Input.KEY_J, Input.KEY_L}, new int[]{FIELDWIDTH/2, FIELDHEIGHT/2}, Color.white);
		ball = new Ball(ballConsts, new int[]{FIELDWIDTH, FIELDHEIGHT}, goals, new float[]{FIELDWIDTH/2, FIELDHEIGHT/2}, GOALSIZE,  mySoundSystem);
		
		float[] p1Start = {FIELDWIDTH/2-250, FIELDHEIGHT/2};
		float[] p2Start = {FIELDWIDTH/2+250, FIELDHEIGHT/2};
		if(NAME.equals(TENNIS)){
			p1Start = new float[]{120, FIELDHEIGHT/2};
			p2Start = new float[]{FIELDWIDTH-120, FIELDHEIGHT/2};
			ball.setPos(350+randomNum*(FIELDWIDTH-700), FIELDHEIGHT/2+150-300*randomNum);
		}else if(NAME.equals(SQUASH)){
			p1Start = new float[]{100, FIELDHEIGHT/2-300};
			p2Start = new float[]{100, FIELDHEIGHT/2+300};
			ball.setPos(FIELDWIDTH/2-200, FIELDHEIGHT/2-300+600*randomNum);
		}
		
		int[] p1lim;
		int[] p2lim;
		if(FULLCOURT==1){
			p1lim = new int[]{0,FIELDWIDTH, 0, FIELDHEIGHT};
			p2lim = p1lim;
		}else {
			p1lim = new int[]{0,FIELDWIDTH/2, 0, FIELDHEIGHT};
			p2lim = new int[]{FIELDWIDTH/2, FIELDWIDTH, 0, FIELDHEIGHT};
		}
		p1 = new PlayerTwoTouch(0, playerConsts, new int[]{FIELDWIDTH,FIELDHEIGHT},new int[]{Input.KEY_W, Input.KEY_S, Input.KEY_A, Input.KEY_D, Input.KEY_Q}, c1, c1Exist, p1Start, p1lim, Color.orange, mySoundSystem, "slow1", p2);
		//p1 = new PlayerPuffer(0, playerConsts, new int[]{FIELDWIDTH,FIELDHEIGHT},new int[]{Input.KEY_W, Input.KEY_S, Input.KEY_A, Input.KEY_D, Input.KEY_E, Input.KEY_Q}, c1, c1Exist, p1Start, p1lim, Color.orange, mySoundSystem, "slow1", p2);
		//p2 = new PlayerTwoTouch(1, playerConsts, new int[]{FIELDWIDTH,FIELDHEIGHT},new int[]{Input.KEY_UP, Input.KEY_DOWN, Input.KEY_LEFT, Input.KEY_RIGHT, Input.KEY_RSHIFT}, c2, c2Exist, p2Start, p2lim, Color.cyan, mySoundSystem, "slow2", p1);
		p2 = new PlayerNeo(1, playerConsts, new int[]{FIELDWIDTH,FIELDHEIGHT},new int[]{Input.KEY_UP, Input.KEY_DOWN, Input.KEY_LEFT, Input.KEY_RIGHT, Input.KEY_RSHIFT}, c2, c2Exist, p2Start, p2lim, Color.cyan, mySoundSystem, "slow2", p1);
		
		p1.setOtherPlayer(p2);
		players = new Player[]{p1,p2};
		
		Input input = gc.getInput();
		//input.addKeyListener(ball);
		input.addKeyListener(p1);
		input.addKeyListener(p2);
		//ball.inputStarted();
		//p1.inputStarted();
		//p2.inputStarted();
		
		kickFloat = new float[]{0f,0f};
		spinFloat = new float[]{0f,0f};
		scores = new int[]{0,0};
		scored = false;
		
		tempTrailArr = new float[]{0,0,0,0};
		
		gameModeAlpha = 1f;
		
		scrollX = 2000;
		scrollY = 2000;
		scrollXDir = 0;
		scrollYDir = 0;
		resetVelocity = new float[]{0,-1f};
		targetX = FIELDWIDTH/2;
		targetY = 0;
		
//		gc.getGraphics().setAntiAlias(true);
	}
	
	public void initGoals(int randomNum){
		if(GOALTYPE == 0){//Left and Right goals
			goals = new Goal[2];
			goals[0] = new Goal(0,FIELDHEIGHT/2-GOALSIZE/2, -25, GOALSIZE, -1 , 0, 0);
			goals[1] = new Goal(FIELDWIDTH, FIELDHEIGHT/2-GOALSIZE/2, 25, GOALSIZE, 1, 0, 1);
		}else if(GOALTYPE == 1){//One sided goals. Squash
			goals = new Goal[1];
			goals[0] = new Goal(0, 0, -25, FIELDHEIGHT, -1, 0, randomNum);
		}else if(GOALTYPE == -1){//Horizontal Squash Goals
			goals = new Goal[1];
			goals[0] = new Goal(0,0, FIELDWIDTH, -25, 0, -1, 0);
		}else if(GOALTYPE == 2){//FOURSQUARE STYLE GOALS
			goals = new Goal[4];//= new Goal[8]
			goals[0] = new Goal(0,0,FIELDWIDTH/2,-25,0,-1,0);
			goals[1] = new Goal(0,0,-25,FIELDHEIGHT/2,-1,0,0);
			goals[2] = new Goal(FIELDWIDTH/2,0,FIELDWIDTH/2,-25,0,-1,1);
			goals[3] = new Goal(FIELDWIDTH,0,25,FIELDHEIGHT/2,1,0,1);
/**			goals[4] = new Goal(FIELDWIDTH,FIELDHEIGHT/2,25,FIELDHEIGHT/2,1,0,2);
			goals[5] = new Goal(FIELDWIDTH/2,FIELDHEIGHT,FIELDWIDTH/2,25,0,1,2);
			goals[6] = new Goal(0,FIELDHEIGHT/2,-25,FIELDHEIGHT/2,-1,0,3);
			goals[7] = new Goal(0,FIELDHEIGHT,FIELDWIDTH/2,25,0,1,3); */
		}
	}
	
	@Override
	public void render(GameContainer gc, StateBasedGame sbg, Graphics g) throws SlickException {
		
		g.setAntiAlias(true);

		g.scale(scaleFactor,  scaleFactor);
		g.translate(-viewX, -viewY);

		//Background
		g.setColor(Color.white);
		g.fillRect(-2000, -2000, FIELDWIDTH+4000, FIELDHEIGHT+4000);
		g.setColor(Color.black);
		g.fillRect(0,0, FIELDWIDTH, FIELDHEIGHT);
		g.setColor(Color.white);
		
		//Draw camera box for debug
		//g.setColor(Color.green);
		//g.drawRect(viewX, viewY, tempX, tempY);
		//g.setColor(Color.white);
		
		//Draw Field
		drawField(g);
		
		//Draw goals
		for(Goal goal: goals){
			g.setColor(players[goal.getPlayer()].getColor().darker());
			g.fillRect(goal.getX(), goal.getMinY(), goal.getWidth(), goal.getHeight());
		}
		//g.fillRect(FIELDWIDTH-10, FIELDHEIGHT/2-GOALWIDTH/2, 15, GOALWIDTH);

		//Draw Players
		for(Player p: players){
			p.render( g, triangle, BALLSIZE, font_small);
		}
		
//		g.drawLine(p1.getX(), p1.getY(), p1.getX()+spinFloat[0]*100, p1.getY()+spinFloat[1]*100);
		
		//Draw ball
		if(!scored){
			g.setColor(Color.red);
			g.rotate(ball.getX(), ball.getY(), ball.getTheta());
			g.fillRect(ball.getX()-BALLSIZE/2, ball.getY()-BALLSIZE/2, BALLSIZE, BALLSIZE);
			g.rotate(ball.getX(), ball.getY(), -ball.getTheta());
			g.setColor(Color.white);
		}
		
		//draw goal scroll
		g.drawImage(goalScroll, scrollX, scrollY);
		
		//draw gamemode
		if(gameModeAlpha>0){
			g.setFont(font_white);
			g.setColor(new Color(1f, 1f, 1f, gameModeAlpha));
			g.drawString(NAME, FIELDWIDTH/2-font.getWidth(NAME)/2, FIELDHEIGHT/2-font.getHeight("0")/2);
		}
		
		g.resetTransform();
	}
	
	public void drawField(Graphics g){
		//Draw Text
		g.setFont(font);
		if(GOALTYPE == 1 || GOALTYPE == -1){//Squash or FourSquare
			g.rotate(0,0,-90);
			g.drawString("PLAY "+NAME, -FIELDHEIGHT/2 - font.getWidth("PLAY "+NAME)/2, FIELDWIDTH);
			g.rotate(0,0,90);
			g.drawString("FERNANDO TORANGE", 0, -font.getHeight("0")-10);
			g.drawString("DIDIER DROGBLUE", 0 , FIELDHEIGHT);
			
			g.drawString(""+scores[1], FIELDWIDTH-font.getWidth(""+scores[1]) , -font.getHeight("0")-10);
			g.drawString(""+scores[0], FIELDWIDTH-font.getWidth(""+scores[0]),  FIELDHEIGHT);
		}else if(GOALTYPE==2){//Same as above but more spacing, for onesquare
			g.rotate(0,0,-90);
			g.drawString("PLAY "+NAME, -FIELDHEIGHT/2 - font.getWidth("PLAY "+NAME)/2, FIELDWIDTH+10);
			g.rotate(0,0,90);
			g.drawString("FERNANDO TORANGE", 0, -font.getHeight("0")-30);
			g.drawString("DIDIER DROGBLUE", 0 , FIELDHEIGHT+12);
			
			g.drawString(""+scores[1], FIELDWIDTH-font.getWidth(""+scores[1]) , -font.getHeight("0")-30);
			g.drawString(""+scores[0], FIELDWIDTH-font.getWidth(""+scores[0]),  FIELDHEIGHT+12);
		}else{
			//Draw game mode
			g.drawString("PLAY "+NAME, FIELDWIDTH/2 - font.getWidth("PLAY "+NAME)/2, FIELDHEIGHT);
			
			//Draw Player Names. have these be randomized for now, maybe based on character select
			g.drawString("FERNANDO TORANGE", 0, -font.getHeight("0")-10);
			g.drawString("DIDIER DROGBLUE", FIELDWIDTH - font.getWidth("DIDIER DROGBLUE"), -font.getHeight("0")-10);
			
			//Draw scores
			g.drawString(""+scores[1], FIELDWIDTH/2-font.getWidth(""+scores[1])-20 , -font.getHeight("0")-10);
			g.drawString(""+scores[0], FIELDWIDTH/2+20,  -font.getHeight("0")-10);
			g.drawString(":", FIELDWIDTH/2-font.getWidth(":")/2, -font.getHeight("0")-14);
		}
		
		//Draw the field markings
		g.setColor(Color.white);
		g.setLineWidth(1);
		g.drawRect(0,0,FIELDWIDTH, FIELDHEIGHT);
		g.setLineWidth(5);
		if(NAME.equals(SOCCER)){
			g.drawLine(FIELDWIDTH/2,0,FIELDWIDTH/2,FIELDHEIGHT);
			g.drawOval(FIELDWIDTH/2-150, FIELDHEIGHT/2-150, 300, 300);
			g.drawLine(FIELDWIDTH/2, 0, FIELDWIDTH/2, FIELDHEIGHT);
			g.drawOval(-50, FIELDHEIGHT/2-190,380,380);
			g.drawOval(FIELDWIDTH+50, FIELDHEIGHT/2-190,-380,380);
			g.setColor(Color.black);//Fill in goalie box
			g.fillRect(0,FIELDHEIGHT/2-postWidth/2-100,250,postWidth+200);
			g.fillRect(FIELDWIDTH, FIELDHEIGHT/2-postWidth/2-100,-250,postWidth+200);
			g.setColor(Color.white);//Goalie box
			g.drawRect(0,FIELDHEIGHT/2-postWidth/2-100,250,postWidth+200);
			g.drawRect(FIELDWIDTH, FIELDHEIGHT/2-postWidth/2-100,-250,postWidth+200);
			g.drawRect(0,FIELDHEIGHT/2-postWidth/2,100,postWidth);
			g.drawRect(FIELDWIDTH, FIELDHEIGHT/2-postWidth/2,-100,postWidth);
		}else if (NAME.equals(HOCKEY)){
			g.drawLine(FIELDWIDTH/2,0,FIELDWIDTH/2,FIELDHEIGHT);
			g.drawRect(FIELDWIDTH/3, 0, FIELDWIDTH/3, FIELDHEIGHT);
			g.drawOval(-40,FIELDHEIGHT/2-GOALSIZE/2,80,GOALSIZE);
			g.drawOval(FIELDWIDTH+40, FIELDHEIGHT/2-GOALSIZE/2,-80,GOALSIZE);
			g.fillOval(80+FIELDWIDTH/12-10, 80+FIELDWIDTH/12-10, 20, 20);
			g.fillOval(80+FIELDWIDTH/12-10, FIELDHEIGHT-80-FIELDWIDTH/12-10, 20, 20);
			g.fillOval(FIELDWIDTH-80-FIELDWIDTH/12-10,80+FIELDWIDTH/12-10,20,20);
			g.fillOval(FIELDWIDTH-80-FIELDWIDTH/12-10,FIELDHEIGHT-80-FIELDWIDTH/12-10,20,20);
			g.fillOval(FIELDWIDTH/2-200, 80+FIELDWIDTH/12-10, 20, 20);
			g.fillOval(FIELDWIDTH/2-200, FIELDHEIGHT-80-FIELDWIDTH/12-10, 20, 20);
			g.fillOval(FIELDWIDTH/2+200,80+FIELDWIDTH/12-10,-20,20);
			g.fillOval(FIELDWIDTH/2+200,FIELDHEIGHT-80-FIELDWIDTH/12-10,-20,20);
			g.setLineWidth(3);
			g.setColor(Color.black);
			g.fillOval(FIELDWIDTH/2-FIELDWIDTH/12, FIELDHEIGHT/2-FIELDWIDTH/12, FIELDWIDTH/6, FIELDWIDTH/6);
			g.setColor(Color.white);
			g.drawOval(FIELDWIDTH/2-FIELDWIDTH/12, FIELDHEIGHT/2-FIELDWIDTH/12, FIELDWIDTH/6, FIELDWIDTH/6);
			g.drawOval(80,80,FIELDWIDTH/6,FIELDWIDTH/6);
			g.drawOval(80,FIELDHEIGHT-80,FIELDWIDTH/6,-FIELDWIDTH/6);
			g.drawOval(FIELDWIDTH-80,80,-FIELDWIDTH/6,FIELDWIDTH/6);
			g.drawOval(FIELDWIDTH-80,FIELDHEIGHT-80,-FIELDWIDTH/6,-FIELDWIDTH/6);
			g.setLineWidth(5);
		}else if(NAME.equals(TENNIS)){
			g.drawRect(150,150,FIELDWIDTH-300,FIELDHEIGHT-300);
			g.drawLine(FIELDWIDTH/2, 150, FIELDWIDTH/2, FIELDHEIGHT-150);
			g.drawRect(150,225,FIELDWIDTH-300,FIELDHEIGHT-450);
			g.drawRect(350,225,FIELDWIDTH-700,FIELDHEIGHT-450);
			g.drawLine(350, FIELDHEIGHT/2, FIELDWIDTH-350, FIELDHEIGHT/2);
		}else if(NAME.equals(SQUASH)){
			g.drawLine(FIELDWIDTH/2-100, 0, FIELDWIDTH/2-100, FIELDHEIGHT);
			g.drawLine(0, FIELDHEIGHT/2, FIELDWIDTH/2-100, FIELDHEIGHT/2);
			g.drawRect(FIELDWIDTH/2-100, 0, -200, 200);
			g.drawRect(FIELDWIDTH/2-100,FIELDHEIGHT,-200,-200);
		}else if(NAME.equals("FOURSQUARE")){
			g.drawLine(0, FIELDHEIGHT/2, FIELDWIDTH, FIELDHEIGHT/2);
			g.drawLine(FIELDWIDTH/2, 0, FIELDWIDTH/2, FIELDHEIGHT);
		}
	}

	@Override
	public void update(GameContainer gc, StateBasedGame sbg, int delta) throws SlickException {
		
		if(p1.isSlowMoPower() || p2.isSlowMoPower()){
			delta = delta/4;
			ball.setSlowOn(true);
		}else{
			ball.setSlowOn(false);
		}
		
		gameModeAlpha -= (float)delta/1200f;
		if(gameModeAlpha < 0){
			gameModeAlpha = 0;
		}
		
		Input input = gc.getInput();
		if(input.isKeyPressed(Input.KEY_U)){
			reset(gc);
		}else if(input.isKeyPressed(Input.KEY_I)){
			gameType = (gameType+1)%NUMGAMES;
			reset(gc);
		}else if (input.isKeyPressed(Input.KEY_1)){
			gameType = 0;
			reset(gc);
		}else if (input.isKeyPressed(Input.KEY_2) && NUMGAMES>=2){
			gameType = 1;
			reset(gc);
		}else if (input.isKeyPressed(Input.KEY_3) && NUMGAMES>=3){
			gameType = 2;
			reset(gc);
		}else if (input.isKeyPressed(Input.KEY_4) && NUMGAMES>=4){
			gameType = 3;
			reset(gc);
		}else if (input.isKeyPressed(Input.KEY_5) && NUMGAMES>=5){
			gameType = 4;
			reset(gc);
		}else if (input.isKeyPressed(Input.KEY_6) && NUMGAMES>=6){
			gameType = 5;
			reset(gc);
		}else if (input.isKeyPressed(Input.KEY_7) && NUMGAMES>=7){
			gameType = 6;
			reset(gc);
		}else if (input.isKeyPressed(Input.KEY_8) && NUMGAMES>=8){
			gameType = 7;
			reset(gc);
		}else if (input.isKeyPressed(Input.KEY_9) && NUMGAMES>=9){
			gameType = 8;
			reset(gc);
		}else if( input.isKeyPressed(Input.KEY_PERIOD)){
			if(maxZoom<2)
				maxZoom+=.2f;
		}else if( input.isKeyPressed(Input.KEY_COMMA)){
			if(maxZoom>.6)
				maxZoom-=.2f;
		}else if(input.isKeyPressed(Input.KEY_ESCAPE)){
			gc.exit();
			mySoundSystem.cleanup();
		}
		
		scrollX+=delta*scrollXDir;
		if(scrollX>FIELDWIDTH+500 || scrollX<0-goalScroll.getWidth()-500)
			scrollXDir=0;
		
		scrollY+=delta*scrollYDir;
		if(scrollY>FIELDHEIGHT+500 || scrollY<0-goalScroll.getHeight()-500)
			scrollYDir=0;
		
		ball.update(delta);
		
		//Put ball back in play
		if(scored){
			if(Math.abs(ball.getX()-targetX)<15f){
				if(dist(p1)>p1.getKickRange()/2 && dist(p2)>p2.getKickRange()/2){
					ball.setVel(resetVelocity, .5f);
					ball.setPos(targetX, ball.getY());
					ball.setScored(false);
					ball.setSoundCoolDown(50);
					scored = false;
					mySoundSystem.quickPlay( true, "pneng.wav", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0.0f );
				}else{
					ball.setVel(resetVelocity, 0);
				}
			}
		}
		
		//Scoring a goal
		if((ball.getX()<=0 || ball.getX()>=FIELDWIDTH || ball.getY()<=0 || ball.getY()>=FIELDHEIGHT) && !scored){
			//Will put ball in from top if score on left and bottom if score on right
			
			for(Goal goal: goals){
				//For vertical goal
				if(ball.getY()>goal.getMinY() && ball.getY()<goal.getMaxY() && sameDir(ball.getVelX(), goal.getXDir())){
					if(ball.getLastKicker()==goal.getPlayer()){//Own Goal
						mySoundSystem.quickPlay( true, "bwuw.wav", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0.0f );
						goalScroll = goalScroll2;
					}else{
						mySoundSystem.quickPlay( true, "ding.wav", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0.0f );
						goalScroll = goalScroll1;
					}
					scores[goal.getPlayer()]++;
					scored = true;
					scrollX = goal.getX()+((goal.getXDir() < 0 ? goalScroll.getWidth() + 100 : 100)*(goal.getXDir()));
					scrollY = goal.getMinY()+goal.getHeight()/2-goalScroll.getHeight()/2;
					scrollXDir = -goal.getXDir();
					scrollYDir = 0;
				}
				//For horizontal goals
				if(ball.getX()>goal.getMinX() && ball.getX()<goal.getMaxX() && sameDir(ball.getVelY(), goal.getYDir())){
					if(ball.getLastKicker()==goal.getPlayer()){//Own Goal
						mySoundSystem.quickPlay( true, "bwuw.wav", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0.0f );
						goalScroll = goalScroll2v;
					}else{
						mySoundSystem.quickPlay( true, "ding.wav", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0.0f );
						goalScroll = goalScroll1v;
					}
					scores[goal.getPlayer()]++;
					scored = true;
					scrollX = goal.getMinX()+goal.getWidth()/2-goalScroll.getWidth()/2;
					scrollY = goal.getMinY()+((goal.getYDir() < 0 ? goalScroll.getHeight() + 100 : 100)*(goal.getYDir()));
					scrollXDir = 0;
					scrollYDir = -goal.getYDir();
				}

			}
			
			if(scored){
				if(NAME.equals(TENNIS)){//Put in at side from player baseline, as a kind of serve.
					if(ball.getLastKicker()==0){
						targetX = FIELDWIDTH-150;
						targetY = FIELDHEIGHT;
						resetVelocity[0] = 0;
						resetVelocity[1] = -1f;
					}else if(ball.getLastKicker()==1){
						targetX = 150;
						targetY = 0;
						resetVelocity[0] = 0;
						resetVelocity[1] = 1f;
					}
					
				}else if(NAME.equals(SOCCER)){//If two goals, put in from sides at center
					targetX = FIELDWIDTH/2;
					if(ball.getLastKicker()==0){
						targetY=0;
						resetVelocity[0] = 0;
						resetVelocity[1] = 1f;
					}else{
						targetY=FIELDHEIGHT;
						resetVelocity[0] = 0;
						resetVelocity[1] = -1f;
					}
				}else if(NAME.equals(SQUASH)){//Squash, put in from opposite wall
					targetX = FIELDWIDTH-10;
					targetY = FIELDHEIGHT/2;
					resetVelocity[0] = -1f;
					resetVelocity[1] = 0;
				}else if(GOALTYPE==2){//OneSquare, put in middle
					targetX = FIELDWIDTH/2;
					targetY = FIELDHEIGHT/2;
					resetVelocity[0]=0;
					resetVelocity[1]=0;
				}
				ball.setVel(new float[]{(targetX-ball.getX()),(targetY-ball.getY())}, 1f);
				ball.setAcc(new float[]{0f,0f}, 0f);
				p1.setPower();
				p2.setPower();
				ball.setScored(true);//just long enough for it to reach reset
			}
		}

		//Kicking the ball
		for(Player p: players){
			p.update(delta);
			if(p.isKicking() && !scored){//Has kick cooldown reset, can't kick if ball is being reset
				if(dist(p)<p.getKickRange()/2){//Perform a kick
					//Take the ball a step back, to prevent going through the player
					kickFloat[0] = (ball.getPrevX()-p.getX());
					kickFloat[1] = (ball.getPrevY()-p.getY());
					ball.setVel(kickFloat, p.kickStrength());
					
					//Curve the ball TODO: put an if statement around spinFloat, for controller and without controller
					spinFloat = normalNeg(p.getCurve(), kickFloat);
					ball.setAcc(spinFloat, p.kickStrength());
					ball.setLastKicker(p.getPlayerNum());
					
					if(p.flashKick()){//If you want the kick flash and sound effect
						p.setLastKick((int)ball.getPrevX(), (int)ball.getPrevY(), p.getX(), p.getY(), 1f);//player stores coordinates of itself and ball at last kicking event;
						mySoundSystem.quickPlay( true, "pow2.wav", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0.0f );
					}else{
						if(p1.isSlowMoPower() || p2.isSlowMoPower()){
							mySoundSystem.quickPlay( true, "bumpslow.wav", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0.0f );
						}else{
							mySoundSystem.quickPlay( true, "bump.wav", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0.0f );
						}
					}
					p.setKicking(0);//really this does resetKicking()
					p.setPower();//Kicking pulls out of slomo
					
					if(GOALTYPE == 1 || GOALTYPE == -1){//Squash
						for(Goal goal: goals)
							goal.changeSides();
					}
					if(GOALTYPE == 2){//If OneSquare
						for(Goal goal: goals){
							goal.setPlayerNum(ball.getLastKicker());
							goal.changeSides();//Hacky ass way to set goal to opposite side as whoever kicked
						}
					}
				}
			}
		}
		
		//Camera code
		minX = (int)Math.min((int)ball.getX(), Math.min( p1.getX(), p2.getX()));//
		minY = (int)Math.min((int)ball.getY(), Math.min( p1.getY(), p2.getY()));//
		maxX = (int)Math.max((int)ball.getX(), Math.max( p1.getX(), p2.getX()));//
		maxY = (int)Math.max((int)ball.getY(), Math.max( p1.getY(), p2.getY()));//
		
		tempX = maxX - minX;//dimensions of the camera's viewing "box"
		tempY = maxY - minY;
		if(tempX/tempY > SCREENWIDTH/SCREENHEIGHT){
			tempY = (SCREENHEIGHT/SCREENWIDTH * tempX);//Scale the box so it's in the ratio of the window
		}else{
			tempX = (SCREENWIDTH/SCREENHEIGHT * tempY);
		}
		
		//Limit the zoom to 2x
		if(tempX < SCREENWIDTH/maxZoom){
			tempX = SCREENWIDTH/maxZoom;
			tempY = SCREENHEIGHT/maxZoom;
		}

		tempX = (tempX*1.2f);
		tempY = (tempY*1.2f);
		
//		if(tempX > FIELDWIDTH){
//			tempX = FIELDWIDTH;
//			tempY = FIELDHEIGHT;
//		}

//		if(viewX<-10)
//		viewX = -10;
//	if(viewY<-10)
//		viewY = -10;
//	if(viewX+(int)tempX > FIELDWIDTH+10)
//		viewX = FIELDWIDTH +10 - (int)tempX;
//	if(viewY+(int)tempY > FIELDHEIGHT+10)
//		viewY = FIELDHEIGHT +10 - (int)tempY;

		if(ACTIONCAM == 1){
			viewX = minX - (((int)tempX - (maxX - minX))/2);
			viewY = minY - (((int)tempY - (maxY - minY))/2);
			
			scaleFactor = SCREENWIDTH/tempX;
		}else{//hacky ass shit yo
			viewX = -200;
			viewY = -150;
			scaleFactor = .5f;
					
		}
		//System.out.println(scaleFactor);
		
	}
	
	public void reset(GameContainer gc){
		if(mySoundSystem.playing("slow1"))
			mySoundSystem.pause("slow1");
		if(mySoundSystem.playing("slow2"))
			mySoundSystem.pause("slow2");
		mySoundSystem.quickPlay( true, "thud.wav", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0.0f );
		gameModeAlpha = 1f;
		initFields(gc);
	}
	
	public float dist(Player p){//dist to ball
		return (float)Math.sqrt((p.getX()-ball.getX())*(p.getX()-ball.getX()) + (p.getY()-ball.getY())*(p.getY()-ball.getY()));
	}
	
	public float dist(float x, float y){//dist to ball
		return (float)Math.sqrt((x-ball.getX())*(x-ball.getX()) + (y-ball.getY())*(y-ball.getY()));
	}
	
	public float dist(float[] a, float[]b){
		return (float)Math.sqrt((a[0]-b[0])*(a[0]-b[0])+(a[1]-b[1])*(a[1]-b[1]));
	}
	
	public float dist(float a, float b, float c, float d){
		return (float)Math.sqrt((c-a)*(c-a)+(d-b)+(d-b));
	}
	
	public float dot(float[] a, float[] b){
		return a[0]*b[0]+a[1]*b[1];
	}
	
	public float mag(float[] a){
		return (float)Math.sqrt(a[0]*a[0]+a[1]*a[1]);
	}
	
	public float[] unit(float[] f){
		float mag=0;
		for(float a: f){
			mag+= a*a;
		}
		mag = (float)Math.sqrt(mag);
		return new float[]{f[0]/mag,f[1]/mag};
	}
	
	public float[] normalNeg(float[] v, float[] w){//orthogonal proj v on w, negative
		tempX = dot(v,w)/mag(w);//Repurposing this as a temp calculation holder
		return new float[]{-v[0]+tempX*w[0], -v[1]+tempX*w[1]};
	}
	
	public boolean sameDir(float vx, int dir){
		if(vx == 0)
			return false;
		return vx/Math.abs(vx) == (float)dir;
	}
	
	public void setSoundSystem(SoundSystem ss){
		mySoundSystem=ss;
	}
	
	@Override
	public int getID() {
		return stateID;
	}

}