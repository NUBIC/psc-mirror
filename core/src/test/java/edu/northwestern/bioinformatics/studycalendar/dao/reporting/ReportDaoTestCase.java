package edu.northwestern.bioinformatics.studycalendar.dao.reporting;

import edu.northwestern.bioinformatics.studycalendar.dao.ContextDaoTestCase;
import gov.nih.nci.cabig.ctms.domain.DomainObject;

import java.util.List;

/**
 * @author John Dzak
 */
public abstract class ReportDaoTestCase<F extends ReportFilters, R extends DomainObject, D extends ReportDao<F, R>> extends ContextDaoTestCase {
    private D dao;
    protected F filters;

    protected void setUp() throws Exception {
        super.setUp();
        dao = (D) getApplicationContext().getBean(getDaoBeanName());
        filters = createFilters();
    }

    protected abstract F createFilters();

    protected List<R> assertSearchWithResults(long... expectedIds) {
        List<R> rows = doSearch();
        assertEquals("Wrong number of results: " + rows, expectedIds.length, rows.size());
        for (int i = 0; i < expectedIds.length; i++) {
            long id = expectedIds[i];
            assertEquals("Wrong row " + i, id, (long) rows.get(i).getId());
        }
        return rows;
    }

    protected List<R> doSearch() {
        return dao.search(filters);
    }

    protected D getDao() {
        return dao;
    }
}