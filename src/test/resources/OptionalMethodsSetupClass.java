package net.banterly.buildercompletion;

public class TestSetupClass {

    public void testSetup(){

        ClassWithABuilder.Builder builder = new ClassWithABuilder.Builder();

        builder.withIntListItem(1)
                .withStringField1("testString").<caret>;
    }
}
