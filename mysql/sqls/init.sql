create table member(

id_num int auto_increment,

username varchar(200) not null,

password varchar(300) not null,

name varchar(100) not null,

constraint id_num_pk_123 primary key (id_num)  

)



create table token_store(

to_num int auto_increment,

id_num int not null,

refresh TEXT not null,

expiration VARCHAR(100) not null,

constraint primary key to_num_pk(to_num),

constraint foreign key id_num_token_store_fk(id_num)

references member(id_num)

on delete cascade

)