package com.ndevaki.ticketBookingSystem.utils;

import io.vertx.rxjava.core.http.HttpServerResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Utils {

    private static final Logger LOG = LoggerFactory.getLogger(Utils.class);
    public static HttpServerResponse addCachingHeaders(HttpServerResponse r) {
        r.headers()
            .add("Cache-Control", "no-cache")
            .add("Cache-Control", "no-store");
        return r;
    }
}
