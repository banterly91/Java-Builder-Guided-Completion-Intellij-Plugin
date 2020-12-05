import net.banterly.buildercompletion.annotations.BuildMethod;
import net.banterly.buildercompletion.annotations.BuilderClass;
import net.banterly.buildercompletion.annotations.MandatoryBuilderMethod;
import net.banterly.buildercompletion.annotations.RepeatableBuilderMethod;

import java.util.ArrayList;
import java.util.List;

public class ClassWithABuilder {
    private String stringField1;
    private double doubleField1;
    private List<Integer> intListField;
    private List<String> stringListField;

    private ClassWithABuilder(Builder builder){
        stringField1 = builder.stringField1;
        doubleField1 = builder.doubleField1;
        intListField = builder.intListField;
        stringListField = builder.stringListField;
    }

    @net.banterly.buildercompletion.annotations.BuilderClass
    public static class Builder {
        private String stringField1;
        private double doubleField1;
        private List<Integer> intListField = new ArrayList<>();
        private List<String> stringListField = new ArrayList<>();


        @net.banterly.buildercompletion.annotations.RepeatableBuilderMethod
        public Builder withIntListItem(int intListItem){
            intListField.add(intListItem);
            return this;
        }

        public Builder withIntList(List<Integer> intList){
            intListField = intList;
            return this;
        }

        @net.banterly.buildercompletion.annotations.RepeatableBuilderMethod
        @net.banterly.buildercompletion.annotations.MandatoryBuilderMethod
        public Builder withStringListItem(String stringListItem){
            stringListField.add(stringListItem);
            return this;
        }

        @net.banterly.buildercompletion.annotations.MandatoryBuilderMethod
        public Builder withStringList(List<String> stringList){
            stringListField = stringList;
            return this;
        }


        public Builder withStringField1(String stringField1){
            this.stringField1 = stringField1;
            return this;
        }

        @net.banterly.buildercompletion.annotations.MandatoryBuilderMethod
        public Builder withDoubleField1(double doubleField1){
            this.doubleField1 = doubleField1;
            return this;
        }

        @net.banterly.buildercompletion.annotations.BuildMethod
        public ClassWithABuilder build(){
            return new ClassWithABuilder(this);
        }
    }
}
