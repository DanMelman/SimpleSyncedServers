import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;

public class MyServer {

    public static void main(String[] args) throws Exception {
        MyServlet myServlet = new MyServlet();
        myServlet.loadConfig();

        Server server = new Server();
        ServletContextHandler externalHandler = new ServletContextHandler(server, "/exercise");
        externalHandler.setAllowNullPathInfo(true);
        externalHandler.addServlet(MyServlet.class, "/");

        int[] ports = {80, 8080, 8090}; // 8090 for internal synchronization
        for (int port : ports)
            setConnectorOnPort(server, port);

        server.start();
    }

    private static void setConnectorOnPort(Server externalServer, int port) {
        ServerConnector connector = new ServerConnector(externalServer);
        connector.setPort(port);
        externalServer.addConnector(connector);
    }

}
