SET ECHO			OFF
SET TERMOUT 		OFF
SET FEEDBACK		OFF
SET HEADING			OFF
SET LINESIZE 		10000;
SET PAGESIZE		50000;
SET trimspool on
SET trim on
set long 1000000000 longc 60000

declare
begin
  pa_orcas_xtext_model.build();
end;
/

select pa_orcas_xml_syex.get_model( pa_orcas_trans_orig_syex.trans_orig_syex( pa_orcas_trans_syex_orig.trans_syex_orig( pa_orcas_extensions.call_extensions( pa_orcas_model_holder.get_model() ) ))) json_col from dual;

