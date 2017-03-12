package net.mfjassociates.xencenter.util;

import static net.mfjassociates.xencenter.util.FXHelper.addTitledPaneWithGridPane;
import static net.mfjassociates.xencenter.util.FXHelper.addTitledPaneWithTableView;
import static net.mfjassociates.xencenter.util.FXHelper.setupTableViewHander;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.xmlrpc.XmlRpcException;

import com.xensource.xenapi.APIVersion;
import com.xensource.xenapi.Connection;
import com.xensource.xenapi.Host;
import com.xensource.xenapi.Network;
import com.xensource.xenapi.Pool;
import com.xensource.xenapi.Pool.Record;
import com.xensource.xenapi.Session;
import com.xensource.xenapi.Types.BadServerResponse;
import com.xensource.xenapi.Types.HostIsSlave;
import com.xensource.xenapi.Types.SessionAuthenticationFailed;
import com.xensource.xenapi.Types.XenAPIException;
import com.xensource.xenapi.VIF;
import com.xensource.xenapi.VM;
import com.xensource.xenapi.XenAPIObject;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TitledPane;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextFlow;
import net.mfjassociates.xencenter.MainController;
import net.mfjassociates.xencenter.util.FXHelper.TableViewColumnHandler;

public class XenApiHelper {
	
	/**
	 * Interface that must be implemented by producers of Record inner
	 * classes.
	 * @author mario
	 *
	 * @param <X> The XenAPIObject subclass (such as {@link VM} whose Record inner class is sought. 
	 * @param <R> The returned Record object
	 * @param <C> The type of the connection object
	 */
	public static interface HAS_RECORD<X, R, C> {
		public R getRecord(X xobj, C c) throws Exception;
	}
	
	public static interface HAS_VALUE<X, C> {
		public Object getValue(X obj, String fieldName, C c) throws Exception; 
	}

	public static Connection connect(String hostname, String username, String password) throws MalformedURLException, BadServerResponse, SessionAuthenticationFailed, HostIsSlave, XenAPIException, XmlRpcException {
        Connection connection = new Connection(new URL("http://" + hostname));
        Session.loginWithPassword(connection, username, password, APIVersion.latest().toString());
        return connection;
	}
	
	public static <O,NT> void populateInfrastructure(Connection connection, TreeView<NT> tree, Class<NT> nt, Class<O> obj) throws BadServerResponse, XenAPIException, XmlRpcException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		TreeItem<NT> i=null;
		final Constructor<NT> ntCons = nt.getDeclaredConstructor(String.class, obj);
		Set<Pool> pools = Pool.getAll(connection);
		System.out.println("Pools");
		for (Pool pool : pools) {
			Record r = pool.getRecord(connection);
			i=new TreeItem<NT>(ntCons.newInstance(r.nameLabel, pool));
			final TreeItem<NT> ifin=i;
			System.out.println(pool.getRecord(connection));
			Platform.runLater(() -> tree.setRoot(ifin));
		}
		if (i==null) throw new IllegalStateException("No xenserver pools were found.");
		TreeItem<NT> root = i;
		Set<Host> hosts = Host.getAll(connection);
		System.out.println("Hosts");
		for (Host host : hosts) {
			com.xensource.xenapi.Host.Record r = host.getRecord(connection);
			i=new TreeItem<NT>(ntCons.newInstance(r.nameLabel, host));
			final TreeItem<NT> ifin=i;
			for (VM vm : r.residentVMs) {
				Platform.runLater(() -> {
					try {
						ifin.getChildren().add(new TreeItem<NT>(ntCons.newInstance(vm.getNameLabel(connection), vm)));
					} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
							| InvocationTargetException | XenAPIException | XmlRpcException e) {
						e.printStackTrace();
					}
				});
			}
			Platform.runLater(() -> root.getChildren().add(ifin));
			System.out.println(host.getRecord(connection));
		}
	}
	private static class HasNiceStringLabel implements HAS_NICE_LABEL, Comparable<HasNiceStringLabel> {
		private String label;
		public HasNiceStringLabel(String aLabel) {
			this.label=aLabel;
		}
		@Override
		public String name() {
			
			return label;
		}
		@Override
		public String getLabel() {
			return label;
		}
		@Override
		public int compareTo(HasNiceStringLabel o) {
			return this.label.compareTo(o.label);
		}
	}
	private static HAS_NICE_LABEL[] convert(String[] labels) {
		SortedSet<HasNiceStringLabel> hnlSet=new TreeSet<HasNiceStringLabel>();
		for (int i = 0; i < labels.length; i++) {
			HasNiceStringLabel f = new HasNiceStringLabel(labels[i]);
			hnlSet.add(f);
		}
		return hnlSet.toArray(new HAS_NICE_LABEL[]{});
	}
	public static void main(String[] args) {
		Map<String, Object> tm = new VM.Record().toMap();
		String[] t = tm.keySet().toArray(new String[]{});
//		HAS_NICE_LABEL[] x = convert(t);
		System.out.println(t);
//		VM v = Types.toVM("");
		
//		HAS_RECORD ht=(HAS_RECORD) v;
//		System.out.println(ht.getClass().getCanonicalName());
	}
	
	private interface HAS_LABEL {
		public String name();
	}

	private interface HAS_NICE_LABEL extends HAS_LABEL {
		default String getLabel() {
			return name().replace("_"," ");
		}
	}
	
	private interface HAS_NICE_LABEL_AND_PANE extends HAS_NICE_LABEL {
		public HAS_NICE_LABEL[] getPanes();
		
	}
	public static enum GENERAL_INFORMATION implements HAS_NICE_LABEL {
		uuid,name_label,name_description
	}
	public static enum RESIDENT_VM implements HAS_NICE_LABEL {
		resident_VMs
	}
	public static enum VIFS implements HAS_NICE_LABEL {
		VIFs
	}
	public static enum BOOT_OPTIONS implements HAS_NICE_LABEL {
		PV_args
	}
	public static enum CPUS implements HAS_NICE_LABEL {
		VCPUs_at_startup
	}
	public static enum MANAGEMENT_INTERFACES implements HAS_NICE_LABEL {
		hostname,address
	}
	public static enum VM_PANES implements HAS_NICE_LABEL_AND_PANE {
		Boot_Options(BOOT_OPTIONS.values()),CPUs(CPUS.values()), ALL(convert(new VM.Record().toMap().keySet().toArray(new String[]{})));
		private HAS_NICE_LABEL[] panes;
		VM_PANES(HAS_NICE_LABEL[] somePanes) {
			this.panes=somePanes;
		}
		@Override
		public HAS_NICE_LABEL[] getPanes() {
			return this.panes;
		}
		
	}
	public static enum HOST_PANES implements HAS_NICE_LABEL_AND_PANE {
		Management_Intefaces(MANAGEMENT_INTERFACES.values()),ALL(convert(new Host.Record().toMap().keySet().toArray(new String[]{})));
		private HAS_NICE_LABEL[] panes;
		HOST_PANES(HAS_NICE_LABEL[] somePanes) {
			this.panes=somePanes;
		}
		@Override
		public HAS_NICE_LABEL[] getPanes() {
			return this.panes;
		}
		
	}

	/**
	 * This class will produce the {@link VM.Record}
	 * @author mario
	 *
	 */
	private static class VMRecordProducer implements HAS_RECORD<VM, VM.Record, Connection> {
		@Override
		public VM.Record getRecord(VM vm, Connection c) throws BadServerResponse, XenAPIException, XmlRpcException {
			return vm.getRecord(c);
		}		
	}
	
	private static abstract class AbstractRecordProducer<X, R> implements HAS_RECORD<X, R, Connection> {

		private X xobj;
		private R record;
		private int totalCalls=0;
		private int cacheHits=0;
		@Override
		public R getRecord(X anXobj, Connection c) throws Exception {
			totalCalls++;
			if (record==null || (xobj!=null && !xobj.equals(anXobj))) {
				xobj=anXobj;
				record=getRealRecord(anXobj, c);
			} else {
				cacheHits++;
			}
			return record;
		}
		
		@Override
		protected void finalize() throws Throwable {
			super.finalize();
			System.out.println(getClass().getCanonicalName()+" statistics: total calls/cache hits: "+totalCalls+"/"+cacheHits+" "+new Double(((double)cacheHits)/((double)totalCalls)));
		}

		abstract R getRealRecord(X xobj, Connection c) throws Exception;
	}
	private static class NetworkRecordProducer extends AbstractRecordProducer<Network, Network.Record> {

		@Override
		public Network.Record getRealRecord(Network network, Connection c) throws Exception {
			return network.getRecord(c);
		}
		
	}
	
	/**
	 * This class will produce the {@link VIF.Record}
	 * @author mario
	 *
	 */
	private static class VIFRecordProducer extends AbstractRecordProducer<VIF, VIF.Record> {
		@Override
		public VIF.Record getRealRecord(VIF vif, Connection c) throws Exception {
			return vif.getRecord(c);
		}
	}
	
	/**
	 * Abstract table view handler that provides the wiring in of the {@link Connection}
	 * object used when retrieving the Record inner objects.
	 * 
	 * @author mario
	 *
	 */
	private static abstract class XenHandler {
		Connection connection;
		
		public XenHandler(Connection aConnection) {
			this.connection=aConnection;
		}
		
	}
	
	/**
	 * This will display the columns from a Xen VM.Record object.
	 * @author mario
	 *
	 */
	private static class VMHandler extends XenHandler implements TableViewColumnHandler<VM> {

		public VMHandler(Connection aConnection) {
			super(aConnection);
		}

		@SuppressWarnings("unchecked")
		@Override
		public TableColumn<VM, String>[] getColumns() {
			HAS_RECORD<VM, VM.Record, Connection> vmRecordHandler = new VMRecordProducer();
			return new TableColumn[] {
					getColumnFromRecord("Name", vmRecordHandler, "nameLabel", connection),
					getColumnFromRecord("Description", vmRecordHandler, "nameDescription", connection),
					getColumnFromRecord("Memory Max", vmRecordHandler, "memoryDynamicMax", connection),
					getColumnFromRecord("Memory Min", vmRecordHandler, "memoryDynamicMin", connection),
					getColumnFromRecord("Memory", vmRecordHandler, "memoryTarget", connection),
					getColumnFromRecord("Platform", vmRecordHandler, "platform", connection),
					getColumnFromRecord("Powered State", vmRecordHandler, "powerState", connection)
				};
		}
		
	}
//	public static class RecordValueProducer<T, R, C> {
//		
//		private HAS_RECORD<T, R, C> recordHandler;
//		private String fieldName;
//		public RecordValueProducer(HAS_RECORD<T, R, C> aRecordHandler, String aFieldName) {
//			this.setRecordHandler(aRecordHandler);
//			this.fieldName=aFieldName;
//		}
//		public HAS_RECORD<T, R, C> getRecordHandler() {
//			return recordHandler;
//		}
//		public void setRecordHandler(HAS_RECORD<T, R, C> recordHandler) {
//			this.recordHandler = recordHandler;
//		}
//	}
	private static class VIFNetworkValueProducer implements HAS_VALUE<VIF, Connection> {

		private HAS_RECORD<VIF, VIF.Record, Connection> vifRecordProducer;
		private HAS_RECORD<Network, Network.Record, Connection> networkRecordProducer;
		public VIFNetworkValueProducer(HAS_RECORD<VIF, VIF.Record, Connection> aVifRecordProducer) {
			this.vifRecordProducer=aVifRecordProducer;
		}
		@Override
		public Object getValue(VIF obj, String fieldName, Connection c) throws Exception {
			VIF.Record record = vifRecordProducer.getRecord(obj, c);
			Network network=record.network;
			Network.Record nrecord = getNetworkRecordProducer().getRecord(network, c);
			return ReflectionHelper.getField(nrecord, fieldName);
		}
		public HAS_RECORD<Network, Network.Record, Connection> getNetworkRecordProducer() {
			if (this.networkRecordProducer==null) {
				this.networkRecordProducer=new NetworkRecordProducer();
			}
			return this.networkRecordProducer;
		}
		
	}
	/**
	 * This will display the columns from a Xen VIF.Record object.
	 * @author mario
	 *
	 */
	private static class VIFHandler extends XenHandler implements TableViewColumnHandler<VIF> {
		
		public VIFHandler(Connection aConnection) {
			super(aConnection);
		}

		@SuppressWarnings("unchecked")
		@Override
		public TableColumn<VIF, String>[] getColumns() {
			HAS_RECORD<VIF, VIF.Record, Connection> vifRecordHandler = new VIFRecordProducer();
			VIFNetworkValueProducer vifNetwork = new VIFNetworkValueProducer(vifRecordHandler);
			return new TableColumn[] {
					getColumnFromRecord("Device", vifRecordHandler, "device", connection),
					getColumnFromRecord("MAC", vifRecordHandler, "MAC", connection),
					getColumnFromValue("Network Name", vifNetwork, "nameLabel", connection),
					getColumnFromValue("Network Description", vifNetwork, "nameDescription", connection),
					getColumnFromRecord("Status", vifRecordHandler, "statusDetail", connection),
				};
		}
		
	}
	
	public static void handleXenNode(XenAPIObject o, TextFlow rawText, GridPane generalInformation,
			Connection connection, VBox vbox) {
		// cleanupVBox(vbox);
		ObservableList<Node> titledPanes = vbox.getChildren();
		Map<String, TitledPane> titles = new HashMap<String, TitledPane>();
		final TitledPane gi;
		TitledPane agi=null;
		for (Node node : titledPanes) {
			node.setUserData(null);
			TitledPane tp = (TitledPane) node;
			titles.put(tp.getText(), tp);
			if (MainController.GENERAL_INFORMATION_TITLE.equals(tp.getText()))
				agi = (TitledPane) node;
		}
		gi=agi;
		if (o instanceof VM) {
			VM v = (VM) o;
			try {
				Map<String, Object> recs = v.getRecord(connection).toMap();
				Platform.runLater(() -> {
					rawText.getChildren().clear();
					Label t;
					try {
						t = new Label(v.getRecord(connection).toMap().toString().replace(",", "\n"));
						t.setWrapText(true);
						rawText.getChildren().add(t);
						generalInformation.getChildren().clear();
						fillGridPane(generalInformation, recs, GENERAL_INFORMATION.values(), gi);
						fillOtherGridPanes(VM_PANES.values(), vbox, recs, titles);
						fillTableView(vbox, recs, VIFS.values(), titles, new VIFHandler(connection));
					} catch (XenAPIException | XmlRpcException e) {
						e.printStackTrace();
					}
				});
				// TitledPane tp=FXHelper.addTitledPane(vbox, "Boot Options");
				// tp=FXHelper.addTitledPane(vbox, "CPUs");
			} catch (BadServerResponse e) {
				e.printStackTrace();
			} catch (XenAPIException e) {
				e.printStackTrace();
			} catch (XmlRpcException e) {
				e.printStackTrace();
			}
		} else if (o instanceof Host) {
			Host h = (Host) o;
			try {
				Map<String, Object> recs = h.getRecord(connection).toMap();

				Platform.runLater(() -> {
					rawText.getChildren().clear();
					Label t;
					try {
						t = new Label(h.getRecord(connection).toMap().toString().replace(",", "\n"));
						t.setWrapText(true);
						rawText.getChildren().add(t);
						generalInformation.getChildren().clear();
						fillGridPane(generalInformation, recs, GENERAL_INFORMATION.values(), gi);
						fillOtherGridPanes(HOST_PANES.values(), vbox, recs, titles);
						fillTableView(vbox, recs, RESIDENT_VM.values(), titles, new VMHandler(connection));
					} catch (XenAPIException | XmlRpcException e) {
						e.printStackTrace();
					}
				});
			} catch (Exception e) {
				e.printStackTrace();
			}
			// TitledPane tp=FXHelper.addTitledPane(vbox, "Management
			// Interfaces");
			// tp=FXHelper.addTitledPane(vbox, "Memory");
		} else if (o instanceof Pool) {
			try {
				Pool p = (Pool) o;
				Map<String, Object> recs = p.getRecord(connection).toMap();
				Platform.runLater(() -> {
					rawText.getChildren().clear();
					Label t;
					try {
						t = new Label(p.getRecord(connection).toMap().toString().replace(",", "\n"));
						t.setWrapText(true);
						rawText.getChildren().add(t);
						generalInformation.getChildren().clear();
						fillGridPane(generalInformation, recs, GENERAL_INFORMATION.values(), gi);
					} catch (XenAPIException | XmlRpcException e) {
						e.printStackTrace();
					}
				});
			} catch (XenAPIException | XmlRpcException e) {
				e.printStackTrace();
			}
		}
		Platform.runLater(() -> cleanupVBox(vbox));
	}

	private static <T extends Enum<T>> void fillOtherGridPanes(T[] values, VBox vbox, Map<String, Object> recs, Map<String, TitledPane> titles) {
		for (int i = 0; i < values.length; i++) {
			HAS_NICE_LABEL_AND_PANE hnlap=(HAS_NICE_LABEL_AND_PANE) values[i];
			TitledPane tp=titles.get(hnlap.getLabel());
			if (tp==null) tp = addTitledPaneWithGridPane(vbox, hnlap.getLabel());
			fillGridPane(((GridPane)tp.getContent()),recs,hnlap.getPanes(), tp);
		}
	}

	private static void fillGridPane(GridPane pane, Map<String, Object> recs, HAS_NICE_LABEL[] hl, TitledPane tp) {
		int row=0;
		pane.getChildren().clear();
		tp.setUserData(Boolean.TRUE);
		for (int i = 0; i < hl.length; i++) {
			String key=hl[i].getLabel();
			Label label = new Label(key+":");
//								label.setMaxWidth(Double.MAX_VALUE);
			System.out.println("Retrieving key '"+key+"'");
			String labelString = "";
			Object labelObject = recs.get(hl[i].name());
			if (labelObject!=null) labelString = labelObject.toString();
			Label value = new Label(labelString);
			value.setMaxHeight(Double.MAX_VALUE);
			value.setWrapText(true);
			pane.addRow(row, label, value);
			GridPane.setHgrow(label, Priority.ALWAYS);
			GridPane.setValignment(label, VPos.TOP);
			pane.setHgap(5.0d);
			GridPane.setHgrow(value, Priority.NEVER);
			GridPane.setVgrow(value, Priority.ALWAYS);
			row++;
		}
	}
	private static <T> void fillTableView(VBox vbox, Map<String, Object> recs, HAS_NICE_LABEL[] hl, Map<String, TitledPane> titles, TableViewColumnHandler<T> handler) {
		String label=hl[0].getLabel();
		String name=hl[0].name();
		TitledPane tp=titles.get(label);
		if (tp==null) tp = addTitledPaneWithTableView(vbox, label);
		@SuppressWarnings("unchecked")
		TableView<T> tableView = (TableView<T>) tp.getContent();
		tp.setUserData(Boolean.TRUE);
		tableView.getItems().clear();
		setupTableViewHander(tableView, handler);
		@SuppressWarnings("unchecked")
		T[] values=(T[]) ((Set<T>)(recs.get(name))).toArray();
		tableView.getItems().addAll(values);
	}

	private static void cleanupVBox(VBox vbox) {
		ObservableList<Node> titledPanes = vbox.getChildren();
		Iterator<Node> iter = titledPanes.iterator();
		while(iter.hasNext()) {
			Node node = iter.next();
			TitledPane tp=(TitledPane)node;
			if (tp.getUserData()!=null) continue;
			iter.remove();
		}		
	}

}
