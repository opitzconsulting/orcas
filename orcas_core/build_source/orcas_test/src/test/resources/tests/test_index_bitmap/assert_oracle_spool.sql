create bitmap index MOD_BITMAP_IX on TAB_INDEX ( COL_MOD_IX_1 ) parallel;
alter index MOD_BITMAP_IX noparallel;
