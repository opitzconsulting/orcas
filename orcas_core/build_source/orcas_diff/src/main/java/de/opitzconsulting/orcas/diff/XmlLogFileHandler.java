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
  private static final String TAG_STATEMENT_ATTRIBUTE_COMMENT = "comment";
  private static final String TAG_STATEMENT_ATTRIBUTE_IGNORE = "ignore";
  private static final String TAG_STATEMENT_ATTRIBUTE_FAILURE = "failure";

  private static final String TAG_DIFF_ACTION = "diff-action";
  private static final String TAG_DIFF_ACTION_ATTRIBUTE_TYPE = "type";
  private static final String TAG_DIFF_ACTION_ATTRIBUTE_RECREATE = "recreate";
  private static final String TAG_DIFF_ACTION_AND_REASON__ATTRIBUTE_OBJECT_TYPE = "object-type";
  private static final String TAG_DIFF_ACTION_AND_REASON__ATTRIBUTE_OBJECT_NAME = "object-name";
  private static final String TAG_DIFF_ACTION_AND_REASON__ATTRIBUTE_SCHEMA_NAME = "schema-name";
  private static final String TAG_DIFF_ACTION_AND_REASON__ATTRIBUTE_SUBOBJECT_TYPE = "subobject-type";
  private static final String TAG_DIFF_ACTION_AND_REASON__ATTRIBUTE_SUBOBJECT_NAME = "subobject-name";

  private static final String TAG_DIFF_ACTION_REASON_ATTRIBUTE_TYPE = "type";

  private static final String TAG_DIFF_ACTIONS = "diff-actions";

  private Parameters _parameters;

  public XmlLogFileHandler( Parameters pParameters )
  {
    _parameters = pParameters;
  }

  public void logXml( DiffResult pDiffResult, String pXmlLogFile )
  {
    Element lDiffActionsElement = new Element( TAG_DIFF_ACTIONS );
    for( DiffAction lDiffAction : pDiffResult.getDiffActions() )
    {
      lDiffActionsElement.addContent( convertDiffActionToXml( lDiffAction ) );
    }

    writeXmlFile( pXmlLogFile, new Document( lDiffActionsElement ) );
  }

  private void writeXmlFile( String pXmlLogFile, Document pDocument )
  {
    try
    {
      File lXmlLogFile = new File( pXmlLogFile );

      if( !lXmlLogFile.getParentFile().exists() )
      {
        lXmlLogFile.getParentFile().mkdirs();
      }

      Format lFormat = Format.getPrettyFormat();
      lFormat.setEncoding( _parameters.getEncodingForSqlLog().name() );
      new XMLOutputter( lFormat ).output( pDocument, new FileOutputStream( lXmlLogFile ) );
    }
    catch( Exception e )
    {
      throw new RuntimeException( e );
    }
  }

  private Element convertDiffActionToXml( DiffAction lDiffAction )
  {
    Element lDiffActionElement = new Element( TAG_DIFF_ACTION );

    lDiffActionElement.setAttribute( TAG_DIFF_ACTION_ATTRIBUTE_TYPE, lDiffAction.getDiffReasonTypeNoCombinedRecreates().name().toLowerCase() );
    setBooleanAttributeIfTrue( lDiffActionElement, TAG_DIFF_ACTION_ATTRIBUTE_RECREATE, lDiffAction.isRecreate() );

    setDiffReasonKeyToElement( lDiffAction.getDiffReasonKey(), lDiffActionElement );

    for( DiffActionReason lDiffActionReason : lDiffAction.getDiffActionReasons() )
    {
      lDiffActionElement.addContent( convertDiffActionReasonToXml( lDiffActionReason ) );
    }

    for( Statement lStatement : lDiffAction.getStatements() )
    {
      Element lStatementElement = new Element( TAG_STATEMENT );
      lDiffActionElement.addContent( lStatementElement );
      setBooleanAttributeIfTrue( lStatementElement, TAG_STATEMENT_ATTRIBUTE_IGNORE, lStatement.isIgnore() );
      setBooleanAttributeIfTrue( lStatementElement, TAG_STATEMENT_ATTRIBUTE_FAILURE, lStatement.isFailure() );
      if( lStatement.getComment() != null )
      {
        lStatementElement.setAttribute( TAG_STATEMENT_ATTRIBUTE_COMMENT, lStatement.getComment() );
      }
      lStatementElement.addContent( lStatement.getStatement() );
    }
    return lDiffActionElement;
  }

  private void setBooleanAttributeIfTrue( Element pElement, String pAttribute, boolean pValue )
  {
    if( pValue )
    {
      pElement.setAttribute( pAttribute, "" + pValue );
    }
  }

  private void setDiffReasonKeyToElement( DiffReasonKey pDiffReasonKey, Element pElement )
  {
    pElement.setAttribute( TAG_DIFF_ACTION_AND_REASON__ATTRIBUTE_OBJECT_TYPE, pDiffReasonKey.getTextObjectType() );
    if( pDiffReasonKey.getTextSubobjectType() != null )
    {
      pElement.setAttribute( TAG_DIFF_ACTION_AND_REASON__ATTRIBUTE_SUBOBJECT_TYPE, pDiffReasonKey.getTextSubobjectType() );
    }
    pElement.setAttribute( TAG_DIFF_ACTION_AND_REASON__ATTRIBUTE_OBJECT_NAME, pDiffReasonKey.getTextObjectName() );
    if(pDiffReasonKey.getTextSchemaName()!=null){
      pElement.setAttribute( TAG_DIFF_ACTION_AND_REASON__ATTRIBUTE_SCHEMA_NAME, pDiffReasonKey.getTextSchemaName() );
    }
    if( pDiffReasonKey.getTextSubobjectName() != null )
    {
      pElement.setAttribute( TAG_DIFF_ACTION_AND_REASON__ATTRIBUTE_SUBOBJECT_NAME, pDiffReasonKey.getTextSubobjectName() );
    }
  }

  private Element convertDiffActionReasonToXml( DiffActionReason pDiffActionReason )
  {
    Element lReturn = new Element( TAG_DIFF_ACTION_REASON );

    lReturn.setAttribute( TAG_DIFF_ACTION_REASON_ATTRIBUTE_TYPE, pDiffActionReason.getTypeString() );
    if( pDiffActionReason.getDiffReasonKey() != null )
    {
      setDiffReasonKeyToElement( pDiffActionReason.getDiffReasonKey(), lReturn );
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
    DiffReasonKey lDiffReasonKey = parseDiffReasonKey( pDiffActionReasonElement );

    String lTypeString = pDiffActionReasonElement.getAttributeValue( TAG_DIFF_ACTION_REASON_ATTRIBUTE_TYPE );

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
    Document lDocument = readXmlFile( pXmlLogFile );

    return new DiffResult( lDocument.getRootElement().getChildren( TAG_DIFF_ACTION )//
    .stream()//
    .map( this::parseDiffAction )//
    .collect( Collectors.toList() ) );
  }

  private DiffAction parseDiffAction( Element lActionElement )
  {
    DiffReasonKey lDiffReasonKey = parseDiffReasonKey( lActionElement );
    DiffAction lDiffAction = DiffAction.parseFromXml( lActionElement.getAttributeValue( TAG_DIFF_ACTION_ATTRIBUTE_TYPE ), Boolean.TRUE.toString().equals( lActionElement.getAttributeValue( TAG_DIFF_ACTION_ATTRIBUTE_RECREATE ) ), lDiffReasonKey );

    for( Element lActionReasonElement : lActionElement.getChildren( TAG_DIFF_ACTION_REASON ) )
    {
      lDiffAction.addDiffActionReason( parseDiffActionReasonElement( lActionReasonElement ) );
    }

    for( Element lStatementElement : lActionElement.getChildren( TAG_STATEMENT ) )
    {
      boolean lIsFailure = Boolean.TRUE.toString().equals( lStatementElement.getAttributeValue( TAG_STATEMENT_ATTRIBUTE_FAILURE ) );
      boolean lIsIgnored = Boolean.TRUE.toString().equals( lStatementElement.getAttributeValue( TAG_STATEMENT_ATTRIBUTE_IGNORE ) );
      String lStatement = getContentsAsString( lStatementElement );
      String lCommentValue = lStatementElement.getAttributeValue( TAG_STATEMENT_ATTRIBUTE_COMMENT );

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
    return lDiffAction;
  }

  private Document readXmlFile( String pXmlLogFile )
  {
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
    return lDocument;
  }

  private DiffReasonKey parseDiffReasonKey( Element pElement )
  {
    DiffReasonKey lDiffReasonKey = DiffReasonKey.parseFromXml( //
    pElement.getAttributeValue( TAG_DIFF_ACTION_AND_REASON__ATTRIBUTE_OBJECT_TYPE )//
    , pElement.getAttributeValue( TAG_DIFF_ACTION_AND_REASON__ATTRIBUTE_OBJECT_NAME )//
    , pElement.getAttributeValue( TAG_DIFF_ACTION_AND_REASON__ATTRIBUTE_SUBOBJECT_TYPE )//
    , pElement.getAttributeValue( TAG_DIFF_ACTION_AND_REASON__ATTRIBUTE_SUBOBJECT_NAME ) );
    return lDiffReasonKey;
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

  private String toXmlStringInputDiffActions( List<InputDiffAction> pInputDiffActions )
  {
    return pInputDiffActions//
    .stream()//
    .map( p -> p.diffActionElement )//
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

  private RuntimeException createDiffReasonsDifferentExcepotion( List<DiffActionReason> pOriginalDiffActionReasons, List<DiffActionReason> pInputDiffActionReasons )
  {
    return new RuntimeException( TAG_DIFF_ACTION_REASON + "s different: " + toXmlStringDiffActionReasons( pOriginalDiffActionReasons ) + " not equal " + toXmlStringDiffActionReasons( pInputDiffActionReasons ) );
  }

  private RuntimeException createDuplicateDiffActionException( List<InputDiffAction> pInputDiffActions )
  {
    return new RuntimeException( "duplicate " + TAG_DIFF_ACTION + "s: " + toXmlStringInputDiffActions( pInputDiffActions ) );
  }

  private RuntimeException createSurplusDiffActionException( List<InputDiffAction> pUnmatchedInputDiffActionList )
  {
    return new RuntimeException( "surplus " + TAG_DIFF_ACTION + "s: " + toXmlStringInputDiffActions( pUnmatchedInputDiffActionList ) );
  }

  private boolean isInList( DiffActionReason pDiffActionReason, List<DiffActionReason> pDiffActionReasonList )
  {
    return pDiffActionReasonList//
    .stream()//
    .filter( p -> p.equals( pDiffActionReason ) )//
    .findAny()//
    .isPresent();
  }

  private boolean isAnyNotInDiffActionReasonList( DiffAction pDiffAction1, DiffAction pDiffAction2 )
  {
    return pDiffAction1.getDiffActionReasons()//
    .stream()//
    .filter( p -> !isInList( p, pDiffAction2.getDiffActionReasons() ) )//
    .findAny()//
    .isPresent();
  }

  private void checkSameDiffReasons( DiffAction pOriginalDiffAction, DiffAction pInputDiffAction )
  {
    if( isAnyNotInDiffActionReasonList( pOriginalDiffAction, pInputDiffAction ) || isAnyNotInDiffActionReasonList( pInputDiffAction, pOriginalDiffAction ) )
    {
      throw createDiffReasonsDifferentExcepotion( pOriginalDiffAction.getDiffActionReasons(), pInputDiffAction.getDiffActionReasons() );
    }
  }

  private InputDiffAction handleXmlInputFile( DiffAction pOriginalDiffAction, List<InputDiffAction> pInputDiffActions )
  {
    List<InputDiffAction> lMatchingInputDiffActions = pInputDiffActions//
    .stream()//
    .filter( p -> !p.used )//
    .filter( p -> p.diffAction.getTextKey().equals( pOriginalDiffAction.getTextKey() ) )//
    .collect( Collectors.toList() );

    if( lMatchingInputDiffActions.size() > 1 )
    {
      throw createDuplicateDiffActionException( lMatchingInputDiffActions );
    }

    if( lMatchingInputDiffActions.size() == 1 )
    {
      InputDiffAction lInputDiffAction = lMatchingInputDiffActions.get( 0 );
      DiffAction lMatchingInputDiffAction = lInputDiffAction.diffAction;

      checkSameDiffReasons( pOriginalDiffAction, lMatchingInputDiffAction );

      lInputDiffAction.used = true;

      return lInputDiffAction;
    }
    else
    {
      return new InputDiffAction( null, pOriginalDiffAction );
    }
  }

  private class InputDiffAction
  {
    private Element diffActionElement;
    private DiffAction diffAction;
    private boolean used = false;

    private InputDiffAction( Element pDiffActionElement, DiffAction pDiffAction )
    {
      diffActionElement = pDiffActionElement;
      diffAction = pDiffAction;
    }
  }

  private List<DiffAction> handleXmlInputFile( List<DiffAction> pOriginalDiffActionList, Document pInputDocument )
  {
    Element lRootElement = pInputDocument.getRootElement();
    List<InputDiffAction> lInputDiffActionList = lRootElement.getChildren( TAG_DIFF_ACTION )//
    .stream()//
    .map( p -> new InputDiffAction( p, parseDiffAction( p ) ) )//
    .collect( Collectors.toList() );

    lRootElement.removeChildren( TAG_DIFF_ACTION );

    List<DiffAction> lDiffActions = new ArrayList<>();

    pOriginalDiffActionList.forEach( p ->
    {
      InputDiffAction lInputDiffAction = handleXmlInputFile( p, lInputDiffActionList );
      lDiffActions.add( lInputDiffAction.diffAction );
      if( lInputDiffAction.diffActionElement != null )
      {
        lRootElement.addContent( lInputDiffAction.diffActionElement );
      }
      else
      {
        lRootElement.addContent( convertDiffActionToXml( lInputDiffAction.diffAction ) );
      }
    } );

    List<InputDiffAction> lInputDiffActionListUnused = lInputDiffActionList.stream().filter( p -> !p.used ).collect( Collectors.toList() );
    if( !lInputDiffActionListUnused.isEmpty() )
    {
      throw createSurplusDiffActionException( lInputDiffActionListUnused );
    }

    return lDiffActions;
  }

  public DiffResult handleXmlInputFile( DiffResult pOriginalDiffResult, String pXmlInputFile, String pXmlLogFile )
  {
    Document lInputDocument = readXmlFile( pXmlInputFile );

    DiffResult lDiffResult = new DiffResult( handleXmlInputFile( pOriginalDiffResult.getDiffActions(), lInputDocument ) );

    if( pXmlLogFile != null )
    {
      writeXmlFile( pXmlLogFile, lInputDocument );
    }

    return lDiffResult;
  }
}
