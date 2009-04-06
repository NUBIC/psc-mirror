package edu.northwestern.bioinformatics.studycalendar.utility.osgimosis;

import edu.northwestern.bioinformatics.studycalendar.utility.osgimosis.people.DefaultPerson;
import edu.northwestern.bioinformatics.studycalendar.utility.osgimosis.people.Person;

import java.util.Arrays;

/**
 * @author Rhett Sutphin
 */
@SuppressWarnings({ "RawUseOfParameterizedType" })
public class EncapsulatorTest extends OsgimosisTestCase {
    private Membrane aMembrane;
    private Object aInstance, bInstance;
    private Class personB, defaultPersonB;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        aMembrane = Membrane.get(
            loaderA, "edu.northwestern.bioinformatics.studycalendar.utility.osgimosis.people");
        aInstance = classFromLoader(DefaultPerson.class, loaderA).newInstance();
        personB = classFromLoader(Person.class, loaderB);
        defaultPersonB = classFromLoader(DefaultPerson.class, loaderB);
    }

    public void testProxyWithoutSuperclassIsJdkProxy() throws Exception {
        Encapsulator params = new Encapsulator(
            aMembrane, loaderB, Arrays.asList(personB));
        Object actual = params.proxy(aInstance);
        assertTrue("Class should contain $Proxy in name: " + actual.getClass().getName(),
            actual.getClass().getName().contains("$Proxy"));
    }

    public void testProxyWithSuperclassIsCglibProxy() throws Exception {
        Encapsulator params = new Encapsulator(
            aMembrane, loaderB, defaultPersonB, Arrays.asList(personB));
        Object actual = params.proxy(aInstance);
        assertTrue("Class should contain Enhancer in name: " + actual.getClass().getName(),
            actual.getClass().getName().contains("Enhancer"));
        assertTrue("Class should contain superclass in name: " + actual.getClass().getName(),
            actual.getClass().getName().contains("DefaultPerson"));
    }
}
