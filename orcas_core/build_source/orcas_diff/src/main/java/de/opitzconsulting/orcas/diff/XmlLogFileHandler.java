package de.opitzconsulting.orcas.diff;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import de.opitzconsulting.orcas.diff.DiffAction.Statement;
import de.opitzconsulting.orcas.diff.OrcasDiff.DiffResult;

public class XmlLogFileHandler
{
  private static final String TAG_REASON_DETAIL = "reason-detail";
  private static final String TAG_DIFF_ACTION_REASON = "diff-action-reason";
  private static final String TAG_STATEMENT = "statement";
  private static final String TAG_DIFF_ACTION = "diff-action";
  private static final String TAG_DIFF_ACTIONS = "diff-actions";

  private static final String ATTRIBUTE_COMMENT = "comment";
  private static final String ATTRIBUTE_IGNORE = "ignore";
  private static final String ATTRIBUTE_FAILURE = "failure";
  private static final String ATTRIBUTE_KEY = "key";
  private static final String ATTRIBUTE_ACTION_REASON_KEY = "key";
  private static final String ATTRIBUTE_ACTION_REASON_TYPE = "type";

  public void logXml( DiffResult pDiffResult, String pXmlLogFile )
  {
    Element lDiffActionsElement = new Element( TAG_DIFF_ACTIONS );
    for( DiffAction lDiffAction : pDiffResult.getDiffActions() )
    {
      lDiffActionsElement.addContent( convertDiffActionToXml( lDiffAction ) );
    }

    try
    {
      File lXmlLogFile = new File( pXmlLogFile );

      if( !lXmlLogFile.getParentFile().exists() )
      {
        lXmlLogFile.getParentFile().mkdirs();
      }

      new XMLOutputter( Format.getPrettyFormat() ).output( new Document( lDiffActionsElement ), new FileOutputStream( lXmlLogFile ) );
    }
    catch( Exception e )
    {
      throw new RuntimeException( e );
    }
  }

  private Element convertDiffActionToXml( DiffAction lDiffAction )
  {
    Element lDiffActionElement = new Element( TAG_DIFF_ACTION );

    lDiffActionElement.setAttribute( ATTRIBUTE_KEY, lDiffAction.getTextKey() );

    for( DiffActionReason lDiffActionReason : lDiffAction.getDiffActionReasons() )
    {
      lDiffActionElement.addContent( convertDiffActionReasonToXml( lDiffActionReason ) );
    }

    for( Statement lStatement : lDiffAction.getStatements() )
    {
      Element lStatementElement = new Element( TAG_STATEMENT );
      lDiffActionElement.addContent( lStatementElement );
      lStatementElement.setAttribute( ATTRIBUTE_IGNORE, "" + lStatement.isIgnore() );
      lStatementElement.setAttribute( ATTRIBUTE_FAILURE, "" + lStatement.isFailure() );
      if( lStatement.getComment() != null )
      {
        lStatementElement.setAttribute( ATTRIBUTE_COMMENT, lStatement.getComment() );
      }
      lStatementElement.addContent( lStatement.getStatement() );
    }
    return lDiffActionElement;
  }

  private Element convertDiffActionReasonToXml( DiffActionReason pDiffActionReason )
  {
    Element lReturn = new Element( TAG_DIFF_ACTION_REASON );

    lReturn.setAttribute( ATTRIBUTE_ACTION_REASON_TYPE, pDiffActionReason.getTypeString() );
    if( pDiffActionReason.getDiffReasonKey() != null )
    {
      lReturn.setAttribute( ATTRIBUTE_ACTION_REASON_KEY, pDiffActionReason.getDiffReasonKey().getTextKey() );
    }

    if( pDiffActionReason instanceof DiffActionReasonDifferent )
    {
      DiffActionReasonDifferent lDiffActionReasonDifferent = (DiffActionReasonDifferent) pDiffActionReason;

      for( String lDiffReasonDetail : lDiffActionReasonDifferent.getDiffReasonDetails() )
      {
        Element lStatementElement = new Element( TAG_REASON_DETAIL );
        lReturn.addContent( lStatementElement );
        lStatementElement.addContent( lDiffReasonDetail );
      }
    }

    if( pDiffActionReason instanceof DiffActionReasonDependsOn )
    {
      DiffActionReasonDependsOn lDiffActionReasonDependsOn = (DiffActionReasonDependsOn) pDiffActionReason;

      for( DiffActionReason lDiffActionReason : lDiffActionReasonDependsOn.getDiffActionReasonDependsOnList() )
      {
        lReturn.addContent( convertDiffActionReasonToXml( lDiffActionReason ) );
      }
    }

    return lReturn;
  }

  private List<DiffActionReason> parseDependsOnDiffActionReasons( Element pDiffActionReasonElement )
  {
    return pDiffActionReasonElement.getChildren( TAG_DIFF_ACTION_REASON )//
    .stream()//
    .map( this::parseDiffActionReasonElement )//
    .collect( Collectors.toList() );
  }

  private List<String> parseDiffReasonDetails( Element pDiffActionReasonElement )
  {
    return pDiffActionReasonElement.getChildren( TAG_REASON_DETAIL )//
    .stream()//
    .map( this::getContentsAsString )//
    .collect( Collectors.toList() );
  }

  public DiffActionReason parseDiffActionReasonElement( Element pDiffActionReasonElement )
  {
    DiffReasonKey lDiffReasonKey = DiffReasonKey.createByTextKey( pDiffActionReasonElement.getAttributeValue( ATTRIBUTE_ACTION_REASON_KEY ) );

    String lTypeString = pDiffActionReasonElement.getAttributeValue( ATTRIBUTE_ACTION_REASON_TYPE );

    switch( lTypeString )
    {
      case "dependent":
        return new DiffActionReasonDependsOn( lDiffReasonKey, parseDependsOnDiffActionReasons( pDiffActionReasonElement ) );
      case "different":
        return new DiffActionReasonDifferent( lDiffReasonKey, parseDiffReasonDetails( pDiffActionReasonElement ) );
      case "missing":
        return new DiffActionReasonMissing( lDiffReasonKey );
      case "surplus":
        return new DiffActionReasonSurplus( lDiffReasonKey );
      default:
        throw new IllegalStateException( "unknown action type: " + lTypeString );
    }
  }

  public DiffResult parseXml( String pXmlLogFile )
  {
    List<DiffAction> lDiffActions = new ArrayList<>();

    SAXBuilder lSAXBuilder = new SAXBuilder();

    Document lDocument;
    try
    {
      lDocument = lSAXBuilder.build( new FileInputStream( pXmlLogFile ) );
    }
    catch( Exception e )
    {
      throw new RuntimeException( e );
    }

    for( Element lActionElement : lDocument.getRootElement().getChildren( TAG_DIFF_ACTION ) )
    {
      DiffAction lDiffAction = DiffAction.parseFromTextKey( lActionElement.getAttributeValue( ATTRIBUTE_KEY ) );
      lDiffActions.add( lDiffAction );

      for( Element lActionReasonElement : lActionElement.getChildren( TAG_DIFF_ACTION_REASON ) )
      {
        lDiffAction.addDiffActionReason( parseDiffActionReasonElement( lActionReasonElement ) );
      }

      for( Element lStatementElement : lActionElement.getChildren( TAG_STATEMENT ) )
      {
        boolean lIsFailure = Boolean.TRUE.toString().equals( lStatementElement.getAttributeValue( ATTRIBUTE_FAILURE ) );
        boolean lIsIgnored = Boolean.TRUE.toString().equals( lStatementElement.getAttributeValue( ATTRIBUTE_IGNORE ) );
        String lStatement = getContentsAsString( lStatementElement );
        String lCommentValue = lStatementElement.getAttributeValue( ATTRIBUTE_COMMENT );

        if( lIsIgnored )
        {
          lDiffAction.addIgnoredStatement( lStatement, lCommentValue );
        }
        else
        {
          if( lIsFailure )
          {
            lDiffAction.addFailureStatement( lStatement, lCommentValue );
          }
          else
          {
            lDiffAction.addStatement( lStatement, lCommentValue );
          }
        }
      }
    }

    return new DiffResult( lDiffActions );
  }

  private String getContentsAsString( Element pElement )
  {
    return pElement.getText();
  }

  private String getXmlString( Element pElement )
  {
    try
    {
      StringWriter lStringWriter = new StringWriter();
      new XMLOutputter( Format.getPrettyFormat() ).output( pElement, lStringWriter );

      return lStringWriter.getBuffer().toString();
    }
    catch( IOException e )
    {
      throw new RuntimeException( e );
    }
  }

  private String toXmlStringInputDiffActions( List<DiffAction> pInputDiffActions )
  {
    return pInputDiffActions//
    .stream()//
    .map( this::convertDiffActionToXml )//
    .map( this::getXmlString )//
    .collect( Collectors.joining( "," ) );
  }

  private String toXmlStringDiffActionReasons( List<DiffActionReason> pDiffActionReasons )
  {
    return pDiffActionReasons//
    .stream()//
    .map( this::convertDiffActionReasonToXml )//
    .map( this::getXmlString )//
    .collect( Collectors.joining( "," ) );
  }

  public RuntimeException createDiffReasonsDifferentExcepotion( List<DiffActionReason> pOriginalDiffActionReasons, List<DiffActionReason> pInputDiffActionReasons )
  {
    return new RuntimeException( TAG_DIFF_ACTION_REASON + "s different: " + toXmlStringDiffActionReasons( pOriginalDiffActionReasons ) + " not equal " + toXmlStringDiffActionReasons( pInputDiffActionReasons ) );
  }

  public RuntimeException createDuplicateDiffActionException( List<DiffAction> pInputDiffActions )
  {
    return new RuntimeException( "duplicate " + TAG_DIFF_ACTION + "s: " + toXmlStringInputDiffActions( pInputDiffActions ) );
  }

  public RuntimeException createSurplusDiffActionException( List<DiffAction> pUnmatchedInputDiffActionList )
  {
    return new RuntimeException( "surplus " + TAG_DIFF_ACTION + "s: " + toXmlStringInputDiffActions( pUnmatchedInputDiffActionList ) );
  }
}
