spool build/tmp/drop_all_fks.sql

SELECT 'ALTER TABLE '
    + (OBJECT_SCHEMA_NAME(parent_object_id))
    + '.'
    +  QUOTENAME(OBJECT_NAME(parent_object_id))
    + ' '
    + 'DROP CONSTRAINT'
    + QUOTENAME(name) + ';'
FROM sys.foreign_keys
where upper(OBJECT_SCHEMA_NAME(parent_object_id)) = upper('&1');

spool off;

@build/tmp/drop_all_fks.sql

spool build/tmp/drop_all_tables.sql

SELECT 'DROP TABLE '
    + (OBJECT_SCHEMA_NAME(object_id))
    + '.'
    +  QUOTENAME(OBJECT_NAME(object_id)) + ';'
FROM sys.tables
where upper(OBJECT_SCHEMA_NAME(object_id)) = upper('&1');

spool off;

@build/tmp/drop_all_tables.sql

spool build/tmp/drop_all_objects.sql

SELECT 'DROP ' + REPLACE(type_desc,'_',' ') + ' '
    + (OBJECT_SCHEMA_NAME(object_id))
    + '.'
    +  QUOTENAME(OBJECT_NAME(object_id)) + ';'
FROM sys.objects
where upper(OBJECT_SCHEMA_NAME(parent_object_id)) = upper('&1');

spool off;

@build/tmp/drop_all_objects.sql

ALTER USER &1 WITH DEFAULT_SCHEMA = dbo;
drop schema IF EXISTS  &1;
drop user IF EXISTS  &1;
drop login  &1;
CREATE LOGIN &1 WITH PASSWORD = '&1', CHECK_POLICY = OFF;
CREATE USER &1 FOR LOGIN &1;
CREATE SCHEMA &1 AUTHORIZATION &1;
ALTER ROLE db_ddladmin ADD MEMBER &1;
ALTER USER &1 WITH DEFAULT_SCHEMA = &1;



