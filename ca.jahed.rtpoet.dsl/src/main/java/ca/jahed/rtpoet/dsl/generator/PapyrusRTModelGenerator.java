package ca.jahed.rtpoet.dsl.generator;

import ca.jahed.rtpoet.papyrusrt.PapyrusRTWriter;
import ca.jahed.rtpoet.papyrusrt.rts.PapyrusRTLibrary;
import ca.jahed.rtpoet.rtmodel.*;
import ca.jahed.rtpoet.utils.RTModelValidator;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xtext.generator.IFileSystemAccess2;

import java.io.IOException;

public class PapyrusRTModelGenerator {
    public boolean doGenerate(Resource input, IFileSystemAccess2 fsa) {
        String outputFileName = input.getURI().trimFileExtension().lastSegment();
        Resource umlResource = PapyrusRTLibrary.INSTANCE.createResourceSet()
                .createResource(fsa.getURI(outputFileName+ ".uml"));
        try {
            umlResource.delete(null);
        } catch (IOException e) {
            e.printStackTrace();
        }

        RTModel rtModel;
        try {
            rtModel = new RTModelGenerator().doGenerate(input);
        } catch (Exception e) {
            fsa.generateFile(outputFileName + ".errors", e.getMessage());
            return false;
        }

        if(rtModel != null) {
            RTModelValidator validator = new RTModelValidator(rtModel, false);
            validator.validate(false);

            try {
                PapyrusRTWriter.write(umlResource, rtModel);
                umlResource.save(null);
            } catch (IOException e) {
                e.printStackTrace();
            }

            if(validator.hasErrors()) {
                fsa.generateFile(outputFileName + ".errors",
                        String.join("\n", validator.getErrors()));
            }

            if(validator.hasWarnings())
                fsa.generateFile(outputFileName + ".warnings",
                        String.join("\n", validator.getWarnings()));
        }

        return true;
    }
}
