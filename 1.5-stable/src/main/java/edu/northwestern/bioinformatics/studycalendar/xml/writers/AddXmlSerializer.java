package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.domain.PlanTreeNode;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Add;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Change;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.ChildrenChange;
import org.dom4j.Element;

import java.util.List;

public class AddXmlSerializer extends AbstractChildrenChangeXmlSerializer {
    private static final String ADD = "add";
    private static final String INDEX = "index";

    protected Change changeInstance() {
        return new Add();
    }

    protected String elementName() {
        return ADD;
    }

    protected void addAdditionalAttributes(final Change change, Element element) {
        Add add = (Add) change;
        if (add.getIndex() != null) {
            element.addAttribute(INDEX, add.getIndex().toString());
        }
//        PlanTreeNode<?> planTreeNode = add.getChild();
        PlanTreeNode<?> child = (PlanTreeNode<?>) getChild(((ChildrenChange)change).getChildId(), childClass);
        AbstractPlanTreeNodeXmlSerializer serializer = getPlanTreeNodeSerializerFactory().createXmlSerializer(child);
        Element ePlanTreeNode = serializer.createElement(child);
        element.add(ePlanTreeNode);
    }

    protected void setAdditionalProperties(final Element element, Change add) {
        ((Add)add).setIndex(new Integer(element.attributeValue(INDEX)));
        List<Element> ePlanTreeNodes = element.elements();
        Element ePlanTreeNode = ePlanTreeNodes.get(0);
        AbstractPlanTreeNodeXmlSerializer serializer = getPlanTreeNodeSerializerFactory().createXmlSerializer(ePlanTreeNode);
        PlanTreeNode<?> planTreeNode = serializer.readElement(ePlanTreeNode);
        ((Add)add).setChild(planTreeNode);
    }
}
