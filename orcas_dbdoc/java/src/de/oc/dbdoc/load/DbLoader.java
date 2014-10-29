package de.oc.dbdoc.load;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import de.oc.dbdoc.Main;
import de.oc.dbdoc.ant.Diagram;
import de.oc.dbdoc.schemadata.Association;
import de.oc.dbdoc.schemadata.Column;
import de.oc.dbdoc.schemadata.Schema;
import de.oc.dbdoc.schemadata.Table;

public class DbLoader
{
  public DbLoader()
  {
  }

  /**
   * Loads a Database Schema from the Database.
   * 
   * @param pSql
   */
  public Schema loadSchema( Connection pConnection, Diagram pRootDiagram ) throws SQLException
  {
    Schema lSchema = new Schema();

    String lSql = "select table_name from user_tables";

    CallableStatement lTableStatement = pConnection.prepareCall( lSql );
    ResultSet lTableResultSet = lTableStatement.executeQuery();

    while( lTableResultSet.next() )
    {
      if( pRootDiagram.isTableIncluded( lTableResultSet.getString( 1 ) ) )
      {
        Main.log( "Loading Table: " + lTableResultSet.getString( 1 ) );

        Table lTable = new Table( lTableResultSet.getString( 1 ) );

        lSchema.addTable( lTable );

        CallableStatement lColumnStatement = pConnection.prepareCall( "select column_name from user_tab_columns where table_name = :1 order by column_id" );
        lColumnStatement.setString( 1, lTable.getName() );
        ResultSet lColumnResultSet = lColumnStatement.executeQuery();

        while( lColumnResultSet.next() )
        {
          lTable.addColumn( new Column( lColumnResultSet.getString( 1 ) ) );
        }

        lColumnResultSet.close();
        lColumnStatement.close();
      }
    }

    lTableResultSet.close();
    lTableStatement.close();

    String lFkSql = "   select /*+ full(o)*/ "
                    + "        o.constraint_name fk_name, "
                    + "        o.table_name tab_from, "
                    + "        o_fk_cols.column_name col_from, "
                    + "        i.table_name tab_to, "
                    + "        i_fk_cols.column_name col_to, "
                    + "        nvl "
                    + "        ( "
                    + "          ( "
                    + "          select distinct "
                    + "                 'N' "
                    + "             from user_cons_columns ox_fk_cols, "
                    + "                  user_tab_columns "
                    + "            where ox_fk_cols.constraint_name = o.constraint_name "
                    + "              and ox_fk_cols.table_name = o.table_name "
                    + "              and user_tab_columns.column_name = ox_fk_cols.column_name "
                    + "              and ox_fk_cols.table_name = user_tab_columns.table_name              "
                    + "              and nullable = 'N' "
                    + "          ), "
                    + "          'Y' "
                    + "        ) as nullable, "
                    + "        nvl "
                    + "        ( "
                    + "          ( "
                    + "          select distinct "
                    + "                 'Y' "
                    + "            from user_constraints o_uk "
                    + "           where o_uk.table_name = o.table_name "
                    + "             and o_uk.constraint_type = 'U' "
                    + "             and not exists "
                    + "                 ( "
                    + "                 select 1 "
                    + "                   from user_cons_columns o_uk_cols "
                    + "                  where o_uk_cols.constraint_name = o_uk.constraint_name "
                    + "                    and o_uk_cols.table_name = o_uk.table_name "
                    + "                    and not exists "
                    + "                        ( "
                    + "                        select 1 "
                    + "                          from user_cons_columns ox_fk_cols "
                    + "                         where ox_fk_cols.constraint_name = o.constraint_name "
                    + "                           and ox_fk_cols.table_name = o.table_name "
                    + "                           and ox_fk_cols.column_name = o_uk_cols.column_name "
                    + "                        ) "
                    + "                 ) "
                    + "          ), "
                    + "         'N' "
                    + "        ) as uk_on_fk "
                    + "   from user_constraints o, "
                    + "        user_constraints i, "
                    + "        user_cons_columns o_fk_cols, "
                    + "        user_tab_columns, "
                    + "        user_cons_columns i_fk_cols "
                    + "  where o.constraint_type = 'R' "
                    + "    and i.constraint_name = o.r_constraint_name "
                    + "    and o_fk_cols.constraint_name = o.constraint_name "
                    + "    and o_fk_cols.column_name = user_tab_columns.column_name "
                    + "    and o.table_name = user_tab_columns.table_name "
                    + "    and i_fk_cols.constraint_name = i.constraint_name "
                    + "    and i_fk_cols.table_name = i.table_name "
                    + "  order by 1,2    ";

    CallableStatement lFkStatement = pConnection.prepareCall( lFkSql );
    ResultSet lFkResultSet = lFkStatement.executeQuery();

    Association lAssociation = null;

    while( lFkResultSet.next() )
    {
      Table lTableFrom = lSchema.findTable( lFkResultSet.getString( "tab_from" ) );
      Table lTableTo = lSchema.findTable( lFkResultSet.getString( "tab_to" ) );

      if( lTableFrom != null && lTableTo != null )
      {
        String lConstraintName = lFkResultSet.getString( "fk_name" );

        if( lAssociation == null || !lAssociation.getAssociationName().equals( lConstraintName ) )
        {
          lAssociation = new Association( lConstraintName, lTableFrom, lTableTo, true, 0, lFkResultSet.getString( "uk_on_fk" ).equals( "N" ) ? Association.MULTIPLICITY_N : 1, lFkResultSet.getString(
              "nullable" ).equals( "N" ) ? 1 : 0, 1 );

          lSchema.addAssociation( lAssociation );
        }

        lAssociation.addColumnFrom( lFkResultSet.getString( "col_from" ) );
        lAssociation.addColumnTo( lFkResultSet.getString( "col_to" ) );
      }
    }

    lFkResultSet.close();
    lFkStatement.close();

    return lSchema;
  }
}
