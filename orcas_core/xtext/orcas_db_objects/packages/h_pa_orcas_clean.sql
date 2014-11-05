CREATE OR REPLACE package pa_orcas_clean authid current_user is

/**
 * Loescht alle DDL daten zu der Tabelle, bis auf Saplten und die Tabelle selbst.
 */
procedure clean_table( p_table_name in varchar2 );

/**
 * Loescht alle DDL daten zu allen Tabelle, bis auf Saplten und die Tabellen selbst.
 */
procedure clean_all_tables;


end;
/
