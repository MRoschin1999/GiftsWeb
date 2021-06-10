delete from user_wishes;
delete from user_info;
delete from users;

insert into users (id, confirmed, email, password, username) values
(1, true, 'a', 'a', 'a'),
(2, true, 'b', 'b', 'b');

insert into user_info (id, first_name, last_name, user_id) values
(1, 'A', 'A', 1);

insert into user_wishes(id, wish_name, user_id, friend_create_wish, deleted, is_closed, shopurl) values
(1, 'chocolate', 1, false, false, false, ''),
(2, 'phone', 1, false, false, false, '' ),
(3, 'guitar', 1, true, false, false, '' ),
(4, 'pencil', 2, false, false, false, '');
