package bwj.logging.jdbc;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.sql.Clob;
import java.sql.SQLException;
import java.util.TreeMap;

class BatchItem
{
    private Renderer renderer;

    private String sql = "";

    private TreeMap<Integer, Object> parameters = null;

    private boolean logTextReaderStreams = true;

    private static final String CLOB_VALUE_PLACEHOLDER = "{Clob}";
    private static final String READER_PLACEHOLDER = "{Reader}";



    public BatchItem(Renderer renderer) {
        this.renderer = renderer;
    }

    public BatchItem(BatchItem that) {
        this(that, that.sql);
    }



    public BatchItem(BatchItem that, String sql) {
        this.sql = sql;
        if (that.parameters != null) {
            this.parameters = new TreeMap<Integer, Object>();
            this.parameters.putAll(that.parameters);
        }
        if (that.renderer != null)
            this.renderer = that.renderer;
    }

    public void setSQL(String sql) {
        this.sql = sql;
    }

    public void setParameter(int index, Object parameter) {
        if (this.parameters == null) {
            if (parameter == null)
                return;
            this.parameters = new TreeMap<Integer, Object>();
        }
        this.parameters.put(index, parameter);
    }



    public Reader setReaderParameter(int index, Reader parameter) {
        if (! logTextReaderStreams) {
            return parameter;
        }
        else if (parameter == null) {
            return null;
        }

        try
        {
            String readerString = IOUtils.toString(parameter);
            this.setParameter(index, readerString);
            return new StringReader(readerString);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            throw new RuntimeException("Error: " + e.getMessage(), e);
        }
    }



    public void clearParameters() {
        if (this.parameters != null)
            this.parameters.clear();
    }

    public String toString() {
        if (this.parameters == null || this.parameters.size() == 0)
            return this.sql;
        return TagUpdater.replace(this.sql, "?", this.parameters.values().iterator(), this.renderer);
    }
}
