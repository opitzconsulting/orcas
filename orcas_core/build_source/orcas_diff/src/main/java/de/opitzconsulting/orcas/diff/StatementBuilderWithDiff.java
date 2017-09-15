package de.opitzconsulting.orcas.diff;

import java.util.function.Supplier;

import de.opitzconsulting.orcas.orig.diff.AbstractDiff;

public class StatementBuilderWithDiff<T extends AbstractDiff> extends StatementBuilder
{
  T diff;

  public StatementBuilderWithDiff( Supplier<DiffAction> pDiffActionSupplier, boolean pIsAdditionsOnlyMode, T pDiff, AlterTableCombiner pAlterTableCombiner )
  {
    super( pDiffActionSupplier, pIsAdditionsOnlyMode, pAlterTableCombiner );
    diff = pDiff;
  }
}
