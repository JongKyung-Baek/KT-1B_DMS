BEGIN;

-- ---------------------------------------------------------------------------
-- Remove the unused video, 3D-document, and legacy viewer configuration.
-- The migration is intentionally idempotent so it can be rerun during
-- Windows validation and the final AIX deployment.
-- ---------------------------------------------------------------------------

-- One legacy software record was classified under the unused 3D node.
-- Preserve the record and move only its classification to the 2D node.
UPDATE docs_sw
SET tree_cd = CASE
        WHEN tree_cd = 'TRB000014' THEN 'TRB000013'
        ELSE tree_cd
    END,
    business_type_cd = CASE
        WHEN business_type_cd = 'TRB000014' THEN 'TRB000013'
        ELSE business_type_cd
    END
WHERE tree_cd = 'TRB000014'
   OR business_type_cd = 'TRB000014';

DELETE FROM docs_sw_tree
WHERE tree_cd = 'TRB000014';

DELETE FROM docs_product_tree
WHERE tree_cd = 'TRB000014';

DELETE FROM docs_dxf_tree
WHERE tree_cd = 'TRB000014';

DELETE FROM docs_drawing_tree
WHERE tree_cd = 'TRB000014';

DELETE FROM docs_combo
WHERE combo_cd = 'drawingType'
  AND (
      upper(coalesce(value, '')) = '3D'
      OR upper(coalesce(text, '')) = '3D'
  );

DELETE FROM docs_toolbar_info
WHERE lower(toolbar_id) = 'toolbarvideorequest';

DELETE FROM docs_grid_info
WHERE lower(grid_id) = 'gridvideorequestlist';

DELETE FROM docs_form_info
WHERE lower(form_id) = 'formvideorequest';

-- Delete the child entry first in case a deployment adds a menu hierarchy FK.
DELETE FROM docs_menu
WHERE menu_cd = 'MENU_197';

DELETE FROM docs_menu
WHERE menu_cd = 'MENU_196'
   OR lower(coalesce(menu_url, '')) LIKE '%videorequest%';

DELETE FROM docs_system_config
WHERE upper(system_config_cd) IN (
    '3D_FILE_PATH',
    'ADAP_3D_PATH',
    'ADAP_3D_URL',
    'VIDEO_PATH',
    'ADAP_VIDEO_PATH',
    'ADAP_VIDEO_URL'
);

ALTER TABLE IF EXISTS docs_document
    DROP COLUMN IF EXISTS check_3dfile;

ALTER TABLE IF EXISTS docs_document_file
    DROP COLUMN IF EXISTS check_3dfile;

ALTER TABLE IF EXISTS docs_drawing
    DROP COLUMN IF EXISTS check_3dfile;

ALTER TABLE IF EXISTS docs_dxf_document
    DROP COLUMN IF EXISTS check_3dfile;

ALTER TABLE IF EXISTS docs_dxf_document_file
    DROP COLUMN IF EXISTS check_3dfile;

ALTER TABLE IF EXISTS docs_product_document
    DROP COLUMN IF EXISTS check_3dfile;

ALTER TABLE IF EXISTS docs_product_document_file
    DROP COLUMN IF EXISTS check_3dfile;

ALTER TABLE IF EXISTS docs_sw
    DROP COLUMN IF EXISTS check_3dfile;

ALTER TABLE IF EXISTS docs_sw_file
    DROP COLUMN IF EXISTS check_3dfile;

COMMIT;
