create index MOD_PARALLEL_IX on TAB_INDEX ( COL_MOD_IX_3 ) parallel;
create index MOD_NOPARALLEL_IX on TAB_INDEX ( COL_MOD_IX_4 ) parallel;
alter index MOD_NOPARALLEL_IX noparallel;
create index INLINE_MOD_PARALLEL_IX on TAB_INDEX_INLINE ( COL_MOD_IX_3 ) parallel;
create index INLINE_MOD_NOPARALLEL_IX on TAB_INDEX_INLINE ( COL_MOD_IX_4 ) parallel;
alter index INLINE_MOD_NOPARALLEL_IX noparallel;
