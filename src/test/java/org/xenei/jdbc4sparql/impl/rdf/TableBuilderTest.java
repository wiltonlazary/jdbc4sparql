package org.xenei.jdbc4sparql.impl.rdf;

import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.xenei.jdbc4sparql.iface.Column;
import org.xenei.jdbc4sparql.iface.NameFilter;
import org.xenei.jdbc4sparql.iface.name.SchemaName;
import org.xenei.jena.entities.EntityManagerFactory;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFList;
import com.hp.hpl.jena.rdf.model.RDFNode;

public class TableBuilderTest {

	private Model model;
	private RdfTableDef tableDef;
	private RdfSchema mockSchema;

	@Before
	public void setUp() throws Exception {
		model = ModelFactory.createDefaultModel();
		final RdfTableDef.Builder builder = new RdfTableDef.Builder()
				.addColumnDef(
						RdfColumnDef.Builder.getStringBuilder().build(model))
				.addColumnDef(
						RdfColumnDef.Builder.getIntegerBuilder().build(model));
		tableDef = builder.build(model);
		mockSchema = Mockito.mock(RdfSchema.class);
		Mockito.when(mockSchema.getResource()).thenReturn(
				model.createResource("http://example.com/mockSchema"));
		Mockito.when(mockSchema.getName()).thenReturn(
				new SchemaName("catalog", "schema"));
	}

	@After
	public void tearDown() throws Exception {
		model.close();
	}

	@Test
	public void testDefaultBuilder() throws Exception {
		final RdfTable.Builder builder = new RdfTable.Builder()
				.setTableDef(tableDef).setName("table")
				.setColumn(0, "StringCol").setColumn(1, "IntCol")
				.setSchema(mockSchema).setType("testing Table");
		final RdfTable table = builder.build(model);

		Assert.assertEquals(2, table.getColumnCount());
		Assert.assertEquals("table", table.getName().getShortName());
		final NameFilter<Column> nf = table.findColumns("StringCol");
		Assert.assertTrue(nf.hasNext());
		final Column c = nf.next();
		Assert.assertEquals("StringCol", c.getName().getShortName());
		Assert.assertFalse(nf.hasNext());

		EntityManagerFactory.getEntityManager();

		final Property p = model.createProperty(
				ResourceBuilder.getNamespace(RdfTable.class), "column");

		final List<RDFNode> columns = table.getResource()
				.getRequiredProperty(p).getResource().as(RDFList.class)
				.asJavaList();

		Assert.assertEquals(2, columns.size());

	}

}
