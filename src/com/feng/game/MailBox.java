package com.feng.game;

public class MailBox {
	
	private Piece p;
	private boolean ready;
	private GameBoard gb;
	public PreviewBoard pb;
	
	public MailBox(){
		p = null;
		ready = false;
	}
	
	public void regGameBoard(GameBoard gb){
		this.gb = gb;
	}
	
	public void regPreviewBoard(PreviewBoard pb){
		this.pb = pb;
	}
	
	public synchronized GameBoard getGameBoard(){
		return gb;
	}
	
	public synchronized Piece get(){
		while(!ready){
			try{
				wait();
			}catch(InterruptedException e){
				e.printStackTrace();
			}
		}
		System.out.print("get one piece \n");
		ready = false;
		notify();
		return p;
	}
	
	public synchronized void put(Piece p){
		while(ready){
			try{
				wait();
			}catch(InterruptedException e){
				e.printStackTrace();
			}
		}
		this.p = p;
		System.out.print("putting in " + p.num + " item \n");
		ready = true;
		notify();
	}
	
	public void init(Piece p){
		this.p = p;
	}
}
