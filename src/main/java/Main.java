import com.alibaba.fastjson.JSONObject;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import entities.Book;
import queries.ApiResult;
import queries.BookQueryConditions;
import queries.BookQueryResults;
import utils.ConnectConfig;
import utils.DatabaseConnector;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class Main {

    private static final Logger log = Logger.getLogger(Main.class.getName());
    public static LibraryManagementSystem library;
    public static void main(String[] args) {
        try {
            // parse connection config from "resources/application.yaml"
            ConnectConfig conf = new ConnectConfig();
            log.info("Success to parse connect config. " + conf.toString());
            // connect to database
            DatabaseConnector connector = new DatabaseConnector(conf);
            library = new LibraryManagementSystemImpl(connector);

            boolean connStatus = connector.connect();
            if (!connStatus) {
                log.severe("Failed to connect database.");
                System.exit(1);
            }
            /* do somethings */

            HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
            server.createContext("/api/card", new CardHandler());
            server.createContext("/api/book", new BookHandler());

            server.start();
            log.info("Server started at 8000");
            // release database connection handler
            /*
            if (connector.release()) {
                log.info("Success to release connection.");
            } else {
                log.warning("Failed to release connection.");
            }*/
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



}

