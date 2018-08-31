import org.eclipse.jetty.http.HttpStatus;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class MyServlet extends HttpServlet {
    private static Logger logger = Logger.getLogger(MyServlet.class.getName());
    private static String otherServer;
    private String inputContent;

    private enum SyncStatus {
        SUCCESS,
        FAIL
    }

    void loadConfig() throws IOException {
        String propertiesFileName = "config.properties";
        Properties prop = new Properties();
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(propertiesFileName)) {
            if (inputStream != null) {
                prop.load(inputStream);
            } else {
                throw new FileNotFoundException("Properties file '" + propertiesFileName + "' not found in the classpath");
            }
        } catch (FileNotFoundException e) {
            logger.severe("Config file not found.");
        }

        otherServer = prop.getProperty("other_server");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        logger.info(String.format("Got GET request, responding with '%s'", inputContent));
        int status = HttpStatus.OK_200;
        try {
            resp.getWriter().println(inputContent);
        } catch (Exception e) {
            status = HttpStatus.BAD_REQUEST_400;
        } finally {
            resp.setStatus(status);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        int serverPort = req.getServerPort();
        logger.info(String.format("Got POST request on port %d", serverPort));

        switch (serverPort) {

            // External request
            case 80:
            case 8080:
                inputContent = req.getReader().lines().collect(Collectors.joining());
                logger.info(String.format("Client POST content: '%s'", inputContent));
                SyncStatus syncStatus = updateSecondServer();
                resp.setStatus(syncStatus == SyncStatus.SUCCESS ? HttpStatus.OK_200 : HttpStatus.BAD_REQUEST_400);
                break;

            // Internal sync request
            case 8090:
                try {
                    inputContent = req.getReader().lines().collect(Collectors.joining());
                    logger.info(String.format("Sync POST content: '%s'", inputContent));
                    resp.setStatus(HttpStatus.OK_200);
                } catch (Exception e) {
                    resp.setStatus(HttpStatus.BAD_REQUEST_400);
                }
                break;
        }
    }

    private SyncStatus updateSecondServer() {
        try {
            URL url = new URL(otherServer + ":8090/exercise");
            byte[] postDataBytes = inputContent.getBytes();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.getOutputStream().write(postDataBytes);

            if(conn.getResponseCode()!= HttpStatus.OK_200)
            {
                logger.warning(String.format("Sync with server %s has failed.", otherServer));
                return SyncStatus.FAIL;
            }
        }
        catch(Exception e)
        {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            logger.warning(String.format("Sync with other server has failed: %s \n%s", e.getMessage(), exceptionAsString));
            return SyncStatus.FAIL;
        }

        return SyncStatus.SUCCESS;
    }
}

