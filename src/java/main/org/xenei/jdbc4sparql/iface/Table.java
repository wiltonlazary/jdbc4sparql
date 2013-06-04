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
package org.xenei.jdbc4sparql.iface;

import java.util.Iterator;

import org.xenei.jena.entities.ResourceWrapper;

public interface Table extends NamedObject, ResourceWrapper
{
	// /**
	// * An iterator of the columns in the table.
	// *
	// * Remove is not supported.
	// */
	// public static class ColumnIterator implements Iterator<Column>
	// {
	// // the table
	// private final Table table;
	// // the namespace
	// private final String namespace;
	// // an iterator over the columnDefs
	// private final Iterator<? extends ColumnDef> iter;
	//
	// /**
	// * Constructor
	// *
	// * @param namespace
	// * The namespace of the table.
	// * @param table
	// * The table.
	// * @param colDefs
	// * The collection of column definitions.
	// */
	// public ColumnIterator( final String namespace, final Table table,
	// final Collection<? extends ColumnDef> colDefs )
	// {
	// this.table = table;
	// this.namespace = namespace;
	// iter = colDefs.iterator();
	// }
	//
	// /**
	// * Constructor
	// *
	// * @param table
	// * The table.
	// * @param colDefs
	// * The collection of column definitions.
	// */
	// public ColumnIterator( final Table table,
	// final Collection<? extends ColumnDef> colDefs )
	// {
	// this(table.getResource().getURI(), table, colDefs);
	// }
	//
	// @Override
	// public boolean hasNext()
	// {
	// return iter.hasNext();
	// }
	//
	// @Override
	// public Column next()
	// {
	// RdfColumn.Builder builder = new RdfColumn.Builder();
	// builder.setColumnDef( iter.next() )
	// .setName(name)
	// .setTable( table );
	// return builder.build();
	// }
	//
	// @Override
	// public void remove()
	// {
	// throw new UnsupportedOperationException();
	// }
	//
	// }

	/**
	 * delete the table. Removes the table from the schema.
	 */
	void delete();

	/**
	 * Find all columns with the columnNamePattern.
	 * 
	 * If columnNamePattern is null all columns are matched.
	 * 
	 * @param columnNamePattern
	 *            The pattern to match or null.
	 * @return
	 */
	NameFilter<? extends Column> findColumns( String columnNamePattern );

	/**
	 * 
	 * @return Get the catalog this table is in.
	 */
	Catalog getCatalog();

	/**
	 * Get the column by index.
	 * 
	 * @param idx
	 *            The index of the column to retrieve.
	 * @return the column.
	 * @thows IndexOutOfBoundsException
	 */
	Column getColumn( int idx );

	/**
	 * Get the column by name
	 * 
	 * @param name
	 *            the name of the column to retrieve
	 * @return the column or null if name not found.
	 */
	Column getColumn( String name );

	int getColumnCount();

	public int getColumnIndex( Column column );

	/**
	 * Get the index (zero based) for the column name.
	 * 
	 * @param columnName
	 *            The column name to search for
	 * @return index for column name or -1 if not found.
	 */
	public int getColumnIndex( String columnName );

	/**
	 * Get an iterator over all the columns in order.
	 * 
	 * @return The column iterator.
	 */
	Iterator<? extends Column> getColumns();

	/**
	 * @return The schema the table belongs in.
	 */
	Schema getSchema();

	/**
	 * 
	 * @return the SPARQL formatted table name.
	 */
	String getSPARQLName();

	/**
	 * @return the SQL formatted table name
	 */
	String getSQLName();

	/**
	 * Get the supertable for this table.
	 * 
	 * @return The super table or null.
	 */
	Table getSuperTable();

	public TableDef getTableDef();

	/**
	 * Get the type of table.
	 * Typical types are "TABLE", "VIEW", "SYSTEM TABLE", "GLOBAL TEMPORARY",
	 * "LOCAL TEMPORARY", "ALIAS", "SYNONYM".
	 * 
	 * @return The table type
	 */
	String getType();

}
