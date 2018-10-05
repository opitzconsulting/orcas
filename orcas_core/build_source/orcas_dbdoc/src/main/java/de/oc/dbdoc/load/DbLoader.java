package de.oc.dbdoc.load;

import de.oc.dbdoc.ant.Diagram;
import de.oc.dbdoc.ant.Tableregistry;
import de.oc.dbdoc.schemadata.Association;
import de.oc.dbdoc.schemadata.Column;
import de.oc.dbdoc.schemadata.Schema;
import de.oc.dbdoc.schemadata.Table;
import de.opitzconsulting.orcasDsl.Index;
import de.opitzconsulting.orcasDsl.Model;
import de.opitzconsulting.orcasDsl.UniqueKey;

public class DbLoader
{
  public DbLoader()
  {
  }

  public Schema loadSchema( Diagram pRootDiagram, Model pModel, Tableregistry pTableregistry )
  {
    Schema lSchema = new Schema();

    pModel.getModel_elements().stream().filter( p -> p instanceof de.opitzconsulting.orcasDsl.Table ).map( p -> (de.opitzconsulting.orcasDsl.Table) p ).forEach( pTable ->
    {
      if( pRootDiagram.isTableIncluded( pTable.getName(), pTableregistry ) )
      {
        Table lTable = new Table( pTable.getName() );

        lSchema.addTable( lTable );

        pTable.getColumns().forEach( pColumn -> lTable.addColumn( new Column( pColumn.getName() ) ) );
      }
    } );

    pModel.getModel_elements().stream().filter( p -> p instanceof de.opitzconsulting.orcasDsl.Table ).map( p -> (de.opitzconsulting.orcasDsl.Table) p ).forEach( pTable ->
    {
      pTable.getForeign_keys().forEach( pFK ->
      {
        Table lTableFrom = lSchema.findTable( pTable.getName() );
        Table lTableTo = lSchema.findTable( pFK.getDestTable() );

        if( lTableFrom != null && lTableTo != null )
        {
          String lConstraintName = pFK.getConsName();

          boolean lIsAllSrcColumnsNullable = !pFK.getSrcColumns().stream().map( pSrcColumnRef -> pTable.getColumns().stream().filter( p -> p.getName().equalsIgnoreCase( pSrcColumnRef.getColumn_name() ) ).findAny().get() ).filter( p -> p.isNotnull() ).findAny().isPresent();
          boolean lIsExistsUkForSrcColumns = pFK.getSrcColumns().size() == 1 && pTable.getInd_uks().stream().filter( p ->
          {
            if( p instanceof UniqueKey )
            {
              UniqueKey lUniqueKey = (UniqueKey) p;

              return lUniqueKey.getUk_columns().size() == 1 && lUniqueKey.getUk_columns().get( 0 ).getColumn_name().equalsIgnoreCase( pFK.getSrcColumns().get( 0 ).getColumn_name() );
            }
            else
            {
              Index lIndex = (Index) p;

              return lIndex.getIndex_columns().size() == 1 && lIndex.getIndex_columns().get( 0 ).getColumn_name().equalsIgnoreCase( pFK.getSrcColumns().get( 0 ).getColumn_name() );
            }
          } ).findAny().isPresent();

          Association lAssociation = new Association( lConstraintName, lTableFrom, lTableTo, true, 0, lIsExistsUkForSrcColumns ? 1 : Association.MULTIPLICITY_N, lIsAllSrcColumnsNullable ? 0 : 1, 1 );

          lSchema.addAssociation( lAssociation );

          pFK.getSrcColumns().forEach( pColumn -> lAssociation.addColumnFrom( pColumn.getColumn_name() ) );
          pFK.getDestColumns().forEach( pColumn -> lAssociation.addColumnTo( pColumn.getColumn_name() ) );
        }
      } );
    } );

    return lSchema;
  }
}
