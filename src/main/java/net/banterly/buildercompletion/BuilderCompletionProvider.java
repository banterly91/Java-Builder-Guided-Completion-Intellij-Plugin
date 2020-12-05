package net.banterly.buildercompletion;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.*;

import com.intellij.psi.*;

import com.intellij.psi.util.PsiTreeUtil;
import net.banterly.buildercompletion.annotations.BuildMethod;
import net.banterly.buildercompletion.annotations.BuilderClass;
import net.banterly.buildercompletion.annotations.MandatoryBuilderMethod;
import net.banterly.buildercompletion.annotations.RepeatableBuilderMethod;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class BuilderCompletionProvider extends CompletionContributor {

    private enum LookupElementStyle {
        INVALID(1000) {
            @Override
            public LookupElementDecorator<LookupElement> apply(final LookupElement lookupElement) {
                LookupElement prioritizedLookupElement = PrioritizedLookupElement.withPriority(lookupElement, getPriority());
                return LookupElementDecorator.withRenderer(prioritizedLookupElement, new LookupElementRenderer<LookupElementDecorator<LookupElement>>() {
                    @Override
                    public void renderElement(LookupElementDecorator<LookupElement> element, LookupElementPresentation presentation) {
                        element.getDelegate().renderElement(presentation);
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
        }, MANDATORY(1002) {
            @Override
            public LookupElementDecorator<LookupElement> apply(final LookupElement lookupElement) {
                LookupElement prioritizedLookupElement = PrioritizedLookupElement.withPriority(lookupElement, getPriority());
                return LookupElementDecorator.withRenderer(prioritizedLookupElement, new LookupElementRenderer<LookupElementDecorator<LookupElement>>() {
                    @Override
                    public void renderElement(LookupElementDecorator<LookupElement> element, LookupElementPresentation presentation) {
                        element.getDelegate().renderElement(presentation);
                        presentation.setItemTextBold(true);
                        presentation.setItemTextUnderlined(true);
                        presentation.appendTailTextItalic("  Mandatory", false);
                    }
                });
            }
        };

        private final double priority;

        LookupElementStyle(double priority) {
            this.priority = priority;
        }

        public double getPriority(){
            return priority;
        }

        public abstract LookupElementDecorator<LookupElement> apply(LookupElement lookupElement);
    }

    private enum LookupElementStyleSelector {
        MANDATORY_METHOD {
            @Override
            public LookupElementStyle select(final Set<PsiMethod> builderMethods, final Set<PsiMethod> invokedBuilderMethods, final PsiMethod psiMethod) {
                boolean isMethodInvoked = invokedBuilderMethods.contains(psiMethod);
                if (isMethodInvoked) {
                    boolean isMethodInvocationRepeatable = psiMethod.getAnnotation(RepeatableBuilderMethod.class.getCanonicalName()) != null;
                    if (isMethodInvocationRepeatable) {
                        return LookupElementStyle.OPTIONAL;
                    } else {
                        return LookupElementStyle.INVALID;
                    }
                } else {
                    return LookupElementStyle.MANDATORY;
                }
            }
        }, BUILDER_METHOD {
            @Override
            public LookupElementStyle select(final Set<PsiMethod> builderMethods, final Set<PsiMethod> invokedBuilderMethods, final PsiMethod psiMethod) {
                if (builderMethods.stream()
                        .filter(m -> m.getAnnotation(MandatoryBuilderMethod.class.getCanonicalName()) != null)
                        .allMatch(invokedBuilderMethods::contains)) {

                    if (!invokedBuilderMethods.contains(psiMethod)) {
                        return LookupElementStyle.MANDATORY;
                    } else {
                        return LookupElementStyle.OPTIONAL;
                    }
                } else {
                    return LookupElementStyle.INVALID;
                }
            }
        }, OPTIONAL_METHOD {
            @Override
            public LookupElementStyle select(final Set<PsiMethod> builderMethods, final Set<PsiMethod> invokedBuilderMethods, final PsiMethod psiMethod) {
                if (!invokedBuilderMethods.contains(psiMethod)) {
                    return LookupElementStyle.OPTIONAL;
                } else {
                    if (psiMethod.getAnnotation(RepeatableBuilderMethod.class.getCanonicalName()) != null) {
                        return LookupElementStyle.OPTIONAL;
                    } else {
                        return LookupElementStyle.INVALID;
                    }
                }
            }
        };

        public abstract LookupElementStyle select(Set<PsiMethod> builderMethods, Set<PsiMethod> invokedBuilderMethods, PsiMethod lookupPsiElement);
    }

    @Override
    public void fillCompletionVariants(@NotNull CompletionParameters parameters, @NotNull CompletionResultSet result) {
        final CompletionResultSet finalResult = result;

        Set<PsiMethod> builderMethods = new HashSet<>();
        Set<PsiMethod> invokedBuilderMethods = new HashSet<>();

        result.runRemainingContributors(parameters, completionResult -> {
            LookupElement lookupElement = completionResult.getLookupElement();
            if (isProducedByJavaCompletion(lookupElement) &&
                    lookupElement.getPsiElement() instanceof PsiMethod &&
                    isFromBuilderClass((PsiMethod) lookupElement.getPsiElement())) {
                PsiMethod lookupPsiMethodElement = (PsiMethod) lookupElement.getPsiElement();
                PsiClass builderClassPsi = (PsiClass) lookupPsiMethodElement.getParent();

                if (builderMethods.isEmpty() && invokedBuilderMethods.isEmpty()){
                    builderMethods.addAll(Arrays.asList(builderClassPsi.getAllMethods()));
                    invokedBuilderMethods.addAll(getInvokedBuilderMethods(parameters, builderMethods));
                }

                LookupElementDecorator<LookupElement> decoratedLookupElement = getStyleSelector(lookupPsiMethodElement)
                        .select(builderMethods, invokedBuilderMethods, lookupPsiMethodElement)
                        .apply(lookupElement);
                completionResult = completionResult.withLookupElement(decoratedLookupElement);
            }
            finalResult.passResult(completionResult);
        });
    }

    private static boolean isProducedByJavaCompletion(final LookupElement lookupElement) {
        return lookupElement.getUserData(BaseCompletionService.LOOKUP_ELEMENT_CONTRIBUTOR) instanceof JavaCompletionContributor;
    }

    private static boolean isFromBuilderClass(final PsiMethod psiMethod) {
        if (psiMethod.getParent() instanceof PsiClass) {
            return ((PsiClass) psiMethod.getParent()).getAnnotation(BuilderClass.class.getCanonicalName()) != null;
        }
        return false;
    }

    private static Set<PsiMethod> getInvokedBuilderMethods(final CompletionParameters parameters, final Set<PsiMethod> builderMethods) {
        PsiElement currentElement = parameters.getPosition();
        PsiElement enclosingMethod = PsiTreeUtil.getParentOfType(currentElement, PsiMethod.class);
        return Arrays.stream(PsiTreeUtil.collectElements(enclosingMethod, element -> {
            if (element.getTextRange().getEndOffset() > parameters.getOffset()) {
                return false;
            }
            if (element instanceof PsiMethodCallExpression) {
                PsiMethodCallExpression methodCall = (PsiMethodCallExpression) element;
                return builderMethods.contains(methodCall.resolveMethod());
            }
            return false;
        })).map(e -> ((PsiMethodCallExpression) e).resolveMethod()).collect(Collectors.toSet());
    }

    private static LookupElementStyleSelector getStyleSelector(final PsiMethod psiMethod) {
        boolean isMandatoryMethod = psiMethod.getAnnotation(MandatoryBuilderMethod.class.getCanonicalName()) != null;
        boolean isBuilderMethod = psiMethod.getAnnotation(BuildMethod.class.getCanonicalName()) != null;
        if (isMandatoryMethod) {
            return LookupElementStyleSelector.MANDATORY_METHOD;
        } else if (isBuilderMethod) {
            return LookupElementStyleSelector.BUILDER_METHOD;
        } else {
            return LookupElementStyleSelector.OPTIONAL_METHOD;
        }
    }

}
