set names utf8mb4;
set charset utf8mb4;

drop database if exists bank;

create database bank charset utf8mb4;
use bank;

create table rekeningen (
  nummer char(16) NOT NULL  PRIMARY KEY,
  saldo decimal(12, 2) not null default 0
);

insert into rekeningen(nummer, saldo) VALUES ('BE68539007547034', 100);

create user if not exists cursist identified by 'cursist';
grant select, insert, update on  rekeningen to cursist;