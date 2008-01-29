package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.xml.writers.StudyXmlSerializer;
import org.apache.commons.lang.StringUtils;
import static org.easymock.EasyMock.expect;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.validation.BindException;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

public class ImportTemplateXmlCommandTest extends StudyCalendarTestCase {

    private StudyXmlSerializer serializer;
    private ImportTemplateXmlCommand command;
    private MultipartFile file;
    private InputStream stream;

    protected void setUp() throws Exception {
        super.setUp();

        serializer = registerMockFor(StudyXmlSerializer.class);
        file = registerMockFor(MockMultipartFile.class);
        stream = registerMockFor(InputStream.class);

        command = new ImportTemplateXmlCommand();
        command.setStudyXml(file);
        command.setStudyXmlSerializer(serializer);
    }

    public void testApply() throws Exception {
        expect(file.getInputStream()).andReturn(stream);
        expect(serializer.readDocument(stream)).andReturn(null);
        replayMocks();

        command.apply();
        verifyMocks();
    }

    public void testValidate() throws Exception {
        expect(file.isEmpty()).andReturn(true);
        replayMocks();

        BindException errors = new BindException(file, StringUtils.EMPTY);
        command.validate(errors);
        verifyMocks();

        assertTrue(errors.hasErrors());
        assertEquals("Wrong error count", 1, errors.getErrorCount());
        assertEquals("Wrong error code", "error.template.xml.not.specified", errors.getGlobalError().getCode());
    }
}
