package com.ndevaki.ticketBookingSystem.api;

import com.ndevaki.ticketBookingSystem.DataSource.DatabaseHandler;
import com.ndevaki.ticketBookingSystem.DataSource.RedisOperation;
import com.ndevaki.ticketBookingSystem.utils.SqlConstants;
import com.ndevaki.ticketBookingSystem.utils.Utils;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import io.vertx.ext.sql.ResultSet;
import io.vertx.rxjava.ext.web.Router;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Single;

public class SearchApi {

    Logger logger = LoggerFactory.getLogger(SearchApi.class);

    private String apiVersion;
    private Router router;

    private DatabaseHandler db;

    public SearchApi(DatabaseHandler db, String apiVersion, Router router) {
        this.apiVersion = apiVersion;
        this.router = router;
        this.db = db;
        initRoutes();
    }

    private void initRoutes() {

        this.router.route()
                .method(HttpMethod.GET).path(apiVersion + "cities")
                .produces("application/json")
                .handler(routingContext -> {
                    logger.info("Get Cities list");

                    db.getClient().rxGetConnection().flatMap(conn -> {
                        Single<ResultSet> res = conn.rxQuery(SqlConstants.GET_ALL_CITIES);
                        return res.doAfterTerminate(conn::close);
                    }).subscribe(result -> {
                        logger.info(result.getResults().toString());

                        JsonObject citiesList = new JsonObject();

                        if (result.getNumRows() > 0) {
                            Utils.addCachingHeaders(routingContext.response()).end(new JsonObject()
                                    .put("status", "success")
                                    .put("data", new JsonArray(result.getRows()).encode())
                                    .encode());
                        } else {
                            Utils.addCachingHeaders(routingContext.response()).end(new JsonObject()
                                    .put("status", "success")
                                    .put("message", "Request Failed!")
                                    .encode());
                        }
                    }, err -> {
                        logger.error("Database problem");
                        logger.error(err.getLocalizedMessage());

                        Utils.addCachingHeaders(routingContext.response()).end(new JsonObject()
                                .put("status", "fail")
                                .put("message", "Request Failed!")
                                .encode());
                    });

                });
        this.router.route()
                .method(HttpMethod.GET).path(apiVersion + "theaters")
                .produces("application/json")
                .handler(routingContext -> {
                    logger.info("Get theaters list");
                    String cityId = routingContext.request().getParam("city");
                    Integer city = Integer.parseInt(cityId);
                    db.getClient().rxGetConnection().flatMap(conn -> {
                        Single<ResultSet> res = conn.rxQueryWithParams(SqlConstants.GET_THEATERS, new JsonArray().add(city));
                        return res.doAfterTerminate(conn::close);
                    }).subscribe(result -> {
                        logger.info(result.getResults().toString());

                        if (result.getNumRows() > 0) {
                            Utils.addCachingHeaders(routingContext.response()).end(new JsonObject()
                                    .put("status", "success")
                                    .put("data", new JsonArray(result.getRows()).encode())
                                    .encode());
                        } else {
                            Utils.addCachingHeaders(routingContext.response()).end(new JsonObject()
                                    .put("status", "success")
                                    .put("message", "Request Failed!")
                                    .encode());
                        }
                    }, err -> {
                        logger.error("Database problem");
                        logger.error(err.getLocalizedMessage());

                        Utils.addCachingHeaders(routingContext.response()).end(new JsonObject()
                                .put("status", "fail")
                                .put("message", "Request Failed!")
                                .encode());
                    });

                });
        this.router.route()
                .method(HttpMethod.GET).path(apiVersion + "seats")
                .produces("application/json")
                .handler(routingContext -> {
                    logger.info("Getting seats list");
                    int theater_id = Integer.parseInt(routingContext.request().getParam("theater"));
//                    String slot_time = routingContext.request().getParam("slot");
//                    int seatNum = Integer.parseInt(routingContext.request().getParam("seat"));
//                    String showTime = routingContext.request().getParam("time");
                    int showId = Integer.parseInt(routingContext.request().getParam("show"));
                    JsonObject reservedList= RedisOperation.getReservedList(theater_id + "_" + showId + "_*");
                    JsonObject theaterSize=new JsonObject().put ("Total Seats",RedisOperation.getValue(theater_id+""));
                    JsonObject seats=new JsonObject().mergeIn(theaterSize).mergeIn(reservedList);
                    db.getClient().rxGetConnection().flatMap(conn -> {
                        Single<ResultSet> res = conn.rxQueryWithParams(SqlConstants.GET_BOOKED_SEATS, new JsonArray()
                                                                        .add(theater_id)
                                                                        .add(showId));
                        return res.doAfterTerminate(conn::close);
                    }).subscribe(result -> {
                        logger.info(result.getResults().toString());
                        if (result.getNumRows() > 0) {
                            seats.mergeIn(new JsonObject().put("Booked Seats",result.getRows()));
                        }
                        Utils.addCachingHeaders(routingContext.response()).end(new JsonObject()
                                .put("status", "success")
                                .put("data", seats)
                                .encode());
//                        else {
//                            Utils.addCachingHeaders(routingContext.response()).end(new JsonObject()
//                                    .put("status", "success")
//                                    .put("message", "Request Failed!")
//                                    .encode());
//                        }
                    }, err -> {
                        logger.error("Database problem");
                        logger.error(err.getLocalizedMessage());

                        Utils.addCachingHeaders(routingContext.response()).end(new JsonObject()
                                .put("status", "fail")
                                .put("message", "Request Failed!")
                                .encode());
                    });

                });
    }
}
