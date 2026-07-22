CREATE TABLE IF NOT EXISTS docs_production_sub_file (
    object_id         varchar(32)   PRIMARY KEY,
    parent_object_id  varchar(32)   NOT NULL,
    file_no           integer       NOT NULL,
    org_file_nm       varchar(255)  NOT NULL,
    file_path_nm      varchar(1000) NOT NULL,
    file_size         bigint        NOT NULL DEFAULT 0,
    use_yn            char(1)       NOT NULL DEFAULT 'Y',
    insert_uid        varchar(30)   NOT NULL,
    insert_dt         timestamp     NOT NULL DEFAULT now(),
    update_uid        varchar(30),
    update_dt         timestamp,
    CONSTRAINT uq_docs_production_sub_file_parent_no
        UNIQUE (parent_object_id, file_no),
    CONSTRAINT ck_docs_production_sub_file_use_yn
        CHECK (use_yn IN ('Y', 'N'))
);

CREATE INDEX IF NOT EXISTS idx_docs_production_sub_file_parent
    ON docs_production_sub_file (parent_object_id);

CREATE INDEX IF NOT EXISTS idx_docs_production_sub_file_parent_useyn
    ON docs_production_sub_file (parent_object_id, use_yn);
