alter table pesos_corporales
    add column if not exists comentario varchar(1000) null;

alter table registros_entrenamiento
    add column if not exists deleted_at datetime(6) null;
