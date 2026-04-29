alter table admin_ideas
    add column if not exists activo boolean not null default true;
