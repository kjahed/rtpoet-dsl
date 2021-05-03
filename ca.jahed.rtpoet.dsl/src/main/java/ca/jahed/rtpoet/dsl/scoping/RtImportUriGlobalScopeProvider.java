package ca.jahed.rtpoet.dsl.scoping;

import ca.jahed.rtpoet.dsl.RTSLibrary;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xtext.scoping.impl.ImportUriGlobalScopeProvider;

import java.util.LinkedHashSet;

public class RtImportUriGlobalScopeProvider extends ImportUriGlobalScopeProvider {
    @Override
    protected LinkedHashSet<URI> getImportedUris(Resource resource) {
        LinkedHashSet<URI> importedURIs = super.getImportedUris(resource);
        importedURIs.add(RTSLibrary.getURI());
        return importedURIs;
    }
}