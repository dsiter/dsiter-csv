package dsiter.dataset.csv;

import dsiter.IteratorExpectations;
import dsiter.reader.IReaderFactory;
import dsiter.reader.StringReaderFactory;
import dsiter.row.ColumnDescriptor;
import dsiter.row.ColumnType;
import org.apache.commons.csv.CSVFormat;
import org.junit.Test;

public class TestCsvDataset {

	private IReaderFactory lines(String... lines) {
		return new StringReaderFactory(
			String.join("\n", lines)
		);
	}

	@Test
	public void testCsv1() throws Exception {
		ColumnDescriptor[] cds = new ColumnDescriptor[] {
			new ColumnDescriptor("l1", ColumnType.LONG, 0),
			new ColumnDescriptor("d1", ColumnType.DOUBLE, 0),
			new ColumnDescriptor("s1", ColumnType.STRING, 0)
		};

		CsvDataset csvds = new CsvDataset(
			lines(
				"1, 1.5, foo bar",
				"2, 2.5, bat baz"
			),
			cds,
			CSVFormat.DEFAULT.withIgnoreSurroundingSpaces()
		);

		IteratorExpectations e = new IteratorExpectations();
		e.expectLongs("l1", 1L, 2L);
		e.expectDoubles("d1", 1.5, 2.5);
		e.expectStrings("s1", "foo bar", "bat baz");

		e.checkIterator(csvds.getIterator());
	}
}
