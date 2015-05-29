create table xml_tabelle
(
  col_xml_1 xmltype,
  col_xml_2 xmltype
);

create index add_xml_ix on xml_tabelle (col_xml_1) indextype is CTXSYS.CONTEXT PARAMETERS ('');
create index add_txt_ix on xml_tabelle (col_xml_1) indextype is XDB.XMLINDEX PARAMETERS ('PATH TABLE XML_TABELLE$D');


create table xml_tabelle_inline
(
  col_xml_1 xmltype,
  col_xml_2 xmltype
);

create index index_add_xml_ix on xml_tabelle_inline (col_xml_1) indextype is CTXSYS.CONTEXT PARAMETERS ('');
create index index_add_txt_ix on xml_tabelle_inline (col_xml_1) indextype is XDB.XMLINDEX PARAMETERS ('PATH TABLE XML_TABELLE_INLINE$D');


