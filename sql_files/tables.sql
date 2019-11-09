create schema map;

CREATE TABLE map.city(
   id SERIAL PRIMARY KEY,
   name VARCHAR NOT NULL
);

CREATE TABLE map.theater(
   id SERIAL PRIMARY KEY,
   name VARCHAR NOT NULL,
   city integer REFERENCES map.city(id),
   capacity integer
);

CREATE TABLE map.show(
   id integer primary key,
   time varchar);

create schema transaction;

create table transaction.booking(
       id SERIAL PRIMARY KEY,
       theater_id integer references map.theater(id),
       show_id integer,
       mobilenum bigint,
       status varchar,
       seat_num integer,
       slot_time timestamp
);

alter table transaction.booking add column created_at timestamp default now();