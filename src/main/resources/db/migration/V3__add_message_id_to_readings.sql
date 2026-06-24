alter table environmental_readings
    add column if not exists message_id uuid;

create unique index if not exists uk_environmental_readings_message_id
    on environmental_readings (message_id);
