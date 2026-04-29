create temporary table if not exists tmp_map_sesiones_frontend as
select duplicado.id as old_id, repetidos.keep_id
  from plantillas_sesion_entrenamiento duplicado
  join (
        select usuario_id, id_frontend, min(id) as keep_id
          from plantillas_sesion_entrenamiento
         where id_frontend is not null and trim(id_frontend) <> ''
         group by usuario_id, id_frontend
        having count(*) > 1
  ) repetidos
    on repetidos.usuario_id = duplicado.usuario_id
   and repetidos.id_frontend = duplicado.id_frontend
 where duplicado.id <> repetidos.keep_id;

update registros_entrenamiento registro
  join tmp_map_sesiones_frontend mapa on mapa.old_id = registro.plantilla_sesion_id
   set registro.plantilla_sesion_id = mapa.keep_id;

update plantillas_ejercicio ejercicio
  join tmp_map_sesiones_frontend mapa on mapa.old_id = ejercicio.plantilla_sesion_id
   set ejercicio.plantilla_sesion_id = mapa.keep_id;

create temporary table if not exists tmp_map_ejercicios_catalogo as
select duplicado.id as old_id, repetidos.keep_id
  from ejercicios duplicado
  join (
        select usuario_id, client_id, min(id) as keep_id
          from ejercicios
         where client_id is not null and trim(client_id) <> ''
         group by usuario_id, client_id
        having count(*) > 1
  ) repetidos
    on repetidos.usuario_id = duplicado.usuario_id
   and repetidos.client_id = duplicado.client_id
 where duplicado.id <> repetidos.keep_id;

update plantillas_ejercicio ejercicio
  join tmp_map_ejercicios_catalogo mapa on mapa.old_id = ejercicio.ejercicio_id
   set ejercicio.ejercicio_id = mapa.keep_id;

update registros_ejercicio ejercicio
  join tmp_map_ejercicios_catalogo mapa on mapa.old_id = ejercicio.ejercicio_id
   set ejercicio.ejercicio_id = mapa.keep_id;

create temporary table if not exists tmp_map_registros_entrenamiento as
select duplicado.id as old_id, repetidos.keep_id
  from registros_entrenamiento duplicado
  join (
        select usuario_id, id_frontend, min(id) as keep_id
          from registros_entrenamiento
         where id_frontend is not null and trim(id_frontend) <> ''
         group by usuario_id, id_frontend
        having count(*) > 1
  ) repetidos
    on repetidos.usuario_id = duplicado.usuario_id
   and repetidos.id_frontend = duplicado.id_frontend
 where duplicado.id <> repetidos.keep_id;

update registros_ejercicio ejercicio
  join tmp_map_registros_entrenamiento mapa on mapa.old_id = ejercicio.registro_entrenamiento_id
   set ejercicio.registro_entrenamiento_id = mapa.keep_id;

create temporary table if not exists tmp_map_registros_ejercicio as
select duplicado.id as old_id, repetidos.keep_id
  from registros_ejercicio duplicado
  join (
        select registro_entrenamiento_id, id_frontend, min(id) as keep_id
          from registros_ejercicio
         where id_frontend is not null and trim(id_frontend) <> ''
         group by registro_entrenamiento_id, id_frontend
        having count(*) > 1
  ) repetidos
    on repetidos.registro_entrenamiento_id = duplicado.registro_entrenamiento_id
   and repetidos.id_frontend = duplicado.id_frontend
 where duplicado.id <> repetidos.keep_id;

update registros_serie serie
  join tmp_map_registros_ejercicio mapa on mapa.old_id = serie.registro_ejercicio_id
   set serie.registro_ejercicio_id = mapa.keep_id;

delete duplicado
  from registros_serie duplicado
  join (
        select registro_ejercicio_id, id_frontend, min(id) as keep_id
          from registros_serie
         where id_frontend is not null and trim(id_frontend) <> ''
         group by registro_ejercicio_id, id_frontend
        having count(*) > 1
  ) repetidos
    on repetidos.registro_ejercicio_id = duplicado.registro_ejercicio_id
   and repetidos.id_frontend = duplicado.id_frontend
 where duplicado.id <> repetidos.keep_id;

delete duplicado
  from plantillas_ejercicio duplicado
  join (
        select plantilla_sesion_id, id_frontend, min(id) as keep_id
          from plantillas_ejercicio
         where id_frontend is not null and trim(id_frontend) <> ''
         group by plantilla_sesion_id, id_frontend
        having count(*) > 1
  ) repetidos
    on repetidos.plantilla_sesion_id = duplicado.plantilla_sesion_id
   and repetidos.id_frontend = duplicado.id_frontend
 where duplicado.id <> repetidos.keep_id;

delete duplicado
  from registros_ejercicio duplicado
  join tmp_map_registros_ejercicio mapa on mapa.old_id = duplicado.id;

delete duplicado
  from registros_entrenamiento duplicado
  join tmp_map_registros_entrenamiento mapa on mapa.old_id = duplicado.id;

delete duplicado
  from ejercicios duplicado
  join tmp_map_ejercicios_catalogo mapa on mapa.old_id = duplicado.id;

delete duplicado
  from plantillas_sesion_entrenamiento duplicado
  join tmp_map_sesiones_frontend mapa on mapa.old_id = duplicado.id;

drop temporary table if exists tmp_map_registros_ejercicio;
drop temporary table if exists tmp_map_registros_entrenamiento;
drop temporary table if exists tmp_map_ejercicios_catalogo;
drop temporary table if exists tmp_map_sesiones_frontend;

alter table plantillas_sesion_entrenamiento
    add constraint uq_plantillas_sesion_usuario_id_frontend unique (usuario_id, id_frontend);

alter table plantillas_ejercicio
    add constraint uq_plantillas_ejercicio_sesion_id_frontend unique (plantilla_sesion_id, id_frontend);

alter table ejercicios
    add constraint uq_ejercicios_usuario_client_id unique (usuario_id, client_id);

alter table registros_entrenamiento
    add constraint uq_registros_entrenamiento_usuario_id_frontend unique (usuario_id, id_frontend);

alter table registros_ejercicio
    add constraint uq_registros_ejercicio_entrenamiento_id_frontend
        unique (registro_entrenamiento_id, id_frontend);

alter table registros_serie
    add constraint uq_registros_serie_ejercicio_id_frontend unique (registro_ejercicio_id, id_frontend);
