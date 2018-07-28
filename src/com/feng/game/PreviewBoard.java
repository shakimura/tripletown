package com.feng.game;

//import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

public class PreviewBoard extends JPanel implements Runnable{

	private static final long serialVersionUID = 1L;
	private static final int WIDTH = 30;
	private static final int HEIGHT = 50;
	private ArrayList<Hierarchy> h;
	private MailBox mb;
	private Piece p, inqP;
	private BufferedImage bg;
	//private Thread t = new Thread(this);
	private int i = 1;
//	private double[] weights = {0.7, 0.2, 0.1};
	private HashMap<Integer, WeightedRandomGen> genLUT = new HashMap<Integer, WeightedRandomGen>();
	private double[] hierWeights = {2, 0.1};
	private WeightedRandomGen gen = new WeightedRandomGen(hierWeights);
	
	public PreviewBoard(MailBox mb, ArrayList<Hierarchy> h){
		setPreferredSize(new Dimension(WIDTH, HEIGHT));
		//setBackground(Color.WHITE);
		this.mb = mb;
		this.h = h;
		try{
			bg = ImageIO.read(getClass().getResource("/image/pbdbg.gif"));
		}catch(IOException e){
			e.printStackTrace();
		}
		initGen();
		p = initPiece();
	//t.start();
	}
	
	@Override
	public void run() {
		while (true){
			mb.put(p);
			inqP=p;
			p = initPiece();
			repaint();
		}
	}
	
	private Piece initPiece (){
//		int hierIndex = nextRandomInt(1, h.size())-1;
		int hierIndex = gen.next();
		WeightedRandomGen pgen = genLUT.get(hierIndex);
		int level = pgen.next();
		Piece p = h.get(hierIndex).getCurPiece(level);
		p.num = i++;
		return p;
	}
	
	private void initGen(){
		int id = 0;
		for(Hierarchy hier : h){
			double[] weights = hier.getWeight();
			WeightedRandomGen gen = new WeightedRandomGen(weights);
			genLUT.put(id++, gen);
		}
	}
	
	@Override
	protected void paintComponent(Graphics g){
		super.paintComponents(g);
		g.drawImage(bg, 0, 0,null);
		int x = 0;
		int y = 0;
		if (inqP!=null){
		g.drawImage(inqP.getImage(),x, y, null);
		}
	}
	
	private int nextRandomInt(int low, int hi) {
		return low + (int) (Math.random() * ((hi - low) + 1));
	}
	
	/**
	 * Weighted random number generator
	 * @author Willie
	 *
	 */
	public class WeightedRandomGen {
		private double[] totals;
		
		public WeightedRandomGen(double[] weights){
			totals = new double[weights.length];
			initWRNG(weights);
		}
		
		private void initWRNG(double[] weights){
			double runningTotal = 0;
			int i = 0;
			for(double w : weights){
				runningTotal += w;
				totals[i++] = runningTotal;
			}
		}
		
		public int next(){
			Random rnd = new Random(System.nanoTime());
			double rndNum = rnd.nextDouble()*totals[totals.length-1];
			int sNum = Arrays.binarySearch(totals, rndNum);
			int idx = (sNum < 0)? (Math.abs(sNum) - 1) : sNum;
			return idx + 1;
		}
	}
}
