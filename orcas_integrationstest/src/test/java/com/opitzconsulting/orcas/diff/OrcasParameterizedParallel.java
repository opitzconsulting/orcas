package com.opitzconsulting.orcas.diff;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.AssumptionViolatedException;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.Parameterized;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerScheduler;
import org.junit.runners.parameterized.BlockJUnit4ClassRunnerWithParameters;
import org.junit.runners.parameterized.ParametersRunnerFactory;
import org.junit.runners.parameterized.TestWithParameters;

public abstract class OrcasParameterizedParallel extends Parameterized
{
  @Override
  protected void runChild( Runner pRunner, RunNotifier pNotifier )
  {
    OrcasBlockJUnit4ClassRunnerWithParameters lOrcasBlockJUnit4ClassRunnerWithParameters = (OrcasBlockJUnit4ClassRunnerWithParameters)pRunner;
    String lTestName = lOrcasBlockJUnit4ClassRunnerWithParameters.testName;

    try
    {
      assumeShouldExecuteTestcase( lTestName );
    }
    catch( AssumptionViolatedException e )
    {
      Description lDescribeChildRunner = describeChild( pRunner );
      pNotifier.fireTestAssumptionFailed( new Failure( lDescribeChildRunner, e ) );
      pNotifier.fireTestIgnored( lDescribeChildRunner );

      for( FrameworkMethod lChildFrameworkMethod : lOrcasBlockJUnit4ClassRunnerWithParameters.getChildren() )
      {
        Description lDescribeChildFrameworkMethod = lOrcasBlockJUnit4ClassRunnerWithParameters.describeChild( lChildFrameworkMethod );

        pNotifier.fireTestAssumptionFailed( new Failure( lDescribeChildFrameworkMethod, e ) );
        pNotifier.fireTestIgnored( lDescribeChildFrameworkMethod );
      }

      return;
    }

    super.runChild( pRunner, pNotifier );
  }

  public abstract void assumeShouldExecuteTestcase( String pTestName );

  public OrcasParameterizedParallel( Class<?> pKlass ) throws Throwable
  {
    super( pKlass );

    setScheduler( new OrcasThreadPoolRunnerScheduler() );
  }

  private static class OrcasThreadPoolRunnerScheduler implements RunnerScheduler
  {
    private ExecutorService _executorService;

    public OrcasThreadPoolRunnerScheduler()
    {
      _executorService = Executors.newFixedThreadPool( OrcasCoreIntegrationConfigSystemProperties.getOrcasCoreIntegrationConfig().getParallelThreads() );
    }

    public void finished()
    {
      _executorService.shutdown();
      try
      {
        _executorService.awaitTermination( 3, TimeUnit.HOURS );
      }
      catch( InterruptedException exc )
      {
        throw new RuntimeException( exc );
      }
    }

    public void schedule( Runnable childStatement )
    {
      _executorService.submit( childStatement );
    }
  }

  public static class OrcasParametersRunnerFactory implements ParametersRunnerFactory
  {
    public Runner createRunnerForTestWithParameters( TestWithParameters pTest ) throws InitializationError
    {
      return new OrcasBlockJUnit4ClassRunnerWithParameters( pTest );
    }
  }

  public static class OrcasBlockJUnit4ClassRunnerWithParameters extends BlockJUnit4ClassRunnerWithParameters
  {
    private String testName;

    public OrcasBlockJUnit4ClassRunnerWithParameters( TestWithParameters pTest ) throws InitializationError
    {
      super( pTest );

      testName = (String)pTest.getParameters().get( 0 );
    }

    @Override
    public Description describeChild( FrameworkMethod pMethod )
    {
      return super.describeChild( pMethod );
    }

    @Override
    protected String testName( FrameworkMethod pMethod )
    {
      if( OrcasCoreIntegrationConfigSystemProperties.getOrcasCoreIntegrationConfig().isFlatTestNames() )
      {
        return getName() + pMethod.getName();
      }
      else
      {
        return super.testName( pMethod );
      }
    }

    @Override
    public List<FrameworkMethod> getChildren()
    {
      return super.getChildren();
    }
  }
}
