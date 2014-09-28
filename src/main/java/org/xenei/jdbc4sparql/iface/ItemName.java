package org.xenei.jdbc4sparql.iface;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.xenei.jdbc4sparql.impl.NameUtils;

public abstract class ItemName
{
	static final String FOUND_IN_MULTIPLE_ = "%s was found in multiple %s";

	private final String schema;
	private final String table;
	private final String col;

	protected ItemName( final ItemName name )
	{
		this.schema = name.getSchema();
		this.table = name.getTable();
		this.col = name.getCol();
	}

	protected ItemName( final String schema, final String table,
			final String col )
	{
		this.schema = schema;
		this.table = table;
		this.col = col;
	}
	
	public String getShortName() {
		return StringUtils.defaultIfEmpty(getCol(), StringUtils.defaultIfEmpty(getTable(), getSchema()));
	}


	@Override
	public boolean equals( final Object o )
	{
		if (super.equals(o))
		{
			return true;
		}
		if (this.getClass().equals( o.getClass()))
		{
			final ItemName n = (ItemName) o;
			return new EqualsBuilder().append(this.getClass(), o.getClass())
					.append(this.getSchema(), n.getSchema())
					.append(this.getTable(), n.getTable())
					.append(this.getCol(), n.getCol()).isEquals();

		}
		return false;
	}

	/**
	 * Find the object matching the key in the map.
	 * Uses matches() method to determine match.
	 * 
	 * @param map
	 *            The map to find the object in.
	 * @return The Object (T) or null if not found
	 * @throws IllegalArgumentException
	 *             if more than one object matches.
	 */
	public <T> T findMatch( final Map<? extends ItemName, T> map )
	{
		// exact match
		if (map.containsKey(this))
		{
			return map.get(this);
		}
		T retval = null;
		// no match pattern
		if (isWild())
		{
			if (map.size() > 0)
			{
				// no match pattern so see if there is only one table in the
				// list
				retval = map.values().iterator().next();
				if (map.size() == 1)
				{
					return retval;
				}
				throw new IllegalArgumentException(String.format(
						FOUND_IN_MULTIPLE_, this, retval
								.getClass().getSimpleName()));
			}
		}
		else if (hasWild())
		{
			for (final ItemName n : map.keySet())
			{
				if (matches(n))
				{
					if (retval != null)
					{
						throw new IllegalArgumentException(String.format(
								FOUND_IN_MULTIPLE_, this,
								retval.getClass().getSimpleName()));
					}
					retval = map.get(n);
				}
			}
		}
		return retval;
	}

	
	/**
	 * Find the object matching the key in the map.
	 * Uses matches() method to determine match.
	 * 
	 * @param map
	 *            The map to find the object in.
	 * @return The Object (T) or null if not found
	 * @throws IllegalArgumentException
	 *             if more than one object matches.
	 */
	public <T> Set<T> listMatches( final Map<? extends ItemName, T> map )
	{
		Set<T> retval = new HashSet<T>();
		for (final ItemName n : map.keySet())
		{
			if (matches(n))
			{
				retval.add( map.get(n));
			}
		}
		return retval;
	}

	
	/**
	 * Get the column name string
	 * 
	 * @return
	 */
	public String getCol()
	{
		return col;
	}

	/**
	 * Get the name in DB format
	 * 
	 * @return
	 */
	public String getDBName()
	{
		return createName( NameUtils.DB_DOT );
	}

	/**
	 * Get the schema segment of the name.
	 * 
	 * @return
	 */
	public String getSchema()
	{
		return schema;
	}

	/**
	 * Get the complete name in SPARQL format
	 * 
	 * @return
	 */
	public String getSPARQLName()
	{
		return createName( NameUtils.SPARQL_DOT );
	}

	/**
	 * Get the table portion of the complete name.
	 * 
	 * @return
	 */
	public String getTable()
	{
		return table;
	}

	@Override
	public int hashCode()
	{
		return new HashCodeBuilder().append(getSchema()).append(getTable())
				.append(getCol()).toHashCode();
	}

	/**
	 * check if this matches that.
	 * 
	 * this matches that if this.equals( that ) or
	 * if this.schema, this.table, this.col == null then that
	 * segment is not used in the comparison.
	 * 
	 * Note that this is a.matches(b) does not imply b.matches(a)
	 * 
	 * @param n
	 * @return
	 */
	public boolean matches( final ItemName that )
	{
		if (that == null)
		{
			return false;
		}
		final EqualsBuilder eb = new EqualsBuilder().append(this.getClass(),
				that.getClass());
		if (this.getSchema() != null)
		{
			eb.append(this.getSchema(), that.getSchema());
		}
		if (this.getTable() != null)
		{
			eb.append(this.getTable(), that.getTable());
		}
		if (this.getCol() != null)
		{
			eb.append(this.getCol(), that.getCol());
		}
		return eb.isEquals();
	}

	@Override
	public String toString()
	{
		return getDBName();
	}
	
	/**
	 * @return true if this is a complete wildcard name (e.g. all segments are null)
	 */
	public boolean isWild()
	{
		return getSchema() == null && getTable()==null && getCol() == null;
	}
	
	/**
	 * @return true if this has a wildcard (null) segment
	 */
	public boolean hasWild()
	{
		return getSchema() == null || getTable()==null || getCol() == null;
	}
	
	public String createName( final String separator )
	{
		final StringBuilder sb = new StringBuilder();

		if (StringUtils.isNotEmpty(schema))
		{
			sb.append(schema).append(separator);
		}

		if (StringUtils.isNotEmpty(table) || (sb.length() > 0))
		{
			sb.append(table);
		}

		if (StringUtils.isNotEmpty(col))
		{
			if (sb.length() > 0)
			{
				sb.append(separator);
			}
			sb.append(col);
		}

		return sb.toString();
	}
}