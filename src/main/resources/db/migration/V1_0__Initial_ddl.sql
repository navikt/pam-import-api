CREATE SEQUENCE provider_id_seq;

CREATE TABLE provider (
    id BIGINT NOT NULL DEFAULT NEXTVAL('provider_id_seq'),
    uuid UUID NOT NULL,
    username VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    created TIMESTAMP NOT NULL DEFAULT NOW(),
    updated TIMESTAMP NOT NULL DEFAULT NOW(),
    PRIMARY KEY(id),
    UNIQUE KEY(uuid),
    UNIQUE KEY(username)
);

CREATE SEQUENCE transfer_log_id_seq;

CREATE TABLE transfer_log (
    id BIGINT NOT NULL DEFAULT NEXTVAL('transfer_log_id_seq'),
    provider_id BIGINT NOT NULL REFERENCES provider(id),
    md5 VARCHAR(32) NOT NULL,
    payload TEXT NOT NULL,
    status VARCHAR(36) NOT NULL,
    message VARCHAR(512),
    created TIMESTAMP NOT NULL DEFAULT NOW(),
    updated TIMESTAMP NOT NULL DEFAULT NOW(),
    PRIMARY KEY(id),
    UNIQUE KEY(provider_id,md5),
);

CREATE INDEX transfer_log_status_idx ON transfer_log(status);

CREATE SEQUENCE adstate_id_seq;

CREATE TABLE ad_state(
    id BIGINT NOT NULL DEFAULT NEXTVAL('adstate_id_seq'),
    uuid UUID NOT NULL,
    provider_id BIGINT NOT NULL REFERENCES provider(id),
    reference VARCHAR(255) NOT NULL,
    json_payload TEXT NOT NULL,
    transfer_version BIGINT NOT NULL,
    created TIMESTAMP NOT NULL DEFAULT NOW(),
    updated TIMESTAMP NOT NULL DEFAULT NOW(),
    PRIMARY KEY(id),
    UNIQUE KEY(uuid),
    UNIQUE KEY(provider_id,reference)
);

CREATE SEQUENCE ad_admin_status_seq;

CREATE TABLE ad_admin_status(
    id BIGINT NOT NULL DEFAULT NEXTVAL('ad_admin_status_seq'),
    uuid UUID NOT NULL,
    provider_id BIGINT NOT NULL REFERENCES provider(id),
    status VARCHAR(36),
    message VARCHAR(512),
    reference VARCHAR(255),
    created TIMESTAMP NOT NULL DEFAULT NOW(),
    updated TIMESTAMP NOT NULL DEFAULT NOW(),
    PRIMARY KEY(id),
    UNIQUE KEY(uuid),
    UNIQUE KEY(provider_id, reference)
);