drop function IF EXISTS transaction.book_ticket(long,integer,varchar,integer,timestamp,integer);

create function transaction.book_ticket(mob_num long,t_id integer,slot_time varchar,
                                         s_num integer,s_time timestamp,sh_id integer) returns TABLE(id integer,status vachar)
    language plpgsql
as
$$
    -- exception management variables
DECLARE
    exception_error_code  text;
    exception_message     text;
    exception_detail      text;
    exception_hint        text;
    exception_context     text;

BEGIN

 RETURN QUERY
	insert into transaction.booking ( theater_id,show_id,mobilenum,status,seat_num,slot_time) values
	                               (t_id,sh_id,mob_num,"RESERVED",s_num,s_time) RETURNING id,status;
END;

$$;