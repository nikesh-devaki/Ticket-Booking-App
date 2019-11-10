package com.ndevaki.ticketBookingSystem.DataSource;

import com.ndevaki.ticketBookingSystem.utils.ApplicationSettings;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;
import redis.clients.jedis.Transaction;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.util.*;

public class RedisOperation {
    public static boolean checkRedisConnection() {
        boolean result = true;
        try (Jedis jedis = JedisFactory.getInstance().getJedisPool().getResource()) {

        } catch (JedisConnectionException e) {
            result = false;
        }
        return result;
    }

    public static void insertSortedSet(String key, Map<String, Double> values) {
        try (Jedis jedis = JedisFactory.getInstance().getJedisPool().getResource()) {
            jedis.zadd(key, values);
        }
    }

    public static void removeSortedSetByScore(String key, double score) {
        try (Jedis jedis = JedisFactory.getInstance().getJedisPool().getResource()) {
            jedis.zremrangeByScore(key, Double.MIN_VALUE, score);
        }
    }

    public static Set<String> getSortedSetByScore(String key, double score) {
        Set<String> result;
        try (Jedis jedis = JedisFactory.getInstance().getJedisPool().getResource()) {
            result = jedis.zrangeByScore(key, score, Double.MAX_VALUE);
        }
        return result;
    }

    public static Map<String, String> getHashByName(String key) {
        Map<String, String> result;
        try (Jedis jedis = JedisFactory.getInstance().getJedisPool().getResource()) {
             result = jedis.hgetAll(key);
        }
        return result;
    }

    public static void setHash(String key, Map<String, String> values) {
        String result;
        try (Jedis jedis = JedisFactory.getInstance().getJedisPool().getResource()) {
            result = jedis.hmset(key, values);
        }
        //return result;
    }

    public static void setSet(String key, String[] values) {
        long result;
        try (Jedis jedis = JedisFactory.getInstance().getJedisPool().getResource()) {
            result = jedis.sadd(key, values);
        }
    }

    public static String getValue(String key) {
        String result;
        try (Jedis jedis = JedisFactory.getInstance().getJedisPool().getResource()) {
            result = jedis.get(key);
        }
        return result;
    }

    public static void deleteKey(String key) {

        try (Jedis jedis = JedisFactory.getInstance().getJedisPool().getResource()) {
            jedis.del(key);
        }
    }

    public static void deleteKeysByPattern(String pattern) {
        Set<String> matchingKeys = new HashSet<>();
        ScanParams params = new ScanParams();
        params.match(pattern);

        try (Jedis jedis = JedisFactory.getInstance().getJedisPool().getResource()) {
            String nextCursor = "0";
            do {
                ScanResult<String> scanResult = jedis.scan(nextCursor, params);
                List<String> keys = scanResult.getResult();
                nextCursor = scanResult.getStringCursor();

                matchingKeys.addAll(keys);

            } while(!nextCursor.equals("0"));

            if(matchingKeys.size() > 0) {
                jedis.del(matchingKeys.toArray(new String[matchingKeys.size()]));
            }
        }
    }

    public static ArrayList<Object> blockSeat(String key,Long value) {
        try (Jedis jedis = JedisFactory.getInstance().getJedisPool().getResource()) {
            Transaction trans=jedis.multi();
            trans.setnx(key,value.toString());
            trans.expire(key, ApplicationSettings.reserveTime*60);
            return (ArrayList<Object>)trans.exec();
        }
    }

    public static boolean checkUserReservation(String key, Long mobNum) {
        try (Jedis jedis = JedisFactory.getInstance().getJedisPool().getResource()) {
            if(mobNum.toString().equals(jedis.get(key))){
                return true;
            }
            return false;
        }
    }

    public static JsonObject getReservedList(String pattern) {
        JsonArray blockedseats=new JsonArray();
        try (Jedis jedis = JedisFactory.getInstance().getJedisPool().getResource()) {
            Set<byte[]> redisKeys = jedis.keys(pattern.getBytes());
            Iterator<byte[]> it = redisKeys.iterator();
            while (it.hasNext()) {
                byte[] data = (byte[]) it.next();
                blockedseats.add(Integer.parseInt(new String(data, 0, data.length)
                                        .split("_")[2]));
            }
        }
        JsonObject reservedSeats=new JsonObject().put("Reserved Seats",blockedseats);
        return reservedSeats;
    }
}