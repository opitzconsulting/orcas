package de.opitzconsulting.orcas.diff;

import java.util.List;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EStructuralFeature;

import de.opitzconsulting.orcas.diff.RecreateNeededBuilder.Difference;
import de.opitzconsulting.orcas.orig.diff.AbstractDiff;

public interface RecreateNeededBuilderHandler<T extends AbstractDiff>
{
  T getDiff();

  void setRecreateNeededDependsOn( List<DiffActionReason> pDiffActionReasonDependsOnList );

  void setRecreateNeededDifferentAttributes( List<EStructuralFeature> pDiffReasonDetails );

  void setRecreateNeededDifferent( List<Difference> pDiffReasonDetails );
}
