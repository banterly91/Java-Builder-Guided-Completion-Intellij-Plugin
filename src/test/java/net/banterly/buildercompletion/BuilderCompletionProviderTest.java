package net.banterly.buildercompletion;

import com.intellij.codeInsight.completion.PrioritizedLookupElement;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementDecorator;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import static net.banterly.buildercompletion.BuilderCompletionProvider.LookupElementStyle.*;

public class BuilderCompletionProviderTest extends LightJavaCodeInsightFixtureTestCase {


    @Override
    protected String getTestDataPath() {
        return "src/test/resources";
    }

    @Test
    public void testMandatoryMethodsCompletion(){

        //the class setting up the scenario has invoked 1 repeatable mandatory method and 1 non-repeatable mandatory method
        myFixture.configureByFiles("MandatoryMethodsSetupClass.java","ClassWithABuilderDefinition.java");

        LookupElement[] lookupElements =  myFixture.completeBasic();

        Map<String, Double> lookupElementName2Priority = Arrays.stream(lookupElements).collect(Collectors.toMap(LookupElement::getLookupString,
                e -> ((PrioritizedLookupElement)((LookupElementDecorator) e).getDelegate()).getPriority()));

        //mandatory method which was not invoked so we expect to see it marked as mandatory
        Assert.assertEquals(MANDATORY.getPriority(), lookupElementName2Priority.get("withStringList"), 0.0);
        //mandatory repeatable method which was invoked so we expect to see it marked as optional
        Assert.assertEquals(OPTIONAL.getPriority(), lookupElementName2Priority.get("withStringListItem"), 0.0);
        //optional repeatable method which was not invoked so we expect to see it marked as optional
        Assert.assertEquals(OPTIONAL.getPriority(), lookupElementName2Priority.get("withIntListItem"), 0.0);
        //optional method which was not invoked so we expect to see it marked as optional
        Assert.assertEquals(OPTIONAL.getPriority(), lookupElementName2Priority.get("withIntList"), 0.0);
        //optional method which was not invoked so we expect to see it marked as optional
        Assert.assertEquals(OPTIONAL.getPriority(), lookupElementName2Priority.get("withStringField1"), 0.0);
        //mandatory non-repeatable method which was invoked so we expect to see it marked as invalid
        Assert.assertEquals(INVALID.getPriority(), lookupElementName2Priority.get("withDoubleField1"), 0.0);
        //build method and not all mandatory methods have been invoked so we expect it to be marked as invalid
        Assert.assertEquals(INVALID.getPriority(), lookupElementName2Priority.get("build"), 0.0);
    }

    @Test
    public void testBuildMethodCompletion(){

        //the class setting up the scenario has invoked all mandatory methods
        myFixture.configureByFiles("BuilderMethodSetupClass.java","ClassWithABuilderDefinition.java");

        LookupElement[] lookupElements =  myFixture.completeBasic();

        Map<String, Double> lookupElementName2Priority = Arrays.stream(lookupElements).collect(Collectors.toMap(LookupElement::getLookupString,
                e -> ((PrioritizedLookupElement)((LookupElementDecorator) e).getDelegate()).getPriority()));

        //mandatory repeatable method which was invoked so we expect to see it marked as optional
        Assert.assertEquals(OPTIONAL.getPriority(), lookupElementName2Priority.get("withStringListItem"), 0.0);
        //optional repeatable method which was not invoked so we expect to see it marked as optional
        Assert.assertEquals(OPTIONAL.getPriority(), lookupElementName2Priority.get("withIntListItem"), 0.0);
        //optional method which was not invoked so we expect to see it marked as optional
        Assert.assertEquals(OPTIONAL.getPriority(), lookupElementName2Priority.get("withIntList"), 0.0);
        //optional method which was not invoked so we expect to see it marked as optional
        Assert.assertEquals(OPTIONAL.getPriority(), lookupElementName2Priority.get("withStringField1"), 0.0);
        //mandatory non-repeatable method which was invoked so we expect to see it marked as invalid
        Assert.assertEquals(INVALID.getPriority(), lookupElementName2Priority.get("withStringList"), 0.0);
        //mandatory non-repeatable method which was invoked so we expect to see it marked as invalid
        Assert.assertEquals(INVALID.getPriority(), lookupElementName2Priority.get("withDoubleField1"), 0.0);
        //build method and all mandatory methods have been invoked so we expect it to be marked as mandatory
        Assert.assertEquals(MANDATORY.getPriority(), lookupElementName2Priority.get("build"), 0.0);
    }

    @Test
    public void testOptionalMethodsCompletion(){

        //the class setting up the scenario has invoked 1 repeatable optional method and 1 non-repeatable optional method
        myFixture.configureByFiles("OptionalMethodsSetupClass.java","ClassWithABuilderDefinition.java");

        LookupElement[] lookupElements =  myFixture.completeBasic();

        Map<String, Double> lookupElementName2Priority = Arrays.stream(lookupElements).collect(Collectors.toMap(LookupElement::getLookupString,
                e -> ((PrioritizedLookupElement)((LookupElementDecorator) e).getDelegate()).getPriority()));

        //mandatory repeatable method which was not invoked so we expect to see it marked as mandatory
        Assert.assertEquals(MANDATORY.getPriority(), lookupElementName2Priority.get("withStringListItem"), 0.0);
        //optional repeatable method which was invoked so we expect to see it marked as optional
        Assert.assertEquals(OPTIONAL.getPriority(), lookupElementName2Priority.get("withIntListItem"), 0.0);
        //optional method which was not invoked so we expect to see it marked as optional
        Assert.assertEquals(OPTIONAL.getPriority(), lookupElementName2Priority.get("withIntList"), 0.0);
        //optional method which was invoked so we expect to see it marked as invalid
        Assert.assertEquals(INVALID.getPriority(), lookupElementName2Priority.get("withStringField1"), 0.0);
        //mandatory non-repeatable method which was not invoked so we expect to see it marked as mandatory
        Assert.assertEquals(MANDATORY.getPriority(), lookupElementName2Priority.get("withStringList"), 0.0);
        //mandatory non-repeatable method which was not invoked so we expect to see it marked as mandatory
        Assert.assertEquals(MANDATORY.getPriority(), lookupElementName2Priority.get("withDoubleField1"), 0.0);
        //build method and no mandatory methods have been invoked so we expect it to be marked as invalid
        Assert.assertEquals(INVALID.getPriority(), lookupElementName2Priority.get("build"), 0.0);
    }
}