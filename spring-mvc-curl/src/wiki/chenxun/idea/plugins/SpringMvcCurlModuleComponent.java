package wiki.chenxun.idea.plugins;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleComponent;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiModifierListOwner;
import com.intellij.psi.javadoc.PsiDocComment;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;


/**
 * @author chenxun
 */
public class SpringMvcCurlModuleComponent implements ModuleComponent {

    private final String BASE_PATH = "curl-spring-mvc";

    private File baseDir;

    private final Module module;

    public SpringMvcCurlModuleComponent(Module module) {
        this.module=module;
    }

    @Override
    public void initComponent() {
        baseDir = new File(module.getModuleFile().getParent().getPath(), BASE_PATH);
        if (!baseDir.exists()) {
            baseDir.mkdir();
        }
    }

    @Override
    public void disposeComponent() {
        // TODO: insert component disposal logic here
    }

    @Override
    @NotNull
    public String getComponentName() {
        return "SpringMvcCurlModuleComponet";
    }

    @Override
    public void projectOpened() {
        // called when project is opened
    }

    @Override
    public void projectClosed() {
        // called when project is being closed
    }

    @Override
    public void moduleAdded() {
        // Invoked when the module corresponding to this component instance has been completely
        // loaded and added to the project.
    }

    public void curl(PsiFile psiFile) {
        PsiClass psiClass = PsiTreeUtil.getChildOfType(psiFile, PsiClass.class);
        PsiAnnotation controller = getAnnotation(psiClass, "org.springframework.stereotype.Controller");
        if(controller==null){
            Messages.showMessageDialog(psiClass.getProject(),
                    "annotation @Controller must ",
                    "warn",
                    Messages.getInformationIcon());
            return;
        }


        PsiMethod[] psiMethods = psiClass.getMethods();
        CountDownLatch countDownLatch = new CountDownLatch(psiMethods.length);
        for (PsiMethod psiMethod : psiMethods) {
            ApplicationManager.getApplication().runReadAction(new Runnable() {
                @Override
                public void run() {
                    try{
                        exec(psiMethod);
                    }finally {
                        countDownLatch.countDown();
                    }
                }
            });
        }
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            Thread.interrupted();
            Messages.showMessageDialog(psiClass.getProject(),
                    "export curl cancel ",
                    "warn",
                    Messages.getInformationIcon());
            return;
        }

        Messages.showMessageDialog(psiClass.getProject(),
                "export curl count :" + psiMethods.length,
                "success",
                Messages.getInformationIcon());

    }

    private void exec( PsiMethod psiMethod){
        PsiAnnotation requestMapping= getAnnotation(psiMethod,"org.springframework.web.bind.annotation.RequestMapping");
        if(requestMapping==null){
            return;
        }
        String path = requestMapping.findAttributeValue("value").getText();
        path=path.replaceAll("\"","");
        String httpMethod= requestMapping.findAttributeValue("method").getText().split("\\.")[1];
        PsiDocComment psiDocComment = psiMethod.getDocComment();
        String javaDoc = psiDocComment == null ? "warn no java doc" : psiDocComment.getText();
        String template="%s \ncurl http://%s:%d%s -X %s \\\n  -H 'Accept-Language: zh-CN,zh;q=0.8' \\\n-H 'Content-type: application/json;charset=UTF-8' \\\n-H 'Accept: */*'  \\\n--compressed";

        String curl=String.format(template,javaDoc,"localhost",8080,path,httpMethod);
        String filePath = psiMethod.getContainingClass().getQualifiedName() + "-" + psiMethod.getName() + ".curl";

        File file = new File(baseDir, filePath);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try(BufferedWriter writer=new BufferedWriter(new FileWriter(file))){
            writer.write(curl);
            writer.flush();
        }catch (Exception e){
            e.printStackTrace();
        }








    }

    private PsiAnnotation getAnnotation(PsiModifierListOwner modifierListOwner , String annotationName) {
        PsiAnnotation psiAnnotation = null;
        for (PsiAnnotation annotation : modifierListOwner.getModifierList().getAnnotations()) {
            String name = annotation.getQualifiedName();
            if (name.equals(annotationName)) {
                psiAnnotation = annotation;
                break;
            }

        }
        return psiAnnotation;
    }


}
