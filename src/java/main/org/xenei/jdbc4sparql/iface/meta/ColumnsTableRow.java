package org.xenei.jdbc4sparql.iface.meta;

import java.sql.DatabaseMetaData;
import java.sql.Types;

import javax.persistence.Column;
import javax.persistence.Entity;

import org.xenei.jdbc4sparql.meta.MetaColumn;
import org.xenei.jdbc4sparql.meta.MetaSchema;

@Entity( name=MetaSchema.COLUMNS_TABLE)
public interface ColumnsTableRow
{
	@Column( name="TABLE_CAT", nullable=true )
	String getCatalogName();
	
	@Column( name="TABLE_SCHEM", nullable=true)
	String getSchemaName();
	
	@Column( name="TABLE_NAME")
	String getTableName();
	
	@Column( name="COLUMN_NAME")
	String getColumnName();
	
	@Column( name="DATA_TYPE")
	int getDataType();
	
	@Column( name="TYPE_NAME", nullable=true)
	String getTypeName();
	
	@Column( name="COLUMN_SIZE" )
	int getColumnSize();
	
	@Column(name="BUFFER_LENGTH", nullable=true)
	Integer getBufferLength();
	
	@Column(name="DECIMAL_DIGITS", nullable=true)
	Integer getDecimalDigits();
	
	@Column(name="NUM_PREC_RADIX", nullable=true)
	Integer getRadix();
	
	@Column(name="NULLABLE")
	int getNullable();
	
	
	@Column(name="REMARKS", nullable=true)
	String getRemarks();
	
	@Column(name="COLUMN_DEF", nullable=true)
	String getDefaultValue();
	
	@Column(name="SQL_DATA_TYPE", nullable=true)
	Integer getSQLDataType();
	
	@Column(name="SQL_DATETIME_SUB", nullable=true)
	Integer getSQLDatetimeSub();
	
	@Column(name="CHAR_OCTET_LENGTH", nullable=true)
	Integer getCharOctetLength();
	
	@Column(name="ORDINAL_POSITION")
	int getOrdinalPosition();

	@Column(name="IS_NULLABLE")
	String getIsNullable();
	
	@Column(name="SCOPE_CATLOG", nullable=true)
	String getScopeCatalog();
	
	@Column(name="SCOPE_SCHEMA", nullable=true)
	String getScopeSchema();

	
	@Column(name="SCOPE_TABLE", nullable=true)
	String getScopeTable();
	
	@Column(name="SOURCE_DATA_TYPE", nullable=true)
	Short getSourceDataType();
	
	@Column(name="DECIMAL_DIGITS", nullable=true)
	String getIsAutoincrement();
}