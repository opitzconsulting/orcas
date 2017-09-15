package de.opitzconsulting.orcas.diff;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import de.opitzconsulting.orcas.diff.DiffAction.DiffReasonType;
import de.opitzconsulting.orcas.diff.DiffAction.Statement;
import de.opitzconsulting.orcas.diff.DiffReasonKey.DiffReasonKeyRegistry;
import de.opitzconsulting.orcas.orig.diff.TableDiff;

public class AlterTableCombiner
{
  private DiffAction combinedDiffAction;
  private String currentStatementStart;
  private DiffReasonKeyRegistry diffReasonKeyRegistry;
  private Consumer<DiffAction> combinedDiffActionHandler;
  private DiffAction dummyDiffAction;
  private List<DiffAction> handledDiffAction;

  public AlterTableCombiner( DiffReasonKeyRegistry pDiffReasonKeyRegistry, Consumer<DiffAction> pCombinedDiffActionHandler )
  {
    diffReasonKeyRegistry = pDiffReasonKeyRegistry;
    combinedDiffActionHandler = pCombinedDiffActionHandler;
  }

  public void finishIfNeeded()
  {
    if( isCombining() )
    {
      finish();
    }
  }

  private void finish()
  {
    List<Statement> lStatementsToCombine = new ArrayList<>();

    if( handledDiffAction.size() == 1 )
    {
      combinedDiffAction = handledDiffAction.get( 0 );
    }

    for( Statement lStatement : dummyDiffAction.getStatements() )
    {
      if( lStatement.isIgnore() )
      {
        combinedDiffAction.addIgnoredStatement( lStatement.getStatement(), lStatement.getComment() );
      }
      else
      {
        if( lStatement.isFailure() )
        {
          combinedDiffAction.addFailureStatement( lStatement.getStatement(), lStatement.getComment() );
        }
        else
        {
          lStatementsToCombine.add( lStatement );
        }
      }
    }

    if( lStatementsToCombine.size() != 0 )
    {
      String lCombinedStatement;

      if( lStatementsToCombine.size() == 1 )
      {
        lCombinedStatement = lStatementsToCombine.get( 0 ).getStatement();
      }
      else
      {
        String lSeparator = "\n  ";
        lCombinedStatement = currentStatementStart + lSeparator + //
                             lStatementsToCombine.stream()//
                             .map( Statement::getStatement )//
                             .map( p -> p.substring( currentStatementStart.length() + 1 ) )//
                             .collect( Collectors.joining( lSeparator ) );
      }

      List<String> lComments = lStatementsToCombine.stream()//
      .map( Statement::getComment )//
      .filter( Objects::nonNull )//
      .collect( Collectors.toList() );

      if( lComments.isEmpty() )
      {
        combinedDiffAction.addStatement( lCombinedStatement );
      }
      else
      {
        combinedDiffAction.addStatement( lCombinedStatement, lComments.stream().collect( Collectors.joining( ";" ) ) );
      }
    }

    combinedDiffActionHandler.accept( combinedDiffAction );

    dummyDiffAction = null;
    combinedDiffAction = null;
    currentStatementStart = null;
  }

  private boolean isCombining()
  {
    return combinedDiffAction != null && currentStatementStart != null && dummyDiffAction != null;
  }

  public void handleStartAlterTable( TableDiff pTableDiff, String pStatementStart )
  {
    finishIfNotMatching( pStatementStart );

    if( !isCombining() )
    {
      dummyDiffAction = new DiffAction( diffReasonKeyRegistry.getDiffReasonKey( pTableDiff ), DiffReasonType.ALTER );
      combinedDiffAction = new DiffAction( diffReasonKeyRegistry.getDiffReasonKey( pTableDiff ), DiffReasonType.ALTER );
      currentStatementStart = pStatementStart;
      handledDiffAction = new ArrayList<>();
    }
  }

  public DiffAction getDiffActionForAddStatement( DiffAction pDiffAction, String pStatement )
  {
    finishIfNotMatching( pStatement );

    if( isCombining() )
    {
      if( !handledDiffAction.contains( pDiffAction ) )
      {
        pDiffAction.getDiffActionReasons().forEach( combinedDiffAction::addDiffActionReason );
        handledDiffAction.add( pDiffAction );
      }

      return dummyDiffAction;
    }

    return pDiffAction;
  }

  private void finishIfNotMatching( String pStatement )
  {
    if( isCombining() )
    {
      if( !pStatement.startsWith( currentStatementStart ) )
      {
        finish();
      }
    }
  }
}
