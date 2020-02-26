CREATE SEQUENCE provider_id_seq;

CREATE TABLE provider (
    id BIGINT NOT NULL DEFAULT NEXTVAL('provider_id_seq'),
    identifier VARCHAR(64) NOT NULL,
    email VARCHAR(255) NOT NULL,
    phone VARCHAR(32) NOT NULL,
    created TIMESTAMP NOT NULL DEFAULT NOW(),
    updated TIMESTAMP NOT NULL DEFAULT NOW(),
    PRIMARY KEY(id),
    UNIQUE KEY(identifier)
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
    version_id BIGINT NOT NULL,
    created TIMESTAMP NOT NULL DEFAULT NOW(),
    updated TIMESTAMP NOT NULL DEFAULT NOW(),
    PRIMARY KEY(id),
    UNIQUE KEY(uuid),
    UNIQUE KEY(provider_id,reference)
);

CREATE INDEX ad_state_updated_idx ON ad_state(updated);
CREATE INDEX ad_state_version_id_idx ON ad_state(version_id);

CREATE SEQUENCE ad_admin_status_seq;

CREATE TABLE ad_admin_status(
    id BIGINT NOT NULL DEFAULT NEXTVAL('ad_admin_status_seq'),
    uuid UUID NOT NULL,
    provider_id BIGINT NOT NULL REFERENCES provider(id),
    status VARCHAR(36),
    message VARCHAR(512),
    reference VARCHAR(255),
    version_id BIGINT NOT NULL,
    created TIMESTAMP NOT NULL DEFAULT NOW(),
    updated TIMESTAMP NOT NULL DEFAULT NOW(),
    PRIMARY KEY(id),
    UNIQUE KEY(uuid),
    UNIQUE KEY(provider_id, reference)
);

CREATE INDEX ad_admin_status_version_id_idx ON ad_admin_status(version_id);

CREATE TABLE shedlock(
    name VARCHAR(64),
    lock_until TIMESTAMP(3) NULL,
    locked_at TIMESTAMP(3) NULL,
    locked_by  VARCHAR(255),
    PRIMARY KEY (name)
)