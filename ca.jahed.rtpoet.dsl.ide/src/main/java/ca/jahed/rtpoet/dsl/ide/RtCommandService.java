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
import java.util.List;
import java.util.concurrent.ExecutionException;

public class RtCommandService implements IExecutableCommandService {
    @Inject
    private Provider<JavaIoFileSystemAccess> fsaProvider;
    @Inject
    private Provider<RtOutputConfigurationProvider> outConfigProvider;

    private JavaIoFileSystemAccess fsa;

    @Override
    public List<String> initialize() {
        fsa = fsaProvider.get();
        fsa.setOutputConfigurations(Collections.singletonMap(IFileSystemAccess.DEFAULT_OUTPUT,
                outConfigProvider.get().getOutputConfigurations().iterator().next()));
        return Lists.newArrayList("rt.prtgen", "rt.cppgen", "rt.jsgen", "rt.rtgen", "rt.devcontainergen");
    }

    @Override
    public Object execute(ExecuteCommandParams params, ILanguageServerAccess access, CancelIndicator cancelIndicator) {
        if ("rt.prtgen".equals(params.getCommand())
                || "rt.cppgen".equals(params.getCommand())
                || "rt.jsgen".equals(params.getCommand())) {
            JsonPrimitive uri = (JsonPrimitive) Iterables.getFirst(params.getArguments(), null);
            if (uri != null) {
                try {
                    Resource resource = access.doRead(uri.getAsString(),
                            ILanguageServerAccess.Context::getResource).get();

                    if("rt.prtgen".equals(params.getCommand())) return executeGeneratePapyrusRTModel(resource);
                    else if("rt.cppgen".equals(params.getCommand())) return executeGenerateCppCode(resource);
                    else if("rt.jsgen".equals(params.getCommand())) {
                        JsonPrimitive inspector = (JsonPrimitive) Iterables.getLast(params.getArguments(), false);
                        return executeGenerateJsCode(resource, inspector.getAsBoolean());
                    }
                    else return "Generation Failed";
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            } else {
                return "Missing resource URI";
            }
        }

        if("rt.rtgen".equals(params.getCommand())) {
            JsonPrimitive uri = (JsonPrimitive) Iterables.getFirst(params.getArguments(), null);
            if (uri != null) {
                return executeGenerateRTModel(new File(uri.getAsString().substring(7))); // trim 'file://'
            } else {
                return "Missing resource URI";
            }
        }

        if("rt.devcontainergen".equals(params.getCommand()))
            return executeGenerateDevContainer();
        return "Bad Command";
    }

    private String executeGeneratePapyrusRTModel(Resource resource) {
        if(new PapyrusRTModelGenerator().doGenerate(resource, fsa))
            return "Generation Successful";
        return "Generation Failed";
    }

    private String executeGenerateCppCode(Resource resource) {
        RTModel model = (new RTModelGenerator()).doGenerate(resource);
        if(model == null) return "Error generating RTModel";
        if(model.getTop() == null) return "Top capsule not found";

        if(CppCodeGenerator.generate(model, fsa.getOutputConfigurations()
                .get(IFileSystemAccess.DEFAULT_OUTPUT).getOutputDirectory())) {
            return "Generation Successful";
        }

        return "Generation Failed";
    }

    private String executeGenerateJsCode(Resource resource, boolean inspector) {
        RTModel model = (new RTModelGenerator()).doGenerate(resource);
        if(model == null) return "Error generating RTModel";
        if(model.getTop() == null) return "Top capsule not found";

        if(RTJavaScriptCodeGenerator.generate(model, fsa.getOutputConfigurations()
                .get(IFileSystemAccess.DEFAULT_OUTPUT).getOutputDirectory(), inspector)) {
            return "Generation Successful";
        }

        return "Generation Failed";
    }

    private String executeGenerateRTModel(File umlFile) {
        try {
            RTModel model = PapyrusRTReader.read(umlFile.getAbsolutePath());
            String textualModel = RTTextualModelGenerator.generate(model);
            fsa.generateFile(umlFile.getName().substring(0, umlFile.getName()
                    .lastIndexOf(".") + 1) + "rt", textualModel);
            return "Generation Successful";
        } catch (Exception e) {
            e.printStackTrace();
            return "Error generating RTModel";
        }
    }

    private String executeGenerateDevContainer() {
        fsa.generateFile(".." + File.separator + ".devcontainer" + File.separator + "devcontainer.json",
                DevContainerGenerator.generate());
        return "Generation Successful";
    }
}
