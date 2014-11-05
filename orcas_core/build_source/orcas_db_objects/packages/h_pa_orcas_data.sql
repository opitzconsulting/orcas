create or replace package pa_orcas_data authid current_user is
/**
 * Die Routinen für den Abgleich von Daten.
 *
 * @author fsa
 */

/**
 * Registriert die Tabellenstruktur.
 */
procedure register_table( p_metadata in ot_data_metadata );  
 
/**
 * Registriert Datensaetze fuer den merge/check.
 */
procedure insert_into( p_table_name in varchar2, p_rows in ct_data_rdl );  

/**
 * Fuegt die Datensaetze ein, falls sei noch nicht existieren, anderenfalls werden sie evtl. aktualisiert.
 */
procedure do_merge;

/**
 * Prueft, ob die defaultdaten noch passen.
 */
procedure do_check_only;

/**
 * Erzeugt ein SQLPlus-Skript fuer reverse engineering der daten.
 */
procedure genertae_reverse_script;

end;
/
