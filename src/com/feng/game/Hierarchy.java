package com.feng.game;

import java.util.ArrayList;

public class Hierarchy {

	private ArrayList<Piece> tree;
	private double[] weight;
	
	public Hierarchy(){
		tree = new ArrayList<Piece>();
	}
	
	public void setWeight(double[] w){
		weight = w;
	}
	
	public double[] getWeight(){
		return weight;
	}
	
	public void registerPieces(Piece p){
		tree.add(p);
	}
	
	public int getMaxLevel(){
		return tree.size()-1;
	}
	
	public Piece getCurPiece(int level){
		int index = level - 1;
		return tree.get(index);
	}
	
/*	public Piece upgrade(Piece p){
		int level = p.getLevel();
		int index = level - 1;
		return tree.get(++index);
	}
*/	
	public ArrayList<Piece> getTree(){
		return tree;
	}
}



