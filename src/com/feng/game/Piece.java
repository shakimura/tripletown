package com.feng.game;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import com.feng.game.GameBoard.Canvas;
import com.feng.game.GameBoard.PreserveTile;
import com.feng.game.GameBoard.Tile;

public class Piece {
	
	private Image img;
	private int minUpgradeCnt;
	private Hierarchy hier;
	private int level;
	private int hierId;
	public int num;
	private boolean mobile = false;
	
	public Piece (Image img, int up, Hierarchy hier, int level){
		this.img = img;
		minUpgradeCnt = up;
		this.hier = hier;
		this.level = level;
		hierId = hier.hashCode();
	}

	/**
	 * @return the color
	 */
	public Image getImage() {
		return img;
	}

	/**
	 * @param color the color to set
	 */
	public void setImage(Image img) {
		this.img = img;
	}
	
	public void setmobile(boolean b){
		mobile = b;
	}
	
	public boolean ismobile(){
		return mobile;
	}
	/**
	 * @return the minUpgradeCnt
	 */
	public int getMinUpgradeCnt() {
		return minUpgradeCnt;
	}

	/**
	 * @param minUpgradeCnt the minUpgradeCnt to set
	 */
	public void setMinUpgradeCnt(int minUpgradeCnt) {
		this.minUpgradeCnt = minUpgradeCnt;
	}

	public int getHierID(){
		return hierId;
	}

	public Piece upgrade(){
		int index = level - 1;
		return hier.getTree().get(++index);
		
	}
	public int getLevel(){
		return level;
	}
	
	public int getMaxLevel(){
		return hier.getMaxLevel();
	}
	
	public Piece transform(Canvas c, int id){
		return c.getTileMap().get(id).getAttachedPiece();
	}
	
	
	public Dimension getImgDemension(){
		return new Dimension(img.getWidth(null), img.getHeight(null));
	}
	
	public boolean apply(Tile t){
		if (!t.isAttached()){
			t.setAttachedPiece(this);
			t.setAttached(true);
			t.setLocked(true);
			return true;
			}else return false;
	}
	
	public boolean hover(Tile t){
		if(!t.isAttached()){
			t.setAttachedPiece(this);
			t.setAttached(false);
			t.setLocked(false);
			return true;
		}else return false;
		
	}
	
	public boolean unhover(Tile t){
		if(!t.isLocked()){
			t.detach();
			return true;
		}else return false;
	}
}

class Robot extends Piece{
	
	public Robot(Image img, int up, Hierarchy hier, int level){
		super(img, up, hier, level);
	}
	
	@Override
	public boolean apply(Tile t){
		if(!(t instanceof PreserveTile)){
			t.detach();
			return true;
		}else return super.apply(t);
		
	}
	
	@Override
	public boolean hover(Tile t){		
		if(t instanceof PreserveTile){
			super.hover(t);
		}else{ 
			if (t.isAttached() && t.isLocked()){
				t.setHoverPiece(t.getAttachedPiece());
			}
			t.setAttachedPiece(this);
			}
		return true;
	}
	
	@Override
	public boolean unhover(Tile t){
		System.out.print("unhovering... \n ");
		if(!super.unhover(t)){
			if(t.getHoverPiece()!=null){
				t.setAttachedPiece(t.getHoverPiece());
			}
		}
		return true;
	}
	
	@Override
	public Piece upgrade(){
		return this;
	}
}

class Crystal extends Piece{
	
	private Rock rock;
	private BufferedImage rockimg;
	
	public Crystal(Image img, int up, Hierarchy hier, int level){
		super(img, up, hier, level);
		try{
			
			rockimg = ImageIO.read(getClass().getResource("/image/rock30.gif"));
		}catch(IOException e){
			e.printStackTrace();
		}
		rock = new Rock(rockimg, up, hier, level);
	}
	
	public Piece transform(Canvas c, int id){
		int row = GameBoard.WIDTH/GameBoard.TILE_W;
		int leftId = id - 1, rightId = id + 1, topId = id - row, downId = id + row;
		ArrayList<Piece> plist = new ArrayList<Piece>();
		Piece pc = null;
		if(c.isValidTile(leftId) && id % row != 1) {
			pc = c.getTileMap().get(leftId).getAttachedPiece();
			plist.add(pc);
		}
		if(c.isValidTile(rightId) && id % row != 0) {
			pc = c.getTileMap().get(rightId).getAttachedPiece();
			plist.add(pc);
		}
		if(c.isValidTile(topId)) {
			pc = c.getTileMap().get(topId).getAttachedPiece();
			plist.add(pc);
		}
		if(c.isValidTile(downId)) {
			pc = c.getTileMap().get(downId).getAttachedPiece();
			plist.add(pc);
		}
		int level = 0;
		boolean combo = false;
		for(Piece p : plist){
			c.getTileMap().get(id).setAttachedPiece(p);
			int newlevel = p.getLevel();
			int newcount = c.cntConnected(id).size();
			if (newcount >= p.getMinUpgradeCnt() && newlevel > level && newlevel <= p.getMaxLevel()) {
				level = newlevel;
				pc = p;
				combo = true;
			}
			
		}
		if (combo == false) {
			pc = rock;
		}
		c.getTileMap().get(id).setAttachedPiece(pc);
		return pc;
	}
	
	@Override
	public Piece upgrade(){
		return this;
	}
	
	class Rock extends Piece{
		
		public Rock(Image img, int up, Hierarchy hier, int level){
			super(img, up, hier, level);
		}
	}
}

class Bear extends Piece{
	
	private Grave grave;
	private BufferedImage graveimg;
	
	public Bear(Image img, int up, Hierarchy hier, int level){
		super(img, up, hier, level);
		super.setmobile(true);
		try{
			
			graveimg = ImageIO.read(getClass().getResource("/image/grave30.gif"));
		}catch(IOException e){
			e.printStackTrace();
		}
		grave = new Grave(graveimg, up, hier, level);
	}	
	
	public void move(Canvas c){
		
	}
	
	@Override
	public Piece upgrade(){
		return grave;
	}
	
}

class Grave extends Piece{
	
	public Grave(Image img, int up, Hierarchy hier, int level){
		super(img, up, hier, level);
	}
}