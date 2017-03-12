package net.mfjassociates.xencenter.util;

public class Node<OBJECT> {
	public String label;
	public OBJECT o;
	public Node(String aLabel, OBJECT anObject) {
		this.label=aLabel;
		this.o=anObject;
	}
	@Override
	public String toString() {return this.label;}; 
}