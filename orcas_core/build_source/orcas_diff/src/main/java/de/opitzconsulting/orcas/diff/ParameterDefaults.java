package de.opitzconsulting.orcas.diff;

import de.opitzconsulting.orcas.diff.Parameters.FailOnErrorMode;

public class ParameterDefaults {
  public static final String jdbcdriver = "oracle.jdbc.OracleDriver";
  public static final String scriptfolderPostfix = ".sql";
  public static final String scriptfolderPrefix = "";
  public static final boolean scriptfolderrecursive = true;
  public static final String spoolfile = "orcas_spoolfile.sql";
  public static final String spoolfolder = "log/";
  public static final String loglevel = "info";
  public static final FailOnErrorMode failOnErrorMode = FailOnErrorMode.ALWAYS;
  public static final String usernameorcas = "";
  public static final boolean logonly = false;
  public static final boolean dropmode = false;
  public static final boolean indexparallelcreate = true;
  public static final boolean indexmovetablespace = true;
  public static final boolean tablemovetablespace = true;
  public static final boolean createmissingfkindexes = true;
  public static final String excludewheretable = "object_name like '%$%'";
  public static final String excludewheresequence = "object_name like '%$%'";
  public static final String dateformat = "dd.mm.yy";
  public static final String extensionparameter = "";
  public static final String targetplsql = "";
  public static final String replaceablesfolder = "src/main/sql/replaceables";
  public static final String staticsfolder = "src/main/sql/statics" ;
  public static final boolean additionsonly = false;
  public static final boolean logignoredstatements = true;
  public static final String xmllogfile = "log.xml";
  public static final boolean setunusedinsteadofdropcolumn = false;
  public static final boolean indexonlinecreate = false;
  public static final boolean minimizestatementcount = false;
  public static final String charsetname = "UTF-8";
  public static final String charsetnamesqllog = null;

  public static final String excludewhereview = "object_name not like '%'";
  public static final String excludewhereobjecttype = "object_name not like '%'";
  public static final String excludewherepackage = "object_name not like '%'";
  public static final String excludewheretrigger = "object_name not like '%'";
  public static final String excludewherefunction = "object_name not like '%'";
  public static final String excludewhereprocedure = "object_name not like '%'";

  public static final boolean extractremovedefaultvaluesfrommodel = true;
  public static final String viewextractmode = "text";
  public static boolean sqlplustable = false;
  public static boolean orderColumnsByName = false;
}
