-- Keep the application-wide initial password deterministic for user creation,
-- administrator password reset and the mandatory first-login change flow.
INSERT INTO docs_system_config (
    system_config_group,
    system_config_cd,
    system_config_value,
    system_config_desc
)
VALUES (
    'SYSTEM_CONFIG',
    'BASIC_PASSWORD',
    '0000',
    'Initial password for newly created or reset users'
)
ON CONFLICT (system_config_group, system_config_cd)
DO UPDATE SET
    system_config_value = EXCLUDED.system_config_value,
    system_config_desc = EXCLUDED.system_config_desc;
