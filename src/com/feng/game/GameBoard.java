package com.feng.game;

import javax.imageio.ImageIO;
import javax.swing.JPanel;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;

public class GameBoard extends JPanel implements Runnable {

	private static final long serialVersionUID = 1L;
	static final int WIDTH = 300;
	static final int HEIGHT = 330;
	static final int TILE_W = 30;
	private int id, oldId=0;
	private Canvas canvas = new Canvas();
	private MailBox mb;
	private Piece p;
	private BufferedImage bg;
	Thread t = new Thread(this);
	public GameBoard(MailBox mb) {
		setPreferredSize(new Dimension(WIDTH, HEIGHT));
//		setBackground(Color.WHITE);
		setFocusable(true);
		this.mb = mb;
		initCanvas();
		addMouseMotionListener(new MouseAdapter() {
			public void mouseMoved(MouseEvent e) {
				mouseMovedControl(e);
			};
		});
		addMouseListener(new MouseAdapter(){
			public void mouseClicked(MouseEvent e) {
				mouseClickedControl(e);
			};
			});
		//t.start();
	}
	
	private void initCanvas() {
		try{
			bg = ImageIO.read(getClass().getResourceAsStream("/image/gbbg.gif"));
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.drawImage(bg, 0, 0, null);
		//for (Integer key : canvas.paintList) {
		for (Integer key : canvas.tileMap.keySet()){
			canvas.tileMap.get(key).paint(g);
		}
	}

	private synchronized void mouseMovedControl(MouseEvent e) {
		id = getId(e.getPoint());
		Tile newTile = canvas.tileMap.get(id);
		Tile oldTile;
		if(oldId == 0){
			p = mb.get();
			oldId = -1;
		}
		if(id != oldId){
			if(oldId >= 11){
				oldTile = canvas.tileMap.get(oldId);
				oldTile.unhover(p);
			}
			if(newTile != null){
				newTile.hover(p);
			}
			oldId = id;
		}
		
		repaint();
	}

	private synchronized void mouseClickedControl(MouseEvent e) {
//		canvas.paintList.clear();
		Piece nextP = p;
		Tile tile = canvas.tileMap.get(id);
	//	canvas.paintList.add(id);
		if(tile.attach(nextP)){
			if (tile instanceof PreserveTile){
				nextP = ((PreserveTile) tile).getSavedPiece();
				if (nextP != null){
					p = nextP;
				}else p = mb.get();
			}else{
				nextP = canvas.transform(id);
				canvas.addBears(id);
				canvas.moveBears();
				if(tile.getAttachedPiece() != null && !tile.getAttachedPiece().ismobile()){
					canvas.combine(id);
				}
				p = mb.get();
			}
		}
		repaint();
	}

	private int getId(Point p) {
		int x = p.x / TILE_W;
		int y = p.y / TILE_W;
		int tileId = x + y * (WIDTH / TILE_W) + 1;
		return tileId;
	}

	@Override
	public void run() {
	}

	/**
	 * Class Canvas
	 * @author feng
	 *
	 */
	class Canvas {
		private HashMap<Integer, Tile> tileMap;
	//	private ArrayList<Integer> paintList;
		private LinkedList<Integer> bearList;

		public Canvas() {
			tileMap = new HashMap<Integer, Tile>();
			bearList = new LinkedList<Integer>();
		//	paintList = new ArrayList<Integer>();
			init();
		}

		private void init() {
			Tile tile;
			
			for (int i = 2; i <= (HEIGHT -TILE_W)/ TILE_W; i++) {
				for (int j = 1; j <= WIDTH / TILE_W; j++) {
					if(i == 2 && j == 1){
						tile = new PreserveTile(WIDTH/TILE_W+1, 0, TILE_W);
					}else{
					tile = new Tile(j + (i - 1) * WIDTH / TILE_W, (j - 1)
							* TILE_W, (i - 1) * TILE_W);
					}
					tileMap.put(tile.id, tile);
			//		paintList.add(tile.id);
				}
			}
		}
		
		private void addBears(int id){
			Piece p = tileMap.get(id).getAttachedPiece();
			if(p.ismobile()){
//				if(p.isTrapped() == false)
				bearList.addFirst(id);
			}
		}
		
		public void moveBears(){
			ArrayList<Integer> availTile = new ArrayList<Integer>();
			for(ListIterator<Integer> it = bearList.listIterator();it.hasNext();){
				int id = it.next();
				availTile = getAvailTile(id);
				if(availTile.size() == 0){
					Piece b = tileMap.get(id).getAttachedPiece();
					tileMap.get(id).detach();
					b = b.upgrade();
					b.setmobile(false);
					bearList.remove(id);
					tileMap.get(id).attach(b);					
				}else{
					int newId = availTile.get(nextInt(1, availTile.size()) - 1);
					tileMap.get(newId).attach(tileMap.get(id).getAttachedPiece());
					tileMap.get(id).detach();
					it.set(newId);
				}
			}
			
		}
		
		private int nextInt(int low, int hi) {
			return low + (int) (Math.random() * ((hi - low) + 1));
		}
		private ArrayList<Integer> getAvailTile(int id){
			int leftId = id - 1, rightId = id + 1, topId = id - WIDTH
			/ TILE_W, downId = id + WIDTH / TILE_W;
			ArrayList<Integer> list = new ArrayList<Integer>();
			if(tileMap.containsKey(leftId)
					&& (!(tileMap.get(leftId) instanceof PreserveTile))
					&& id % (WIDTH / TILE_W) != 1 
					&& tileMap.get(leftId).getAttachedPiece() == null) 
//					|| tileMap.get(leftId).getAttachedPiece().ismobile())
//				tileMap.
				list.add(leftId);
			if(tileMap.containsKey(rightId)
					&& (!(tileMap.get(rightId) instanceof PreserveTile))
					&& id % (WIDTH / TILE_W) != 0 
					&& tileMap.get(rightId).getAttachedPiece() == null)
//					|| tileMap.get(rightId).getAttachedPiece().ismobile());
				list.add(rightId);
			if(tileMap.containsKey(topId)
					&& (!(tileMap.get(topId) instanceof PreserveTile))
					&& tileMap.get(topId).getAttachedPiece() == null)
//					|| tileMap.get(topId).getAttachedPiece().ismobile())
				list.add(topId);
			if(tileMap.containsKey(downId)
					&& (!(tileMap.get(downId) instanceof PreserveTile))
					&& tileMap.get(downId).getAttachedPiece() == null)
//					|| tileMap.get(downId).getAttachedPiece().ismobile())
				list.add(downId);
			return list;
		}
		
		private Piece transform(int id){
			return p.transform(canvas, id);
			
//			return nextP;
		}
		
		public void combine(int id){
			ArrayList<Integer> connList; 
			Piece p = tileMap.get(id).getAttachedPiece();
			int maxLevel = p.getMaxLevel();
			while(true){
				connList = this.cntConnected(id);
				System.out.print("Current Count is " + connList.size() + " \n");
				if (connList.size() >= p.getMinUpgradeCnt() && maxLevel >= p.getLevel() && !p.ismobile()) {
					for (Integer key : connList) {
						//canvas.paintList.add(key);
						this.tileMap.get(key).detach();
					}
					p = p.upgrade();
					this.tileMap.get(id).attach(p);
				}else{
					//canvas.paintList.clear();
					break;
					}
				}
		}
		
		public ArrayList<Integer> cntConnected(int srcId) {
			
			int hierId = tileMap.get(srcId).getAttachedPiece().getHierID();
			int level = tileMap.get(srcId).getAttachedPiece().getLevel();
			
			ArrayList<Integer> list = new ArrayList<Integer>();
			int leftId = srcId - 1, rightId = srcId + 1, topId = srcId - WIDTH
					/ TILE_W, downId = srcId + WIDTH / TILE_W;
			list.add(srcId);
			tileMap.get(srcId).setChecked(true);
			if (isValidTile(leftId)
					&& srcId % (WIDTH / TILE_W) != 1
					&& isSamePiece(leftId, hierId, level)) {
				list.addAll(cntConnected(leftId));
			}
			if (isValidTile(rightId)
					&& srcId % (WIDTH / TILE_W) != 0
					&& isSamePiece(rightId, hierId, level)) {
				 list.addAll(cntConnected(rightId));
			}
			if (isValidTile(topId)
					&& isSamePiece(topId, hierId, level)) {
				 list.addAll(cntConnected(topId));
			}
			if (isValidTile(downId)
					&& isSamePiece(downId, hierId, level)) {
				 list.addAll(cntConnected(downId));
			}
			for (Integer k :list){
				canvas.getTileMap().get(k).setChecked(false);
			}
			return list;
		}
		
		public boolean isValidTile(int TileID){
			return (tileMap.containsKey(TileID)
					&& (!(tileMap.get(TileID) instanceof PreserveTile))
					&& tileMap.get(TileID).isAttached()
					&& !tileMap.get(TileID).isChecked()
					);
		}
		
		private boolean isSamePiece(int TileID, int hierID, int level){
			return (tileMap.get(TileID).getAttachedPiece().getHierID() == hierID
					&& tileMap.get(TileID).getAttachedPiece().getLevel() == level);
		}
		
		public HashMap<Integer, Tile> getTileMap(){
			return tileMap;
		}
	}
	/**
	 * *
	 * @author feng
	 *  class TILE
	 */
	class Tile {
		/*File name for the background image when something is attached*/
		private static final String empty = "/image/emptyGd.gif";
		
		/*File name for the background image when nothing is attached*/
		private static final String nonempty = "/image/nonemptyGd.jpg";
		
		/*Tile ID*/
		private int id = -1;
		
		/*Tile's x axis coordinate on canvas */
		private int xpos = 0;
		
		/*Tile's y axis coordinate on canvas*/
		private int ypos = 0;
		
		/*Indicates whether the current tile has a piece displaying (can be hovered over or attached)*/
		private boolean attached;
		
		/*Indicates whether the current tile is accounted for connecting pieces*/
		private boolean checked;
		
		/*Indicates whether the current hovered over piece is locked(attached)*/
		private boolean locked;
		
		/*Indicates whether the current attached piece is still hoping*/
//		private boolean moving;
		
		private Piece attachedPiece, hoverPiece;
		private BufferedImage emptyBg;
		private BufferedImage nonemptyBg;
		

		public Tile(int id, int x, int y) {
			this.id = id;
			xpos = x;
			ypos = y;
			attached = false;
			checked = false;
			locked = false;
			attachedPiece = null;
			hoverPiece = null;
			try {
				emptyBg= ImageIO.read(getClass().getResourceAsStream(empty));
				nonemptyBg = ImageIO.read(getClass().getResourceAsStream(nonempty));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		public boolean attach(Piece p) {
			System.out.print("Parent attach is called \n");
			return p.apply(this);
		}
		
		public void detach() {
			attachedPiece = null;
			attached = false;
			locked = false;
		}
		
		public boolean hover(Piece p){
			return p.hover(this);
		}
				
		public void unhover(Piece p){
			p.unhover(this);
		}
		
		public void setX(int xpos) {
			this.xpos = xpos;
		}

		public void setY(int ypos) {
			this.ypos = ypos;
		}

		private void paint(Graphics g) {
			if (attachedPiece != null){
				if(attached && !attachedPiece.ismobile())g.drawImage(nonemptyBg, xpos, ypos, null);
				else g.drawImage(emptyBg, xpos, ypos, null);
				Dimension dm = attachedPiece.getImgDemension();
				int yaxis;
				if(this instanceof PreserveTile){
					yaxis = ypos - (dm.height - GameBoard.TILE_W) - 10;
				}else{
					yaxis = ypos - (dm.height - GameBoard.TILE_W) - 5;
				}
				//ypos -= dm.height - GameBoard.TILE_W; 
				g.drawImage(attachedPiece.getImage(), xpos, yaxis, null);
			}else{
				g.drawImage(emptyBg, xpos, ypos, null);
			}
		}
		
		public void setAttached(boolean b) {
			attached = b;
		}
		
		public boolean isAttached() {
			return attached;
		}

		private boolean isChecked() {
			return checked;
		}

		private void setChecked(boolean b) {
			checked = b;
		}
		
		public void setAttachedPiece(Piece p){
			attachedPiece = p;
		}
		
		public void setHoverPiece(Piece p){
			hoverPiece = p;
		}

		public Piece getAttachedPiece() {
			return attachedPiece;
		}
		
		public Piece getHoverPiece(){
			return hoverPiece;
		}
		
		public void setLocked(boolean b){
			locked = b;
		}
		
		public boolean isLocked(){
			return locked;
		}
		public Point getPoint(){
			return new Point(xpos,ypos);
		}
	}
	/**
	 * Class PreservTile, the plate
	 * @author Willie
	 *
	 */
	class PreserveTile extends Tile{
		
		private static final String platefh = "/image/plate.gif";
		private Piece savedPiece;
		private BufferedImage plate;
		
		public PreserveTile (int id, int x, int y){
			super(id, x, y);
			try {
				plate = ImageIO.read(getClass().getResourceAsStream(platefh));
			} catch (IOException e) {
				e.printStackTrace();
			}
			super.nonemptyBg = plate;
			super.emptyBg = plate;
			savedPiece = null;
		}
		
		@Override
		public boolean attach(Piece p){
			System.out.print("PreserveTile is called");
			if(!super.attach(p)){
				System.out.print("replacing \n");
				savedPiece = super.attachedPiece;
				super.attachedPiece = p;
			}
			return true;
		}
		
		public Piece getSavedPiece(){
			return savedPiece;
		}
		
		public void setSavedPiece(Piece p){
			savedPiece = p;
		}
	}

}
