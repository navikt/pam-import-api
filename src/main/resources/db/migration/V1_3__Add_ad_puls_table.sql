CREATE SEQUENCE ad_puls_seq;

CREATE TABLE ad_puls
(
    id          NUMERIC(19, 0) NOT NULL DEFAULT NEXTVAL('ad_puls_seq'),
    uuid        varchar(36)    not null,
    type        varchar(255)   not null,
    provider_id NUMERIC(19, 0) NOT NULL,
    reference   VARCHAR(255)   NOT NULL,
    total       numeric(19,0)  not null,
    created     timestamp      not null default CURRENT_TIMESTAMP,
    updated     timestamp      not null default CURRENT_TIMESTAMP,
    primary key (id),
    unique (uuid,type),
    CONSTRAINT fk_provider_ad_puls_event FOREIGN KEY (provider_id) REFERENCES provider (id)
);

CREATE INDEX ad_info_updated_idx ON ad_puls(updated);
