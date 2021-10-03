package ca.jahed.rtpoet.dsl.ide;

import ca.jahed.rtpoet.dsl.generator.PapyrusRTModelGenerator;
import ca.jahed.rtpoet.dsl.generator.RTModelGenerator;
import ca.jahed.rtpoet.dsl.ide.generator.DevContainerGenerator;
import ca.jahed.rtpoet.js.generators.RTJavaScriptCodeGenerator;
import ca.jahed.rtpoet.generators.RTTextualModelGenerator;
import ca.jahed.rtpoet.papyrusrt.PapyrusRTReader;
import ca.jahed.rtpoet.papyrusrt.generators.CppCodeGenerator;
import ca.jahed.rtpoet.rtmodel.RTModel;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.gson.JsonPrimitive;
import com.google.inject.Inject;
import com.google.inject.Provider;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.lsp4j.ExecuteCommandParams;
import org.eclipse.xtext.generator.IFileSystemAccess;
import org.eclipse.xtext.generator.JavaIoFileSystemAccess;
import org.eclipse.xtext.ide.server.ILanguageServerAccess;
import org.eclipse.xtext.ide.server.commands.IExecutableCommandService;
import org.eclipse.xtext.util.CancelIndicator;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class RtCommandService implements IExecutableCommandService {
    @Inject
    private Provider<JavaIoFileSystemAccess> fsaProvider;
    @Inject
    private Provider<RtOutputConfigurationProvider> outConfigProvider;

    private JavaIoFileSystemAccess fsa;

    private File outputDir;

    @Override
    public List<String> initialize() {
        fsa = fsaProvider.get();
        fsa.setOutputConfigurations(Collections.singletonMap(IFileSystemAccess.DEFAULT_OUTPUT,
                outConfigProvider.get().getOutputConfigurations().iterator().next()));
        outputDir = new File(fsa.getOutputConfigurations()
                .get(IFileSystemAccess.DEFAULT_OUTPUT).getOutputDirectory());

        return Lists.newArrayList(
                "rt.prtgen",
                "rt.cppgen",
                "rt.jsgen",
                "rt.rtgen",
                "rt.devcontainergen"
        );
    }

    @Override
    public Object execute(ExecuteCommandParams params, ILanguageServerAccess access, CancelIndicator cancelIndicator) {
        Map<String, Object> result = new HashMap<>();
        result.put("error", false);

        if ("rt.prtgen".equals(params.getCommand())
                || "rt.cppgen".equals(params.getCommand())
                || "rt.jsgen".equals(params.getCommand())
                || "rt.rtgen".equals(params.getCommand())) {

            JsonPrimitive uri = (JsonPrimitive) Iterables.getFirst(params.getArguments(), null);

            if (uri != null) {
                if("rt.rtgen".equals(params.getCommand()))
                    return executeGenerateRTModel(new File(uri.getAsString().substring(7))); // trim 'file://'

                try {
                    Resource resource = access.doRead(uri.getAsString(),
                            ILanguageServerAccess.Context::getResource).get();

                    if("rt.prtgen".equals(params.getCommand())) return executeGeneratePapyrusRTModel(resource);
                    else if("rt.cppgen".equals(params.getCommand())) return executeGenerateCppCode(resource);
                    else if("rt.jsgen".equals(params.getCommand())) {
                        JsonPrimitive inspector = (JsonPrimitive) Iterables.getLast(params.getArguments(), false);
                        return executeGenerateJsCode(resource, inspector.getAsBoolean());
                    }
                } catch (InterruptedException | ExecutionException e) {
                    result.put("error", true);
                    result.put("message", "Internal server error: "+e.getMessage());
                }
            } else {
                result.put("error", true);
                result.put("message", "Missing resource URI");
            }
        }


        if("rt.devcontainergen".equals(params.getCommand()))
            return executeGenerateDevContainer();

        result.put("error", true);
        result.put("message", "Bad Command");
        return result;
    }

    private Object executeGeneratePapyrusRTModel(Resource resource) {
        Map<String, Object> result = new HashMap<>();
        result.put("error", false);

        Resource prtResource = new PapyrusRTModelGenerator().doGenerate(resource, outputDir, fsa);
        if(prtResource != null) {
            String path = prtResource.getURI().toString().substring(5);
            result.put("path", path);
            result.put("message", "Generation Successful");
        } else {
            result.put("error", true);
            result.put("message", "Generation Failed");
        }

        return result;
    }

    private Object executeGenerateCppCode(Resource resource) {
        Map<String, Object> result = new HashMap<>();
        result.put("error", false);

        RTModel model = (new RTModelGenerator()).doGenerate(resource);
        if(model == null) {
            result.put("error", true);
            result.put("message", "Error generating RTModel");
            return result;
        }

        if(model.getTop() == null) {
            result.put("error", true);
            result.put("message", "Top capsule not found");
            return result;
        }

        File codeDir = new File(outputDir, model.getName() + ".cpp");
        if(CppCodeGenerator.generate(model, codeDir.getAbsolutePath())) {
            String path = new File(codeDir, "src").getAbsolutePath();
            result.put("path", path);
            result.put("message", "Generation Successful");
        }
        else {
            result.put("error", true);
            result.put("message", "Generation Failed");
        }

        return result;
    }

    private Object executeGenerateJsCode(Resource resource, boolean inspector) {
        Map<String, Object> result = new HashMap<>();
        result.put("error", false);

        RTModel model = (new RTModelGenerator()).doGenerate(resource);
        if(model == null) {
            result.put("error", true);
            result.put("message", "Error generating RTModel");
            return result;
        }

        if(model.getTop() == null) {
            result.put("error", true);
            result.put("message", "Top capsule not found");
            return result;
        }

        File codeDir = new File(outputDir, model.getName() + ".js");
        if(RTJavaScriptCodeGenerator.generate(model, codeDir, inspector)) {
            String path = codeDir.getAbsolutePath();
            result.put("path", path);
            result.put("message", "Generation Successful");
        } else {
            result.put("error", true);
            result.put("message", "Generation Failed");
        }

        return result;
    }

    private Object executeGenerateRTModel(File umlFile) {
        Map<String, Object> result = new HashMap<>();
        result.put("error", false);

        try {
            RTModel model = PapyrusRTReader.read(umlFile.getAbsolutePath());
            String textualModel = RTTextualModelGenerator.generate(model);

            String fileName = umlFile.getName().substring(0, umlFile.getName().lastIndexOf(".") + 1) + "rt";
            fsa.generateFile(model.getName() + File.separator + fileName, textualModel);

            String path = new File(new File(outputDir, model.getName()), fileName).getAbsolutePath();
            result.put("path", path);
            result.put("message", "Generation Successful");

        } catch (Exception e) {
            e.printStackTrace();
            result.put("error", true);
            result.put("message", "Generation Failed");
        }

        return result;
    }

    private Object executeGenerateDevContainer() {
        Map<String, Object> result = new HashMap<>();
        result.put("error", false);

        String filePath = "../.devcontainer" + File.separator + "devcontainer.json";
        fsa.generateFile(filePath, DevContainerGenerator.generate());

        String path = new File(filePath).getAbsolutePath();
        result.put("path", path);
        result.put("message", "Generation Successful");
        return result;
    }
}
