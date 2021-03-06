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
package org.xenei.jdbc4sparql.sparql;

import java.sql.SQLException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xenei.jdbc4sparql.iface.Catalog;
import org.xenei.jdbc4sparql.iface.Column;
import org.xenei.jdbc4sparql.iface.ColumnDef;
import org.xenei.jdbc4sparql.iface.NameFilter;
import org.xenei.jdbc4sparql.iface.Schema;
import org.xenei.jdbc4sparql.iface.Table;
import org.xenei.jdbc4sparql.iface.TableDef;
import org.xenei.jdbc4sparql.iface.name.ColumnName;
import org.xenei.jdbc4sparql.iface.name.TableName;
import org.xenei.jdbc4sparql.impl.AbstractTable;
import org.xenei.jdbc4sparql.impl.NameUtils;
import org.xenei.jdbc4sparql.impl.virtual.VirtualCatalog;
import org.xenei.jdbc4sparql.impl.virtual.VirtualSchema;
import org.xenei.jdbc4sparql.sparql.items.QueryColumnInfo;

import com.hp.hpl.jena.util.iterator.Map1;
import com.hp.hpl.jena.util.iterator.WrappedIterator;

public class SparqlView extends AbstractTable {
	class RenamedColumn implements Column {

		QueryColumnInfo columnInfo;

		RenamedColumn(final QueryColumnInfo columnInfo) {
			this.columnInfo = columnInfo;
		}

		@Override
		public Catalog getCatalog() {
			return getTable().getCatalog();
		}

		@Override
		public ColumnDef getColumnDef() {
			return columnInfo.getColumn().getColumnDef();
		}

		@Override
		public ColumnName getName() {
			return columnInfo.getName();
		}

		@Override
		public String getQuerySegmentFmt() {
			return columnInfo.getColumn().getQuerySegmentFmt();
		}

		@Override
		public String getRemarks() {
			return columnInfo.getColumn().getRemarks();
		}

		@Override
		public Schema getSchema() {
			return getTable().getSchema();
		}

		@Override
		public String getSPARQLName() {
			return columnInfo.getName().getSPARQLName();
		}

		@Override
		public String getSQLName() {
			return columnInfo.getName().getDBName();
		}

		@Override
		public Table getTable() {
			return SparqlView.this;
		}

		@Override
		public boolean hasQuerySegments() {
			return columnInfo.getColumn().hasQuerySegments();
		}

		@Override
		public boolean isOptional() {
			return columnInfo.isOptional();
		}

	}

	private final static Logger LOG = LoggerFactory.getLogger(SparqlView.class);
	private final Schema schema;
	private final TableName name;
	private final SparqlQueryBuilder builder;

	private List<Column> columns;

	public static final String NAME_SPACE = "http://org.xenei.jdbc4sparql/vocab#View";

	public SparqlView(final SparqlQueryBuilder builder) {
		if (LOG.isDebugEnabled()) {
			SparqlView.LOG.debug(builder.toString());
		}
		this.builder = builder;
		final Catalog catalog = builder.getCatalog(VirtualCatalog.NAME);
		this.schema = catalog.getSchema(VirtualSchema.NAME);
		this.name = schema.getName().getTableName(NameUtils.createUUIDName());
	}

	@Override
	public void delete() {
		// TODO Auto-generated method stub

	}

	@Override
	public NameFilter<Column> findColumns(final String columnNamePattern) {
		return new NameFilter<Column>(columnNamePattern, WrappedIterator
				.create(builder.getResultColumns().iterator()).mapWith(
						new Map1<QueryColumnInfo, Column>() {

							@Override
							public Column map1(final QueryColumnInfo o) {
								return new RenamedColumn(o);
							}
						}));
	}

	@Override
	public Catalog getCatalog() {
		return builder.getCatalog();
	}

	@Override
	public List<Column> getColumnList() {
		if (columns == null) {
			columns = WrappedIterator
					.create(builder.getResultColumns().iterator())
					.mapWith(new Map1<QueryColumnInfo, Column>() {

						@Override
						public Column map1(final QueryColumnInfo o) {
							return new RenamedColumn(o);
						}
					}).toList();
		}
		return columns;
	}

	@Override
	public TableName getName() {
		return name;
	}

	@Override
	public String getQuerySegmentFmt() {
		return null;
	}

	@Override
	public String getRemarks() {
		return "SPARQL View";
	}

	public SparqlResultSet getResultSet() throws SQLException {
		return new SparqlResultSet(this, builder.build());
	}

	@Override
	public Schema getSchema() {
		return schema;
	}

	@Override
	public String getSPARQLName() {
		return NameUtils.getSPARQLName(this);
	}

	@Override
	public String getSQLName() {
		return NameUtils.getDBName(this);
	}

	@Override
	public Table getSuperTable() {
		return null;
	}

	@Override
	public TableDef getTableDef() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getType() {
		return "VIEW";
	}

	@Override
	public boolean hasQuerySegments() {
		return false;
	}

}
