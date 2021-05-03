/*
 * generated by Xtext 2.25.0
 */
package ca.jahed.rtpoet.dsl.ide;


import com.google.inject.Binder;
import com.google.inject.Singleton;
import org.eclipse.xtext.generator.IOutputConfigurationProvider;

/**
 * Use this class to register ide components.
 */
public class RtIdeModule extends AbstractRtIdeModule {
    @Override
    public void configure(Binder binder) {
        super.configure(binder);
        binder.bind(IOutputConfigurationProvider.class)
                .to(RtOutputConfigurationProvider.class).in(Singleton.class);
    }
}