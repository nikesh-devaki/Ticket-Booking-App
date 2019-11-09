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

import java.util.ArrayList;
import java.util.List;

public class BookingApi {

    Logger logger = LoggerFactory.getLogger(SearchApi.class);

    private String apiVersion;
    private Router router;

    private DatabaseHandler db;

    public BookingApi(DatabaseHandler db, String apiVersion, Router router) {
        this.apiVersion = apiVersion;
        this.router = router;
        this.db = db;
        initRoutes();
    }

    private void initRoutes() {
        this.router.route()
                .method(HttpMethod.POST).path(apiVersion + "reserves")
                .produces("application/json")
                .handler(routingContext -> {
                    logger.info("Reserving Ticket");
                    Long mobNum = Long.parseLong(routingContext.request().getParam("mobNum"));
                    int theater_id = Integer.parseInt(routingContext.request().getParam("theater"));
                    String slot_time = routingContext.request().getParam("slot");
                    int seatNum = Integer.parseInt(routingContext.request().getParam("seat"));
                    String showTime = routingContext.request().getParam("time");
                    int showId = Integer.parseInt(routingContext.request().getParam("show"));
                    //Performing redis transaction. Atomicity is guarnateed.
                    ArrayList<Object> result = RedisOperation.blockSeat(theater_id + "_" + showId + "_" + seatNum, mobNum);
                    //If key is set then returns OK, if expiry set then returns 1
                    if (result.get(0).equals("OK") && result.get(1).equals("1")) {
                        logger.info("Seat:"+seatNum+" in theater:"+theater_id+" reserved for user:"+mobNum+" sucesfully");
                        Utils.addCachingHeaders(routingContext.response()).end(new JsonObject()
                                .put("status", "success")
                                .put("message", "Seat Blocked")
                                .encode());
                    } else {
                        Utils.addCachingHeaders(routingContext.response()).end(new JsonObject()
                                .put("status", "success")
                                .put("message", "Request Failed to reserve seat. Someone else could have booked. Please try again later")
                                .encode());
                    }

                }
//                , err -> {
//                    logger.error("Error");
//                    Utils.addCachingHeaders(routingContext.response()).end(new JsonObject()
//                            .put("status", "fail")
//                            .put("message", "Request Failed. Internal Server Error")
//                            .encode());
//                }
                );
        this.router.route()
                .method(HttpMethod.POST).path(apiVersion + "bookseat")
                .produces("application/json")
                .handler(routingContext -> {
                    logger.info("Booking Ticket");
                    Long mobNum = Long.parseLong(routingContext.request().getParam("mobNum"));
                    int theater_id = Integer.parseInt(routingContext.request().getParam("theater"));
                    String slot_time = routingContext.request().getParam("slot");
                    int seatNum = Integer.parseInt(routingContext.request().getParam("seat"));
                    String showTime = routingContext.request().getParam("time");
                    int showId = Integer.parseInt(routingContext.request().getParam("show"));
                    //Checking if seat is reserved for this user
                    if(RedisOperation.checkUserReservation(theater_id + "_" + showId + "_" + seatNum, mobNum)) {
                        db.getClient().rxGetConnection().flatMap(conn -> {
                            Single<ResultSet> res = conn.rxQueryWithParams(SqlConstants.BOOK_TICKET,
                                    new JsonArray().add(mobNum)
                                            .add(theater_id)
                                            .add(slot_time)
                                            .add(seatNum)
                                            .add(showTime)
                                            .add(showId));
                            return res.doAfterTerminate(conn::close);
                        }).subscribe(result -> {
                            logger.info(result.getResults().toString());
                            //Removing seat from reservation list
                            RedisOperation.deleteKey(theater_id + "_" + showId + "_" + seatNum);
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
                    }else{
                        Utils.addCachingHeaders(routingContext.response()).end(new JsonObject()
                                .put("status", "fail")
                                .put("message", "Seat is not reserved for you!")
                                .encode());
                    }
                });
    }
}

