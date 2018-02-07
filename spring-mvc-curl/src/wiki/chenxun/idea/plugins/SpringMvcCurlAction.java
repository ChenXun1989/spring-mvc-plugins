package wiki.chenxun.idea.plugins;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;


/**
 * @author chenxun
 */
public class SpringMvcCurlAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {

        final Module module = DataKeys.MODULE.getData(e.getDataContext());
        if (module == null) {
            return;
        }

        SpringMvcCurlModuleComponent springMvcCurlModuleComponent = module.getComponent(SpringMvcCurlModuleComponent.class);
        if(springMvcCurlModuleComponent==null){
            throw new IllegalArgumentException("Couldn't get springMvcCurl plugin");
        }

        PsiFile psiFile= PsiManager.getInstance(module.getProject()).findFile( getSelectedFile(module.getProject()));
        springMvcCurlModuleComponent.curl(psiFile);


    }

    private VirtualFile getSelectedFile(final Project project) {

        VirtualFile selectedFile = null;
        final Editor selectedTextEditor = FileEditorManager.getInstance(project).getSelectedTextEditor();
        if (selectedTextEditor != null) {
            selectedFile = FileDocumentManager.getInstance().getFile(selectedTextEditor.getDocument());
        }

        if (selectedFile == null) {
            // this is the preferred solution, but it doesn't respect the focus of split editors at present
            final VirtualFile[] selectedFiles = FileEditorManager.getInstance(project).getSelectedFiles();
            if (selectedFiles.length > 0) {
                selectedFile = selectedFiles[0];
            }
        }

        return selectedFile;
    }


}
