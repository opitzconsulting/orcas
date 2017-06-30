package de.opitzconsulting.orcas.diff;

import java.util.ArrayList;
import java.util.List;

import de.opitzconsulting.origOrcasDsl.ColumnRef;
import de.opitzconsulting.origOrcasDsl.impl.ColumnRefImpl;

public class ModelUtil
{
  public static List<ColumnRef> copyColumnRefs( Iterable<ColumnRef> pColumnRefs )
  {
    List<ColumnRef> lReturn = new ArrayList<ColumnRef>();

    for( ColumnRef lColumnRef : pColumnRefs )
    {
      ColumnRef lCopyColumnRef = new ColumnRefImpl();

      lCopyColumnRef.setColumn_name( lColumnRef.getColumn_name() );

      lReturn.add( lCopyColumnRef );
    }

    return lReturn;
  }
}
