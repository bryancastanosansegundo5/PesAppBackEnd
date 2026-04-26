create table if not exists pesos_corporales (
    id bigint not null auto_increment,
    usuario_id bigint not null,
    client_id varchar(120),
    peso decimal(6,2) not null,
    fecha_registro date not null,
    hora_registro varchar(5) not null,
    hora_manual boolean not null default false,
    fecha datetime(6) not null,
    created_at datetime(6) not null,
    updated_at datetime(6) not null,
    version bigint not null default 0,
    primary key (id),
    constraint fk_pesos_corporales_usuario foreign key (usuario_id) references usuarios (id),
    constraint uq_pesos_corporales_usuario_client_id unique (usuario_id, client_id)
);

alter table pesos_corporales add column if not exists hora_registro varchar(5);
alter table pesos_corporales add column if not exists hora_manual boolean not null default false;
alter table pesos_corporales add column if not exists fecha datetime(6);

update pesos_corporales
set hora_registro = coalesce(hora_registro, date_format(created_at, '%H:%i'), '12:00')
where hora_registro is null;

update pesos_corporales
set fecha = coalesce(fecha, timestamp(fecha_registro, hora_registro))
where fecha is null;

alter table pesos_corporales modify column hora_registro varchar(5) not null;
alter table pesos_corporales modify column fecha datetime(6) not null;

set @drop_pesos_fecha_index = (
    select if(
        exists(
            select 1
            from information_schema.statistics
            where table_schema = database()
              and table_name = 'pesos_corporales'
              and index_name = 'uq_pesos_corporales_usuario_fecha'
        ),
        'alter table pesos_corporales drop index uq_pesos_corporales_usuario_fecha',
        'select 1'
    )
);
prepare stmt_drop_pesos_fecha_index from @drop_pesos_fecha_index;
execute stmt_drop_pesos_fecha_index;
deallocate prepare stmt_drop_pesos_fecha_index;

alter table ejercicios add column if not exists client_id varchar(120);
alter table ejercicios add column if not exists version bigint not null default 0;
alter table plantillas_sesion_entrenamiento add column if not exists version bigint not null default 0;
alter table plantillas_ejercicio add column if not exists version bigint not null default 0;
alter table registros_entrenamiento add column if not exists version bigint not null default 0;
alter table registros_ejercicio add column if not exists version bigint not null default 0;
alter table registros_serie add column if not exists version bigint not null default 0;
