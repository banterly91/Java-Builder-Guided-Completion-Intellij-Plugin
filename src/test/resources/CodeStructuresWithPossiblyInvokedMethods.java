package net.banterly.buildercompletion;

public class TestSetupClass {

    public void testSetup() {

        ClassWithABuilder.Builder builder = new ClassWithABuilder.Builder();

        int x = 1;

        if (x > 0) {
            builder.withStringListItem("testItem")
                    .withDoubleField1(2);
        } else if (x < 0) {
            builder.withStringListItem("testItem")
                    .withDoubleField1(2);
        }

        if (x < 0) {

        } else  {
            builder.withStringListItem("testItem")
                    .withDoubleField1(2);
        }

        switch (x) {
            case 0:
                builder.withStringListItem("testItem")
                        .withDoubleField1(2);
                break;
            case 1:
                builder.withStringListItem("testItem")
                        .withDoubleField1(2);
        }

        switch (x) {
            case 0:
                break;
            case 1:
                builder.withStringListItem("testItem")
                        .withDoubleField1(2);
                break;
            default:
                builder.withStringListItem("testItem")
                        .withDoubleField1(2);
        }

        while (x > 0) {
            builder.withStringListItem("testItem")
                    .withDoubleField1(2);
            break;
        }


        for (int i = 0; i < 1; i++) {
            builder.withStringListItem("testItem")
                    .withDoubleField1(2);
        }

        builder.<caret>
    }
}