package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.domain.Activity;
import edu.northwestern.bioinformatics.studycalendar.domain.Source;
import edu.northwestern.bioinformatics.studycalendar.xml.AbstractStudyCalendarXmlCollectionSerializer;
import edu.northwestern.bioinformatics.studycalendar.xml.XsdAttribute;
import edu.northwestern.bioinformatics.studycalendar.xml.XsdElement;
import org.dom4j.Element;

import java.util.List;

/**
 * @author Rhett Sutphin
 */
public class ActivitySourceXmlSerializer extends AbstractStudyCalendarXmlCollectionSerializer<Source> {
    private ActivityXmlSerializer activitySerializer;

    public ActivitySourceXmlSerializer() {
        activitySerializer = new ActivityXmlSerializer(true);
    }

    @Override
    protected XsdElement collectionRootElement() {
        return XsdElement.ACTIVITY_SOURCES;
    }

    @Override
    protected XsdElement rootElement() {
        return XsdElement.ACTIVITY_SOURCE;
    }

    @Override
    protected Element createElement(Source source, boolean inCollection) {

        Element activityElement = rootElement().create();
        XsdAttribute.ACTIVITY_NAME.addTo(activityElement, source.getName());
        for (Activity a : source.getActivities()) {
            activityElement.add(activitySerializer.createElement(a));
        }
        if (inCollection) {
            return activityElement;

        } else {
            Element root = collectionRootElement().create();
            root.add(activityElement);

            return root;
        }


    }

    @Override
    public Source readElement(Element element) {
        Source source = new Source();
        source.setName(XsdAttribute.ACTIVITY_NAME.from(element));
        for (Element aElt : (List<Element>) element.elements("activity")) {
            source.addActivity(activitySerializer.readElement(aElt));
        }
        return source;
    }
}
