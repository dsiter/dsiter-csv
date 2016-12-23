package dsiter.dataset.csv;

import dsiter.dataset.IDataset;
import dsiter.iterator.IDatasetIterator;
import dsiter.reader.IReaderFactory;
import dsiter.row.*;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.util.Iterator;

public class CsvDataset implements IDataset {

	protected CSVFormat format;
	protected IReaderFactory readerFactory;
	protected ColumnDescriptor[] cds;
	protected IParserMutator[] mutators;
	protected RowShape shape;

	public CsvDataset(IReaderFactory readerFactory, ColumnDescriptor[] cds, CSVFormat format) {
		this.format = format;
		this.readerFactory = readerFactory;
		this.cds = cds;

		mutators = new IParserMutator[cds.length];
		shape = new RowShape();
		for(int i=0; i<cds.length; i++) {
			ColumnDescriptor cd = cds[i];
			ColumnType type = cd.getType();
			shape.incrNum(type);

			mutators[i] = IParserMutator.getInstance(cd.getType(), shape.getNum(type)-1);
		}
	}

	public CsvDataset(IReaderFactory readerFactory, ColumnDescriptor[] cds) {
		this(readerFactory, cds, CSVFormat.RFC4180);
	}

	@Override
	public IDatasetIterator getIterator() throws Exception {
		CSVParser p = new CSVParser(readerFactory.getReader(), format);
		return new CsvDatasetIterator(p.iterator());
	}

	private class CsvDatasetIterator implements IDatasetIterator {

		CSVRecord record = null;
		Iterator<CSVRecord> src;
		Row row;

		public CsvDatasetIterator(Iterator<CSVRecord> src) {
			this.src = src;
			row = new Row(shape);
		}

		@Override
		public boolean tryMoveNext() {
			if(src.hasNext()) {
				record = src.next();
				return true;
			}
			else {
				return false;
			}
		}

		@Override
		public Row getCurrentRow() throws Exception {
			if (record.size() != cds.length) {
				throw new IllegalArgumentException(
					"CSV row was wrong size: expected " + cds.length + ", was " + record.size()
				);
			}

			for(int i=0; i<mutators.length; i++) {
				mutators[i].parseAndMutate(record.get(i), row);
			}

			return row;
		}

		@Override
		public long tryGetLength() {
			// TODO - any way to get the length of a CSVParser iterator? (Probably not but worth checking)
			return -1;
		}

		@Override
		public ColumnDescriptor[] getColumnDescriptors() {
			return cds;
		}
	}
}