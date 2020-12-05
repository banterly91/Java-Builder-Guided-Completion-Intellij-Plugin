package net.banterly.buildercompletion;

public class TestSetupClass {

    public void testSetup(){

        ClassWithABuilder.Builder builder = new ClassWithABuilder.Builder();

        builder.withDoubleField1(2)
                .withStringListItem("testItem").<caret>;
    }
}
