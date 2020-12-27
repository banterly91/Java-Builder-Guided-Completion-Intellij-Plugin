package net.banterly.buildercompletion;

public class TestSetupClass {

    public void testSetup() {

        ClassWithABuilder.Builder builder = new ClassWithABuilder.Builder();

        int x = 1;

        if (x > 0) {
            builder.withStringListItem("testItem");
        } else {
            builder.withStringListItem("testItem");
        }

        switch (x) {
            case 0:
                builder.withDoubleField1(2);
                break;
            case 1:
            default:
                builder.withDoubleField1(2);

        }

        builder.<caret>
    }
}