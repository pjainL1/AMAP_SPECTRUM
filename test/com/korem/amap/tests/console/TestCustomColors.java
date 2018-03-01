package com.korem.amap.tests.console;

import com.spinn3r.log5j.Logger;
import java.io.UnsupportedEncodingException;
import org.testng.annotations.Test;

/**
 *
 * @author rarif
 */
@Test(singleThreaded = true)
public class TestCustomColors extends ConsoleBaseTest {

    private static final Logger LOGGER = Logger.getLogger();

    public TestCustomColors() {
        super("console/ColorManagement.safe");
    }
    
    @Override
    protected Object[] getOtherParams() {
        return new Object[] {"sponsor", config.testSponsors()};
    }

    @Test
    void testLoadColorDataGrid() throws UnsupportedEncodingException {
        testLoadDataGrid();
    }

    @Test
    void testSearchColorDataGrid() throws UnsupportedEncodingException {
        testSearchDataGrid("TOR");
    }

    @Test
    void testSearchColorDataGridInvalidSearch() throws UnsupportedEncodingException {
        testSearchDataGridInvalidSearch();
    }

    @Test
    void testFilterColorDataGridWithGreaterThanOnNumeric() throws UnsupportedEncodingException {
        testFilterDataGridWithGreaterThanOnNumeric(4745, "sponsorLocationKey", "locationcolors");
    }

    void testFilterColorDataGridEqualityOnString() throws UnsupportedEncodingException {
        testFilterDataGridEqualityOnString("TOR", "city", "locationcolors");
    }

    @Test
    void testFilterColorDataGridOnTwoColumn() throws UnsupportedEncodingException {
        testFilterDataGridOnTwoColumn(1500, "WINN", "sponsorLocationKey", "city", "locationcolors");
    }

    @Test
    void testSortingColorDataGridASC() throws UnsupportedEncodingException {
        testSortingDataGridASC("postalCode", "locationcolors");
    }

    @Test
    void testSortingColorDataGridDESC() throws UnsupportedEncodingException {
        testSortingDataGridDESC("postalCode", "locationcolors");
    }

    @Test
    void testSortingColorDataGridWithSearch() throws UnsupportedEncodingException {
        testSortingDataGridWithSearch("TORO", "sponsorLocationKey", "city", "locationcolors");
    }

    @Test
    void testPagingColorDataGrid() throws UnsupportedEncodingException {
        testPagingDataGrid("sponsorLocationKey", "locationcolors");
    }
}
