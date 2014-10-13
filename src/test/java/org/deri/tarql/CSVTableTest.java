package org.deri.tarql;

import static org.deri.tarql.Helpers.binding;
import static org.deri.tarql.Helpers.vars;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.deri.tarql.csv.CSVFormat;
import org.junit.Test;

import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.engine.binding.Binding;

public class CSVTableTest {

	private void assertContents(Iterator<Binding> it, Binding... bindings) {
		int counter = 0;
		while (it.hasNext()) {
			assertEquals(bindings[counter], it.next());
			counter += 1;
		}
		assertEquals(bindings.length, counter);
	}
	
	@Test
	public void testEmpty() throws IOException {
		TarqlTable table = new TarqlTable(InputStreamSource.fromString(""));
		assertTrue(table.isEmpty());
		assertEquals(0, table.size());
		assertEquals(vars("ROWNUM"), table.getVars());
		assertFalse(table.rows().hasNext());
		assertFalse(table.iterator(null).hasNext());
	}
	
	@Test
	public void testSimpleTable() throws IOException {
		CSVFormat options = new CSVFormat();
		options.setColumnNamesInFirstRow(false);
		TarqlTable table = new TarqlTable(InputStreamSource.fromString("Alice,Smith\nBob,Cook"), options);
		assertFalse(table.isEmpty());
		assertEquals(2, table.size());
		List<Var> vars = vars("a", "b", "ROWNUM");
		assertEquals(vars, table.getVars());
		Binding[] bindings = {binding(vars, "\"Alice\"", "\"Smith\"", "1"), binding(vars, "\"Bob\"", "\"Cook\"", "2")};
		assertContents(table.rows(), bindings);
		assertContents(table.iterator(null), bindings);
	}
	
	@Test
	public void testMultipleParallelIterators() throws IOException {
		CSVFormat options = new CSVFormat();
		options.setColumnNamesInFirstRow(false);
		TarqlTable table = new TarqlTable(InputStreamSource.fromString("Alice,Smith\nBob,Cook"), options);
		List<Var> vars = vars("a", "b", "ROWNUM");
		Binding row1 = binding(vars, "\"Alice\"", "\"Smith\"", "1");
		Binding row2 = binding(vars, "\"Bob\"", "\"Cook\"", "2");
		QueryIterator it1 = table.iterator(null);
		assertEquals(row1, it1.next());
		QueryIterator it2 = table.iterator(null);
		QueryIterator it3 = table.iterator(null);
		assertEquals(row1, it2.next());
		assertEquals(row2, it1.next());
		it2.close();
		assertEquals(row1, it3.next());
		assertEquals(row2, it3.next());
	}
}
