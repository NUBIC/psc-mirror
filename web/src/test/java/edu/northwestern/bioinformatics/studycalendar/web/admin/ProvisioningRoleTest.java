package edu.northwestern.bioinformatics.studycalendar.web.admin;

import gov.nih.nci.cabig.ctms.suite.authorization.SuiteRole;
import junit.framework.TestCase;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @author Rhett Sutphin
 */
public class ProvisioningRoleTest extends TestCase {
    public void testCreateForPscRole() throws Exception {
        ProvisioningRole role = new ProvisioningRole(SuiteRole.SYSTEM_ADMINISTRATOR);
        assertTrue("Used in PSC", role.isPscRole());
    }

    public void testCreateForSuiteOnlyRole() throws Exception {
        ProvisioningRole role = new ProvisioningRole(SuiteRole.AE_STUDY_DATA_REVIEWER);
        assertFalse("Not used in PSC", role.isPscRole());
    }

    public void testJSONIncludesName() throws Exception {
        JSONObject actual = new ProvisioningRole(SuiteRole.STUDY_SUBJECT_CALENDAR_MANAGER).toJSON();
        assertEquals("Study Subject Calendar Manager", actual.optString("name"));
    }

    public void testJSONIncludesKey() throws Exception {
        JSONObject actual = new ProvisioningRole(SuiteRole.DATA_ANALYST).toJSON();
        assertEquals("data_analyst", actual.optString("key"));
    }

    public void testJSONIncludesDescription() throws Exception {
        JSONObject actual = new ProvisioningRole(SuiteRole.DATA_IMPORTER).toJSON();
        assertEquals(SuiteRole.DATA_IMPORTER.getDescription(), actual.optString("description"));
    }

    public void testJSONIncludesScopesWhenScoped() throws Exception {
        JSONObject actual = new ProvisioningRole(SuiteRole.STUDY_SUBJECT_CALENDAR_MANAGER).toJSON();
        JSONArray scopes = actual.optJSONArray("scopes");
        assertNotNull(scopes);
        assertEquals("Wrong number of scopes", 2, scopes.length());
        assertEquals("Wrong scope", "site", scopes.getString(0));
        assertEquals("Wrong scope", "study", scopes.getString(1));
    }

    public void testJSONDoesNotIncludeScopesWhenNotScoped() throws Exception {
        JSONObject actual = new ProvisioningRole(SuiteRole.SYSTEM_ADMINISTRATOR).toJSON();
        assertNull(actual.optJSONArray("scopes"));
    }

    public void testJSONIncludesPscRoleUsesForPscRole() throws Exception {
        JSONObject actual = new ProvisioningRole(SuiteRole.STUDY_CALENDAR_TEMPLATE_BUILDER).toJSON();
        JSONArray uses = actual.optJSONArray("uses");
        assertNotNull(uses);
        assertEquals("Wrong number of uses", 1, uses.length());
        assertEquals("Wrong use", "template_management", uses.get(0));
    }

    public void testJSONDoesNotIncludesPscRoleUsesForSuiteOnlyRole() throws Exception {
        JSONObject actual = new ProvisioningRole(SuiteRole.SUPPLEMENTAL_STUDY_INFORMATION_MANAGER).toJSON();
        JSONArray uses = actual.optJSONArray("uses");
        assertNull(uses);
    }
}
