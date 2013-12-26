package org.ssg.Cambridge;
import net.java.games.input.*;
import net.java.games.input.Component.Identifier;
import net.java.games.input.Component.Identifier.Axis;
import net.java.games.input.Component.Identifier.Button;

public class CambridgeController {
	private Controller c;
	private Component lStickX, lStickY, rStickX, rStickY, action, action2, menuSelect, menuBack, start, select, dpad;
	private boolean exist;
	private boolean selectFlag, backFlag, startFlag;
	
	public CambridgeController() {
		c = null;
		lStickX = null;
		lStickY = null;
		rStickX = null;
		rStickY = null;
		action = null;
		action2 = null;
		menuSelect = null;
		menuBack = null;
		start = null;
		select = null;
		dpad = null;
		exist = false;
		selectFlag = true;
		backFlag = true;
		startFlag = true;
	}
	
	public CambridgeController(Controller controller) {
		this.c = controller;
		lStickX = this.c.getComponent(Component.Identifier.Axis.X);
		lStickY = this.c.getComponent(Component.Identifier.Axis.Y);
		rStickY = this.c.getComponent(Component.Identifier.Axis.RY);
		rStickX = this.c.getComponent(Component.Identifier.Axis.RX);
		action = this.c.getComponent(Component.Identifier.Button._5);
		action2 = this.c.getComponent(Component.Identifier.Button._4);
		menuSelect = this.c.getComponent(Component.Identifier.Button._0);
		menuBack = this.c.getComponent(Component.Identifier.Button._1);
		start = this.c.getComponent(Component.Identifier.Button._7);
		select = this.c.getComponent(Component.Identifier.Button._6);
		dpad = this.c.getComponent(Component.Identifier.Axis.POV);
		exist = c.poll();
		selectFlag = true;
		backFlag = true;
		startFlag = true;
	}
	
	public float getLeftStickX() {
		return lStickX.getPollData();
	}
	
	public float getLeftStickY() {
		return lStickY.getPollData();
	}
	
	public float getRightStickX() {
		return rStickX.getPollData();
	}
	
	public float getRightStickY() {
		return rStickY.getPollData();
	}
	
	public boolean getAction() {
		return action.getPollData() == 1;
	}
	
	public boolean getAction2() {
		return action2.getPollData() == 1;
	}
	
	public boolean getMenuSelect() {
		if (selectFlag) {
			if (menuSelect.getPollData() != 1) {
				selectFlag = false;
			}
			return false;
		} else {
			if (menuSelect.getPollData() == 1) {
				selectFlag = true;
				return true;
			} else {
				return false;
			}
		}
//		return menuSelect.getPollData() == 1;
	}
	
	public boolean getMenuBack() {
		if (backFlag) {
			if (menuBack.getPollData() != 1) {
				backFlag = false;
			}
			return false;
		} else {
			if (menuBack.getPollData() == 1) {
				backFlag = true;
				return true;
			} else {
				return false;
			}
		}
//		return menuBack.getPollData() == 1;
	}
	
	public boolean getStart() {
		if (startFlag) {
			if (start.getPollData() != 1) {
				startFlag = false;
			}
			return false;
		} else {
			if (start.getPollData() == 1) {
				startFlag = true;
				return true;
			} else {
				return false;
			}
		}
		//return start.getPollData() == 1;
	}
	
	public boolean getSelect() {
		return select.getPollData() == 1;
	}
	
	public float getDPad() {
		return dpad.getPollData();
	}
	
	public boolean exists() {
		return exist;
	}
	
	public boolean poll() {
		return c.poll();
	}
	
	public String getName() {
		return c.getName();
	}
	
	public Controller c() {
		return c;
	}
	
	public Component leftStickX() {
		return lStickX;
	}
	
	public Component leftStickY() {
		return lStickY;
	}
	
	public Component rightStickX() {
		return rStickX;
	}
	
	public Component rightStickY() {
		return rStickY;
	}
	
}
