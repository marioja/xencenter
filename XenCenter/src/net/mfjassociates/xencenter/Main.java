package net.mfjassociates.xencenter;
	
import java.io.IOException;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;


/**
 * To make a JavaFX application integrate with Spring Boot here is what you need to do:
 * <p><b>Change output folder</b></p>
 * In eclipse, go to configure the build path of the JavaFX project and change under source
 * the default output folder to <project-name>/target/classes and accept the prompt to delete
 * the previous output folder.
 * <p><b>Maven setup</b></p>
 * Then right mouse click on the project and select Convert to Maven project under the
 * Configure menu.  Accept the defaults.
 * Add to the pom.xml file under the parent section the following:
 * <pre><code>
     Group Id: org.springframework.boot
  Artifact Id: spring-boot-starter-parent
      Version: 1.5.1.RELEASE (that was the current version at the time of writing)
 * </code></pre>
 * And in the dependencies section add the following two dependencies:
 * <pre><code>
     Group Id: org.springframework.boot
  Artifact Id: spring-boot-starter
 
     Group Id: org.springframework.boot
  Artifact Id: spring-boot-starter-test
 * </code></pre>
 * Right mouse click the project and select maven/Update project...
 * <p><b>Source modification</b></p>
 * Then add to the JavaFX source file that extends {@link Application} and add
 * the init() and stop() overridden methods.  Also add the @{@link SpringBootApplication} annotation
 * <p>
 * In the init() method make sure you add the call to {@link SpringApplication#run}
 * and save the result in a field of type {@link ConfigurableApplicationContext}
 * <p>
 * Create a method similar to to:
 * <pre><code>
 private Object createControllerForType(Class<?> type) {
     return this.context.getBean(type);
 }
 * </code></pre>
 * <p>
 * In the start() method, modify it to create an instance of the loader, set the controller
 * factory using Java 8 syntax such as:
 * <br>loader.setControllerFactory(this::createControllerForType);
 * <p>
 * Make sure you add the @{@link org.springframework.stereotype.Component} annotation to all your
 * controllers.
 * <p><b>Properties</b></p>
 * In order to start the Spring Boot application you can pass 3 pairs of properties to setup logging into
 * the XenServer.  To supply them on the command line you must pass a program argument of the format
 * <br><br>--property=value<br><br>For example,<br><br>--xenserver.hostname=abc.microsoft.com<br><br>could be supplied.
 * Here is a list of the properties (properties with dl. prefix will appear as defaults on the logon window, the others
 * (without the dl. prefix) will be used to login immediately (without a logon prompt) when the application starts:
 * <p>
 * <b>xenserver.hostname</b> or <b>dl.xenserver.hostname</b> - hostname of the XenServer pool master.<br>
 * <b>xenserver.username</b> or <b>dl.xenserver.username</b> - username to logon to the XenServer pool master.<br>
 * <b>xenserver.password</b> or <b>dl.xenserver.password</b> - password to logon to the XenServer pool master.<br>
 * <p>
 * @author Mario Jauvin
 */
@SpringBootApplication
public class Main extends Application {
	
	private static String[] savedArgs;

	private ConfigurableApplicationContext context;
	
	private Object createControllerForType(Class<?> type) {
		return this.context.getBean(type);
	}

	@Override
	public void start(Stage primaryStage) {
		try {
			FXMLLoader loader=new FXMLLoader(getClass().getResource("Main.fxml"));
			loader.setControllerFactory(this::createControllerForType);
			BorderPane root = loader.load();
//			BorderPane root = (BorderPane)FXMLLoader.load(getClass().getResource("Main.fxml"));
			
			Scene scene = new Scene(root,900,500);
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			primaryStage.setScene(scene);
			primaryStage.show();
		} catch(IOException e) {
			throw new IllegalStateException("Unable to load view:", e);
		}
	}
	@Override
	public void init() throws Exception {
		context=SpringApplication.run(getClass(), savedArgs);
	}

	@Override
	public void stop() throws Exception {
		context.close();
		System.gc();
		System.runFinalization();
	}

	public static void main(String[] args) {
		savedArgs=args;
		launch(args);
	}
}
