drop function IF EXISTS transaction.book_ticket(bigint,integer,integer,integer);

create function transaction.book_ticket(mob_num bigint,t_id integer,s_num integer,sh_id integer) returns TABLE(txn_id integer,confirmation varchar)
    language plpgsql
as
$$

BEGIN

 RETURN QUERY
	insert into transaction.booking ( theater_id,show_id,mobilenum,status,seat_num) values
	                               (t_id,sh_id,mob_num,'BOOKED',s_num) RETURNING id as txn_id,status as confirmation;
END;

$$;