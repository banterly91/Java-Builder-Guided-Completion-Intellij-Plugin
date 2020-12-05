package net.banterly.buildercompletion;

import java.util.ArrayList;

public class TestSetupClass {

    public void testSetup(){

        ClassWithABuilder.Builder builder = new ClassWithABuilder.Builder();

        builder.withStringList(new ArrayList<>())
                .withStringListItem("testItem")
                .withDoubleField1(2).<caret>;
    }
}
