alter table environmental_readings
    add column if not exists created_at timestamp with time zone;

alter table environmental_readings
    add column if not exists updated_at timestamp with time zone;

update environmental_readings
set created_at = coalesce(created_at, received_at, current_timestamp),
    updated_at = coalesce(updated_at, received_at, current_timestamp)
where created_at is null
   or updated_at is null;

alter table environmental_readings
    alter column created_at set not null;

alter table environmental_readings
    alter column updated_at set not null;

create index if not exists idx_environmental_readings_device_recorded_at
    on environmental_readings (device_id, recorded_at desc);

