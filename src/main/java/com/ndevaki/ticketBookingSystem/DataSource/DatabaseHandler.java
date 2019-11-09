package com.ndevaki.ticketBookingSystem.DataSource;

import com.ndevaki.ticketBookingSystem.utils.ApplicationSettings;
import io.vertx.core.json.JsonObject;

import io.vertx.rxjava.core.Vertx;

import io.vertx.rxjava.ext.auth.jdbc.JDBCAuth;
import io.vertx.rxjava.ext.jdbc.JDBCClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class DatabaseHandler {
    private JDBCClient client;
    private JDBCAuth authProvider;
    private static DatabaseHandler db;
    Logger logger = LoggerFactory.getLogger(DatabaseHandler.class);


    public DatabaseHandler(Vertx vertx){

        this.client = JDBCClient.createShared(vertx, new JsonObject()
                .put("provider_class", "io.vertx.ext.jdbc.spi.impl.C3P0DataSourceProvider")
                .put("url", ApplicationSettings.dbUrl)
                .put("user", ApplicationSettings.dbUser)
                .put("password", ApplicationSettings.dbPassword)
                .put("min_pool_size", ApplicationSettings.dbPoolSize/2)
                .put("max_pool_size", ApplicationSettings.dbPoolSize)
                .put("max_idle_time", 5)
                .put("driver_class", "org.postgresql.Driver"));
    }

    public static DatabaseHandler getInstance(Vertx vertx) {
        if (db == null){
            db = new DatabaseHandler(vertx);
        }

        return db;

    }

    public JDBCClient getClient(){
        return client;
    }
}

