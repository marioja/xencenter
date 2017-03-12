package net.mfjassociates.xencenter.util;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.value.ChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TitledPane;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import net.mfjassociates.xencenter.util.XenApiHelper.HAS_RECORD;
import net.mfjassociates.xencenter.util.XenApiHelper.HAS_VALUE;

public class FXHelper {
	
	public static abstract class ResponsiveTask<T> extends Task<T> {
		
		public Scene getScene() {
			return scene == null ? null : scene.get();
		}
		
		public void setScene(Scene scene) {
			sceneProperty().set(scene);
		}
		
		public ResponsiveTask<T> bindScene(ReadOnlyObjectProperty<Scene> sceneProperty) {
			sceneProperty.addListener((obs, oldv, newv) -> setCursorEventHandler());
			if (sceneProperty.get() != null) {
				setCursorEventHandler();
			}
			sceneProperty().bind(sceneProperty);
			return ResponsiveTask.this;
		}

		private void setCursorEventHandler() {
			setOnScheduled(event -> {
				sceneProperty().get().setCursor(Cursor.WAIT);
			});
			setOnSucceeded(event -> {
				sceneProperty().get().setCursor(Cursor.DEFAULT);
			});
		}
		
		private ObjectProperty<Scene> sceneProperty() {
			if (scene == null) {
				scene=new ObjectPropertyBase<Scene>(null) {

					@Override
					public Object getBean() {
						return ResponsiveTask.this;
					}

					@Override
					public String getName() {
						return "sceneProperty";
					}};
			}
			return scene;
		}
		
		private ObjectProperty<Scene> scene;
		
	}

	@FunctionalInterface
	public static interface TreeItemClickHandler<T> {
		public void handleTreeItemClick(TreeItem<T> treeItem);
	}
	
	/**
	 * Interface that must be implemented by classes that will handle a JavaFX
	 * TableView that will contain the generic type T.
	 * 
	 * @author mario
	 *
	 * @param <T> The type of the item that the table view will contain
	 */
	@FunctionalInterface
	public static interface TableViewColumnHandler<T> {
		/**
		 * Default implementation of the method that will retrieve the property
		 * from the TableView item.
		 * @param colLabel - label to display in the table column heading
		 * @param colPropertyName - name of the property from the table item to
		 * display using a PropertyValueFactory.
		 * @return a TableColumn for that property
		 */
		default public TableColumn<T, String> getColumn(String colLabel, String colPropertyName) {
			TableColumn<T, String> col = new TableColumn<>(colLabel);
			col.setCellValueFactory(new PropertyValueFactory<>(colPropertyName));
			return col;
		}

		/**
		 * Default implementation of the method that will retrieve the value from the
		 * Record inner class to display in the TableColumn.
		 * 
		 * @param colLabel - label to display in the table column heading
		 * @param recordHandler - handler that will return the Record inner class instance
		 * @param colFieldName - Record inner class public field to display in the TableColumn
		 * @param connection - opaque type used to retrieve the Record inner class instance
		 * @param <R> - Record inner class type
		 * @param <C> - opaque type
		 * @return a TableColumn for that field
		 */
		default public <R, C> TableColumn<T, String> getColumnFromRecord(String colLabel, HAS_RECORD<T, R, C> recordHandler, String colFieldName, C connection) {
			TableColumn<T, String> col = new TableColumn<>(colLabel);
			col.setCellValueFactory(t -> {
				ReadOnlyStringWrapper rosw=null;
				try {
					R r = recordHandler.getRecord(t.getValue(), connection);
					Object value = ReflectionHelper.getField(r, colFieldName);
					String valueString="";
					if (value!=null) valueString=value.toString();
					rosw=new ReadOnlyStringWrapper(valueString);
				} catch (Exception e) {
					e.printStackTrace();
				}
				return rosw;
			});
			return col;

		}
		default public <R, C> TableColumn<T, String> getColumnFromValue(String colLabel, HAS_VALUE<T, C> valueProducer, String colFieldName, C connection) {
			TableColumn<T, String> col = new TableColumn<>(colLabel);
			col.setCellValueFactory(t -> {
				ReadOnlyStringWrapper rosw=null;
				try {
					Object value = valueProducer.getValue(t.getValue(), colFieldName, connection);
					String valueString="";
					if (value!=null) valueString=value.toString();
					rosw=new ReadOnlyStringWrapper(valueString);
				} catch (Exception e) {
					e.printStackTrace();
				}
				return rosw;
			});
			return col;
		}
		public TableColumn<T, String>[] getColumns();
	}

	public static <T> void deleteTreeView(TreeView<T> tree) {
		TreeItem<T> rootItem = tree.getRoot();
		if (rootItem!=null) {
			deleteItems(rootItem);
			tree.setRoot(null);
		}
	}
	
	private static <T> void deleteItems(TreeItem<T> item) {
		ObservableList<TreeItem<T>> children = item.getChildren();
		System.out.println("Clearing item "+item.getValue());
		if (children.size()>0) {
			for (TreeItem<T> treeItem : children) {
				deleteItems(treeItem);
			}
			children.clear();
		}
	}
	
	public static GridPane addGridPane(TabPane tabbedPane, String title) {
		TitledPane tp =  new TitledPane();
		tp.setExpanded(false);
		ColumnConstraints cc1=new ColumnConstraints();
		cc1.setFillWidth(false);
		cc1.setHgrow(Priority.ALWAYS);
		cc1.setMinWidth(Double.NEGATIVE_INFINITY);
		ColumnConstraints cc2=new ColumnConstraints();
		cc2.setHgrow(Priority.ALWAYS);
		GridPane gp=new GridPane();
		gp.getColumnConstraints().addAll(cc1, cc2);
		tp.setContent(gp);
		Tab nTab=new Tab(title);
		ScrollPane sp = new ScrollPane();
		VBox vb=new VBox();
		vb.setAlignment(Pos.CENTER);
		vb.getChildren().add(tp);
		sp.setFitToWidth(true);
		sp.setContent(vb);
		nTab.setContent(sp);
		tabbedPane.getTabs().add(nTab);
		return gp;
	}
	
	public static TitledPane addTitledPaneWithGridPane(VBox vbox, String title) {
		ColumnConstraints cc1=new ColumnConstraints();
		cc1.setFillWidth(false);
		cc1.setHgrow(Priority.ALWAYS);
		cc1.setMinWidth(Double.NEGATIVE_INFINITY);
		ColumnConstraints cc2=new ColumnConstraints();
		cc2.setHgrow(Priority.ALWAYS);
		GridPane gp=new GridPane();
		gp.getColumnConstraints().addAll(cc1, cc2);
		TitledPane tp =  new TitledPane(title, gp);
		tp.setExpanded(false);
		vbox.getChildren().add(tp);
		return tp;
	}
	
	public static <T> TitledPane addTitledPaneWithTableView(VBox vbox, String title) {
		TableView<T> tableView = new TableView<>();
		TitledPane tp = new TitledPane(title, tableView);
		tp.setExpanded(false);
		vbox.getChildren().add(tp);
		return tp;
	}

	public static void main(String[] args) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
	}

	public static <T> void setupTreeClickHandler(TreeView<T> tree, FXHelper.TreeItemClickHandler<T> handler) {
		tree.setCellFactory(tree2 -> {
			TreeCell<T> cell = new TreeCell<T>() {
				@Override
				public void updateItem(T item, boolean empty) {
					super.updateItem(item, empty);
					if (empty) {
						setText(null);
					} else {
						setText(item.toString());
					}
				}
			};
			cell.setOnMouseClicked(event -> {
				if (! cell.isEmpty()) {
					TreeItem<T> treeItem = cell.getTreeItem();
					handler.handleTreeItemClick(treeItem);
				}
			});
			return cell;
		});
	}
	
	public static <T> void setupTableViewHander(TableView<T> tableView, TableViewColumnHandler<T> handler) {
		tableView.getColumns().addAll(handler.getColumns());
	}

}
