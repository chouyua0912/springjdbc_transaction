create database jdbc_transaction;
grant all on jdbc_transaction.* to 'dba'@'%';


use jdbc_transaction;

create table `jdbc` (
  `id` INT UNSIGNED AUTO_INCREMENT,
  `name` VARCHAR(128) NOT NULL,
  PRIMARY KEY (`id`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

insert into jdbc (name) values ('zhang');
select * from jdbc;