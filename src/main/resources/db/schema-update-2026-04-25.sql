create table if not exists pesos_corporales (
    id bigint not null auto_increment,
    usuario_id bigint not null,
    client_id varchar(120),
    peso decimal(6,2) not null,
    fecha_registro date not null,
    created_at datetime(6) not null,
    updated_at datetime(6) not null,
    version bigint not null default 0,
    primary key (id),
    constraint fk_pesos_corporales_usuario foreign key (usuario_id) references usuarios (id),
    constraint uq_pesos_corporales_usuario_fecha unique (usuario_id, fecha_registro),
    constraint uq_pesos_corporales_usuario_client_id unique (usuario_id, client_id)
);

alter table ejercicios add column if not exists client_id varchar(120);
alter table ejercicios add column if not exists version bigint not null default 0;
alter table plantillas_sesion_entrenamiento add column if not exists version bigint not null default 0;
alter table plantillas_ejercicio add column if not exists version bigint not null default 0;
alter table registros_entrenamiento add column if not exists version bigint not null default 0;
alter table registros_ejercicio add column if not exists version bigint not null default 0;
alter table registros_serie add column if not exists version bigint not null default 0;
