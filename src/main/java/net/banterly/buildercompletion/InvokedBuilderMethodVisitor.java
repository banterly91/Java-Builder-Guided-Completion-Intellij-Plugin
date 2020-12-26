package net.banterly.buildercompletion;

import com.google.common.collect.Sets;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;

import java.util.*;

public class InvokedBuilderMethodVisitor extends JavaElementVisitor implements PsiRecursiveVisitor {
    Map<PsiElement, Set<PsiMethod>> psiElement2DefinitelyInvokedBuilderMethods = new HashMap<>();
    private boolean currentElementFound = false;
    Set<PsiMethod> builderMethods;
    int endOffset;

    InvokedBuilderMethodVisitor(Set<PsiMethod> builderMethods, int endOffset) {
        this.builderMethods = builderMethods;
        this.endOffset = endOffset;
    }

    @Override
    public void visitBlockStatement(PsiBlockStatement statement) {
        statement.getCodeBlock().accept(this);
        Set<PsiMethod> definitelyInvokedBuilderMethods = psiElement2DefinitelyInvokedBuilderMethods.get(statement.getCodeBlock());
        psiElement2DefinitelyInvokedBuilderMethods.put(statement, definitelyInvokedBuilderMethods);
    }

    @Override
    public void visitCodeBlock(PsiCodeBlock block) {
        Set<PsiMethod> definitelyInvokedBuilderMethods = new HashSet<>();
        Arrays.stream(block.getChildren()).filter(psiElement ->
                psiElement instanceof PsiExpressionStatement ||
                        psiElement instanceof PsiDeclarationStatement ||
                        psiElement instanceof PsiIfStatement ||
                        psiElement instanceof PsiDoWhileStatement ||
                        psiElement instanceof PsiSwitchStatement)
                .forEach(psiElement -> {
                    if (!currentElementFound) {
                        psiElement.accept(this);
                        definitelyInvokedBuilderMethods.addAll(psiElement2DefinitelyInvokedBuilderMethods.get(psiElement));
                    }
                });
        psiElement2DefinitelyInvokedBuilderMethods.put(block, definitelyInvokedBuilderMethods);
    }


    @Override
    public void visitDeclarationStatement(PsiDeclarationStatement statement) {
        Set<PsiMethod> definitelyInvokedBuilderMethods = new HashSet<>();
        Collection<PsiMethodCallExpression> psiMethodCallExpressions = PsiTreeUtil.collectElementsOfType(statement, PsiMethodCallExpression.class);

        if (psiMethodCallExpressions.isEmpty() && statement.getTextRange().getEndOffset() >= endOffset) {
            currentElementFound = true;
        } else {
            for (PsiMethodCallExpression psiMethodCallExpression : psiMethodCallExpressions) {
                PsiMethod psiMethod = psiMethodCallExpression.resolveMethod();
                if (builderMethods.contains(psiMethod)) {
                    definitelyInvokedBuilderMethods.add(psiMethod);
                }

                if (psiMethodCallExpression.getTextRange().getEndOffset() >= endOffset - 1) {
                    currentElementFound = true;
                    break;
                }
            }
        }
        psiElement2DefinitelyInvokedBuilderMethods.put(statement, definitelyInvokedBuilderMethods);
    }

    @Override
    public void visitExpressionStatement(PsiExpressionStatement statement) {
        Set<PsiMethod> definitelyInvokedBuilderMethods = new HashSet<>();
        Collection<PsiMethodCallExpression> psiMethodCallExpressions = PsiTreeUtil.collectElementsOfType(statement, PsiMethodCallExpression.class);

        if (psiMethodCallExpressions.isEmpty() && statement.getTextRange().getEndOffset() >= endOffset) {
            currentElementFound = true;
        } else {
            for (PsiMethodCallExpression psiMethodCallExpression : psiMethodCallExpressions) {
                PsiMethod psiMethod = psiMethodCallExpression.resolveMethod();
                if (builderMethods.contains(psiMethod)) {
                    definitelyInvokedBuilderMethods.add(psiMethod);
                }
                if (psiMethodCallExpression.getTextRange().getEndOffset() >= endOffset - 1) {
                    currentElementFound = true;
                    break;
                }

            }
        }
        psiElement2DefinitelyInvokedBuilderMethods.put(statement, definitelyInvokedBuilderMethods);
    }

    @Override
    public void visitIfStatement(PsiIfStatement statement) {
        PsiStatement thenStatement = statement.getThenBranch();
        thenStatement.accept(this);

        Set<PsiMethod> definitelyInvokedBuilderMethods;
        if (currentElementFound) {
            definitelyInvokedBuilderMethods = psiElement2DefinitelyInvokedBuilderMethods.get(thenStatement);
        } else {
            PsiStatement elseStatement = statement.getElseBranch();
            if (elseStatement != null) {
                elseStatement.accept(this);
                if (currentElementFound) {
                    definitelyInvokedBuilderMethods = psiElement2DefinitelyInvokedBuilderMethods.get(elseStatement);
                } else {
                    definitelyInvokedBuilderMethods = Sets.intersection(
                            psiElement2DefinitelyInvokedBuilderMethods.get(thenStatement),
                            psiElement2DefinitelyInvokedBuilderMethods.get(elseStatement));
                }
            } else {
                definitelyInvokedBuilderMethods = Collections.emptySet();
            }
        }
        psiElement2DefinitelyInvokedBuilderMethods.put(statement, definitelyInvokedBuilderMethods);
    }

    @Override
    public void visitDoWhileStatement(PsiDoWhileStatement statement) {
        statement.getBody().accept(this);
        Set<PsiMethod> definitelyInvokedBuilderMethods = psiElement2DefinitelyInvokedBuilderMethods.get(statement.getBody());
        psiElement2DefinitelyInvokedBuilderMethods.put(statement, definitelyInvokedBuilderMethods);
    }

    @Override
    public void visitSwitchStatement(PsiSwitchStatement statement) {
        PsiStatement[] switchStatements = statement.getBody().getStatements();
        Set<PsiMethod> definitelyInvokedBuilderMethods;
        Map<PsiSwitchLabelStatement, Set<PsiMethod>> switchCase2DefinitelyInvokedBuilderMethods = new HashMap<>();
        List<PsiSwitchLabelStatement> switchCasesWithNoBreak = new ArrayList<>();

        int index = 0;
        PsiSwitchLabelStatement switchLabelStatement = (PsiSwitchLabelStatement) switchStatements[index];
        while (true) {
            index++;
            switchCase2DefinitelyInvokedBuilderMethods.put(switchLabelStatement, new HashSet<>());
            boolean caseHasBreakStatement = false;
            while (index < switchStatements.length && !currentElementFound &&
                    !(switchStatements[index] instanceof PsiSwitchLabelStatement)) {
                if (switchStatements[index] instanceof PsiBreakStatement) {
                    caseHasBreakStatement = true;
                    enrichSwitchCasesWithNoBreak(switchLabelStatement, switchCasesWithNoBreak, switchCase2DefinitelyInvokedBuilderMethods);
                } else {
                    switchStatements[index].accept(this);
                    Set<PsiMethod> switchStatementDefinitelyInvokedMethods = psiElement2DefinitelyInvokedBuilderMethods.getOrDefault(switchStatements[index], Collections.emptySet());
                    switchCase2DefinitelyInvokedBuilderMethods.get(switchLabelStatement).addAll(switchStatementDefinitelyInvokedMethods);
                }
                index++;
            }

            if (currentElementFound) {
                definitelyInvokedBuilderMethods = new HashSet<>(switchCase2DefinitelyInvokedBuilderMethods.get(switchLabelStatement));
                switchCasesWithNoBreak.forEach(switchCase -> definitelyInvokedBuilderMethods.addAll(switchCase2DefinitelyInvokedBuilderMethods.get(switchCase)));
                break;
            } else if (PsiKeyword.DEFAULT.equals(switchLabelStatement.getFirstChild().getText())) {
                enrichSwitchCasesWithNoBreak(switchLabelStatement, switchCasesWithNoBreak, switchCase2DefinitelyInvokedBuilderMethods);
                Set<PsiMethod> currentDefinitelyInvokedMethods = switchCase2DefinitelyInvokedBuilderMethods.get(switchLabelStatement);
                for (Set<PsiMethod> switchCaseDefinitelyInvokedBuilderMethods : switchCase2DefinitelyInvokedBuilderMethods.values()) {
                    currentDefinitelyInvokedMethods = Sets.intersection(currentDefinitelyInvokedMethods, switchCaseDefinitelyInvokedBuilderMethods);
                }
                definitelyInvokedBuilderMethods = currentDefinitelyInvokedMethods;
                break;
            } else if (index < switchStatements.length) {
                if (!caseHasBreakStatement) {
                    switchCasesWithNoBreak.add(switchLabelStatement);
                }
                switchLabelStatement = (PsiSwitchLabelStatement) switchStatements[index];
            } else {
                definitelyInvokedBuilderMethods = Collections.emptySet();
                break;
            }
        }
        psiElement2DefinitelyInvokedBuilderMethods.put(statement, definitelyInvokedBuilderMethods);
    }

    private void enrichSwitchCasesWithNoBreak(PsiSwitchLabelStatement switchLabelStatement, List<PsiSwitchLabelStatement> switchCasesWithNoBreak, Map<PsiSwitchLabelStatement, Set<PsiMethod>> switchCase2DefinitelyInvokedBuilderMethods) {
        Set<PsiMethod> currentDefinitelyInvokedMethods = switchCase2DefinitelyInvokedBuilderMethods.get(switchLabelStatement);
        for (int caseIndex = switchCasesWithNoBreak.size() - 1; caseIndex >= 0; caseIndex--) {
            Set<PsiMethod> switchCaseDefinitelyInvokedBuilderMethods = switchCase2DefinitelyInvokedBuilderMethods.get(switchCasesWithNoBreak.get(caseIndex));
            switchCaseDefinitelyInvokedBuilderMethods.addAll(currentDefinitelyInvokedMethods);
            currentDefinitelyInvokedMethods = switchCaseDefinitelyInvokedBuilderMethods;
        }
        switchCasesWithNoBreak.clear();
    }

    public Set<PsiMethod> getDefinitelyInvokedMethodsAtNode(PsiElement psiElement) {
        return psiElement2DefinitelyInvokedBuilderMethods.get(psiElement);
    }

}
