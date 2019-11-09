package com.ndevaki.ticketBookingSystem.utils;

public class SqlConstants {

    public static final String  GET_ALL_CITIES="SELECT * FROM map.city;";
    public static final String  GET_THEATERS="SELECT * FROM map.theater where city=?";
    public static final String BOOK_TICKET="SELECT * FROM TRANSACTION.book_ticket(?,?,?,?,?,?)";
    public static final String RESERVE_TICKET="SELECT * FROM transaction.reserve_ticket(?,?,?,?,?,?)";
    public static final String GET_BOOKED_SEATS ="SELECT * FROM transaction.get_booked_seats(?,?)";
}
