# --- First database schema

# --- !Ups

create table piece(
  id            bigserial,
  type          varchar(32) not null,
  json_data     text not null,
  created_at    timestamp not null,
  token         varchar(128) not null
);

# --- !Downs

drop table if exists piece;
