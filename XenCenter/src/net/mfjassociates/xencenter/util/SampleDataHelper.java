package net.mfjassociates.xencenter.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

public class SampleDataHelper {

	public static <O,NT extends Node<?>> void createInfrastrucutreTree(TreeView<NT> tree, Class<NT> nt, Class<O> obj) throws NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Constructor<NT> ntCons = nt.getDeclaredConstructor(String.class, obj);
		TreeItem<NT> rootItem= new TreeItem<NT>(ntCons.newInstance("Pools",null));
		rootItem.setExpanded(true);
		TreeItem<NT> xenm = new TreeItem<NT>(ntCons.newInstance("xenserver-master",null));
		TreeItem<NT> xenm2 = new TreeItem<NT>(ntCons.newInstance("xenserver-master2",null));
		rootItem.getChildren().add(xenm);
		rootItem.getChildren().add(xenm2);
		TreeItem<NT> mfjmaster = new TreeItem<NT>(ntCons.newInstance("MFJMASTER PROD SBS2011",null));
		TreeItem<NT> freepbx = new TreeItem<NT>(ntCons.newInstance("FreePBX",null));
		TreeItem<NT> dns2 = new TreeItem<NT>(ntCons.newInstance("dns2",null));
		xenm.getChildren().add(mfjmaster);
		xenm2.getChildren().add(freepbx);
		xenm2.getChildren().add(dns2);
		tree.setRoot(rootItem);
	}

	public static void main(String[] args) {
	}

}
