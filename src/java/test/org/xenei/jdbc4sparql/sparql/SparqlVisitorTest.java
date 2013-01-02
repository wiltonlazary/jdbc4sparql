package org.xenei.jdbc4sparql.sparql;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.syntax.Element;
import com.hp.hpl.jena.sparql.syntax.ElementBind;
import com.hp.hpl.jena.sparql.syntax.ElementFilter;
import com.hp.hpl.jena.sparql.syntax.ElementGroup;
import com.hp.hpl.jena.sparql.syntax.ElementTriplesBlock;
import com.hp.hpl.jena.vocabulary.RDF;

import java.io.StringReader;
import java.sql.DatabaseMetaData;
import java.util.List;

import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.statement.Statement;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.xenei.jdbc4sparql.impl.TableDefImpl;
import org.xenei.jdbc4sparql.meta.MetaColumn;
import org.xenei.jdbc4sparql.mock.MockCatalog;
import org.xenei.jdbc4sparql.mock.MockSchema;
import org.xenei.jdbc4sparql.sparql.visitors.SparqlVisitor;

public class SparqlVisitorTest
{

	private final CCJSqlParserManager parserManager = new CCJSqlParserManager();
	private SparqlVisitor sv;

	@Before
	public void setUp() throws Exception
	{
		final SparqlCatalog catalog = new SparqlCatalog(MockCatalog.NS, null, MockCatalog.LOCAL_NAME);
		final MockSchema schema = new MockSchema(catalog);
		catalog.addSchema(schema);
		// create the foo table
		TableDefImpl tableDef = new TableDefImpl("foo");
		tableDef.add(MetaColumn.getStringInstance("StringCol"));
		tableDef.add(MetaColumn.getStringInstance("NullableStringCol")
				.setNullable(DatabaseMetaData.columnNullable));
		tableDef.add(MetaColumn.getIntInstance("IntCol"));
		tableDef.add(MetaColumn.getIntInstance("NullableIntCol").setNullable(
				DatabaseMetaData.columnNullable));
		schema.addTableDef(tableDef);
		
		// creae the var table
		tableDef = new TableDefImpl("bar");
		tableDef.add(MetaColumn.getStringInstance("BarStringCol"));
		tableDef.add(MetaColumn.getStringInstance("BarNullableStringCol")
				.setNullable(DatabaseMetaData.columnNullable));
		tableDef.add(MetaColumn.getIntInstance("IntCol"));
		tableDef.add(MetaColumn.getIntInstance("BarNullableIntCol").setNullable(
				DatabaseMetaData.columnNullable));
		schema.addTableDef(tableDef);		
		
		sv = new SparqlVisitor(catalog);

	}

	@Test
	public void testNoColParse() throws Exception
	{
		final String query = "SELECT * FROM foo";
		final Statement stmt = parserManager.parse(new StringReader(query));
		stmt.accept(sv);
		final Query q = sv.getBuilder().build();

		List<Var> vLst = q.getProjectVars();
		Assert.assertEquals( 4,  vLst.size() );
		Assert.assertTrue(q.getQueryPattern() instanceof ElementGroup);
		final ElementGroup eg = (ElementGroup) q.getQueryPattern();
		final List<Element> eLst = eg.getElements();
		Assert.assertEquals(8, eLst.size());
		int i = 0;
		for (Element e : eLst)
		{
			if (e instanceof ElementBind)
			{
				i++;
			}
		}
		Assert.assertEquals( 4, i );
	}

	@Test
	public void testSpecColParse() throws Exception
	{
		final String query = "SELECT StringCol FROM foo";
		final Statement stmt = parserManager.parse(new StringReader(query));
		stmt.accept(sv);
		final Query q = sv.getBuilder().build();
		
		final Element e = q.getQueryPattern();
		Assert.assertTrue(e instanceof ElementGroup);
		final ElementGroup eg = (ElementGroup) e;
		final List<Element> eLst = eg.getElements();
		Assert.assertEquals(1, eLst.size());
		Assert.assertTrue(eLst.get(0) instanceof ElementTriplesBlock);
		final ElementTriplesBlock etb = (ElementTriplesBlock) eLst.get(0);
		final List<Triple> tLst = etb.getPattern().getList();
		Assert.assertTrue(tLst.contains(new Triple(Node
				.createVariable("MockSchema.foo"), RDF.type
				.asNode(), Node
				.createURI("http://org.xenei.jdbc4sparql/meta#foo"))));
		Assert.assertTrue(tLst.contains(new Triple(Node
				.createVariable("MockSchema.foo"), Node
				.createURI("http://org.xenei.jdbc4sparql/meta#StringCol"), Node
				.createVariable("MockSchema.foo.StringCol"))));
		List<Var> vLst = q.getProjectVars();
		Assert.assertEquals( 1,  vLst.size() );
		Assert.assertEquals( Var.alloc("StringCol"), vLst.get(0));

	}
	
	@Test
	public void testSpecColWithEqnParse() throws Exception
	{
		final String query = "SELECT StringCol FROM foo WHERE StringCol != 'baz'";
		final Statement stmt = parserManager.parse(new StringReader(query));
		stmt.accept(sv);
		final Query q = sv.getBuilder().build();
		
		final Element e = q.getQueryPattern();
		Assert.assertTrue(e instanceof ElementGroup);
		final ElementGroup eg = (ElementGroup) e;
		final List<Element> eLst = eg.getElements();
		Assert.assertEquals(2, eLst.size());
		Assert.assertTrue(eLst.get(0) instanceof ElementTriplesBlock);
		final ElementTriplesBlock etb = (ElementTriplesBlock) eLst.get(0);
		final List<Triple> tLst = etb.getPattern().getList();
		Assert.assertTrue(tLst.contains(new Triple(Node
				.createVariable("MockSchema.foo"), RDF.type
				.asNode(), Node
				.createURI("http://org.xenei.jdbc4sparql/meta#foo"))));
		Assert.assertTrue(tLst.contains(new Triple(Node
				.createVariable("MockSchema.foo"), Node
				.createURI("http://org.xenei.jdbc4sparql/meta#StringCol"), Node
				.createVariable("MockSchema.foo.StringCol"))));
		Assert.assertTrue(eLst.get(1) instanceof ElementFilter);
		Assert.assertEquals( "FILTER ( ?MockSchema.foo.StringCol != \"baz\" )", eLst.get(1).toString());
		List<Var> vLst = q.getProjectVars();
		Assert.assertEquals( 1,  vLst.size() );
		Assert.assertEquals( Var.alloc("StringCol"), vLst.get(0));

	}
	
	@Test
	public void testTwoTableJoin() throws Exception
	{
		final String query = "SELECT * FROM foo, bar WHERE foo.IntCol = bar.IntCol";
		final Statement stmt = parserManager.parse(new StringReader(query));
		stmt.accept(sv);
		final Query q = sv.getBuilder().build();

		final Element e = q.getQueryPattern();
		Assert.assertTrue(e instanceof ElementGroup);
		final ElementGroup eg = (ElementGroup) e;
		final List<Element> eLst = eg.getElements();
		Assert.assertEquals(17, eLst.size());
		List<Var> vLst = q.getProjectVars();
		Assert.assertEquals( 8,  vLst.size() );
	}
}
