
  CREATE TABLE "TIMBER"."NWATCH" 
   (	"DA" CHAR(8 CHAR), 
	"SPONSOR_KEY" NUMBER(*,0), 
	"SPONSOR_LOCATION" NUMBER(*,0), 
	"TRANSACTION_DATE" DATE, 
	"TOTAL_SPEND" NUMBER, 
	"TRANSACTION_COUNT" NUMBER, 
	"ISNEW" NUMBER(*,0)
   ) SEGMENT CREATION IMMEDIATE 
  PCTFREE 10 PCTUSED 40 INITRANS 1 MAXTRANS 255 NOCOMPRESS LOGGING
  STORAGE(INITIAL 65536 NEXT 1048576 MINEXTENTS 1 MAXEXTENTS 2147483645
  PCTINCREASE 0 FREELISTS 1 FREELIST GROUPS 1 BUFFER_POOL DEFAULT FLASH_CACHE DEFAULT CELL_FLASH_CACHE DEFAULT)
  TABLESPACE "TIMBER" ;
--------------------------------------------------------
--  DDL for Table PROPERTIES
--------------------------------------------------------

  CREATE TABLE "TIMBER"."PROPERTIES" 
   (	"NAME" VARCHAR2(64 CHAR), 
	"VALUE" VARCHAR2(1024 CHAR)
   ) SEGMENT CREATION IMMEDIATE 
  PCTFREE 10 PCTUSED 40 INITRANS 1 MAXTRANS 255 NOCOMPRESS LOGGING
  STORAGE(INITIAL 65536 NEXT 1048576 MINEXTENTS 1 MAXEXTENTS 2147483645
  PCTINCREASE 0 FREELISTS 1 FREELIST GROUPS 1 BUFFER_POOL DEFAULT FLASH_CACHE DEFAULT CELL_FLASH_CACHE DEFAULT)
  TABLESPACE "TIMBER" ;
--------------------------------------------------------
--  DDL for Table TXN
--------------------------------------------------------

  CREATE TABLE "TIMBER"."TXN" 
   (	"COLLECTOR_KEY" NUMBER(*,0), 
	"SPONSOR_KEY" NUMBER(*,0), 
	"SPONSOR_LOCATION" NUMBER(*,0), 
	"TRANSACTION_DATE" DATE, 
	"SPEND" NUMBER(10,2), 
	"BASE_MILE" NUMBER(10,0), 
	"DISTANCE" NUMBER(7,3), 
	"EXTRACT_TIME" DATE, 
	"NEW" NUMBER(*,0) DEFAULT 1, 
	"COUNT" NUMBER(*,0)
   ) SEGMENT CREATION IMMEDIATE 
  PCTFREE 10 PCTUSED 40 INITRANS 1 MAXTRANS 255 NOCOMPRESS LOGGING
  STORAGE(INITIAL 65536 NEXT 1048576 MINEXTENTS 1 MAXEXTENTS 2147483645
  PCTINCREASE 0 FREELISTS 1 FREELIST GROUPS 1 BUFFER_POOL DEFAULT FLASH_CACHE DEFAULT CELL_FLASH_CACHE DEFAULT)
  TABLESPACE "TIMBER" ;
--------------------------------------------------------
--  DDL for Table UNIVERSE
--------------------------------------------------------

  CREATE TABLE "TIMBER"."UNIVERSE" 
   (	"ID" NUMBER, 
	"COLLECTOR_KEY" NUMBER(*,0), 
	"SPONSOR_KEY" NUMBER(*,0), 
	"SPONSOR_LOCATION" NUMBER(*,0), 
	"TRANSACTION_DATE" DATE, 
	"SPEND" NUMBER(10,2), 
	"BASE_MILE" NUMBER(10,0), 
	"COUNT" NUMBER(*,0), 
	"DISTANCE" NUMBER(7,3), 
	"ACTUAL_DISTANCE" NUMBER, 
	"LONGITUDE" NUMBER(15,10), 
	"LATITUDE" NUMBER(15,10), 
	"GEOM" "MDSYS"."SDO_GEOMETRY" , 
	"DA" CHAR(8 CHAR), 
	"FSA" CHAR(3 CHAR), 
	"ISNEW" NUMBER(*,0), 
	"SPONSOR_LOCATION_CODE" CHAR(4 CHAR), 
	"SPONSOR_LOCATION_NAME" VARCHAR2(100 CHAR), 
	"SPONSOR_LOCATION_LONGITUDE" NUMBER(15,10), 
	"SPONSOR_LOCATION_LATITUDE" NUMBER(15,10), 
	"PROMO_MAILABLE_FLAG" NUMBER(1,0) DEFAULT 0, 
	"EMAILABLE_FLAG" NUMBER(1,0) DEFAULT 0, 
	"WEB_ACTIVITY_FLAG" NUMBER(1,0) DEFAULT 0, 
	"MOBILE_APP_ACTIVITY_FLAG" NUMBER(1,0) DEFAULT 0
   ) SEGMENT CREATION IMMEDIATE 
  PCTFREE 10 PCTUSED 40 INITRANS 1 MAXTRANS 255 NOCOMPRESS LOGGING
  STORAGE(INITIAL 65536 NEXT 1048576 MINEXTENTS 1 MAXEXTENTS 2147483645
  PCTINCREASE 0 FREELISTS 1 FREELIST GROUPS 1 BUFFER_POOL DEFAULT FLASH_CACHE DEFAULT CELL_FLASH_CACHE DEFAULT)
  TABLESPACE "TIMBER" ;

  CREATE UNIQUE INDEX "TIMBER"."PROPERTIES_PK" ON "TIMBER"."PROPERTIES" ("NAME") 
  PCTFREE 10 INITRANS 2 MAXTRANS 255 COMPUTE STATISTICS 
  STORAGE(INITIAL 65536 NEXT 1048576 MINEXTENTS 1 MAXEXTENTS 2147483645
  PCTINCREASE 0 FREELISTS 1 FREELIST GROUPS 1 BUFFER_POOL DEFAULT FLASH_CACHE DEFAULT CELL_FLASH_CACHE DEFAULT)
  TABLESPACE "TIMBER" ;
--------------------------------------------------------
--  DDL for Index TXN_UK1
--------------------------------------------------------

  CREATE UNIQUE INDEX "TIMBER"."TXN_UK1" ON "TIMBER"."TXN" ("COLLECTOR_KEY", "SPONSOR_KEY", "SPONSOR_LOCATION", "TRANSACTION_DATE", "EXTRACT_TIME") 
  PCTFREE 10 INITRANS 2 MAXTRANS 255 COMPUTE STATISTICS 
  STORAGE(INITIAL 65536 NEXT 1048576 MINEXTENTS 1 MAXEXTENTS 2147483645
  PCTINCREASE 0 FREELISTS 1 FREELIST GROUPS 1 BUFFER_POOL DEFAULT FLASH_CACHE DEFAULT CELL_FLASH_CACHE DEFAULT)
  TABLESPACE "TIMBER" ;
--------------------------------------------------------
--  DDL for Index NWATCH_IDX
--------------------------------------------------------

  CREATE INDEX "TIMBER"."NWATCH_IDX" ON "TIMBER"."NWATCH" ("SPONSOR_LOCATION", "TRANSACTION_DATE", "TRANSACTION_COUNT", "DA", "TOTAL_SPEND") 
  PCTFREE 10 INITRANS 2 MAXTRANS 255 COMPUTE STATISTICS 
  STORAGE(INITIAL 65536 NEXT 1048576 MINEXTENTS 1 MAXEXTENTS 2147483645
  PCTINCREASE 0 FREELISTS 1 FREELIST GROUPS 1 BUFFER_POOL DEFAULT FLASH_CACHE DEFAULT CELL_FLASH_CACHE DEFAULT)
  TABLESPACE "TIMBER" ;
--------------------------------------------------------
--  DDL for Index UNIVERSE_COORDS_I
--------------------------------------------------------

  CREATE INDEX "TIMBER"."UNIVERSE_COORDS_I" ON "TIMBER"."UNIVERSE" ("LONGITUDE", "LATITUDE") 
  PCTFREE 10 INITRANS 2 MAXTRANS 255 COMPUTE STATISTICS 
  STORAGE(INITIAL 65536 NEXT 1048576 MINEXTENTS 1 MAXEXTENTS 2147483645
  PCTINCREASE 0 FREELISTS 1 FREELIST GROUPS 1 BUFFER_POOL DEFAULT FLASH_CACHE DEFAULT CELL_FLASH_CACHE DEFAULT)
  TABLESPACE "TIMBER" ;

--------------------------------------------------------
--  DDL for Index UNIVERSE_INFOTOOL_IDX
--------------------------------------------------------

  CREATE INDEX "TIMBER"."UNIVERSE_INFOTOOL_IDX" ON "TIMBER"."UNIVERSE" ("FSA", "SPONSOR_KEY", "TRANSACTION_DATE") 
  PCTFREE 10 INITRANS 2 MAXTRANS 255 COMPUTE STATISTICS 
  STORAGE(INITIAL 65536 NEXT 1048576 MINEXTENTS 1 MAXEXTENTS 2147483645
  PCTINCREASE 0 FREELISTS 1 FREELIST GROUPS 1 BUFFER_POOL DEFAULT FLASH_CACHE DEFAULT CELL_FLASH_CACHE DEFAULT)
  TABLESPACE "TIMBER" ;
--------------------------------------------------------
--  DDL for Index UNIVERSE_LOC_IDX
--------------------------------------------------------

  CREATE INDEX "TIMBER"."UNIVERSE_LOC_IDX" ON "TIMBER"."UNIVERSE" ("SPONSOR_LOCATION") 
  PCTFREE 10 INITRANS 2 MAXTRANS 255 COMPUTE STATISTICS 
  STORAGE(INITIAL 65536 NEXT 1048576 MINEXTENTS 1 MAXEXTENTS 2147483645
  PCTINCREASE 0 FREELISTS 1 FREELIST GROUPS 1 BUFFER_POOL DEFAULT FLASH_CACHE DEFAULT CELL_FLASH_CACHE DEFAULT)
  TABLESPACE "TIMBER" ;
--------------------------------------------------------
--  DDL for Index UNIVERSE_SUBSELECT_IDX
--------------------------------------------------------

  CREATE INDEX "TIMBER"."UNIVERSE_SUBSELECT_IDX" ON "TIMBER"."UNIVERSE" ("SPONSOR_LOCATION", "SPONSOR_KEY", "TRANSACTION_DATE") 
  PCTFREE 10 INITRANS 2 MAXTRANS 255 COMPUTE STATISTICS 
  STORAGE(INITIAL 65536 NEXT 1048576 MINEXTENTS 1 MAXEXTENTS 2147483645
  PCTINCREASE 0 FREELISTS 1 FREELIST GROUPS 1 BUFFER_POOL DEFAULT FLASH_CACHE DEFAULT CELL_FLASH_CACHE DEFAULT)
  TABLESPACE "TIMBER" ;

--------------------------------------------------------
--  DDL for Index TXN_SEARCH_IDX
--------------------------------------------------------

  CREATE INDEX "TIMBER"."TXN_SEARCH_IDX" ON "TIMBER"."TXN" ("NEW", "TRANSACTION_DATE") 
  PCTFREE 10 INITRANS 2 MAXTRANS 255 COMPUTE STATISTICS 
  STORAGE(INITIAL 65536 NEXT 1048576 MINEXTENTS 1 MAXEXTENTS 2147483645
  PCTINCREASE 0 FREELISTS 1 FREELIST GROUPS 1 BUFFER_POOL DEFAULT FLASH_CACHE DEFAULT CELL_FLASH_CACHE DEFAULT)
  TABLESPACE "TIMBER" ;
--------------------------------------------------------
--  DDL for Index UNIVERSE_INFO_IDX
--------------------------------------------------------

  CREATE INDEX "TIMBER"."UNIVERSE_INFO_IDX" ON "TIMBER"."UNIVERSE" ("LONGITUDE", "LATITUDE", "SPONSOR_KEY", "TRANSACTION_DATE", "SPONSOR_LOCATION", "COLLECTOR_KEY", "DISTANCE", "ACTUAL_DISTANCE", "SPEND") 
  PCTFREE 10 INITRANS 2 MAXTRANS 255 COMPUTE STATISTICS 
  STORAGE(INITIAL 65536 NEXT 1048576 MINEXTENTS 1 MAXEXTENTS 2147483645
  PCTINCREASE 0 FREELISTS 1 FREELIST GROUPS 1 BUFFER_POOL DEFAULT FLASH_CACHE DEFAULT CELL_FLASH_CACHE DEFAULT)
  TABLESPACE "TIMBER" ;

--------------------------------------------------------
--  Constraints for Table UNIVERSE
--------------------------------------------------------

  ALTER TABLE "TIMBER"."UNIVERSE" MODIFY ("COLLECTOR_KEY" NOT NULL ENABLE);
 
  ALTER TABLE "TIMBER"."UNIVERSE" MODIFY ("SPONSOR_LOCATION" NOT NULL ENABLE);
 
  ALTER TABLE "TIMBER"."UNIVERSE" MODIFY ("TRANSACTION_DATE" NOT NULL ENABLE);
 
  ALTER TABLE "TIMBER"."UNIVERSE" MODIFY ("COUNT" NOT NULL ENABLE);
 
  ALTER TABLE "TIMBER"."UNIVERSE" MODIFY ("LONGITUDE" NOT NULL ENABLE);
 
  ALTER TABLE "TIMBER"."UNIVERSE" MODIFY ("LATITUDE" NOT NULL ENABLE);
 
  ALTER TABLE "TIMBER"."UNIVERSE" MODIFY ("DA" NOT NULL ENABLE);
 
  ALTER TABLE "TIMBER"."UNIVERSE" MODIFY ("FSA" NOT NULL ENABLE);
 
  ALTER TABLE "TIMBER"."UNIVERSE" MODIFY ("ISNEW" NOT NULL ENABLE);
--------------------------------------------------------
--  Constraints for Table PROPERTIES
--------------------------------------------------------

  ALTER TABLE "TIMBER"."PROPERTIES" ADD CONSTRAINT "PROPERTIES_PK" PRIMARY KEY ("NAME")
  USING INDEX PCTFREE 10 INITRANS 2 MAXTRANS 255 COMPUTE STATISTICS 
  STORAGE(INITIAL 65536 NEXT 1048576 MINEXTENTS 1 MAXEXTENTS 2147483645
  PCTINCREASE 0 FREELISTS 1 FREELIST GROUPS 1 BUFFER_POOL DEFAULT FLASH_CACHE DEFAULT CELL_FLASH_CACHE DEFAULT)
  TABLESPACE "TIMBER"  ENABLE;
 
  ALTER TABLE "TIMBER"."PROPERTIES" MODIFY ("NAME" NOT NULL ENABLE);

--------------------------------------------------------
--  Constraints for Table NWATCH
--------------------------------------------------------

  ALTER TABLE "TIMBER"."NWATCH" MODIFY ("DA" NOT NULL ENABLE);
 
  ALTER TABLE "TIMBER"."NWATCH" MODIFY ("SPONSOR_LOCATION" NOT NULL ENABLE);
 
  ALTER TABLE "TIMBER"."NWATCH" MODIFY ("TRANSACTION_DATE" NOT NULL ENABLE);

--------------------------------------------------------
--  Constraints for Table TXN
--------------------------------------------------------

  ALTER TABLE "TIMBER"."TXN" MODIFY ("COLLECTOR_KEY" NOT NULL ENABLE);
 
  ALTER TABLE "TIMBER"."TXN" MODIFY ("SPONSOR_KEY" NOT NULL ENABLE);
 
  ALTER TABLE "TIMBER"."TXN" MODIFY ("SPONSOR_LOCATION" NOT NULL ENABLE);
 
  ALTER TABLE "TIMBER"."TXN" MODIFY ("TRANSACTION_DATE" NOT NULL ENABLE);
 
  ALTER TABLE "TIMBER"."TXN" MODIFY ("EXTRACT_TIME" NOT NULL ENABLE);
 
  ALTER TABLE "TIMBER"."TXN" MODIFY ("NEW" NOT NULL ENABLE);
 
  ALTER TABLE "TIMBER"."TXN" MODIFY ("COUNT" NOT NULL ENABLE);
 
  ALTER TABLE "TIMBER"."TXN" ADD CONSTRAINT "TXN_UK1" UNIQUE ("COLLECTOR_KEY", "SPONSOR_KEY", "SPONSOR_LOCATION", "TRANSACTION_DATE", "EXTRACT_TIME")
  USING INDEX PCTFREE 10 INITRANS 2 MAXTRANS 255 COMPUTE STATISTICS 
  STORAGE(INITIAL 65536 NEXT 1048576 MINEXTENTS 1 MAXEXTENTS 2147483645
  PCTINCREASE 0 FREELISTS 1 FREELIST GROUPS 1 BUFFER_POOL DEFAULT FLASH_CACHE DEFAULT CELL_FLASH_CACHE DEFAULT)
  TABLESPACE "TIMBER"  ENABLE;

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
      sum(t.count) transaction_count
    from universe t
    where t.isnew = 1
    group by t.da,
      t.sponsor_key,
      t.sponsor_location,
      t.transaction_date) da_transaction
  )
  loop
    insert
    into nwatch (da, sponsor_key, sponsor_location, transaction_date, total_spend, transaction_count)
    values (r.da, r.sponsor_key, r.sponsor_location, r.transaction_date, r.total_spend, r.transaction_count);
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
		select id, collector_key, sponsor_key, sponsor_location, transaction_date, spend, base_mile, count, distance, 
			sdo_geom.sdo_distance(geom, loc_geom, 0.005, 'unit=KM') actual_distance,
			longitude, latitude, geom, da, fsa, isnew, sponsor_location_code, sponsor_location_name, sponsor_location_longitude, sponsor_location_latitude,
			PROMO_MAILABLE_FLAG, EMAILABLE_FLAG, WEB_ACTIVITY_FLAG, MOBILE_APP_ACTIVITY_FLAG
		from (
        select rownum as id,
            t.collector_key collector_key,
            t.sponsor_key sponsor_key,
            t.sponsor_location sponsor_location,
            t.transaction_date transaction_date,
            t.spend spend,
            t.base_mile base_mile,
            t.count count,
            t.distance distance,
            c.longitude longitude,
            c.latitude latitude,
			get_point(c.longitude,c.latitude) geom,
			get_point(sl.longitude,sl.latitude) loc_geom,
            c.da da,
            c.fsa fsa,
            1 isnew,
            sl.sponsor_location_code sponsor_location_code,
            sl.sponsor_location_name sponsor_location_name,
            sl.longitude sponsor_location_longitude,
            sl.latitude sponsor_location_latitude,
            c.PROMO_MAILABLE_FLAG PROMO_MAILABLE_FLAG,
            c.EMAILABLE_FLAG EMAILABLE_FLAG,
            c.WEB_ACTIVITY_FLAG WEB_ACTIVITY_FLAG,
            c.MOBILE_APP_ACTIVITY_FLAG MOBILE_APP_ACTIVITY_FLAG
        from txn t
        join collector c on c.collector_key = t.collector_key join sponsor_location sl on t.sponsor_location = sl.sponsor_location_key
        where t.new = 1 and t.transaction_date = rec.transaction_date ));
        commit;
    end loop;
    commit;
        
    delete from universe u
    where u.transaction_date < to_date((select value from properties where name = 'date.latest.retained'), 'YYYY-MM-DD');
    commit;
    -- update min and max date
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
