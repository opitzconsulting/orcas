create table xml_tabelle
(
  col_xml_1 xmltype,
  col_xml_2 xmltype
);

create index del_xml_ix on xml_tabelle (col_xml_2) indextype is CTXSYS.CONTEXT PARAMETERS ('');
create index del_txt_ix on xml_tabelle (col_xml_2) indextype is XDB.XMLINDEX PARAMETERS ('PATH TABLE XML_TABELLE_DEL$D');


create table xml_tabelle_inline
(
  col_xml_1 xmltype,
  col_xml_2 xmltype
);

create index index_del_xml_ix on xml_tabelle_inline (col_xml_2) indextype is CTXSYS.CONTEXT PARAMETERS ('');
create index index_del_txt_ix on xml_tabelle_inline (col_xml_2) indextype is XDB.XMLINDEX PARAMETERS ('PATH TABLE XML_TABELLE_INLINE_DEL$D');



