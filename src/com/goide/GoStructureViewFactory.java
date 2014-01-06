package com.goide;

import com.goide.psi.*;
import com.intellij.ide.structureView.*;
import com.intellij.ide.util.treeView.smartTree.TreeElement;
import com.intellij.lang.PsiStructureViewFactory;
import com.intellij.navigation.ItemPresentation;
import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.pom.Navigatable;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.ArrayList;

public class GoStructureViewFactory implements PsiStructureViewFactory {
  @Nullable
  @Override
  public StructureViewBuilder getStructureViewBuilder(final PsiFile psiFile) {
    return new TreeBasedStructureViewBuilder() {
      @NotNull
      @Override
      public StructureViewModel createStructureViewModel(@Nullable Editor editor) {
        return new Model(psiFile);
      }

      @Override
      public boolean isRootNodeShown() {
        return false;
      }
    };
  }

  public static class Model extends StructureViewModelBase implements StructureViewModel.ElementInfoProvider {
    public Model(@NotNull PsiFile psiFile) {
      super(psiFile, new Element(psiFile));
      withSuitableClasses(GoFile.class);
    }

    @Override
    public boolean isAlwaysShowsPlus(StructureViewTreeElement structureViewTreeElement) {
      return false;
    }

    @Override
    public boolean isAlwaysLeaf(StructureViewTreeElement structureViewTreeElement) {
      return false;
    }
  }

  public static class Element implements StructureViewTreeElement, ItemPresentation, NavigationItem {
    private final PsiElement myElement;

    public Element(PsiElement element) {
      this.myElement = element;
    }

    @Override
    public Object getValue() {
      return myElement;
    }

    @Override
    public void navigate(boolean requestFocus) {
      ((Navigatable)myElement).navigate(requestFocus);
    }

    @Override
    public boolean canNavigate() {
      return ((Navigatable)myElement).canNavigate();
    }

    @Override
    public boolean canNavigateToSource() {
      return ((Navigatable)myElement).canNavigateToSource();
    }

    @Nullable
    @Override
    public String getName() {
      return myElement.getText();
    }

    @Override
    public ItemPresentation getPresentation() {
      return this;
    }

    @Override
    public TreeElement[] getChildren() {
      ArrayList<TreeElement> result = new ArrayList<TreeElement>();
      if (myElement instanceof GoFile) {
        for (GoTypeSpec o : ((GoFile)myElement).getTypes()) result.add(new Element(o));
        for (GoConstDefinition o : ((GoFile)myElement).getConsts()) result.add(new Element(o));
        for (GoVarDefinition o : ((GoFile)myElement).getVars()) result.add(new Element(o));
        for (GoFunctionDeclaration o : ((GoFile)myElement).getFunctions()) result.add(new Element(o));
        for (GoMethodDeclaration o : ((GoFile)myElement).getMethods()) result.add(new Element(o));
      }
      return result.toArray(new TreeElement[result.size()]);
    }

    @Override
    public String getPresentableText() {
      if (myElement instanceof GoFile) return ((GoFile)myElement).getName();
      else if (myElement instanceof GoFunctionDeclaration) {
        String receiver = myElement instanceof GoMethodDeclaration ?
                          ((GoMethodDeclaration)myElement).getReceiver().getTypeReferenceExpression().getText() + "." :
                          "";
        GoSignature signature = ((GoFunctionDeclaration)myElement).getSignature();
        String signatureText = signature != null ? signature.getText() : "";
        return receiver + ((GoFunctionDeclaration)myElement).getIdentifier().getText() + StringUtil.first(signatureText, 20, true);
      }
      else if (myElement instanceof GoTypeSpec) {
        GoType type = ((GoTypeSpec)myElement).getType();
        return ((GoTypeSpec)myElement).getIdentifier().getText() + ":" + (type != null ? type.getText() : "");
      }
      else if (myElement instanceof GoNamedElement) {
        return ((GoNamedElement)myElement).getName();
      }
      throw new AssertionError(myElement.getClass().getName());
    }

    @Nullable
    @Override
    public String getLocationString() {
      return null;
    }

    @Override
    public Icon getIcon(boolean open) {
      if (!myElement.isValid()) return null;
      if (myElement instanceof GoMethodDeclaration) return GoIcons.METHOD;
      if (myElement instanceof GoFunctionDeclaration) return GoIcons.FUNCTION;
      if (myElement instanceof GoTypeSpec) return GoIcons.TYPE;
      if (myElement instanceof GoVarDefinition) return GoIcons.VARIABLE;
      if (myElement instanceof GoConstDefinition) return GoIcons.CONST;
      return myElement.getIcon(0);
    }
  }
}
