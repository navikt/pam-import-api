CREATE SEQUENCE ad_info_seq;

CREATE TABLE ad_info
(
    id          NUMERIC(19, 0) NOT NULL DEFAULT NEXTVAL('ad_info_seq'),
    uuid        varchar(36)    not null,
    provider_id NUMERIC(19, 0) NOT NULL,
    reference   VARCHAR(255)   NOT NULL,
    activity    jsonb   not null,
    created     timestamp      not null default CURRENT_TIMESTAMP,
    updated     timestamp      not null default CURRENT_TIMESTAMP,
    primary key (id),
    unique (uuid),
    CONSTRAINT fk_provider_puls_event FOREIGN KEY (provider_id) REFERENCES provider (id)
);

CREATE INDEX ad_info_updated_idx ON ad_info (updated);
