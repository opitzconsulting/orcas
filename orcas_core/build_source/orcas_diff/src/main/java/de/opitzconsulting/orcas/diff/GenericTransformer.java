package de.opitzconsulting.orcas.diff;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class GenericTransformer
{
  private List<CopyValuesHandlerImpl<?, ?>> copyValuesHandler = new ArrayList<>();
  private List<BaseProperty> ignorePropertyList = new ArrayList<>();
  private Map<Class<?>, Class<?>> typeMappingMap = new HashMap<>();
  private List<Function<Class<?>, Optional<Object>>> valueCreators = new ArrayList<>();
  private List<Function<Object, Optional<Object>>> valueTransformers = new ArrayList<>();
  private Map<BaseProperty, String> propertyMappingMap = new HashMap<>();
  private boolean ignoreUnmappedProperties;
  private boolean ignoreUnmappedTypes;

  public void copyDataTo( Object pSrcObject, Object pDstObject )
  {
    copyDataToRecursive( pSrcObject, pDstObject, new IdentityMap() );
  }

  protected void copyDataToRecursive( Object pSrcObject, Object pDstObject, IdentityMap pIdentityMap )
  {
    getReadableProperties( pSrcObject.getClass() ).forEach( p -> copyValue( pSrcObject, pDstObject, p, pIdentityMap ) );

    copyValuesHandler.forEach( p -> p.handleCopyValuesIfAppropriate( pSrcObject, pDstObject, pIdentityMap ) );
  }

  protected void copyValue( Object pSrcObject, Object pDstObject, ReadableProperty pReadableProperty, IdentityMap pIdentityMap )
  {
    if( isIgnoreProperty( pReadableProperty ) )
    {
      return;
    }

    Object lValue = pReadableProperty.readValue( pSrcObject );

    if( lValue != null )
    {
      Optional<WritableProperty> lWritableProperty = findProperty( pReadableProperty, getWritableProperties( pDstObject.getClass() ), true );

      if( !lWritableProperty.isPresent() )
      {
        if( lValue instanceof Collection<?> )
        {
          Collection<?> lCollection = (Collection<?>) lValue;

          Optional<? extends AbstractWritableCollectionAddPropertyImpl> lWritableCollectionAddPropertyImpl = findWritablePropertyForCollection( pReadableProperty, pDstObject.getClass() );

          if( lWritableCollectionAddPropertyImpl.isPresent() )
          {
            lCollection.forEach( p -> transformValue( p, pReadableProperty.getCollectionValueType().get(), lWritableCollectionAddPropertyImpl.get().getCollectionValueType().get(), pIdentityMap ).ifPresent( p1 -> lWritableCollectionAddPropertyImpl.get().addValue( pDstObject, p1 ) ) );
          }
          else
          {
            handleMissingPropertyMatching( pReadableProperty );
          }
        }
        else
        {
          handleMissingPropertyMatching( pReadableProperty );
        }
      }
      else
      {
        transformValue( lValue, pReadableProperty.getValueType(), lWritableProperty.get().getValueType(), pIdentityMap ).ifPresent( p -> lWritableProperty.get().writeValue( pDstObject, p ) );
      }
    }
  }

  protected void handleMissingPropertyMatching( ReadableProperty pReadableProperty )
  {
    if( !ignoreUnmappedProperties )
    {
      throw new IllegalStateException( "no writable property found for:" + pReadableProperty );
    }
  }

  protected boolean isIgnoreProperty( ReadableProperty pReadableProperty )
  {
    return ignorePropertyList.stream().filter( p -> isPropertyEqual( p, pReadableProperty ) ).findAny().isPresent();
  }

  private Optional<? extends AbstractWritableCollectionAddPropertyImpl> findWritablePropertyForCollection( ReadableProperty pReadableProperty, Class<?> pDstClass )
  {
    Optional<ReadableProperty> lOptionalReadableProperty = findProperty( pReadableProperty, getReadableProperties( pDstClass ), false );

    Optional<WritableCollectionAddPropertyImpl> lAdderWriter = lOptionalReadableProperty.//
    flatMap( lReadableProperty -> Arrays.stream( pDstClass.getMethods() ).filter( p -> p.getParameters().length == 1 ).filter( p ->
    {
      String lDstPropertyName = getRemappedPropertyName( pReadableProperty );

      return p.getName().equals( "add" + lDstPropertyName.substring( 0, 1 ).toUpperCase() + lDstPropertyName.substring( 1 ) );
    } ).map( p -> new WritableCollectionAddPropertyImpl( p, getRemappedPropertyName( pReadableProperty ), lReadableProperty.getValueType() ) ).findFirst() );

    if( lAdderWriter.isPresent() )
    {
      return lAdderWriter;
    }

    return lOptionalReadableProperty.map( p -> new WritableCollectionAddToCollectionPropertyImpl( p ) );
  }

  public void ignoreUnmappedProperties( boolean pIgnoreUnmappedProperties )
  {
    ignoreUnmappedProperties = pIgnoreUnmappedProperties;
  }

  public void ignoreUnmappedTypes( boolean pIgnoreUnmappedTypes )
  {
    ignoreUnmappedTypes = pIgnoreUnmappedTypes;
  }

  private String getRemappedPropertyName( BaseProperty pProperty )
  {
    for( BaseProperty lTestBaseProperty : propertyMappingMap.keySet() )
    {
      if( isPropertyEqual( lTestBaseProperty, pProperty ) )
      {
        return propertyMappingMap.get( lTestBaseProperty );
      }
    }

    return pProperty.getPropertyName();
  }

  public void registerTypeMapping( Class<?> pSrcClass, Class<?> pDstClass )
  {
    typeMappingMap.put( pSrcClass, pDstClass );
  }

  public void registerValueTransformer( Function<Object, Optional<Object>> pValueTransformer )
  {
    valueTransformers.add( pValueTransformer );
  }

  public void registerPropertyMapping( Class<?> pSrcClass, String pSrcPropertyName, String pDstPropertyName )
  {
    propertyMappingMap.put( createBaseProperty( pSrcClass, pSrcPropertyName ), pDstPropertyName );
  }

  public <T_SRC, T_DST> void registerCopyValuesHandler( Class<T_SRC> pSrcClass, Class<T_DST> pDstClass, BiConsumer<T_SRC, T_DST> pBiConsumer )
  {
    copyValuesHandler.add( new CopyValuesHandlerImpl<T_SRC, T_DST>( pSrcClass, pDstClass, pBiConsumer ) );
  }

  public void ignoreProperty( Class<?> pClass, String pPropertyNameString )
  {
    ignorePropertyList.add( createBaseProperty( pClass, pPropertyNameString ) );
  }

  private BaseProperty createBaseProperty( Class<?> pClass, String pPropertyNameString )
  {
    return new BaseProperty()
    {
      @Override
      public Type getValueType()
      {
        throw new UnsupportedOperationException();
      }

      @Override
      public String getPropertyName()
      {
        return pPropertyNameString;
      }

      @Override
      public Class<?> getPropertyClass()
      {
        return pClass;
      }
    };
  }

  @SuppressWarnings( { "rawtypes", "unchecked" } )
  protected Optional<Object> transformValue( Object pSrcValue, Type pSrcType, Type pDstType, IdentityMap pIdentityMap )
  {
    Optional<Object> lValueFormIdentityMap = pIdentityMap.getValue( pSrcValue );

    if( lValueFormIdentityMap.isPresent() )
    {
      return lValueFormIdentityMap;
    }

    for( Function<Object, Optional<Object>> lValueTransformer : valueTransformers )
    {
      Optional<Object> lDstValue = lValueTransformer.apply( pSrcValue );

      if( lDstValue.isPresent() )
      {
        return lDstValue;
      }
    }

    if( pSrcType.equals( pDstType ) )
    {
      return Optional.of( pSrcValue );
    }

    if( pSrcValue instanceof Collection )
    {
      if( pDstType instanceof ParameterizedType )
      {
        if( pSrcType instanceof ParameterizedType )
        {
          ParameterizedType lParameterizedDstType = (ParameterizedType) pDstType;
          Type lCollectionDstType = lParameterizedDstType.getActualTypeArguments()[0];
          ParameterizedType lParameterizedSrcType = (ParameterizedType) pSrcType;
          Type lCollectionSrcType = lParameterizedSrcType.getActualTypeArguments()[0];

          Collection lNewValue = null;

          if( pSrcValue instanceof List<?> )
          {
            lNewValue = new ArrayList();
          }

          if( pSrcValue instanceof Set<?> )
          {
            lNewValue = new TreeSet();
          }

          if( lNewValue == null )
          {
            throw new IllegalStateException();
          }

          pIdentityMap.putValue( pSrcValue, lNewValue );

          Collection lFinalNewValue = lNewValue;

          ((Collection) pSrcValue).forEach( p -> transformValue( p, lCollectionSrcType, lCollectionDstType, pIdentityMap ).ifPresent( lFinalNewValue::add ) );

          return Optional.of( lNewValue );
        }
      }
    }

    if( pDstType instanceof Class<?> )
    {
      Optional<Object> lNewValue = createNewDstValue( pSrcType, pDstType, pSrcValue );

      lNewValue.ifPresent( p ->
      {
        pIdentityMap.putValue( pSrcValue, p );

        copyDataToRecursive( pSrcValue, p, pIdentityMap );
      } );

      return lNewValue;
    }

    throw new IllegalStateException( "" + pDstType );
  }

  protected Optional<Object> createNewDstValue( Type pDsrcType, Type pDstType, Object pSrcValue )
  {
    Class<? extends Object> lSrcClass = pSrcValue.getClass();

    for( Function<Class<?>, Optional<Object>> lValueCreatorImpl : valueCreators )
    {
      Optional<Object> lValue = lValueCreatorImpl.apply( lSrcClass );

      if( lValue.isPresent() )
      {
        return lValue;
      }
    }

    if( typeMappingMap.containsKey( lSrcClass ) )
    {
      return Optional.of( createNewInstance( typeMappingMap.get( lSrcClass ) ) );
    }
    else
    {
      Class<?> lDstType = (Class<?>) pDstType;
      if( ignoreUnmappedTypes )
      {
        try
        {
          return Optional.of( createNewInstance( lDstType ) );
        }
        catch( Exception e )
        {
          return Optional.empty();
        }
      }
      else
      {
        return Optional.of( createNewInstance( lDstType ) );
      }
    }
  }

  protected Object createNewInstance( Class<?> pDstClass )
  {
    try
    {
      return pDstClass.newInstance();
    }
    catch( InstantiationException | IllegalAccessException e )
    {
      throw new RuntimeException( e );
    }
  }

  private <T extends BaseProperty> Optional<T> findProperty( ReadableProperty pReadableProperty, List<T> pProperties, boolean pShouldRemap )
  {
    String lSrcPropertyName = pShouldRemap ? getRemappedPropertyName( pReadableProperty ) : pReadableProperty.getPropertyName();

    return pProperties.stream().filter( p -> lSrcPropertyName.endsWith( p.getPropertyName() ) ).findFirst();
  }

  private boolean isPropertyNameEqual( BaseProperty pBaseProperty1, BaseProperty pBaseProperty2 )
  {
    return pBaseProperty1.getPropertyName().equals( pBaseProperty2.getPropertyName() );
  }

  private boolean isPropertyClassEqual( BaseProperty pBaseProperty1, BaseProperty pBaseProperty2 )
  {
    return pBaseProperty1.getPropertyClass().equals( pBaseProperty2.getPropertyClass() );
  }

  private boolean isPropertyEqual( BaseProperty pBaseProperty1, BaseProperty pBaseProperty2 )
  {
    return isPropertyClassEqual( pBaseProperty1, pBaseProperty2 ) && isPropertyNameEqual( pBaseProperty1, pBaseProperty2 );
  }

  private static List<ReadableProperty> getReadableProperties( Class<?> pClass )
  {
    return Arrays.stream( pClass.getMethods() ).filter( p -> p.getParameters().length == 0 ).filter( p -> ReadablePropertyImpl.isGetterName( p.getName() ) ).map( ReadablePropertyImpl::new ).collect( Collectors.toList() );
  }

  public void registerValueCreator( Class<?> pSrcClass, Supplier<?> pSupplier )
  {
    registerValueCreator( p -> p.equals( pSrcClass ) ? Optional.of( pSupplier.get() ) : Optional.empty() );
  }

  public void registerValueCreator( Function<Class<?>, Optional<Object>> pFunction )
  {
    valueCreators.add( pFunction );
  }

  private static List<WritableProperty> getWritableProperties( Class<?> pClass )
  {
    return Arrays.stream( pClass.getMethods() ).filter( p -> p.getParameters().length == 1 ).filter( p -> WritablePropertyImpl.isSetterName( p.getName() ) ).map( WritablePropertyImpl::new ).collect( Collectors.toList() );
  }

  public interface BaseProperty
  {
    Type getValueType();

    default Optional<Type> getCollectionValueType()
    {
      if( getValueType() instanceof ParameterizedType )
      {
        ParameterizedType lParameterizedType = (ParameterizedType) getValueType();

        if( Collection.class.isAssignableFrom( (Class<?>) lParameterizedType.getRawType() ) )
        {
          return Optional.of( lParameterizedType.getActualTypeArguments()[0] );
        }
      }

      return Optional.empty();
    }

    Class<?> getPropertyClass();

    String getPropertyName();
  }

  public interface ReadableProperty extends BaseProperty
  {
    Object readValue( Object pSrcObject );
  }

  public interface WritableProperty extends BaseProperty
  {
    void writeValue( Object pDstObject, Object pValue );
  }

  private static class IdentityMap
  {
    private Map<Object, Object> identityMap = new HashMap<>();

    public Optional<Object> getValue( Object pValue )
    {
      return Optional.ofNullable( identityMap.get( pValue ) );
    }

    public void putValue( Object pSrcValue, Object pDstValue )
    {
      if( getValue( pSrcValue ).isPresent() )
      {
        throw new IllegalArgumentException( "" + pSrcValue );
      }

      identityMap.put( pSrcValue, pDstValue );
    }
  }

  private class CopyValuesHandlerImpl<T_SRC, T_DST>
  {
    private Class<T_SRC> srcClass;
    private Class<T_DST> dstClass;
    private BiConsumer<T_SRC, T_DST> biConsumer;

    CopyValuesHandlerImpl( Class<T_SRC> pSrcClass, Class<T_DST> pDstClass, BiConsumer<T_SRC, T_DST> pBiConsumer )
    {
      srcClass = pSrcClass;
      dstClass = pDstClass;
      biConsumer = pBiConsumer;
    }

    @SuppressWarnings( "unchecked" )
    void handleCopyValuesIfAppropriate( Object pSrcObject, Object pDstObject, IdentityMap pIdentityMap )
    {
      if( srcClass.isInstance( pSrcObject ) && dstClass.isInstance( pDstObject ) )
      {
        biConsumer.accept( (T_SRC) pSrcObject, (T_DST) pDstObject );
      }
    }
  }

  private abstract static class NamedPropertyImpl implements BaseProperty
  {
    private String propertyName;
    private Type type;
    private Class<?> propertyClass;

    NamedPropertyImpl( String pPropertyName, Type pType, Class<?> pPropertyClass )
    {
      propertyName = pPropertyName;
      type = pType;
      propertyClass = pPropertyClass;
    }

    @Override
    public Class<?> getPropertyClass()
    {
      return propertyClass;
    }

    @Override
    public Type getValueType()
    {
      return type;
    }

    @Override
    public String getPropertyName()
    {
      return propertyName;
    }

    @Override
    public String toString()
    {
      return getPropertyTypeName() + "-" + propertyName + " from " + getPropertyClass();
    }

    protected abstract String getPropertyTypeName();
  }

  private static class ReadablePropertyImpl extends NamedPropertyImpl implements ReadableProperty
  {
    private Method getter;

    private static boolean isGetterName( String pName )
    {
      if( pName.equals( "getClass" ) )
      {
        return false;
      }

      return pName.startsWith( "get" ) || pName.startsWith( "is" );
    }

    private static String getPropertyNameForGetter( Method pGetter )
    {
      String lReturn = pGetter.getName();

      if( !isGetterName( lReturn ) )
      {
        throw new IllegalArgumentException();
      }

      int lPrefixSize = 3;

      if( lReturn.startsWith( "is" ) )
      {
        lPrefixSize = 2;
      }

      return lReturn.substring( lPrefixSize, lPrefixSize + 1 ).toLowerCase() + lReturn.substring( lPrefixSize + 1 );
    }

    ReadablePropertyImpl( Method pGetter )
    {
      super( getPropertyNameForGetter( pGetter ), pGetter.getGenericReturnType(), pGetter.getDeclaringClass() );
      getter = pGetter;
    }

    @Override
    public Object readValue( Object pObject )
    {
      try
      {
        return getter.invoke( pObject );
      }
      catch( IllegalAccessException | IllegalArgumentException | InvocationTargetException e )
      {
        throw new RuntimeException( e );
      }
    }

    @Override
    protected String getPropertyTypeName()
    {
      return "get";
    }
  }

  private static class WritablePropertyImpl extends NamedPropertyImpl implements WritableProperty
  {
    private Method setter;

    private static boolean isSetterName( String pName )
    {
      return pName.startsWith( "set" );
    }

    private static String getPropertyNameForSetter( Method pSetter )
    {
      String lReturn = pSetter.getName();

      if( !isSetterName( lReturn ) )
      {
        throw new IllegalArgumentException();
      }

      return lReturn.substring( 3, 4 ).toLowerCase() + lReturn.substring( 4 );
    }

    WritablePropertyImpl( Method pSetter )
    {
      super( getPropertyNameForSetter( pSetter ), pSetter.getParameters()[0].getParameterizedType(), pSetter.getDeclaringClass() );
      setter = pSetter;
    }

    @Override
    public void writeValue( Object pObject, Object pValue )
    {
      try
      {
        setter.invoke( pObject, pValue );
      }
      catch( IllegalAccessException | IllegalArgumentException | InvocationTargetException e )
      {
        throw new RuntimeException( e );
      }
    }

    @Override
    protected String getPropertyTypeName()
    {
      return "set";
    }
  }

  private static abstract class AbstractWritableCollectionAddPropertyImpl extends NamedPropertyImpl
  {
    AbstractWritableCollectionAddPropertyImpl( String pPropertyName, Type pType, Class<?> pPropertyClass )
    {
      super( pPropertyName, pType, pPropertyClass );
    }

    abstract void addValue( Object pObject, Object pValue );
  }

  private static class WritableCollectionAddPropertyImpl extends AbstractWritableCollectionAddPropertyImpl
  {
    private Method adder;

    WritableCollectionAddPropertyImpl( Method pAdder, String pPropertyName, Type pType )
    {
      super( pPropertyName, pType, pAdder.getDeclaringClass() );
      adder = pAdder;
    }

    void addValue( Object pObject, Object pValue )
    {
      try
      {
        adder.invoke( pObject, pValue );
      }
      catch( IllegalAccessException | IllegalArgumentException | InvocationTargetException e )
      {
        throw new RuntimeException( e );
      }
    }

    @Override
    protected String getPropertyTypeName()
    {
      return "add";
    }
  }

  private static class WritableCollectionAddToCollectionPropertyImpl extends AbstractWritableCollectionAddPropertyImpl
  {
    private ReadableProperty collectionGetterProperty;

    WritableCollectionAddToCollectionPropertyImpl( ReadableProperty pCollectionGetterProperty )
    {
      super( pCollectionGetterProperty.getPropertyName(), pCollectionGetterProperty.getValueType(), pCollectionGetterProperty.getPropertyClass() );

      collectionGetterProperty = pCollectionGetterProperty;
    }

    @SuppressWarnings( { "rawtypes", "unchecked" } )
    void addValue( Object pObject, Object pValue )
    {
      Collection lCollection = (Collection) collectionGetterProperty.readValue( pObject );

      lCollection.add( pValue );
    }

    @Override
    protected String getPropertyTypeName()
    {
      return "add";
    }
  }
}
