package com.opitzconsulting.orcas.gradle

import de.oc.dbdoc.ant.*;
import de.opitzconsulting.orcas.diff.ParametersCall
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.OutputDirectory;

public class OrcasDbDocTask extends BaseOrcasTask
{
  private String logname = "dbdoc";
  private OrcasDbDoc orcasDbDoc = new OrcasDbDoc();

  @Override
  protected String getLogname()
  {
    return logname;
  }

  @OutputDirectory
  public String getOutfolder()
  {
    return project.buildDir.toString() + "/dbdoc";
  }

  @InputDirectory
  public String getInputfolder()
  {
    return project.file(project.orcasconfiguration.staticsfolder).toString();
  }

  private String getTmpfolder()
  {
    return project.buildDir.toString() + "/tmpdbdoc";
  }

  public void diagram(pClosure) {
    orcasDbDoc.setDiagram( new DiagramWrapper() );
    callClosure( pClosure, orcasDbDoc.getDiagram() );
  }

  public OrcasDbDoc getOrcasDbDoc() {
    return orcasDbDoc;
  }

  public void styles(pClosure) {
    orcasDbDoc.setStyles( new StylesWrapper() );
    callClosure( pClosure, orcasDbDoc.getStyles() );
  }

  public void tableregistry(pClosure) {
    orcasDbDoc.setTableregistry( new TableregistryWrapper() );
    callClosure( pClosure, orcasDbDoc.getTableregistry() );
  }

  public void configure(pClosure) {
    callClosure( pClosure, this );
  }

  public Tableregistry getTableregistry() {
    return orcasDbDoc.tableregistry;
  }

  public void setSvg( boolean pSvg )
  {
    orcasDbDoc.setSvg( pSvg );
  }

  @Override
  protected void executeOrcasTaskWithParameters( ParametersCall pParameters )
  {
    pParameters.setModelFile( getInputfolder() );

    pParameters.setSqlplustable( false );
    pParameters.setOrderColumnsByName( false );

    new File(getOutfolder()).deleteDir();
    new File(getOutfolder()).mkdirs();

    if(!pParameters.getDbdocPlantuml()) {
      new File(getTmpfolder()).deleteDir();
      new File(getTmpfolder() + "/mapfiles").mkdirs();
    }

    orcasDbDoc.setOutfolder( getOutfolder() );
    orcasDbDoc.setTmpfolder( getTmpfolder() );
  
    if( orcasDbDoc.tableregistry == null )
    {
      tableregistry {
        ALL {
          includes = ".*";
        }
      }
    } 

    if( orcasDbDoc.diagram == null )
    {
      diagram {
        label = "Database"
        tablegroup = tableregistry.ALL
      }
    }

    if( orcasDbDoc.styles == null )
    {
      styles {
        diagrams {
        }
        tables {
          style {
            stylename = "fillcolor"
            value = "#ff4040"
            tablegroup = tableregistry.ALL
          }
        }
      }
    }

    orcasDbDoc.execute( modifyParameters( pParameters ) );

    def lTmpfolder = new File(getTmpfolder());

    if(!pParameters.getDbdocPlantuml()) {
      Arrays.stream(lTmpfolder.listFiles()).parallel().forEach
              { p ->
                def lDotIndex = p.getName().lastIndexOf('.');
                if (lDotIndex > 0) {
                  dotExec((String) p.getName().substring(lDotIndex + 1), (String) p.getName());
                }
              }

      de.oc.dbdoc.postprocessing.Main.main(getOutfolder(), "${getTmpfolder()}/mapfiles");
    }
  }

  public void dotExec( String pDotExecutable, String pDotFile )
  {
    def lProcess;
    def lStderr;
    def lDotFileWithoutEnding = pDotFile.take(pDotFile.lastIndexOf('.'))

    if( !orcasDbDoc.isSvg() )
    {
      lProcess = "${pDotExecutable} -Tpng -o${getOutfolder()}/${lDotFileWithoutEnding}.png ${getTmpfolder()}/${pDotFile}".execute();
      lStderr = new StringBuffer();
      lProcess.consumeProcessErrorStream(lStderr);
      if( lStderr.toString().length() != 0 )
      {
        logInfo( lStderr.toString() );
      }
    }

    lProcess = "${pDotExecutable} -Tsvg -o${getOutfolder()}/${lDotFileWithoutEnding}.svg ${getTmpfolder()}/${pDotFile}".execute();
    lStderr = new StringBuffer();
    lProcess.consumeProcessErrorStream(lStderr);
    if( lStderr.toString().length() != 0 )
    {
      logInfo( lStderr.toString() );
    }

    lProcess = "${pDotExecutable} -Tcmapx -o${getTmpfolder()}/mapfiles/${lDotFileWithoutEnding}.map ${getTmpfolder()}/${pDotFile}".execute();
    lStderr = new StringBuffer();
    lProcess.consumeProcessErrorStream(lStderr);
    if( lStderr.toString().length() != 0 )
    {
      logInfo( lStderr.toString() );
    }
  }

  static def callClosure( pClosure, pValue ) {
    pClosure.resolveStrategy = Closure.DELEGATE_FIRST;
    pClosure.delegate = pValue;
    pClosure( pValue );
  }
}

public class StyleWrapper extends Style {
  def setTablegroup( Tablegroup pTablegroup ) {
    setTablegroup( pTablegroup.name );
  }
}

public class DiagramWrapper extends Diagram {
  def setTablegroup( Tablegroup pTablegroup ) {
    setTablegroup( pTablegroup.name );
  }

  public void diagram(pClosure) {
    def lDiagramWrapper = new DiagramWrapper();
    addDiagram( lDiagramWrapper );
    OrcasDbDocTask.callClosure( pClosure, lDiagramWrapper );
  }
}

public class StylesWrapper extends Styles {
  public void tables(pClosure) {
    def lTablesWrapper = new TablesWrapper();
    setTables( lTablesWrapper );
    OrcasDbDocTask.callClosure( pClosure, lTablesWrapper );
  }

  public void diagrams(pClosure) {
    def lDiagramsWrapper = new DiagramsWrapper();
    setDiagrams( lDiagramsWrapper );
    OrcasDbDocTask.callClosure( pClosure, lDiagramsWrapper );
  }
}

public class DiagramsWrapper extends Diagrams {
}

public class TablesWrapper extends Tables {
  public void style(pClosure) {
    def lStyleWrapper = new StyleWrapper();
    addStyle( lStyleWrapper );
    OrcasDbDocTask.callClosure( pClosure, lStyleWrapper );
  }
}

public class TableregistryWrapper extends Tableregistry {
  def tablegroups = [:]
  def propertyMissing(String name) { 
    tablegroups[name] 
  }

  def methodMissing(String pTablegroupName, def args) {
    def lTablegroupWrapper = new TablegroupWrapper();
    lTablegroupWrapper.name = pTablegroupName;
    tablegroups[pTablegroupName] = lTablegroupWrapper;
    addTablegroup( lTablegroupWrapper );
    OrcasDbDocTask.callClosure( args[0], lTablegroupWrapper );
  }
}

public class TablegroupWrapper extends Tablegroup {
  public void include(String pInclude) {
    createInclude().setName(pInclude);
  }

  public void exclude(String pExclude) {
    createExclude().setName(pExclude);
  }
}

