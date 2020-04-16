CREATE SEQUENCE provider_id_seq START WITH 10000;

CREATE TABLE provider(
    id NUMERIC(19,0) DEFAULT provider_id_seq.nextval NOT NULL,
    jwtid VARCHAR(36) NOT NULL,
    identifier VARCHAR(64) NOT NULL,
    email VARCHAR(255) NOT NULL,
    phone VARCHAR(32) NOT NULL,
    created TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY(id),
    UNIQUE (jwtid),
    UNIQUE (identifier)
);

CREATE SEQUENCE transfer_log_id_seq;

CREATE TABLE transfer_log (
    id NUMERIC(19,0) DEFAULT transfer_log_id_seq.nextval NOT NULL,
    provider_id NUMERIC(19,0) NOT NULL,
    items INTEGER NOT NULL,
    md5 VARCHAR(32) NOT NULL,
    payload CLOB NOT NULL,
    status VARCHAR(36) NOT NULL,
    message VARCHAR(512),
    created TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY(id),
    UNIQUE (provider_id,md5),
    CONSTRAINT fk_provider_transfer_log FOREIGN KEY (provider_id) REFERENCES provider(id)
);

CREATE INDEX transfer_log_status_idx ON transfer_log(status);

CREATE SEQUENCE adstate_id_seq;

CREATE TABLE ad_state(
    id NUMERIC(19,0) DEFAULT adstate_id_seq.nextval NOT NULL,
    uuid VARCHAR(36) NOT NULL,
    provider_id NUMERIC(19,0) NOT NULL,
    reference VARCHAR(255) NOT NULL,
    json_payload CLOB NOT NULL,
    version_id NUMERIC(19,0) NOT NULL,
    created TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY(id),
    UNIQUE (uuid),
    UNIQUE (provider_id,reference),
    CONSTRAINT fk_provider_ad_state FOREIGN KEY (provider_id) REFERENCES provider(id)
);

CREATE INDEX ad_state_updated_idx ON ad_state(updated);
CREATE INDEX ad_state_version_id_idx ON ad_state(version_id);

CREATE SEQUENCE admin_status_seq;

CREATE TABLE admin_status(
    id NUMERIC(19,0) DEFAULT admin_status_seq.nextval NOT NULL,
    uuid VARCHAR(36) NOT NULL,
    provider_id NUMERIC(19,0) NOT NULL,
    status VARCHAR(36) NOT NULL,
    message VARCHAR(512),
    reference VARCHAR(255) NOT NULL,
    version_id NUMERIC(19,0) NOT NULL,
    created TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY(id),
    UNIQUE (uuid),
    UNIQUE (provider_id, reference),
    CONSTRAINT fk_provider_admin_status FOREIGN KEY (provider_id) REFERENCES provider(id)
);

CREATE INDEX admin_status_version_id_idx ON admin_status(version_id);

CREATE TABLE shedlock(
    name VARCHAR(64),
    lock_until TIMESTAMP(3) NULL,
    locked_at TIMESTAMP(3) NULL,
    locked_by  VARCHAR(255),
    PRIMARY KEY (name)
);

CREATE TABLE feedtask(
    name VARCHAR(64),
    lastRun TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    PRIMARY KEY(name)
);
