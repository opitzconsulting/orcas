create or replace package pa_orcas_updates is
/**
 * Die Routinen für die Nachverfolgung von update skripten.
 *
 * @author fsa
 */

/**
 * Setzt das Script auf den Zustand ausgefuehrt.
 */                                                                                                      
procedure set_executed( p_script_name in varchar2, p_logname in varchar2  );

end;
/
