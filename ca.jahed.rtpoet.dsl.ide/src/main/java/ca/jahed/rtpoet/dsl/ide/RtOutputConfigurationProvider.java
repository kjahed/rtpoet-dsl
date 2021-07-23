package ca.jahed.rtpoet.dsl.ide;

import org.eclipse.xtext.generator.IFileSystemAccess;
import org.eclipse.xtext.generator.IOutputConfigurationProvider;
import org.eclipse.xtext.generator.OutputConfiguration;

import java.util.Collections;
import java.util.Set;

public class RtOutputConfigurationProvider implements IOutputConfigurationProvider {

    public Set<OutputConfiguration> getOutputConfigurations() {
        OutputConfiguration defaultOutput = new OutputConfiguration(IFileSystemAccess.DEFAULT_OUTPUT);
        defaultOutput.setDescription("Output Folder");
        defaultOutput.setOutputDirectory(".");
        defaultOutput.setOverrideExistingResources(true);
        defaultOutput.setCreateOutputDirectory(true);
        defaultOutput.setCleanUpDerivedResources(true);
        defaultOutput.setCanClearOutputDirectory(true);
        defaultOutput.setSetDerivedProperty(true);
        return Collections.singleton(defaultOutput);
    }
}
