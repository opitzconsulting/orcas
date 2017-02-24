package de.opitzconsulting.orcas.sql;

public interface TransactionalCallableStatementProvider extends CallableStatementProvider
{
  /**
   * Executes a commit.
   */
  void commit();

  /**
   * Executes a rollback.
   */
  void rollback();
}
