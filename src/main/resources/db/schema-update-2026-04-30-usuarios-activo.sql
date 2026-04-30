alter table usuarios add column if not exists activo boolean not null default true;

update usuarios
set activo = true
where activo is null;

alter table usuarios modify column activo boolean not null default true;
