package com.ndevaki.ticketBookingSystem;

import com.ndevaki.ticketBookingSystem.DataSource.DatabaseHandler;
import com.ndevaki.ticketBookingSystem.api.BookingApi;
import com.ndevaki.ticketBookingSystem.api.SearchApi;
import com.ndevaki.ticketBookingSystem.utils.ApplicationSettings;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerOptions;
import java.util.*;
import io.vertx.rxjava.core.AbstractVerticle;
import io.vertx.rxjava.core.http.HttpServer;
import io.vertx.rxjava.ext.web.Router;
import io.vertx.rxjava.ext.web.handler.BodyHandler;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.vertx.rxjava.ext.web.handler.CorsHandler;


public class MainVerticle extends AbstractVerticle {

    Logger logger = LoggerFactory.getLogger(MainVerticle.class);

    @Override
    public void start() throws Exception {

        TimeZone.setDefault( TimeZone.getTimeZone("GMT") );

        logger.info("TimeZone: "+TimeZone.getDefault().getDisplayName());

        // readConfiguration();
        if(readJsonConfiguration()) {
            logger.info("Config Read Successful");
        } else {
            logger.info("Config Read Failed, exiting!");
            vertx.close();
            return;
        }

        DatabaseHandler db  = DatabaseHandler.getInstance(vertx);
        //initDatabase(db.getClient());

        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());

        CorsHandler corsHandler = CorsHandler.create("*");
        corsHandler.allowedMethod(HttpMethod.GET);
        corsHandler.allowedMethod(HttpMethod.POST);
        corsHandler.allowedMethod(HttpMethod.PUT);
        corsHandler.allowedMethod(HttpMethod.DELETE);
        corsHandler.allowedHeader("Authorization");
        corsHandler.allowedHeader("Content-Type");
        router.route().handler(corsHandler);

        // Api initialization
        SearchApi authApi = new SearchApi(db, "/api/v1/", router);
        BookingApi bookingApi=new BookingApi(db,"/api/v1/",router);
        router.route("/*").handler(rc -> {
            logger.info("For refresh handling");
            logger.info(rc.request().path());
            if (rc.request().path().contains("/api")) {
                // resource not found
                logger.info("send resource not found");
                rc.fail(404);
            } else {
                // Reroute if not of api!!
                logger.info("sending index file");

            }

        });
        // support gzip
        HttpServerOptions serverOptions = new HttpServerOptions();
        serverOptions.setPort(ApplicationSettings.serverPort);
        HttpServer httpServer = vertx.createHttpServer(serverOptions);
        httpServer.requestHandler(router::accept);
        httpServer
                .rxListen()
                .subscribe(
                        server -> {
                            // Server is listening
                            logger.info("server is running..."+ApplicationSettings.serverPort);
                        },
                        failure -> {
                            // Server could not start
                            logger.error("Server failed to start...");
                        });
    }

    @Override
    public void stop() throws Exception {
        logger.info("Vertx Verticle Stopping...");
        // close db while stopping verticle
        DatabaseHandler db  = DatabaseHandler.getInstance(vertx);
        db.getClient().close();
    }

    public boolean readJsonConfiguration(){
        boolean readFlag = false;
        JSONParser parser = new JSONParser();
        try
        {
            logger.info("Reading DB Config File");
            ClassLoader classLoader = getClass().getClassLoader();
            JSONObject config = (JSONObject) parser
                    .parse(new java.io.FileReader("config/dbConfig.json"));

            //Reading DB Config
            JSONObject database = (JSONObject) config.get("database");
            ApplicationSettings.dbUrl = (String) database.get("url");
            ApplicationSettings.dbUser = (String) database.get("user");
            ApplicationSettings.dbPassword = (String) database.get("password");
            ApplicationSettings.dbPoolSize = Integer.parseInt((String) database.get("dbPoolSize"));

            //Reading Server Config
            JSONObject server = (JSONObject) config.get("server");
            ApplicationSettings.serverPort = Integer.parseInt((String) server.get("port"));
            ApplicationSettings.serverHost = (String) server.get("host");
            readFlag = true;
            logger.info("Success: Reading DB Config File");

            config = (JSONObject) parser
                    .parse(new java.io.FileReader("config/appConfig.json"));
            JSONObject settings = (JSONObject) config.get("ApplicationSettings");
            ApplicationSettings.reserveTime=Integer.parseInt((String) settings.get("seat_block_time"));
        }
        catch(Exception ex)
        {
            logger.info("Failed: Reading DB Config File");
            ex.printStackTrace();
            readFlag = false;
        }
        return readFlag;

    }
}
