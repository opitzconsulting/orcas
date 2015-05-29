SET ECHO			OFF
SET TERMOUT 		OFF
SET FEEDBACK		OFF
SET HEADING			OFF
SET LINESIZE 		32000;
SET PAGESIZE		50000;
SET trimspool on
SET trim on
set long 1000000000 longc 60000


select pa_orcas_xml_syex.get_model( pa_orcas_extensions.call_reverse_extensions( pa_orcas_trans_orig_syex.trans_orig_syex( pa_orcas_load_ist.get_ist()))) json_col from dual;

