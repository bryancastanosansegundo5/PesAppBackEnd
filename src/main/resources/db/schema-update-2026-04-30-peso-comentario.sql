alter table pesos_corporales
    add column if not exists comentario varchar(1000) null;
