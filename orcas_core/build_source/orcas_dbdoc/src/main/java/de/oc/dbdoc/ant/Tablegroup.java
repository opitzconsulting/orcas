package de.oc.dbdoc.ant;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Tablegroup
{
  private String _name;
  private String _includes;
  private String _excludes;
  private List<Object> _includesExcludes = new ArrayList<>();

  public void setExcludes( String pExcludes )
  {
    _excludes = pExcludes;
  }

  public void setIncludes( String pIncludes )
  {
    _includes = pIncludes;
  }

  public void setName( String pName )
  {
    _name = pName;
  }

  public Include createInclude()
  {
    Include lInclude = new Include();

    _includesExcludes.add( lInclude );

    return lInclude;
  }

  public Exclude createExclude()
  {
    Exclude lExclude = new Exclude();

    _includesExcludes.add( lExclude );

    return lExclude;
  }

  boolean isTableIncluded( String pTableName, Tableregistry pTableregistry )
  {
    if( _excludes != null && pTableName.matches( _excludes ) )
    {
      return false;
    }

    boolean lOneIncludeFound = false;

    List<Object> lIncludesExcludes = new ArrayList<>( _includesExcludes );

    Collections.reverse( lIncludesExcludes );

    for( Object lObject : lIncludesExcludes )
    {
      if( lObject instanceof Include )
      {
        lOneIncludeFound = true;
        Include lInclude = (Include) lObject;

        if( pTableName.matches( lInclude.getName() ) )
        {
          return true;
        }
      }
      else
      {
        Exclude lExclude = (Exclude) lObject;

        if( lExclude.getName() != null && pTableName.matches( lExclude.getName() ) )
        {
          return false;
        }
        if( lExclude.getTablegroup() != null )
        {
          for( Tablegroup lTablesgroup : pTableregistry.getTablesgroups() )
          {
            if( lTablesgroup != this && lTablesgroup.getName().matches( lExclude.getTablegroup() ) )
            {
              if( lTablesgroup.isTableIncluded( pTableName, pTableregistry ) )
              {
                return false;
              }
            }
          }
        }
      }
    }

    if( _includes != null && pTableName.matches( _includes ) )
    {
      return true;
    }

    return _includes == null && !lOneIncludeFound;
  }

  String getName()
  {
    return _name;
  }
}
