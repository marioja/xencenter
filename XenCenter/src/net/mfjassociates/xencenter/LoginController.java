package net.mfjassociates.xencenter;

import static net.mfjassociates.xencenter.util.XenApiHelper.connect;

import java.net.MalformedURLException;

import org.apache.xmlrpc.XmlRpcException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.xensource.xenapi.Types.BadServerResponse;
import com.xensource.xenapi.Types.HostIsSlave;
import com.xensource.xenapi.Types.SessionAuthenticationFailed;
import com.xensource.xenapi.Types.XenAPIException;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

@Component
public class LoginController {
	
	@FXML
	private TextField hostname;

	@FXML
	private TextField username;

	@FXML
	private TextField password;
	
	@Value("${dl.xenserver.hostname:}")
	private String dlhostname;
	
	@Value("${dl.xenserver.username:}")
	private String dlusername;
	
	@Value("${dl.xenserver.password:}")
	private String dlpassword;

	@FXML
	private void initialize() {
		hostname.setText(dlhostname);
		username.setText(dlusername);
		password.setText(dlpassword);
	}
	
	@FXML
	private void loginFired(ActionEvent event) throws BadServerResponse, SessionAuthenticationFailed, HostIsSlave, MalformedURLException, XenAPIException, XmlRpcException {
		Button button = (Button) event.getSource();
		Stage stage = (Stage)button.getScene().getWindow();
		stage.setUserData(connect(hostname.getText(), username.getText(), password.getText()));
		stage.close();
	}

}
