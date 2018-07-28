package com.feng.game;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
//import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class Game extends JFrame{
	
	private static final long serialVersionUID = 1L;
	private GameBoard board;
	private PreviewBoard pboard;
	private JButton startButton = new JButton("Start");
	private StartScreen startScreen;
	private ArrayList<Hierarchy> components;
	private MailBox mb = new MailBox();
	Thread t1, t2;
	
	public Game(){
		super("Triple Town Beta");
		try {
			initGameComponents();
		} catch (IOException e) {
			e.printStackTrace();
		}
		startButton.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				if (e.getActionCommand().equals("Start")){
					remove(startScreen);
					add(board, BorderLayout.CENTER);
					add(pboard, BorderLayout.NORTH);
					startButton.setText("Exit");
					pack();
					t1.start();
					t2.start();
					repaint();
					
				}
				if(e.getActionCommand().equals("Exit")){
					System.exit(0);
				}
			}
			
		});
		startScreen = new StartScreen();
		pboard = new PreviewBoard(mb, components);
		board = new GameBoard(mb);
		mb.regGameBoard(board);
		mb.regPreviewBoard(pboard);
		initUIComponents();
		t1 = new Thread(board);
		t2 = new Thread(pboard);
		
		//t1.start();
		//t2.start();
	}

	
	private void initUIComponents(){
		setPreferredSize(new Dimension(300, 400));
		setBackground(Color.WHITE);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLayout(new BorderLayout());
		add(startScreen, BorderLayout.NORTH);
//		add(board, BorderLayout.CENTER);
//		add(pboard, BorderLayout.NORTH);
		add(startButton, BorderLayout.SOUTH);
		pack();
		setVisible(true);
		setLocationRelativeTo(null);
		
	}
	
	private synchronized void initGameComponents() throws IOException{
		components = new ArrayList<Hierarchy>();
		String[] houseNames = {"/image/grass.gif", "/image/bush.gif", "/image/tree30.gif", "/image/condo.gif", "/image/sncondo30.gif","/image/house.gif", "/image/castle.gif",  "/image/crystal30.gif"};
		double[] houseWeights = {2.0, 0.3, 0.1, 0.05, 0, 0, 0, 0.05, 0.1};
		Hierarchy house = initHierarchy("Piece", houseNames, houseWeights); 
		
		String[] churchNames = {"/image/bear30.gif"};
		double[] churchWeights = {1};
		Hierarchy church = initHierarchy("Bear", churchNames, churchWeights);
		//int objectID = house.hashCode();
/*		BufferedImage img;
		house.setWeight(weight);
		
		for(int index = 0; index < fileNames.length; index++){
			int level = index + 1;
			img = ImageIO.read(getClass().getResourceAsStream(fileNames[index]));
			Piece p;
			if(index == 7) {
				p = new Robot(img, 3, house, level);
			}else if(index ==8) p = new Crystal(img, 3, house, level);
			else p = new Piece(img, 3, house, level);//house or hierID
			house.registerPieces(p);//hierarchy DOES NOT NEED TO CARRY the ENTIRE TREE INFORMATION? MAYBE ONLY THE NEXT PIECE
		}*/
		components.add(church);
		components.add(house);
	}
	
	private Hierarchy initHierarchy(String name, String[] args, double[] weights) throws IOException{
		BufferedImage img;
		Piece p;
		Hierarchy hier = new Hierarchy();
		hier.setWeight(weights);
		for(int index = 0; index < args.length; index ++){
			int level = index + 1;
			img = ImageIO.read(getClass().getResource(args[index]));
			p = Factory.createPiece(name, img, 3, hier, level);
			hier.registerPieces(p);
		}
		return hier;
	}
	
	
	public static void main(String[] args){
		new Game();
	}
	


}

class StartScreen extends JPanel{
	
	private static final long serialVersionUID = 1L;
	private BufferedImage startScrn;
	
	public StartScreen (){
		setPreferredSize(new Dimension(300,350));
		try {
			startScrn = ImageIO.read(getClass().getResource("/image/startscreen.gif"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void paintComponent(Graphics g){
		super.paintComponent(g);
		g.drawImage(startScrn, 0, 0, null);
	}
}

class Factory {
	public static Piece createPiece(String name, Image img, int up, Hierarchy hier, int level){
		if(name.equals("Piece")){
			return new Piece(img, up, hier, level);
		}
		if(name.equals("Crystal")){
			return new Crystal(img, up, hier, level);
		}
		if(name.equals("Bear")){
			return new Bear(img, up, hier, level);
		}
		if(name.equals("Robot")){
			return new Robot(img, up, hier, level);
		}
		throw new IllegalArgumentException("no such piece");
	}
}
