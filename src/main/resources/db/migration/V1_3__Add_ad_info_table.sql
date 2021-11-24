CREATE SEQUENCE puls_event_seq;

CREATE TABLE ad_info
(
    id          NUMERIC(19, 0) NOT NULL DEFAULT NEXTVAL('puls_event_seq'),
    uuid        varchar(36)    not null,
    provider_id NUMERIC(19, 0) NOT NULL,
    reference   VARCHAR(255)   NOT NULL,
    data        jsonb   not null,
    created     timestamp      not null default CURRENT_TIMESTAMP,
    updated     timestamp      not null default CURRENT_TIMESTAMP,
    primary key (id),
    unique (uuid),
    CONSTRAINT fk_provider_puls_event FOREIGN KEY (provider_id) REFERENCES provider (id)
);

CREATE INDEX puls_event_updated_idx ON ad_info (updated);
