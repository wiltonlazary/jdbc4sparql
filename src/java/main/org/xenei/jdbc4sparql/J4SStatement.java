/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.xenei.jdbc4sparql;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.SQLWarning;
import java.sql.Statement;

import org.xenei.jdbc4sparql.sparql.SparqlCatalog;
import org.xenei.jdbc4sparql.sparql.SparqlView;
import org.xenei.jdbc4sparql.sparql.parser.SparqlParser;

public class J4SStatement implements Statement
{
	private final J4SConnection connection;
	private final SparqlCatalog catalog;
	private SQLWarning warnings = null;
	private boolean closed = false;
	private final SparqlParser parser;
	private ResultSet resultSet;
	private int fetchDirection;
	private final int resultSetConcurrency;
	private int queryTimeout;
	private final int resultSetType;
	private final int resultSetHoldability;
	private boolean poolable;

	public J4SStatement( final J4SConnection connection,
			final SparqlCatalog catalog, final int resultSetType,
			final int resultSetConcurrency, final int resultSetHoldability )
			throws SQLException
	{
		this.connection = connection;
		this.catalog = catalog;
		this.queryTimeout = connection.getNetworkTimeout();
		this.parser = connection.getSparqlParser();
		this.resultSet = null;
		this.poolable = true;
		this.resultSetHoldability = resultSetHoldability;
		this.fetchDirection = ResultSet.FETCH_FORWARD;
		this.resultSetConcurrency = resultSetConcurrency;
		this.resultSetType = resultSetType;
	}

	@Override
	public void addBatch( final String arg0 ) throws SQLException
	{
		throw new SQLFeatureNotSupportedException();
	}

	@Override
	public void cancel() throws SQLException
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void clearBatch() throws SQLException
	{
		throw new SQLFeatureNotSupportedException();
	}

	@Override
	public void clearWarnings() throws SQLException
	{
		warnings = null;
	}

	@Override
	public void close() throws SQLException
	{
		closed = true;
	}

	@Override
	public void closeOnCompletion() throws SQLException
	{
		throw new SQLFeatureNotSupportedException();
	}

	@Override
	public boolean execute( final String sql ) throws SQLException
	{
		final SparqlView view = new SparqlView(parser.parse(catalog, sql));
		resultSet = view.getResultSet();
		resultSet.setFetchDirection(getFetchDirection());
		return true;
	}

	@Override
	public boolean execute( final String sql, final int autoGeneratedKeys )
			throws SQLException
	{
		throw new SQLFeatureNotSupportedException();
	}

	@Override
	public boolean execute( final String arg0, final int[] columnIndexes )
			throws SQLException
	{
		throw new SQLFeatureNotSupportedException();
	}

	@Override
	public boolean execute( final String arg0, final String[] columnNames )
			throws SQLException
	{
		throw new SQLFeatureNotSupportedException();
	}

	@Override
	public int[] executeBatch() throws SQLException
	{
		throw new SQLFeatureNotSupportedException();
	}

	@Override
	public ResultSet executeQuery( final String sql ) throws SQLException
	{
		execute(sql);
		return getResultSet();
	}

	@Override
	public int executeUpdate( final String arg0 ) throws SQLException
	{
		throw new SQLFeatureNotSupportedException();
	}

	@Override
	public int executeUpdate( final String arg0, final int arg1 )
			throws SQLException
	{
		throw new SQLFeatureNotSupportedException();
	}

	@Override
	public int executeUpdate( final String arg0, final int[] arg1 )
			throws SQLException
	{
		throw new SQLFeatureNotSupportedException();
	}

	@Override
	public int executeUpdate( final String arg0, final String[] arg1 )
			throws SQLException
	{
		throw new SQLFeatureNotSupportedException();
	}

	@Override
	public Connection getConnection() throws SQLException
	{
		return connection;
	}

	@Override
	public int getFetchDirection() throws SQLException
	{
		return fetchDirection;
	}

	@Override
	public int getFetchSize() throws SQLException
	{
		return Integer.MAX_VALUE;
	}

	@Override
	public ResultSet getGeneratedKeys() throws SQLException
	{
		throw new SQLFeatureNotSupportedException();
	}

	@Override
	public int getMaxFieldSize() throws SQLException
	{
		return Integer.MAX_VALUE;
	}

	@Override
	public int getMaxRows() throws SQLException
	{
		return Integer.MAX_VALUE;
	}

	@Override
	public boolean getMoreResults() throws SQLException
	{
		return false;
	}

	@Override
	public boolean getMoreResults( final int arg0 ) throws SQLException
	{
		return false;
	}

	@Override
	public int getQueryTimeout() throws SQLException
	{
		return queryTimeout;
	}

	@Override
	public ResultSet getResultSet() throws SQLException
	{
		return resultSet;
	}

	@Override
	public int getResultSetConcurrency() throws SQLException
	{
		return resultSetConcurrency;
	}

	@Override
	public int getResultSetHoldability() throws SQLException
	{
		return resultSetHoldability;
	}

	@Override
	public int getResultSetType() throws SQLException
	{
		return resultSetType;
	}

	@Override
	public int getUpdateCount() throws SQLException
	{
		return 0; // don't do updates
	}

	@Override
	public SQLWarning getWarnings() throws SQLException
	{
		return warnings;
	}

	@Override
	public boolean isClosed() throws SQLException
	{
		return closed;
	}

	@Override
	public boolean isCloseOnCompletion() throws SQLException
	{
		return false;
	}

	@Override
	public boolean isPoolable() throws SQLException
	{
		return poolable;
	}

	@Override
	public boolean isWrapperFor( final Class<?> arg0 ) throws SQLException
	{
		return false;
	}

	@Override
	public void setCursorName( final String arg0 ) throws SQLException
	{
		throw new SQLFeatureNotSupportedException();
	}

	@Override
	public void setEscapeProcessing( final boolean arg0 ) throws SQLException
	{
		// no op -- we don't set this
	}

	@Override
	public void setFetchDirection( final int direction ) throws SQLException
	{
		switch (direction)
		{
			case ResultSet.FETCH_REVERSE:
				fetchDirection = direction;
				break;

			case ResultSet.FETCH_UNKNOWN:
			case ResultSet.FETCH_FORWARD:
				fetchDirection = ResultSet.FETCH_FORWARD;
				break;

			default:
				throw new SQLException("invalid fetch direciton value");
		}
	}

	@Override
	public void setFetchSize( final int arg0 ) throws SQLException
	{
		// ignore this
	}

	@Override
	public void setMaxFieldSize( final int arg0 ) throws SQLException
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void setMaxRows( final int arg0 ) throws SQLException
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void setPoolable( final boolean poolable ) throws SQLException
	{
		this.poolable = poolable;
	}

	@Override
	public void setQueryTimeout( final int queryTimeout ) throws SQLException
	{
		this.queryTimeout = queryTimeout;
	}

	@Override
	public <T> T unwrap( final Class<T> arg0 ) throws SQLException
	{
		throw new SQLFeatureNotSupportedException();
	}

}