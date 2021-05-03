package ca.jahed.rtpoet.dsl.ide;

import ca.jahed.rtpoet.dsl.generator.PapyrusRTModelGenerator;
import ca.jahed.rtpoet.dsl.generator.RTModelGenerator;
import ca.jahed.rtpoet.dsl.ide.generator.DevContainerGenerator;
import ca.jahed.rtpoet.papyrusrt.utils.PapyrusRTCodeGenerator;
import ca.jahed.rtpoet.rtmodel.RTModel;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
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

        return Lists.newArrayList("rt.prtgen", "rt.cppgen", "rt.jsongen");
    }

    @Override
    public Object execute(ExecuteCommandParams params, ILanguageServerAccess access, CancelIndicator cancelIndicator) {
        if ("rt.prtgen".equals(params.getCommand())
                || "rt.cppgen".equals(params.getCommand())
                || "rt.jsongen".equals(params.getCommand())) {
            JsonPrimitive uri = (JsonPrimitive) Iterables.getFirst(params.getArguments(), null);
            if (uri != null) {
                try {
                    Resource resource = access.doRead(uri.getAsString(),
                            ILanguageServerAccess.Context::getResource).get();

                    if("rt.prtgen".equals(params.getCommand())) return executeGeneratePapyrusRTModel(resource);
                    else if("rt.cppgen".equals(params.getCommand())) return executeGenerateCppCode(resource);
                    else if("rt.jsongen".equals(params.getCommand())) return executeGenerateJson(resource);
                    else return "Generation Failed";
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            } else {
                return "Missing resource URI";
            }
        }

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

        if(PapyrusRTCodeGenerator.generate(model, fsa.getOutputConfigurations()
                .get(IFileSystemAccess.DEFAULT_OUTPUT).getOutputDirectory())) {
            fsa.generateFile(".." + File.separator + ".devcontainer" + File.separator + "devcontainer.json",
                    DevContainerGenerator.generate(model));
            return "Generation Successful";
        }

        return "Generation Failed";
    }

    private String executeGenerateJson(Resource resource) {
        RTModel model = (new RTModelGenerator()).doGenerate(resource);
        if(model == null) return "Error generating RTModel";

        String json = new Gson().toJson(model);
        if(json != null) {
            fsa.generateFile(resource.getURI().trimFileExtension().lastSegment() + ".json", json);
            return "Generation Successful";
        }
        return "Generation Failed";
    }
}
