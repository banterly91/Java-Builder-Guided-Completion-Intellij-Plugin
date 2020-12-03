package net.banterly.buildercompletion;

import net.banterly.buildercompletion.annotations.BuildMethod;
import net.banterly.buildercompletion.annotations.BuilderClass;
import net.banterly.buildercompletion.annotations.MandatoryBuilderMethod;
import net.banterly.buildercompletion.annotations.RepeatableBuilderMethod;

import java.util.List;

public class ClassWithABuilder {
    private int intField1;
    private String stringField1;
    private double doubleField1;
    private Object objectField1;
    private List<Integer> intListField;

    private ClassWithABuilder(Builder builder){
        intField1 = builder.intField1;
        stringField1 = builder.stringField1;
        doubleField1 = builder.doubleField1;
        objectField1 = builder.objectField1;
        intListField = builder.intListField;
    }

    @BuilderClass
    public static class Builder {
        private int intField1;
        private String stringField1;
        private double doubleField1;
        private Object objectField1;
        private List<Integer> intListField;

        @Deprecated
        public Builder withIntField1(int intField1){
            this.intField1 = intField1;
            return this;
        }

        public Builder withIntList(int intListItem){
            intListField.add(intListItem);
            return this;
        }

        @MandatoryBuilderMethod
        public Builder withIntList(List<Integer> intList){
            intListField.addAll(intList);
            return this;
        }

        public Builder withIntList(int intListItem1, int intListItem2){
            intListField.add(intListItem1, intListItem2);
            return this;
        }

        public Builder withStringField1(String stringField1){
            this.stringField1 = stringField1;
            return this;
        }

        @MandatoryBuilderMethod
        public Builder withDoubleField1(double doubleField1){
            this.doubleField1 = doubleField1;
            return this;
        }

        @MandatoryBuilderMethod
        @RepeatableBuilderMethod
        public Builder withObjectField1(Object objectField1){
            this.objectField1 = objectField1;
            return this;
        }

        @BuildMethod
        public ClassWithABuilder build(){
            return new ClassWithABuilder(this);
        }
    }
}
