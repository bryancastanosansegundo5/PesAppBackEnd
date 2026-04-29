create table if not exists admin_ideas (
    id bigint not null auto_increment,
    client_id varchar(120) not null,
    titulo varchar(160) not null,
    descripcion varchar(2000) not null,
    completada boolean not null default false,
    activo boolean not null default true,
    created_at datetime(6) not null,
    updated_at datetime(6) not null,
    version bigint not null default 0,
    primary key (id),
    constraint uq_admin_ideas_client_id unique (client_id)
);
