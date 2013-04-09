package org.xenei.jdbc4sparql;

import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class J4SStatementTest
{
	// file URL
	private URL fUrl;

	// J4SUrl
	private String url;

	// JDBC Connection
	private Connection conn;

	private Statement stmt;

	private List<String> getColumnNames( final String table )
			throws SQLException
	{
		final ResultSet rs = conn.getMetaData().getColumns(conn.getCatalog(),
				conn.getSchema(), table, null);
		final List<String> colNames = new ArrayList<String>();
		while (rs.next())
		{
			colNames.add(rs.getString(4));
		}
		return colNames;
	}

	@Before
	public void setup() throws Exception
	{
		Class.forName("org.xenei.jdbc4sparql.J4SDriver");

		fUrl = J4SDriverTest.class.getResource("./J4SStatementTest.ttl"); // /org/xenei/jdbc4sparql/J4SDriverTest.ttl");

		url = "jdbc:j4s?catalog=test&type=turtle&builder=org.xenei.jdbc4sparql.sparql.builders.SimpleNullableBuilder:"
				+ fUrl.toString();

		conn = DriverManager.getConnection(url, "myschema", "mypassw");
		conn.setAutoCommit(false);
		stmt = conn.createStatement();
	}

	@After
	public void tearDown()
	{
		try
		{
			stmt.close();
		}
		catch (final SQLException ignore)
		{
		}
		try
		{
			conn.close();
		}
		catch (final SQLException ignore)
		{
		}
	}

	@Test
	public void testBadValueInEqualsConst() throws ClassNotFoundException,
			SQLException
	{
		final ResultSet rset = stmt
				.executeQuery("select fooTable.IntCol, barTable.IntCol from fooTable inner, barTable where fooTable.StringCol=barTable.StringCol and fooTable.IntCol='Foo3String'");
		try
		{
			Assert.assertFalse("Should not return any values", rset.next());
		}
		finally
		{
			rset.close();
		}
	}

	@Test
	public void testColumnEqualConst() throws ClassNotFoundException,
			SQLException
	{
		final List<String> colNames = getColumnNames("fooTable");
		final ResultSet rset = stmt
				.executeQuery("select * from fooTable where StringCol='Foo2String'");
		int i = 0;
		while (rset.next())
		{
			final StringBuilder sb = new StringBuilder();
			for (final String colName : colNames)
			{
				sb.append(String.format("[%s]=%s ", colName,
						rset.getString(colName)));
			}
			final String s = sb.toString();
			Assert.assertTrue(s.contains("[StringCol]=Foo2String"));
			Assert.assertTrue(s.contains("[IntCol]=5"));
			Assert.assertTrue(s
					.contains("[type]=http://example.com/jdbc4sparql#fooTable"));
			Assert.assertTrue(s.contains("[NullableStringCol]=null"));
			Assert.assertTrue(s.contains("[NullableIntCol]=null"));
			i++;
		}
		Assert.assertEquals(1, i);
		rset.close();
		stmt.close();
	}

	@Test
	public void testFullRetrieval() throws ClassNotFoundException, SQLException
	{
		final String[][] results = {
				{ "[StringCol]=FooString",
						"[NullableStringCol]=FooNullableFooString",
						"[NullableIntCol]=6", "[IntCol]=5",
						"[type]=http://example.com/jdbc4sparql#fooTable" },
				{ "[StringCol]=Foo2String", "[NullableStringCol]=null",
						"[NullableIntCol]=null", "[IntCol]=5",
						"[type]=http://example.com/jdbc4sparql#fooTable" } };

		// get the column names.
		final List<String> colNames = getColumnNames("fooTable");
		final ResultSet rset = stmt.executeQuery("select * from fooTable");
		int i = 0;
		while (rset.next())
		{
			final List<String> lst = Arrays.asList(results[i]);
			for (final String colName : colNames)
			{
				lst.contains(String.format("[%s]=%s", colName,
						rset.getString(colName)));
			}
			i++;
		}
		Assert.assertEquals(2, i);
		rset.close();

	}

	@Test
	public void testInnerJoinSelect() throws SQLException
	{
		final ResultSet rset = stmt
				.executeQuery("select fooTable.IntCol, barTable.IntCol, fooTable.StringCol, barTable.StringCol from fooTable inner join barTable ON fooTable.StringCol=barTable.StringCol");

		while (rset.next())
		{
			Assert.assertEquals(5, rset.getInt(1));
			Assert.assertEquals(15, rset.getInt(2));
		}
		rset.close();
	}

	@Test
	public void testJoinSelect() throws SQLException
	{
		final ResultSet rset = stmt
				.executeQuery("select fooTable.IntCol, barTable.IntCol from fooTable join barTable ON fooTable.StringCol=barTable.StringCol");

		while (rset.next())
		{
			Assert.assertEquals(5, rset.getInt(1));
			Assert.assertEquals(15, rset.getInt(2));
		}
		rset.close();
	}

	@Test
	public void testWhereEqualitySelect() throws SQLException
	{
		final ResultSet rset = stmt
				.executeQuery("select fooTable.IntCol, barTable.IntCol from fooTable, barTable WHERE fooTable.StringCol=barTable.StringCol");

		while (rset.next())
		{
			Assert.assertEquals(5, rset.getInt(1));
			Assert.assertEquals(15, rset.getInt(2));
		}
		rset.close();
	}

}
