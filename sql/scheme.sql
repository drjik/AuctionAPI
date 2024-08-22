create table advertisements
(
    id               serial4,
    user_id          int8  not null,
    title            text  not null,
    description      text  not null,
    starting_price   float not null,
    current_price    float not null,
    active           bool      default (true),
    image            text  not null,
    auction_end_time timestamp default (null),

    primary key (id),
    foreign key (user_id) references users (id)
);

create table users
(
    id       serial4,
    username text not null,
    email    text not null,
    password text not null,
    primary key (id)
);

create table bids
(
    id               serial4,
    user_id          int4      not null,
    advertisement_id int4      not null,
    amount           float     not null,
    timestamp        timestamp not null,

    primary key (id),
    foreign key (user_id) references users (id),
    foreign key (advertisement_id) references advertisements (id)
);

-- ПРИМЕРЫ ДЛЯ ТЕСТА ОБЪЯВЛЕНИЙ

-- Toyota Camry 2020
-- Описание: Надежный седан с экономичным 2.5L двигателем, удобным интерьером и множеством функций комфорта.
-- Начальная цена: 22000
--
-- Audi A4 2018
-- Описание: Элегантный седан с турбированным двигателем, превосходной управляемостью и высококлассным салоном.
-- Начальная цена: 28000
--
-- Jeep Wrangler 2021
-- Описание: Легендарный внедорожник с мощным 3.6L V6 двигателем, съемной крышей и способностью преодолевать любые препятствия.
-- Начальная цена: 38,000