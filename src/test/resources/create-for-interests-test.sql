delete from user_has_interest;
delete from participants;
delete from messages;
delete from chat;
delete from user_wishes;
delete from tbl_friends;
delete from user_info;
delete from users;

insert into users (id, confirmed, email, password, username) values
(1, true, 'a', 'a', 'a'),
(2, true, 'b', 'b', 'b');

insert into user_info (id, first_name, last_name, user_id) values
(1, 'A', 'A', 1),
(2, 'B', 'B', 2);

insert into tbl_friends (friend_id, user_id) values
(1, 2),
(2, 1);

-- insert into user_has_interest (user_id, interest_id) values
-- (2, 11),
-- (1, 7);
