package ca.jahed.rtpoet.dsl.generator;

import ca.jahed.rtpoet.papyrusrt.PapyrusRTWriter;
import ca.jahed.rtpoet.rtmodel.*;
import ca.jahed.rtpoet.utils.RTModelValidator;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xtext.generator.IFileSystemAccess2;

import java.io.File;

public class PapyrusRTModelGenerator {

    public Resource doGenerate(Resource input, File outputDir, IFileSystemAccess2 fsa) {
        String outputFileName = input.getURI().trimFileExtension().lastSegment();
        outputDir = new File(outputDir, outputFileName);

        if(outputDir.exists())
            outputDir.delete();
        outputDir.mkdirs();

        File errorFile = new File(outputDir, outputFileName + ".errors");
        File warningFile = new File(outputDir, outputFileName + ".warning");

        RTModel rtModel;
        try {
            rtModel = new RTModelGenerator().doGenerate(input);
        } catch (Exception e) {
            fsa.generateFile(errorFile.getAbsolutePath(), e.getMessage());
            return null;
        }

        RTModelValidator validator = new RTModelValidator(rtModel, false);
        validator.validate(false);

        Resource resource = PapyrusRTWriter.writeAll(outputDir.getAbsolutePath(), rtModel);

        if(validator.hasErrors()) {
            fsa.generateFile(errorFile.getAbsolutePath(),
                    String.join("\n", validator.getErrors()));
        }

        if(validator.hasWarnings())
            fsa.generateFile(warningFile.getAbsolutePath(),
                    String.join("\n", validator.getWarnings()));

        return resource;
    }
}