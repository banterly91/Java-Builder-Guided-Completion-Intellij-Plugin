package net.banterly.buildercompletion;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.*;

import com.intellij.psi.*;

import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.ui.JBColor;
import net.banterly.buildercompletion.annotations.*;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public class BuilderCompletionProvider extends CompletionContributor {

    enum LookupElementStyle {
        INVALID(1000) {
            @Override
            public LookupElementDecorator<LookupElement> apply(final LookupElement lookupElement) {
                LookupElement prioritizedLookupElement = PrioritizedLookupElement.withPriority(lookupElement, getPriority());
                return LookupElementDecorator.withRenderer(prioritizedLookupElement, new LookupElementRenderer<LookupElementDecorator<LookupElement>>() {
                    @Override
                    public void renderElement(LookupElementDecorator<LookupElement> element, LookupElementPresentation presentation) {
                        element.getDelegate().renderElement(presentation);
                        presentation.setItemTextForeground(JBColor.RED);
                        presentation.setItemTextBold(false);
                        presentation.setItemTextUnderlined(false);
                        presentation.setStrikeout(true);
                        presentation.appendTailTextItalic("  Invalid", false);
                    }
                });
            }
        }, OPTIONAL(1001) {
            @Override
            public LookupElementDecorator<LookupElement> apply(final LookupElement lookupElement) {
                LookupElement prioritizedLookupElement = PrioritizedLookupElement.withPriority(lookupElement, getPriority());
                return LookupElementDecorator.withRenderer(prioritizedLookupElement, new LookupElementRenderer<LookupElementDecorator<LookupElement>>() {
                    @Override
                    public void renderElement(LookupElementDecorator<LookupElement> element, LookupElementPresentation presentation) {
                        element.getDelegate().renderElement(presentation);
                        presentation.setItemTextBold(true);
                        presentation.setItemTextUnderlined(false);
                        presentation.appendTailTextItalic("  Optional", false);
                    }
                });
            }
        }, REQUIRED(1002) {
            @Override
            public LookupElementDecorator<LookupElement> apply(final LookupElement lookupElement) {
                LookupElement prioritizedLookupElement = PrioritizedLookupElement.withPriority(lookupElement, getPriority());
                return LookupElementDecorator.withRenderer(prioritizedLookupElement, new LookupElementRenderer<LookupElementDecorator<LookupElement>>() {
                    @Override
                    public void renderElement(LookupElementDecorator<LookupElement> element, LookupElementPresentation presentation) {
                        element.getDelegate().renderElement(presentation);
                        presentation.setItemTextForeground(JBColor.BLUE);
                        presentation.setItemTextBold(true);
                        presentation.setItemTextUnderlined(true);
                        presentation.appendTailTextItalic("  Required", false);
                    }
                });
            }
        };

        private final double priority;

        LookupElementStyle(double priority) {
            this.priority = priority;
        }

        public double getPriority() {
            return priority;
        }

        public abstract LookupElementDecorator<LookupElement> apply(LookupElement lookupElement);
    }

    private enum LookupElementStyleSelector {
        MANDATORY_METHOD {
            @Override
            public LookupElementStyle select(final Map<PsiMethod, BuilderMethodAnnotationAttributes> builderMethods2AnnotationAttributes, final Set<PsiMethod> invokedBuilderMethods, final PsiMethod psiMethod) {
                BuilderMethodAnnotationAttributes annotationAttributes = builderMethods2AnnotationAttributes.get(psiMethod);
                if (invokedBuilderMethods.stream().anyMatch(m -> annotationAttributes.getIncompatibleMethods().contains(m.getName()))) {
                    return LookupElementStyle.INVALID;
                }
                boolean isMethodInvoked = invokedBuilderMethods.contains(psiMethod);
                if (isMethodInvoked) {
                    if (builderMethods2AnnotationAttributes.get(psiMethod).isRepeatable()) {
                        return LookupElementStyle.OPTIONAL;
                    } else {
                        return LookupElementStyle.INVALID;
                    }
                } else {
                    return LookupElementStyle.REQUIRED;
                }
            }
        }, BUILD_METHOD {
            @Override
            public LookupElementStyle select(final Map<PsiMethod, BuilderMethodAnnotationAttributes> builderMethods2AnnotationAttributes, final Set<PsiMethod> invokedBuilderMethods, final PsiMethod psiMethod) {
                boolean areAllMandatoryMethodsInvoked = builderMethods2AnnotationAttributes.entrySet().stream()
                        .filter(e -> e.getValue().getType() == BuilderMethod.Type.MANDATORY)
                        .allMatch(e -> invokedBuilderMethods.contains(e.getKey()) || invokedBuilderMethods.stream().anyMatch(invokedMethod -> e.getValue().getIncompatibleMethods().contains(invokedMethod.getName())));
                if (areAllMandatoryMethodsInvoked) {
                    if (!invokedBuilderMethods.contains(psiMethod)) {
                        return LookupElementStyle.REQUIRED;
                    } else {
                        return LookupElementStyle.OPTIONAL;
                    }
                } else {
                    return LookupElementStyle.INVALID;
                }
            }
        }, OPTIONAL_METHOD {
            @Override
            public LookupElementStyle select(final Map<PsiMethod, BuilderMethodAnnotationAttributes> builderMethods2AnnotationAttributes, final Set<PsiMethod> invokedBuilderMethods, final PsiMethod psiMethod) {
                BuilderMethodAnnotationAttributes annotationAttributes = builderMethods2AnnotationAttributes.get(psiMethod);
                if (invokedBuilderMethods.stream().anyMatch(m -> annotationAttributes.getIncompatibleMethods().contains(m.getName()))) {
                    return LookupElementStyle.INVALID;
                }

                if (!invokedBuilderMethods.contains(psiMethod)) {
                    return LookupElementStyle.OPTIONAL;
                } else {
                    if (builderMethods2AnnotationAttributes.get(psiMethod).isRepeatable()) {
                        return LookupElementStyle.OPTIONAL;
                    } else {
                        return LookupElementStyle.INVALID;
                    }
                }
            }
        };

        public abstract LookupElementStyle select(Map<PsiMethod, BuilderMethodAnnotationAttributes> builderMethods2AnnotationAttributes, Set<PsiMethod> invokedBuilderMethods, PsiMethod lookupPsiElement);
    }

    @Override
    public void fillCompletionVariants(@NotNull CompletionParameters parameters, @NotNull CompletionResultSet result) {
        final CompletionResultSet finalResult = result;

        Map<PsiMethod, BuilderMethodAnnotationAttributes> builderMethods2AnnotationAttributes = new HashMap<>();
        Set<PsiMethod> invokedBuilderMethods = new HashSet<>();

        result.runRemainingContributors(parameters, completionResult -> {
            LookupElement lookupElement = completionResult.getLookupElement();
            if (lookupElement.getPsiElement() instanceof PsiMethod &&
                    isBuilderMethod((PsiMethod) lookupElement.getPsiElement())) {
                PsiMethod lookupPsiMethodElement = (PsiMethod) lookupElement.getPsiElement();

                if (builderMethods2AnnotationAttributes.isEmpty() || invokedBuilderMethods.isEmpty()) {
                    populateMethodDataCaches(builderMethods2AnnotationAttributes, invokedBuilderMethods, lookupPsiMethodElement, parameters);
                }

                LookupElementDecorator<LookupElement> decoratedLookupElement = getStyleSelector(lookupPsiMethodElement, builderMethods2AnnotationAttributes.get(lookupPsiMethodElement))
                        .select(builderMethods2AnnotationAttributes, invokedBuilderMethods, lookupPsiMethodElement)
                        .apply(lookupElement);
                completionResult = completionResult.withLookupElement(decoratedLookupElement);
            }
            finalResult.passResult(completionResult);
        });
    }

    private void populateMethodDataCaches(final Map<PsiMethod, BuilderMethodAnnotationAttributes> builderMethods2AnnotationAttributes, final Set<PsiMethod> invokedBuilderMethods, PsiElement lookupPsiMethodElement, final CompletionParameters parameters) {
        PsiClass builderClassPsi = (PsiClass) lookupPsiMethodElement.getParent();
        builderMethods2AnnotationAttributes.putAll(Arrays.stream(builderClassPsi.getAllMethods())
                .filter(m -> m.hasAnnotation(BuilderMethod.class.getCanonicalName()))
                .collect(Collectors.toMap(method -> method,
                        method -> {
                            PsiAnnotation annotation = method.getAnnotation(BuilderMethod.class.getCanonicalName());
                            BuilderMethod.Type type = BuilderMethod.Type.valueOf(annotation.findAttributeValue("type").getReference().resolve().getText());
                            boolean isRepeatable = "true".equals(annotation.findAttributeValue("repeatable").getText());
                            Set<String> incompatibleMethods = getIncompatibleMethods(annotation);
                            return new BuilderMethodAnnotationAttributes(type, isRepeatable, incompatibleMethods);
                        })));

        invokedBuilderMethods.addAll(getInvokedBuilderMethods(parameters, builderMethods2AnnotationAttributes.keySet()));
    }

    private Set<String> getIncompatibleMethods(PsiAnnotation annotation) {
        PsiAnnotationMemberValue incompatibleWithValue = annotation.findAttributeValue("incompatibleWith");
        if (incompatibleWithValue instanceof PsiArrayInitializerMemberValue) {
            return Arrays.stream(((PsiArrayInitializerMemberValue) incompatibleWithValue)
                    .getInitializers())
                    .map(v -> (String) ((PsiLiteral) v).getValue())
                    .collect(Collectors.toSet());
        } else if (incompatibleWithValue instanceof PsiLiteral) {
            String singleValue = (String) ((PsiLiteral) incompatibleWithValue).getValue();
            if (!singleValue.isEmpty()) {
                return Collections.singleton((String) ((PsiLiteral) incompatibleWithValue).getValue());
            }
        }
        return Collections.emptySet();
    }

    private static boolean isBuilderMethod(final PsiMethod psiMethod) {
        if (psiMethod.getParent() instanceof PsiClass) {
            return ((PsiClass) psiMethod.getParent()).hasAnnotation(BuilderClass.class.getCanonicalName()) && (psiMethod.hasAnnotation(BuilderMethod.class.getCanonicalName()) || psiMethod.hasAnnotation(BuildMethod.class.getCanonicalName()));
        }
        return false;
    }

    private static Set<PsiMethod> getInvokedBuilderMethods(final CompletionParameters parameters, final Set<PsiMethod> builderMethods) {
        PsiElement currentElement = parameters.getPosition();
        PsiCodeBlock enclosingCodeBlock = PsiTreeUtil.getParentOfType(currentElement, PsiMethod.class).getBody();
        InvokedBuilderMethodVisitor invokedBuilderMethodVisitor = new InvokedBuilderMethodVisitor(builderMethods, parameters.getOffset());
        enclosingCodeBlock.accept(invokedBuilderMethodVisitor);
        return invokedBuilderMethodVisitor.getDefinitelyInvokedMethodsAtNode(enclosingCodeBlock);
    }

    private static LookupElementStyleSelector getStyleSelector(final PsiMethod psiMethod, final BuilderMethodAnnotationAttributes annotationAttributes) {
        if (psiMethod.hasAnnotation(BuildMethod.class.getCanonicalName())) {
            return LookupElementStyleSelector.BUILD_METHOD;
        } else if (BuilderMethod.Type.MANDATORY == annotationAttributes.getType()) {
            return LookupElementStyleSelector.MANDATORY_METHOD;
        } else {
            return LookupElementStyleSelector.OPTIONAL_METHOD;
        }
    }

}