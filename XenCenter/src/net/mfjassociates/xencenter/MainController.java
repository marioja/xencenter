package net.mfjassociates.xencenter;

import static net.mfjassociates.xencenter.util.FXHelper.deleteTreeView;
import static net.mfjassociates.xencenter.util.FXHelper.setupTreeClickHandler;
import static net.mfjassociates.xencenter.util.SampleDataHelper.createInfrastrucutreTree;
import static net.mfjassociates.xencenter.util.XenApiHelper.connect;
import static net.mfjassociates.xencenter.util.XenApiHelper.handleXenNode;
import static net.mfjassociates.xencenter.util.XenApiHelper.populateInfrastructure;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;

import org.apache.xmlrpc.XmlRpcException;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import com.xensource.xenapi.Connection;
import com.xensource.xenapi.Types.BadServerResponse;
import com.xensource.xenapi.Types.HostIsSlave;
import com.xensource.xenapi.Types.SessionAuthenticationFailed;
import com.xensource.xenapi.Types.XenAPIException;
import com.xensource.xenapi.XenAPIObject;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TabPane;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import net.mfjassociates.xencenter.util.FXHelper.ResponsiveTask;
import net.mfjassociates.xencenter.util.FXHelper.TreeItemClickHandler;
import net.mfjassociates.xencenter.util.XenNode;

@Component
public class MainController implements TreeItemClickHandler<XenNode>, ApplicationContextAware {
	
	public static final String GENERAL_INFORMATION_TITLE = "General information";
	public static final String RAW_TITLE = "Raw Text";
	
	private Connection connection;
	
	
	@FXML
	private TreeView<XenNode> infrastructureTree;
	
	@FXML
	private TextFlow rawText;
	
	@FXML
	private GridPane generalInformation;
	
	@FXML
	private TabPane tabbedPane;
	
	@FXML
	private VBox vbox;
	
	@Value("${xenserver.hostname:}")
	private String hostname;
	
	@Value("${xenserver.username:}")
	private String username;
	
	@Value("${xenserver.password:}")
	private String password;

	private ConfigurableApplicationContext context;
	
	private Object createControllerForType(Class<?> type) {
		return this.context.getBean(type);
	}

	@FXML
	private void initialize() throws NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, BadServerResponse, SessionAuthenticationFailed, HostIsSlave, MalformedURLException, XenAPIException, XmlRpcException {
		Thread th=new Thread(new ResponsiveTask<Void>(){

			private TreeItemClickHandler<XenNode> handler;
			private ResponsiveTask<Void> addHandler(TreeItemClickHandler<XenNode> aHandler) {
				this.handler=aHandler;
				return this;
			}
			@Override
			protected Void call() throws Exception {
				Platform.runLater(() -> {
					try {
						createInfrastrucutreTree(infrastructureTree, XenNode.class, XenAPIObject.class);
						if (!(hostname.isEmpty() || username.isEmpty() || password.isEmpty())) {
							connection=connect(hostname,username,password);
							populateInfrastructure(connection, infrastructureTree, XenNode.class, XenAPIObject.class);
						}
						setupTreeClickHandler(infrastructureTree, this.handler);
					} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException
							| IllegalArgumentException | InvocationTargetException | MalformedURLException | XenAPIException | XmlRpcException e) {
						e.printStackTrace();
					}
				});
				;
				return null;
			}}.addHandler(this).bindScene(infrastructureTree.sceneProperty()));
		th.start();
	}

	@FXML
	void loginFired(ActionEvent loginEvent) throws IOException, XmlRpcException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		deleteTreeView(infrastructureTree);
		Stage dialogStage=new Stage();
		FXMLLoader loader=new FXMLLoader(getClass().getResource("Login.fxml"));
		loader.setControllerFactory(this::createControllerForType);
		Parent dialog = loader.load();
		dialogStage.setScene(new Scene(dialog));
		dialogStage.initOwner(infrastructureTree.getScene().getWindow());
		dialogStage.showAndWait();
		connection = (Connection) dialogStage.getUserData();
		if (connection!=null) populateInfrastructure(connection, infrastructureTree, XenNode.class, XenAPIObject.class);
	}

	@Override
	public void handleTreeItemClick(TreeItem<XenNode> treeItem) {
		Thread th=new Thread(new ResponsiveTask<Void>() {

			@Override
			protected Void call() throws Exception {
				handleXenNode(treeItem.getValue().o, rawText, generalInformation, connection, vbox);
				return null;
			}
		}.bindScene(infrastructureTree.sceneProperty()));
		th.start();
	}

	@Override
	public void setApplicationContext(ApplicationContext theContext) throws BeansException {
		this.context = (ConfigurableApplicationContext) theContext;
		
	}

}
