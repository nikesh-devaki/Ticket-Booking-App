drop function IF EXISTS transaction.get_booked_seats(integer,integer);

create function transaction.get_booked_seats(t_id integer,s_id integer) returns TABLE(seatNum integer)
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
	select seatNum from tranasction.booking where satus='Booked' and theater_id=t_id and showId=s_id;
END;

$$;