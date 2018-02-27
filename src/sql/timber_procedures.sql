--------------------------------------------------------
--  DDL for Procedure REFRESH_HOTSPOT_LOCATION
--------------------------------------------------------
set define off;

  CREATE OR REPLACE PROCEDURE "TIMBER"."REFRESH_HOTSPOT_LOCATION" AS 
BEGIN
  execute immediate 'truncate table hotspot_location';
    
  insert
  into hotspot_location (sponsor_key, sponsor_location, transaction_date, longitude, latitude, collector_key,sum, unit_sum, TRANSACTION_COUNT)
    select
    sponsor_key,
    sponsor_location,
    transaction_date,
    m.x,
    m.y,
    collector_key,
    sum(spend) as sum,
    sum(unit) as unit,
    sum(count) as TRANSACTION_COUNT
  from universe u 
  left join lone.LONLAT_METERS m ON m.longitude = u.longitude and m.latitude = u.latitude
  group by sponsor_key, sponsor_location, transaction_date, m.x, m.y, collector_key
  ;
  commit;
END REFRESH_HOTSPOT_LOCATION;

/
--------------------------------------------------------
--  DDL for Procedure REFRESH_HOTSPOT_SPONSOR
--------------------------------------------------------
set define off;

  CREATE OR REPLACE PROCEDURE "TIMBER"."REFRESH_HOTSPOT_SPONSOR" AS 
BEGIN
  execute immediate 'truncate table hotspot_sponsor';
    
  insert
  into hotspot_sponsor (sponsor_key, transaction_date, longitude, latitude, collector_key,sum, unit_sum, TRANSACTION_COUNT)
    select
      u.sponsor_key,
      transaction_date,
      m.x,
      m.y,
      collector_key,
      sum(spend) as sum,
      sum(unit) as unit,
      sum(count) as TRANSACTION_COUNT
    from universe u
    left join lone.LONLAT_METERS m ON m.longitude = u.longitude and m.latitude = u.latitude
    group by u.sponsor_key, transaction_date, m.x, m.y, collector_key;
    
  commit;  
END REFRESH_HOTSPOT_SPONSOR;

/
--------------------------------------------------------
--  DDL for Procedure REFRESH_LOCATION_ACTIVE
--------------------------------------------------------
set define off;

  CREATE OR REPLACE PROCEDURE "TIMBER"."REFRESH_LOCATION_ACTIVE" 
as
  i integer; 
begin
     
    update sponsor_location sl
    set first_active = (
      select min(transaction_date) from universe u where u.sponsor_location = sl.sponsor_location_key
    ) where sponsor_key in (select distinct sponsor_key from universe);
    commit;
    update sponsor_location sl
    set last_active = (
      select max(transaction_date) from universe u where u.sponsor_location = sl.sponsor_location_key
    ) where sponsor_key in (select distinct sponsor_key from universe);
    commit; 
     
end REFRESH_LOCATION_ACTIVE;

/
--------------------------------------------------------
--  DDL for Procedure REFRESH_NWATCH
--------------------------------------------------------
set define off;

  CREATE OR REPLACE PROCEDURE "TIMBER"."REFRESH_NWATCH" as
  i integer;
begin
  execute immediate 'alter index nwatch_idx unusable';
  i := 0;
  for r in (
   select da_transaction.* from (
select t.da,
      t.sponsor_key,
      t.sponsor_location,
      t.transaction_date,
      sum(t.spend) total_spend,
      sum(t.count) transaction_count,
      sum(t.unit) total_unit
    from universe t
    where t.isnew = 1
    group by t.da,
      t.sponsor_key,
      t.sponsor_location,
      t.transaction_date) da_transaction
  )
  loop
    insert
    into nwatch (da, sponsor_key, sponsor_location, transaction_date, total_spend, transaction_count, total_unit)
    values (r.da, r.sponsor_key, r.sponsor_location, r.transaction_date, r.total_spend, r.transaction_count, r.total_unit);
    i:=i+1;
    if i >= 10000 then
      commit;
      i:=0;
    end if; 
  end loop;
  commit; 
  execute immediate 'alter index nwatch_idx rebuild';
  commit;
end refresh_nwatch;

/
--------------------------------------------------------
--  DDL for Procedure REFRESH_UNIVERSE
--------------------------------------------------------
set define off;

  CREATE OR REPLACE PROCEDURE "TIMBER"."REFRESH_UNIVERSE" 
as
  i integer;
begin
  for rec in (select transaction_date from txn where new = 1 group by transaction_date order by transaction_date)
  loop
    insert into universe (
		select id, collector_key, sponsor_key, sponsor_location, transaction_date, spend, count, distance, 
			sdo_geom.sdo_distance(geom, loc_geom, 0.005, 'unit=KM') actual_distance,
			longitude, latitude, geom, da, fsa, isnew, unit
		from (
        select rownum as id,
            t.collector_key collector_key,
            t.sponsor_key sponsor_key,
            t.sponsor_location sponsor_location,
            t.transaction_date transaction_date,
            t.spend spend,
            t.unit unit,
            t.count count,
            t.distance distance,
            c.longitude longitude,
            c.latitude latitude,
			get_point(c.longitude,c.latitude) geom,
			get_point(sl.longitude,sl.latitude) loc_geom,
            c.da da,
            c.fsa fsa,
            1 isnew
        from txn t
        join collector c on c.collector_key = t.collector_key join sponsor_location sl on t.sponsor_location = sl.sponsor_location_key
        where t.new = 1 and t.transaction_date = rec.transaction_date ));
        commit;
    end loop;
    commit;
        
    delete from universe u
    where u.transaction_date < to_date((select value from properties where name = 'date.latest.retained'), 'YYYY-MM-DD');
    commit;
    delete from properties where name = 'date.min';
    delete from properties where name = 'date.max';
    insert into properties (name, value) values ('date.min', ( select to_char(min(transaction_date), 'YYYY-MM-DD') from universe ));
    insert into properties (name, value) values ('date.max', ( select to_char(max(transaction_date), 'YYYY-MM-DD') from universe )) ;
    commit;
        
end refresh_universe;

/
--------------------------------------------------------
--  DDL for Procedure RESET_ISNEW
--------------------------------------------------------
set define off;

  CREATE OR REPLACE PROCEDURE "TIMBER"."RESET_ISNEW" as
begin
  update txn set new = 0 where new = 1;
  commit;
  update universe u set u.isnew = 0 where u.isnew = 1;
  commit;
  update nwatch n set n.isnew = 0 where n.isnew = 1;
  commit;
   
end RESET_ISNEW;

/
